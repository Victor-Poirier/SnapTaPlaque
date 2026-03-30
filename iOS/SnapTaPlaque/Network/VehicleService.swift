//
//  VehicleService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

/// Service réseau chargé de traiter toutes les interactions avec le catalogue des véhicules.
///
/// `VehicleService` s'appuie sur `ApiClient` pour interroger l'API distante. Ses principales
/// vocations incluent la consultation des caractéristiques liées à une immatriculation précise
/// ou encore la récupération de l'historique de scan global d'un compte utilisateur.
class VehicleService {
    
    /// Collecte les informations détaillées d'un véhicule spécifique depuis la base de données distante.
    ///
    /// La plaque cible est tout d'abord formatée pour être intégrée de manière sécurisée (URL Encoding) 
    /// en tant que paramètre de requête.
    ///
    /// - Parameter plate: Le numéro de la plaque d'immatriculation à rechercher (ex: `"AB 123 CD"` ou `"AB-123-CD"`).
    /// - Returns: Une instance `InfoResponse` fournissant la marque, le modèle et la motorisation du véhicule.
    /// - Throws: Lance asynchrone une série d'erreurs : `URLError(.badURL)` en cas de format inexploitable, 
    ///   ou une erreur réseau gérée par `ApiClient.shared.performRequest`.
    func getVehicleInfo(plate: String) async throws -> InfoResponse {
        
        // 1. On "nettoie" la plaque pour qu'elle puisse aller dans une URL sans faire planter l'application
        guard let encodedPlate = plate.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            throw URLError(.badURL)
        }
        
        // 2. On ajoute le paramètre "license_plate" directement à la fin du lien
        let endpoint = "/v1/vehicles/info?license_plate=\(encodedPlate)"
        
        // 3. On crée la requête (Si vous obtenez une erreur "405 Method Not Allowed" après cette modif, changez "POST" en "GET")
        guard let request = ApiClient.shared.createRequest(endpoint: endpoint, method: "POST") else {
            throw URLError(.badURL)
        }
        
        // 4. On lance l'appel réseau générique !
        return try await ApiClient.shared.performRequest(request: request)
    }
    
    /// Requête l'historique complet des plaques d'immatriculation scannées via le compte de l'utilisateur actif.
    ///
    /// Un en-tête d'authentification valide est automatiquement imbriqué dans la requête via le `SessionManager`. 
    /// 
    /// - Returns: Un modèle global `HistoryVehiclesResponse` encapsulant un tableau de chaque immatriculation détectée.
    /// - Throws: Les erreurs natives du client de l'API (expiration du jeton, indisponibilité de la route).
    func getHistory() async throws -> HistoryVehiclesResponse {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/vehicles/history", method: "GET") else {
            throw URLError(.badURL)
        }
        
        return try await ApiClient.shared.performRequest(request: request)
    }
}
