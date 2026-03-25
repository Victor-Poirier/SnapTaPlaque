//
//  FavoriteModels.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

// Modèle pour envoyer la plaque à l'API
struct FavoritesRequest: Codable {
    let licensePlate: String
    
    enum CodingKeys: String, CodingKey {
        case licensePlate = "license_plate"
    }
}

// Modèle générique pour la réponse de l'ajout/suppression
struct FavoritesResponse: Codable {
    let message: String?
}
