//
//  FavoriteAllResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Représente la réponse de l'API lorsqu'on récupère la liste complète des véhicules favoris.
///
/// Cette structure se conforme au protocole `Codable` afin de traduire un retour JSON
/// (contenant typiquement un tableau à la racine sous la clé `favorites`) depuis le backend
/// vers un modèle de données natif exploitable dans les vues SwiftUI de "SnapTaPlaque".
struct FavoriteAllResponse: Codable {
    
    /// Un tableau contenant les informations détaillées de chacun des véhicules marqués en favoris.
    ///
    /// - Note: Ce tableau s'appuie sur le modèle `InfoResponse` pour la structure de chaque élément.
    let favorites: [InfoResponse]
}
