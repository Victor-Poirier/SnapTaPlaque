//
//  RegisterRequest.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Représente la demande d'inscription d'un nouvel utilisateur au service "SnapTaPlaque".
///
/// Ce modèle encode les informations requises pour créer un compte via l'API, en transformant
/// les propriétés locales en JSON (format `snake_case`) grâce à l'implémentation de `Codable`.
struct RegisterRequest: Codable {
    
    /// L'adresse courriel de l'utilisateur.
    let email: String
    
    /// Le nom d'utilisateur unique qui servira d'identifiant de connexion.
    let username: String
    
    /// Le mot de passe en texte clair, choisi par l'utilisateur.
    ///
    /// - Important: Doit être envoyé à l'API impérativement via une connexion chiffrée (HTTPS).
    let password: String
    
    /// Le nom complet (nom et prénom) de l'utilisateur.
    let fullName: String
    
    /// Détermine si l'utilisateur fraîchement créé disposera des privilèges d'administration.
    /// Par défaut, cette valeur devrait toujours être transmise à `false` côté client pour des raisons de sécurité.
    let isAdmin: Bool
    
    /// Témoigne du consentement explicite de l'utilisateur concernant le traitement de ses données
    /// personnelles (obligatoire au regard du RGPD).
    let gdprConsent: Bool
    
    /// Gère la nomenclature des clés lors de l'encodage JSON à destination de l'API.
    enum CodingKeys: String, CodingKey {
        case email
        case username
        case password
        
        /// Encodé en tant que `full_name`.
        case fullName = "full_name"
        
        /// Encodé en tant que `is_admin`.
        case isAdmin = "is_admin"
        
        /// Encodé en tant que `gdpr_consent`.
        case gdprConsent = "gdpr_consent"
    }
}
