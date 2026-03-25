//
//  VehicleService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

class VehicleService {
    
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
    
    // Fonction pour récupérer l'historique
    func getHistory() async throws -> HistoryVehiclesResponse {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/vehicles/history", method: "GET") else {
            throw URLError(.badURL)
        }
        
        return try await ApiClient.shared.performRequest(request: request)
    }
}
