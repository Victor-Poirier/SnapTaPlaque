//
//  InfoRequest.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation

struct InfoRequest: Codable {
    let licensePlate: String
    
    enum CodingKeys: String, CodingKey {
        case licensePlate = "license_plate"
    }
}
