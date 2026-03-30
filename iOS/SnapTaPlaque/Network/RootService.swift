//
//  RootService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import Foundation

/// Service réseau chargé de communiquer avec les routes globales (root) du backend.
///
/// `RootService` gère les appels qui ne sont pas spécifiquement rattachés 
/// à un profil utilisateur ou à la gestion stricte des véhicules (comme les mentions légales ou les conditions générales).
class RootService {
    
    /// Récupère la politique de confidentialité (RGPD) depuis le serveur.
    ///
    /// Cette méthode interroge l'API via une requête `POST` permettant d'y inclure des critères locaux,
    /// tels que la langue souhaitée dans laquelle recevoir le document légal.
    ///
    /// - Parameter requestData: Un modèle `RgpdRequest` (souvent contenant le code langue `"fr"` ou `"en"`).
    /// - Returns: Une structure `RgpdResponse` contenant intégralement les différents points de la politique.
    /// - Throws: `URLError` en asynchrone si la connexion au serveur a échoué où que le parsing de la réponse est impossible.
    func fetchPrivacyPolicy(requestData: RgpdRequest) async throws -> RgpdResponse {
        guard var request = ApiClient.shared.createRequest(endpoint: "/privacy-policy", method: "POST") else {
            throw URLError(.badURL)
        }
        
        request.httpBody = try JSONEncoder().encode(requestData)
        
        return try await ApiClient.shared.performRequest(request: request)
    }
}
