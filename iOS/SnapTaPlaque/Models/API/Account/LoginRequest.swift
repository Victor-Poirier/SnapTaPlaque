//
//  LoginRequest.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Représente la charge utile (payload) envoyée à l'API lors d'une tentative de connexion.
///
/// `LoginRequest` se conforme au protocole `Codable` afin d'être facilement
/// encodé en JSON lors de la soumission des identifiants au serveur d'authentification (AccountService).
struct LoginRequest: Codable {
    
    /// Le nom d'utilisateur de l'utilisateur souhaitant s'authentifier.
    let username: String
    
    /// Le mot de passe en texte clair de l'utilisateur.
    ///
    /// - Important: Les requêtes transmettant ce modèle doivent toujours être effectuées
    /// sur une connexion sécurisée (HTTPS) pour protéger les identifiants.
    let password: String
}
