//
//  LoginResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Représente la réponse de l'API suite à une tentative de connexion réussie.
///
/// `LoginResponse` définit le modèle de données reçu du serveur. Il est conçu pour être
/// décodé automatiquement (via `Codable`) à partir d'une réponse JSON afin de récupérer
/// le jeton local d'authentification.
struct LoginResponse: Codable {
    
    /// Le jeton d'accès sécurisé (JWT) autorisant les appels aux API restreintes.
    ///
    /// Ce jeton doit être stocké de manière persistante (dans `SessionManager`)
    /// et doit être inclus dans l'en-tête `Authorization: Bearer <token>` des futures requêtes réseau.
    let accessToken: String
    
    /// Associe les propriétés de la structure aux clés exactes du JSON renvoyé par le backend.
    enum CodingKeys: String, CodingKey {
        /// Mappe la propriété Swift en "camelCase" avec la clé JSON en "snake_case" retournée par le serveur.
        case accessToken = "access_token"
    }
}
