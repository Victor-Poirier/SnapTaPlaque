//
//  RegisterRequest.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

struct RegisterRequest: Codable {
    let email: String
    let username: String
    let password: String
    let fullName: String
    let isAdmin: Bool
    let gdprConsent: Bool
    
    enum CodingKeys: String, CodingKey {
        case email
        case username
        case password
        case fullName = "full_name"
        case isAdmin = "is_admin"
        case gdprConsent = "gdpr_consent"
    }
}
