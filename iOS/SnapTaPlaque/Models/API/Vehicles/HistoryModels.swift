//
//  HistoryModels.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

/// Représente la réponse de l'API lorsqu'on récupère l'historique complet des véhicules scannés.
///
/// Ce modèle permet de désérialiser la racine de la réponse JSON du serveur, contenant 
/// une liste de véhicules sous la clé `history`.
struct HistoryVehiclesResponse: Codable {
    
    /// Un tableau encapsulant chaque entrée de l'historique de l'utilisateur.
    let history: [HistoryVehicleItem]
}

/// DTO (Data Transfer Object) représentant un véhicule unique au sein de l'historique de scan.
///
/// Implémente `Identifiable` pour permettre à `SwiftUI` de l'utiliser facilement dans un composant
/// `List` ou `ForEach`, ainsi que `Codable` pour la liaison avec les données JSON de l'API.
struct HistoryVehicleItem: Codable, Identifiable {
    
    /// Identifiant unique par défaut requis par le protocole `Identifiable`.
    /// 
    /// - Note: Étant donné que chaque plaque d'immatriculation est unique,
    /// elle est utilisée ici comme garantie d'identifiant pour la boucle de rendu SwiftUI.
    var id: String { licensePlate }
    
    /// Le numéro de la plaque d'immatriculation du véhicule.
    let licensePlate: String
    
    /// La marque constructeur du véhicule (ex: Peugeot, Renault, etc.), si disponible.
    let brand: String?
    
    /// Le modèle spécifique de la voiture au sein de la marque, si disponible.
    let model: String?
    
    /// Un texte libre éventuel renfermant des informations supplémentaires sur le véhicule.
    let info: String?
    
    /// Le type de motorisation ou carburant du véhicule (ex: Essence, Électrique, Diesel).
    let energy: String?
    
    /// Définit le mappage précis entre les conventions de nommage Swift et celles dictées par l'API JSON.
    enum CodingKeys: String, CodingKey {
        /// Transforme la clé `license_plate` du JSON backend vers la constante `licensePlate`.
        case licensePlate = "license_plate"
        case brand, model, info, energy
    }
}

extension HistoryVehicleItem {
    
    /// Convertit l'itération DTO (`HistoryVehicleItem`) en un objet domaine (`Vehicle`).
    ///
    /// Utilisé pour transférer directement un résultat extrait de l'historique
    /// vers les composants de détails ou de logique prévus pour manipuler un `Vehicle`.
    ///
    /// - Returns: Une nouvelle instance de `Vehicle` initialisée avec les données de l'historique.
    func toVehicle() -> Vehicle {
        return Vehicle(
            immatriculation: self.licensePlate, // On fait correspondre les champs
            brand: self.brand,
            model: self.model,
            info: self.info,
            energy: self.energy,
            isFavorite: false // Par défaut, on ne sait pas s'il est favori depuis l'historique
        )
    }
}
