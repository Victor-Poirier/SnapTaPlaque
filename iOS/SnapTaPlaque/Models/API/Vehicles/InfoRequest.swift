//
//  InfoRequest.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

/// Représente la requête formatée envoyée à l'API pour obtenir les informations détaillées d'un véhicule.
///
/// Ce modèle de données standardise l'envoi de la plaque d'immatriculation aux services backend,
/// en s'appuyant sur le protocole `Codable` pour formater le JSON en `snake_case` attendu.
struct InfoRequest: Codable {
    
    /// Le numéro de la plaque d'immatriculation du véhicule à interroger (ex: `"AB-123-CD"`).
    let licensePlate: String
    
    /// Assure la traduction automatique entre le nommage local Swift (`camelCase`)
    /// et le standard attendu par le serveur lors de la sérialisation JSON (`snake_case`).
    enum CodingKeys: String, CodingKey {
        case licensePlate = "license_plate"
    }
}
