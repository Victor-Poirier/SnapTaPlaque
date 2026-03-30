//
//  RegisterResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Représente la réponse de l'API après une inscription réussie d'un nouvel utilisateur.
///
/// `RegisterResponse` contient les informations complètes du profil fraîchement créé en base de données.
/// Conformément au protocole `Codable`, cette structure facilite le parsing automatique d'un objet JSON
/// provenant d'une API backend qui retournerait les propriétés au format `snake_case`.
struct RegisterResponse: Codable {
    
    /// Un identifiant unique assigné par la base de données.
    let id: Int
    
    /// L'adresse courriel validée et liée au nouveau compte.
    let email: String
    
    /// Le nom d'utilisateur (pseudo) unique du joueur/client.
    let username: String
    
    /// Le nom complet (prénom + nom) renseigné lors de l'inscription.
    let fullName: String
    
    /// Indique si le compte créé est automatiquement mis en statut actif, 
    /// permettant immédiatement la connexion (`true` ou `false`).
    let isActive: Bool
    
    /// Précise si ce compte fraîchement créé est doté de droits d'administration.
    let isAdmin: Bool
    
    /// Une chaîne de caractères représentant la date et l'heure (habituellement au format ISO 8601)
    /// auxquelles le compte a été créé.
    let createdAt: String
    
    /// Énumération chargée d'associer les clés JSON (souvent en `snake_case`)
    /// aux propriétés Swift de la structure.
    enum CodingKeys: String, CodingKey {
        case id, email, username
        
        /// Correspond à la clé API `full_name`.
        case fullName = "full_name"
        
        /// Correspond à la clé API `is_active`.
        case isActive = "is_active"
        
        /// Correspond à la clé API `is_admin`.
        case isAdmin = "is_admin"
        
        /// Correspond à la clé API `created_at`.
        case createdAt = "created_at"
    }
}
