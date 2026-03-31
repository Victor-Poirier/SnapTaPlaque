//
//  AccountService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation
import UIKit

/// Service réseau responsable de toute la gestion des requêtes relatives au compte utilisateur.
///
/// `AccountService` gère l'authentification (connexion, inscription) et la manipulation du profil
/// utilisateur (récupération des données, gestion de l'avatar photo, exportation RGPD, suppression de compte).
/// Toutes les méthodes exploitent des appels asynchrones (`async/await`) conformes aux standards Swift d'iOS 26.
class AccountService {
    
    /// Tente de connecter un utilisateur en envoyant ses identifiants.
    ///
    /// Cette méthode spécifie une requête au format `application/x-www-form-urlencoded` attendu par le backend 
    /// (notamment par défaut avec des protocoles comme OAuth2 sur FastAPI).
    ///
    /// - Parameter credentials: Un objet `LoginRequest` contenant le `username` et le `password`.
    /// - Returns: Une structure `LoginResponse` contenant le token JWT d'accès si la requête réussit.
    /// - Throws: `URLError` en cas de mauvaise URL ou d'une réponse non valide de la part du serveur (ex. code 401).
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
    
    /// Inscrit un nouvel utilisateur sur la plateforme.
    ///
    /// L'envoi des données s'effectue via le corps (body) de la requête au format JSON.
    ///
    /// - Parameter requestData: Un objet `RegisterRequest` contenant les éléments nécessaires (email, pseudo, mot de passe, consentements).
    /// - Returns: Une instance `RegisterResponse` validant l'inscription avec les détails du nouveau compte.
    /// - Throws: Une erreur du réseau si les contraintes du backend ne sont pas respectées.
    func register(requestData: RegisterRequest) async throws -> RegisterResponse {
        guard var request = ApiClient.shared.createRequest(endpoint: "/v1/account/register", method: "POST") else{
            throw URLError(.badURL)
        }
        
        // FastAPI attend du JSON pour cette route
        request.httpBody = try JSONEncoder().encode(requestData)
        
        
        return try await ApiClient.shared.performRequest(request: request)
        
    }
    
    /// Récupère l'ensemble des informations du profil (nom, email, rôle) associé au token en cours.
    ///
    /// - Returns: Un modèle `MeResponse` cartographiant la réponse JSON au format Swift.
    /// - Throws: Erreur réseau ou de décodage si le token est manquant/expiré.
    func getMe() async throws -> MeResponse {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/account/me", method: "GET") else {
            throw URLError(.badURL)
        }
        return try await ApiClient.shared.performRequest(request: request)
    }
    
    /// Lance le téléchargement de la photo de profil de l'utilisateur actif.
    ///
    /// - Returns: Un objet visuel `UIImage` contenant la photo, ou `nil` si l'utilisateur n'en possède pas (code API 404/autre).
    /// - Throws: Une erreur purement technique de construction de l'URLClient.
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
    
    /// Supprime définitivement la photo de profil du compte actif de la base de données.
    ///
    /// - Throws: `URLError` en cas de refus d'accès de la part du serveur.
    func deleteProfilePicture() async throws {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/account/me/delete-profile-picture", method: "DELETE") else {
            throw URLError(.badURL)
        }
        let (_, _) = try await URLSession.shared.data(for: request)
    }
    
    /// Modifie (ou ajoute) la photo de profil en envoyant sa donnée au format `multipart/form-data`.
    ///
    /// L'image est au préalable compressée logiciellement (jpegData avec qualité 0.8) pour soulager la bande passante.
    ///
    /// - Parameter image: L'objet interface `UIImage` préalablement sélectionné (ex: par `PhotosPicker` ou `ImagePicker`).
    /// - Throws: Une erreur `URLError` si la compression échoue ou que le serveur rejette la photo.
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
    
    /// Sollicite le backend pour exporter l'ensemble des données personnelles (pour conformité RGPD).
    ///
    /// - Returns: Un objet `Data` contenant le flux JSON brut des informations de l'utilisateur. 
    ///   Ces données peuvent ensuite être encodées dans un fichier ou partagées (`UIActivityViewController`).
    /// - Throws: Erreur serveur si la génération de l'export échoue.
    func exportData() async throws -> Data { // 👈 On renvoie Data maintenant
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/account/me/data-export", method: "GET") else {
            throw URLError(.badURL)
        }
        
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        
        // On retourne directement le JSON brut !
        return data
    }
    
    /// Procède à la suppression irréversible du compte utilisateur et de ses données associées (désinscription).
    ///
    /// - Throws: Une alerte système `URLError(.badServerResponse)` si le backend est dans l'incapacité d'effacer le profil.
    func deleteAccount() async throws {
        guard let request = ApiClient.shared.createRequest(endpoint: "/v1/account/me/delete-account", method: "DELETE") else {
            throw URLError(.badURL)
        }
        
        let (_, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }
}
