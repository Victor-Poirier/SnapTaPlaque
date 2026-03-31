//
//  FavoriteModels.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

/// Représente la requête formatée envoyée à l'API pour manipuler un favori.
///
/// Ce modèle de données transforme la propriété `licensePlate` exploitée localement en Swift
/// vers une clé standardisée `license_plate` attendue par le backend (via l'énumération `CodingKeys`).
struct FavoritesRequest: Codable {
    
    /// La chaîne de caractères représentant le numéro de la plaque d'immatriculation cible (ex: `"AB-123-CD"`).
    let licensePlate: String
    
    /// Définit le mappage précis entre les conventions de nommage Swift et celles dictées par l'API JSON.
    enum CodingKeys: String, CodingKey {
        case licensePlate = "license_plate"
    }
}

/// Représente la réponse générique provenant du serveur lors de l'ajout ou de la suppression d'un favori.
///
/// Souvent renvoyée avec un code de confirmation HTTP (ex: `201 Created` ou `200 OK`), cette structure
/// permet à l'interface `SwiftUI` de notifier l'utilisateur de la réussite de son action.
struct FavoritesResponse: Codable {
    
    /// Le message de confirmation éventuel retourné par l'API (ex: `"Vehicle added to favorites"`).
    let message: String?
}
