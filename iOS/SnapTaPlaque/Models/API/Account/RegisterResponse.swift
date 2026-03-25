//
//  RegisterResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

struct RegisterResponse: Codable {
    let id: Int
    let email: String
    let username: String
    let fullName: String
    let isActive: Bool
    let isAdmin: Bool
    let createdAt: String
    
    // On mappe les variables Swift avec le format de votre API Python
    enum CodingKeys: String, CodingKey {
        case id, email, username
        case fullName = "full_name"
        case isActive = "is_active"
        case isAdmin = "is_admin"
        case createdAt = "created_at"
    }
}
