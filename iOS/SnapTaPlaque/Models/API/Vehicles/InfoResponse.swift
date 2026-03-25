//
//  InfoResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

struct InfoResponse: Codable {
    let licensePlate: String
    let brand: String?
    let model: String?
    let info: String?
    let energy: String?
    
    enum CodingKeys: String, CodingKey {
        case licensePlate = "license_plate"
        case brand
        case model
        case info
        case energy
    }
    
    // Équivalent de votre méthode createVehicles(boolean isFavorite)
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
