//
//  AccountService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation
import UIKit

class AccountService {
    
    // Fonction asynchrone pour se connecter
    func login(credentials: LoginRequest) async throws -> LoginResponse {
        guard var request = ApiClient.shared.createRequest(endpoint: "/v1/account/login", method: "POST") else {
            throw URLError(.badURL)
        }
        
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        let formString = "username=\(credentials.username)&password=\(credentials.password)"
        request.httpBody = formString.data(using: .utf8)
        
        // On lance la requête avec URLSession
        let (data, response) = try await URLSession.shared.data(for: request)
        
        // On vérifie que le code HTTP est bien 200 (OK)
        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
            throw URLError(.badServerResponse)
        }
        
        // On décode la réponse JSON en LoginResponse
        let loginResponse = try JSONDecoder().decode(LoginResponse.self, from: data)
       
        
        return loginResponse
    }
    
    // Fonction asynchrone pour l'inscription
    func register(requestData: RegisterRequest) async throws -> RegisterResponse {
        guard var request = ApiClient.shared.createRequest(endpoint: "/v1/account/register", method: "POST") else{
            throw URLError(.badURL)
        }
        
        // FastAPI attend du JSON pour cette route
        request.httpBody = try JSONEncoder().encode(requestData)
        
        
        return try await ApiClient.shared.performRequest(request: request)
        
    }
    
    // 1. Récupérer les infos (Nom, Email)
    func getMe() async throws -> MeResponse {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/account/me", method: "GET") else {
            throw URLError(.badURL)
        }
        return try await ApiClient.shared.performRequest(request: request)
    }
    
    // 2. Télécharger la photo de profil
    func getProfilePicture() async throws -> UIImage? {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/account/me/profile-picture", method: "GET") else {
            throw URLError(.badURL)
        }
        
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            return nil // Pas de photo ou erreur 404
        }
        
        return UIImage(data: data)
    }
    
    // 3. Supprimer la photo
    func deleteProfilePicture() async throws {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/account/me/delete-profile-picture", method: "DELETE") else {
            throw URLError(.badURL)
        }
        let (_, _) = try await URLSession.shared.data(for: request)
    }
    
    // 4. Modifier la photo (Upload Multipart)
    func uploadProfilePicture(image: UIImage) async throws {
        guard var request = ApiClient.shared.createRequest(endpoint: "/v1/account/me/change-profile-picture", method: "POST") else {
            throw URLError(.badURL)
        }
        
        // Compression de l'image (0.8 = bonne qualité, poids réduit)
        guard let imageData = image.jpegData(compressionQuality: 0.8) else { throw URLError(.cannotDecodeRawData) }
        
        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        // Construction du corps de la requête Multipart
        var body = Data()
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"profile.jpg\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        body.append(imageData)
        body.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        
        request.httpBody = body
        
        let (_, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }
}
