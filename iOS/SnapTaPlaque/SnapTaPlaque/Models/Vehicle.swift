//
//  Vehicle.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//
import Foundation

struct Vehicle: Codable, Identifiable{
    var id: String { immatriculation }
    
    let immatriculation: String
    let brand: String?
    let model: String?
    let info: String?
    let energy: String?
    var isFavorite: Bool
}
