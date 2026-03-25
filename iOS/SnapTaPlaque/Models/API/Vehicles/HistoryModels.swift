//
//  HistoryModels.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

// Le modèle de la réponse globale
struct HistoryVehiclesResponse: Codable {
    let history: [HistoryVehicleItem]
}

// Le modèle pour un seul élément de l'historique
struct HistoryVehicleItem: Codable, Identifiable {
    // Identifiable demande un 'id' unique pour que la liste SwiftUI fonctionne
    var id: String { licensePlate }
    
    let licensePlate: String
    let brand: String?
    let model: String?
    let info: String?
    let energy: String?
    
    enum CodingKeys: String, CodingKey {
        case licensePlate = "license_plate"
        case brand, model, info, energy
    }
}

extension HistoryVehicleItem {
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
