//
//  Profil.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

struct Profil: Codable {
    let username: String
    let firstName: String
    let name: String
    let password: String?
    let email: String
    var favoritesVehicule: [Vehicle] = []
    
    enum CodingKeys: String, CodingKey {
        case username, name, password, email, favoritesVehicule
        case firstName = "firstName"
        
    }
}
