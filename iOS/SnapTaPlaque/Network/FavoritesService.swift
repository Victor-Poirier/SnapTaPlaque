//
//  FavoritesService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

/// Service réseau chargé de centraliser et de gérer toutes les listes de véhicules mémoriées ("Favoris").
///
/// `FavoritesService` fait le pont entre l'application et l'API distante. Ses responsabilités couvrent 
/// l'ajout, le retrait et la récupération des véhicules sauvegardés, en reposant sur des requêtes asynchrones,
/// traitées globalement par l'`ApiClient`.
class FavoritesService {
    
    /// Ajoute ou enregistre un véhicule précis dans la base de favoris de l'utilisateur actif.
    ///
    /// Cette méthode transite par le transfert URL (Query Params). Si la plaque présente des espaces ou des formats
    /// non standard, elle est automatiquement encodée (`percent encoded`) via `addingPercentEncoding`.
    ///
    /// - Parameter plate: Le numéro de la plaque d'immatriculation du véhicule à retenir (ex: `"AB-123-CD"`).
    /// - Throws: `URLError(.badURL)` en cas d'un encodage défectueux ou si l'API est injoignable, 
    ///           ou d'autres erreurs renvoyées par la fonction `performRequest` de l'API Client.
    func addFavorite(plate: String) async throws {
        // On encode la plaque pour éviter les soucis avec les espaces ou caractères spéciaux dans l'URL
        guard let encodedPlate = plate.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            throw URLError(.badURL)
        }
        
        // ATTENTION : On n'oublie pas le "/" avant v1 !
        let endpoint = "/v1/favorites/add?license_plate=\(encodedPlate)"
        
        guard let request = ApiClient.shared.createRequest(endpoint: endpoint, method: "POST") else {
            throw URLError(.badURL)
        }
        
        // Plus de request.httpBody ni de JSONEncoder ici !
        
        let _: FavoritesResponse = try await ApiClient.shared.performRequest(request: request)
    }
    
    /// Supprime un véhicule précis de la liste des favoris de l'utilisateur actif.
    ///
    /// Identiquement à l'ajout, la plaque est transférée par paramètres de requête (Query)
    /// suite à un appel HTTP `DELETE`.
    ///
    /// - Parameter plate: Le numéro de la plaque d'immatriculation du véhicule à amputer de ses favoris.
    /// - Throws: `URLError(.badURL)` si la plaque n'a pas pu être formatée, ou erreur de décodage standard par l'`ApiClient`.
    func removeFavorite(plate: String) async throws {
        guard let encodedPlate = plate.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            throw URLError(.badURL)
        }
        
        let endpoint = "/v1/favorites/remove?license_plate=\(encodedPlate)"
        
        // La méthode est bien DELETE selon votre fichier favorites.py
        guard let request = ApiClient.shared.createRequest(endpoint: endpoint, method: "DELETE") else {
            throw URLError(.badURL)
        }
        
        let _: FavoritesResponse = try await ApiClient.shared.performRequest(request: request)
    }
    
    /// Interroge le serveur afin d'obtenir la liste exhaustive des véhicules en favoris liés au compte.
    ///
    /// La méthode extrait une table au format de transfert de données (DTO) qu'elle convertit
    /// à la volée en grappe d'objets métier de type `Vehicle`.
    ///
    /// - Returns: Un tableau asynchrone (`[Vehicle]`) listant tous les véhicules et contenant l'état natif `isFavorite` à `true`.
    /// - Throws: Une erreur si l'utilisateur n'est pas connecté et fait face à un refus HTTP de l'API.
    func getAllFavorites() async throws -> [Vehicle] {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/favorites/all", method: "GET") else {
            throw URLError(.badURL)
        }
        
        let response: FavoriteAllResponse = try await ApiClient.shared.performRequest(request: request)
        
        // On transforme directement les InfoResponse en modèles Vehicle !
        return response.favorites.map { $0.toVehicle(isFavorite: true) }
    }
}
