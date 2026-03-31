//
//  RgpdModels.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import Foundation

/// Représente la requête facultative envoyée à l'API pour récupérer les mentions légales et informations liées au RGPD.
///
/// Ce modèle permet éventuellement de spécifier la langue souhaitée dans laquelle les mentions légales
/// devront être retournées par le serveur backend.
struct RgpdRequest: Codable {
    
    /// Le code de la langue demandée (ex: "fr", "en").
    /// Si `nil`, le backend devrait retourner la langue par défaut (généralement l'anglais ou le français).
    let language: String?
}

/// DTO (Data Transfer Object) encapsulant l'ensemble des règles RGPD associées à l'application.
///
/// Utilisé pour décoder la réponse JSON de l'API et afficher dynamiquement
/// les politiques de confidentialité dans la vue associée de _SnapTaPlaque_.
struct RgpdResponse: Codable {
    
    /// Nom de l'entité ou de l'entreprise responsable du traitement des données.
    let controller: String
    
    /// Informations de contact pour toute demande relative aux données personnelles (e-mail, adresse...).
    let contact: [String]
    
    /// Description claire de l'objectif visé justifiant la collecte d'informations.
    let purpose: String
    
    /// La base légale appuyant officiellement cette collecte de données selon les directives européennes RGPD.
    let legalBasis: String
    
    /// Une liste des types de données spécifiquement recueillies sur les utilisateurs (email, lieu, IP, etc.).
    let dataCollected: [String]
    
    /// La durée prévue (de manière claire pour l'utilisateur) durant laquelle l'entité conserve les données.
    let retentionPeriod: String
    
    /// L'objet regroupant les différents droits de l'utilisateur (accès, oubli, révision).
    let userRights: RgpdResultUserRight
    
    /// Une déclaration précisant si les données sont partagées à des entités ou services tiers.
    let dataSharing: String
    
    /// Une liste pointant les actions ou règles de sécurité physiques et logicielles mises en oeuvre
    /// par le backend (chiffrement, clés, authentification par token, etc.).
    let securityMeasures: [String]
    
    /// Permet de faire la passerelle entre les propriétés `camelCase` iOS et le formalisme `snake_case` de l'API REST.
    enum CodingKeys: String, CodingKey {
        case controller, contact, purpose
        case legalBasis = "legal_basis"
        case dataCollected = "data_collected"
        case retentionPeriod = "retention_period"
        case userRights = "user_rights"
        case dataSharing = "data_sharing"
        case securityMeasures = "security_measures"
    }
}

/// Modèle niché représentant spécifiquement les droits des utilisateurs selon le RGPD.
///
/// - Les utilisateurs d'applications iOS ont le droit de modifier, accéder ou purger leur compte sur demande.
struct RgpdResultUserRight: Codable {
    
    /// Textuellement, le droit de l'utilisateur d'obtenir une copie des informations collectées par "SnapTaPlaque".
    let access: String
    
    /// Le principe selon lequel l'utilisateur peut exiger la suppression immédiate de ses traces numériques.
    let erasure: String
    
    /// Le droit à la rectification ou mise à jour de données obsolètes ou incorrectes.
    let rectification: String
}
