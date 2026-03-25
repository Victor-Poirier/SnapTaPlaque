//
//  SessionManager.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import Foundation
import Combine

class SessionManager: ObservableObject {
    static let shared = SessionManager()
    
    @Published var isLoggedIn: Bool = false
    
    private let tokenKey = "user_access_token"
    
    private init() {
        // Au lancement, on fait une vérification stricte
        verifySessionOnLaunch()
    }
    
    /// Vérifie si le token existe ET s'il n'est pas expiré
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
    
    func saveToken(_ token: String) {
        UserDefaults.standard.set(token, forKey: tokenKey)
        DispatchQueue.main.async {
            self.isLoggedIn = true
        }
    }
    
    func getToken() -> String? {
        return UserDefaults.standard.string(forKey: tokenKey)
    }
    
    func logout() {
        UserDefaults.standard.removeObject(forKey: tokenKey)
        DispatchQueue.main.async {
            self.isLoggedIn = false
        }
    }
    
    // MARK: - JWT Decoder
    /// Décode le JWT localement pour lire la date d'expiration ("exp")
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
