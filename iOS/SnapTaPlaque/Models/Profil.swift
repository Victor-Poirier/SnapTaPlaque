//
//  Profil.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Le modèle de données métier représentant le profil complet d'un utilisateur de l'application.
///
/// `Profil` agrège les informations d'identité (nom, prénom, pseudo) ainsi que diverses attaches de la vue,
/// telles que la liste complète de ses véhicules ajoutés aux favoris.
/// Conforme à `Codable`, ce modèle peut être sauvegardé localement ou formaté pour échanger avec un serveur backend.
struct Profil: Codable {
    
    /// Le nom d'utilisateur unique de la personne.
    let username: String
    
    /// Le prénom (first name) déclaré de l'utilisateur.
    let firstName: String
    
    /// Le nom de famille (last name) de l'utilisateur.
    let name: String
    
    /// Le mot de passe de l'utilisateur, stocké temporairement en clair au sein des formulaires SwiftUI.
    ///
    /// - Important: Ne retranscrivez ou n'enregistrez presque jamais ce champ non chiffré dans les `UserDefaults` pour son accès libre.
    let password: String?
    
    /// L'adresse de messagerie électronique (e-mail) qui servira de point de contact ou potentiellement d'identifiant de sécurité.
    let email: String
    
    /// Une liste dynamique (mutable) contenant les modèles et métadonnées des véhicules que ce profil a marqués comme "Favoris".
    var favoritesVehicule: [Vehicle] = []
    
    /// Une énumération indispensable pour les traductions des clés issues des dictionnaires JSON en mode `Codable`.
    enum CodingKeys: String, CodingKey {
        case username, name, password, email, favoritesVehicule
        
        /// Précise au parser que la clé JSON en entrée s'appelle explicitement `"firstName"`.
        case firstName = "firstName"
    }
}
