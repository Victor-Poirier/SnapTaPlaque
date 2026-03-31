//
//  InfoResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Représente la réponse détaillée de l'API lorsqu'on scrute les données d'un véhicule spécifique.
///
/// `InfoResponse` encapsule les données propres à un véhicule (plaque, marque, modèle, énergie)
/// et est utilisé pour dé-sérialiser les objets JSON renvoyés par les flux "Vehicles" et "Favorites".
struct InfoResponse: Codable {
    
    /// Le numéro de la plaque d'immatriculation du véhicule.
    let licensePlate: String
    
    /// La marque constructeur du véhicule (ex: Peugeot, Renault, etc.), si celle-ci a pu être trouvée.
    let brand: String?
    
    /// Le modèle spécifique construit de cette marque correspondante.
    let model: String?
    
    /// Un texte libre additionnel apportant une précision éventuelle sur cette immatriculation ou ce type de série.
    let info: String?
    
    /// Le type d'énergie propulsant le véhicule (Électrique, Essence, Hybride, Diesel).
    let energy: String?
    
    /// Lie et convertit les contraintes de noms `snake_case` du backend JSON en des propriétés `camelCase` Swift natives.
    enum CodingKeys: String, CodingKey {
        /// Transforme la clé backend `license_plate` en sa constante Swift `licensePlate`.
        case licensePlate = "license_plate"
        case brand
        case model
        case info
        case energy
    }
    
    /// Transforme dynamiquement cette structure réseau DTO `InfoResponse` en un objet domaine applicatif natif (`Vehicle`).
    ///
    /// Cette méthode est employée pour traduire et envoyer les données finales validées par l'API
    /// vers les composants d'interfaces SwiftUI concernés.
    ///
    /// - Parameter isFavorite: Précise si le véhicule résultant doit acquérir le statut de favori dans l'UI.
    /// - Returns: Une nouvelle instance prête à l'emploi de `Vehicle`.
    func toVehicle(isFavorite: Bool) -> Vehicle {
        return Vehicle(
            immatriculation: self.licensePlate, // Correspondance avec le Vehicle Swift
            brand: self.brand,
            model: self.model,
            info: self.info,
            energy: self.energy,
            isFavorite: isFavorite
        )
    }
}
