//
//  SessionManager.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation
import Combine

/// Un gestionnaire de session utilisateur chargé de maintenir l'état d'authentification.
///
/// `SessionManager` utilise `UserDefaults` pour persister le jeton d'accès (JWT) de l'utilisateur.
/// Il vérifie automatiquement l'expiration du jeton au lancement et expose l'état de connexion (`isLoggedIn`)
/// pour réagir dynamiquement dans les vues SwiftUI.
class SessionManager: ObservableObject {
    
    /// L'instance partagée (singleton) du gestionnaire de session.
    static let shared = SessionManager()
    
    /// Indique si un utilisateur est actuellement authentifié.
    ///
    /// Cette propriété est publiée (`@Published`), ce qui permet aux vues SwiftUI
    /// de se recharger automatiquement lors d'une connexion ou déconnexion.
    @Published var isLoggedIn: Bool = false
    
    private let tokenKey = "user_access_token"
    
    private init() {
        // Au lancement, on fait une vérification stricte
        verifySessionOnLaunch()
    }
    
    /// Vérifie si le jeton existe et s'il n'est pas expiré au démarrage de l'application.
    ///
    /// Si le jeton est expiré, la session est automatiquement fermée via `logout()`.
    private func verifySessionOnLaunch() {
        guard let token = getToken() else {
            self.isLoggedIn = false
            return
        }
        
        if isTokenExpired(token: token) {
            print("🟡 Le token est déjà expiré au lancement. Déconnexion.")
            logout()
        } else {
            print("🟢 Le token est valide.")
            self.isLoggedIn = true
        }
    }
    
    /// Sauvegarde le jeton d'accès de l'utilisateur et met à jour l'état de la session.
    ///
    /// Le jeton est persisté de manière asynchrone pour recharger l'interface en toute sécurité.
    ///
    /// - Parameter token: Le jeton JWT (JSON Web Token) sous forme de chaîne de caractères.
    func saveToken(_ token: String) {
        UserDefaults.standard.set(token, forKey: tokenKey)
        DispatchQueue.main.async {
            self.isLoggedIn = true
        }
    }
    
    /// Récupère le jeton d'accès actuellement persisté.
    ///
    /// - Returns: Le jeton JWT s'il existe, sinon `nil`.
    func getToken() -> String? {
        return UserDefaults.standard.string(forKey: tokenKey)
    }
    
    /// Déconnecte l'utilisateur en supprimant son jeton d'accès et met à jour l'état de la session.
    func logout() {
        UserDefaults.standard.removeObject(forKey: tokenKey)
        DispatchQueue.main.async {
            self.isLoggedIn = false
        }
    }
    
    // MARK: - JWT Decoder
    
    /// Décode localement un jeton JWT pour vérifier sa date d'expiration (`exp`).
    ///
    /// - Parameter token: Le jeton JWT à analyser.
    /// - Returns: `true` si le jeton est expiré ou invalide, `false` s'il est encore valide.
    private func isTokenExpired(token: String) -> Bool {
        let parts = token.components(separatedBy: ".")
        guard parts.count == 3 else { return true } // Un JWT a toujours 3 parties
        
        var base64Payload = parts[1]
        
        // Swift nécessite que le Base64 soit un multiple de 4, on ajoute le "padding" manquant
        let remainder = base64Payload.count % 4
        if remainder > 0 {
            base64Payload = base64Payload.padding(toLength: base64Payload.count + 4 - remainder, withPad: "=", startingAt: 0)
        }
        
        // On remplace les caractères spécifiques aux URL du JWT
        base64Payload = base64Payload.replacingOccurrences(of: "-", with: "+").replacingOccurrences(of: "_", with: "/")
        
        guard let payloadData = Data(base64Encoded: base64Payload),
              let json = try? JSONSerialization.jsonObject(with: payloadData) as? [String: Any],
              let exp = json["exp"] as? TimeInterval else {
            return true // En cas d'erreur de décodage, on considère le token expiré par sécurité
        }
        
        // On compare la date d'expiration (exp) à la date actuelle
        let expirationDate = Date(timeIntervalSince1970: exp)
        return Date() >= expirationDate
    }
}
