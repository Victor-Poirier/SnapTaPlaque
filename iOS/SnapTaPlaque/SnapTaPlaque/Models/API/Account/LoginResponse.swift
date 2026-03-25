//
//  LoginResponse.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

struct LoginResponse: Codable {
    let accessToken: String
    
    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
    }
}
