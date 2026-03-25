//
//  FavoritesService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

class FavoritesService {
    
    // 1. Ajouter un favori
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
    
    // 2. Supprimer un favori
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
    
    // 3. Récupérer la liste des favoris
    func getAllFavorites() async throws -> [Vehicle] {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/favorites/all", method: "GET") else {
            throw URLError(.badURL)
        }
        
        let response: FavoriteAllResponse = try await ApiClient.shared.performRequest(request: request)
        
        // On transforme directement les InfoResponse en modèles Vehicle !
        return response.favorites.map { $0.toVehicle(isFavorite: true) }
    }
}
