//
//  MeResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Représente les données de profil de l'utilisateur connecté ("Me").
///
/// Ce modèle est utilisé par l'application locale pour parser la réponse de l'API (`v1/account/me`)
/// et construire le compte utilisateur dans l'interface, conformément aux conventions REST.
/// Il implémente `Codable` pour la désérialisation du JSON et `Identifiable`
/// pour faciliter l'utilisation directe dans les listes `SwiftUI`.
struct MeResponse: Codable, Identifiable {
    
    /// Un identifiant unique correspondant à l'ID en base de données.
    let id: Int
    
    /// L'adresse courriel (e-mail) attachée au profil.
    let email: String
    
    /// Le nom d'utilisateur (pseudo) choisi lors de l'inscription pour se connecter.
    let username: String
    
    /// Le nom complet de l'utilisateur, qui sera affiché en évidence sur son profil.
    let fullName: String
    
    /// Indique si le compte de cet utilisateur est actuellement actif sur la plateforme.
    ///
    /// Utilisé potentiellement pour masquer des interfaces ou forcer une reconnexion
    /// en cas de suspension ou clôture depuis l'administration.
    let isActive: Bool
    
    /// Indique si cet utilisateur dispose des droits "Administrateur".
    let isAdmin: Bool
    
    /// La date de création du compte, retournée sous forme de chaîne de caractères par l'API (ISO 8601).
    let createdAt: String
    
    /// Associe les propriétés de la structure aux clés exactes du JSON renvoyé par le backend.
    ///
    /// Gère la conversion des standards de nommage de l'API (généralement `snake_case`)
    /// vers les conventions de nommage locale Swift (`camelCase`).
    enum CodingKeys: String, CodingKey {
        case id, email, username
        
        /// Mappe `fullName` depuis la clé `full_name`.
        case fullName = "full_name"
        
        /// Mappe `isActive` depuis la clé `is_active`.
        case isActive = "is_active"
        
        /// Mappe `isAdmin` depuis la clé `is_admin`.
        case isAdmin = "is_admin"
        
        /// Mappe `createdAt` depuis la clé `created_at`.
        case createdAt = "created_at"
    }
}
