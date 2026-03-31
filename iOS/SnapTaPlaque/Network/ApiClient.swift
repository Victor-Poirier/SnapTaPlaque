//
//  ApiClient.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation

/// Le client réseau (Singleton) principal de l'application SnapTaPlaque.
///
/// `ApiClient` centralise la construction des requêtes et leur exécution.
/// Il injecte automatiquement le jeton d'authentification (`SessionManager`) dans chaque appel
/// et intercepte globalement les erreurs, notamment les expirations de session (code HTTP 401).
class ApiClient {
    
    /// L'instance partagée unique du client API.
    static let shared = ApiClient()
    
    /// L'URL serveur de base vers laquelle pointent tous les appels réseau (à adapter pour l'environnement de production).
    let baseURL = "https://danny-nonpresumptive-jadedly.ngrok-free.dev"
    
    private init() {}
    
    // MARK: - 1. Créateur de Requête
    
    /// Construit et configure une requête HTTP initiale.
    ///
    /// Cette méthode attache les headers par défaut (`application/json`)
    /// et injecte automatiquement l'en-tête de sécurité `Authorization: Bearer <token>`
    /// si un utilisateur est actuellement connecté.
    ///
    /// - Parameters:
    ///   - endpoint: La route finale de l'API (ex: `"/v1/vehicles"`).
    ///   - method: La méthode HTTP souhaitée (`"GET"`, `"POST"`, `"DELETE"`, etc.).
    /// - Returns: Une `URLRequest` prête à l'envoi ou `nil` si l'URL est mal formatée.
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
    
    /// Lance la transmission de la requête réseau formatée de manière asynchrone.
    ///
    /// Gère globalement le cycle de vie de la réponse:
    /// - Le suivi (monitoring) dans les instructions en console (`print`).
    /// - L'interception du code `HTTP 401` pour forcer la déconnexion locale de l'utilisateur (`SessionManager.shared.logout()`).
    /// - La vérification de validité de retour (bloc 200 à 299).
    /// - Le décodage automatique du corps de retour (JSON) si un type conforme au protocole `Decodable` est spécifié par l'appelant.
    ///
    /// - Parameter request: La requête finalisée (`URLRequest`) devant être exécutée par la session URL.
    /// - Returns: La donnée serveur formatée au modèle générique `<T>` demandé.
    /// - Throws: `URLError` (si non autorisée ou erreur serveur) ou les erreurs de mapping du `JSONDecoder`.
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
