//
//  RgpdModels.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import Foundation

struct RgpdRequest: Codable {
    let language: String?
}

struct RgpdResponse: Codable {
    let controller: String
    let contact: [String]
    let purpose: String
    let legalBasis: String
    let dataCollected: [String]
    let retentionPeriod: String
    let userRights: RgpdResultUserRight
    let dataSharing: String
    let securityMeasures: [String]
    
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

struct RgpdResultUserRight: Codable {
    let access: String
    let erasure: String
    let rectification: String
}
