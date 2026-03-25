//
//  RootService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import Foundation

class RootService {
    
    func fetchPrivacyPolicy(requestData: RgpdRequest) async throws -> RgpdResponse {
        guard var request = ApiClient.shared.createRequest(endpoint: "/privacy-policy", method: "POST") else {
            throw URLError(.badURL)
        }
        
        request.httpBody = try JSONEncoder().encode(requestData)
        
        return try await ApiClient.shared.performRequest(request: request)
    }
}
