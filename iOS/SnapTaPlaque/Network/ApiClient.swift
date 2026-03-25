//
//  ApiClient.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

class ApiClient {
    static let shared = ApiClient()
    
    // N'oubliez pas de remettre votre vraie URL de base
    let baseURL = "https://danny-nonpresumptive-jadedly.ngrok-free.dev"
    
    private init() {}
    
    // MARK: - 1. Créateur de Requête
    func createRequest(endpoint: String, method: String) -> URLRequest? {
        guard let url = URL(string: baseURL + endpoint) else { return nil }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Injection automatique du token pour toutes les requêtes
        if let token = SessionManager.shared.getToken() {
            request.addValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        return request
    }
    
    // MARK: - 2. Exécuteur de Requête Générique
    /// Lance la requête, gère les erreurs globalement (dont le 401) et décode le JSON
    func performRequest<T: Decodable>(request: URLRequest) async throws -> T {
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        // Affichage de debug pour chaque requête
        print("🔵 Requête API : \(request.httpMethod ?? "N/A") \(request.url?.absoluteString ?? "N/A")")
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }
        
        // 🔴 INTERCEPTION GLOBALE DU TOKEN EXPIRÉ
        if httpResponse.statusCode == 401 {
            print("🔴 Erreur 401 globale : Le token est expiré ou invalide. Déconnexion.")
            SessionManager.shared.logout()
            throw URLError(.userAuthenticationRequired) // Stoppe l'exécution
        }
        
        // 🔴 GESTION DES AUTRES ERREURS HTTP (400, 404, 500...)
        if !(200...299).contains(httpResponse.statusCode) {
            let serverMessage = String(data: data, encoding: .utf8) ?? "Erreur inconnue"
            print("🔴 API ERREUR \(httpResponse.statusCode) : \(serverMessage)")
            throw URLError(.badServerResponse)
        }
        
        // 🟢 DÉCODAGE JSON AUTOMATIQUE
        do {
            let decodedData = try JSONDecoder().decode(T.self, from: data)
            return decodedData
        } catch {
            print("🔴 Erreur de décodage JSON vers \(T.self) : \(error)")
            throw error
        }
    }
}
