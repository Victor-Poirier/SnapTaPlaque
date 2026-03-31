//
//  SignInView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import SwiftUI

/// La vue principale d'authentification permettant à un utilisateur existant de se connecter à "SnapTaPlaque".
///
/// `SignInView` gère le formulaire de connexion (nom d'utilisateur et mot de passe),
/// lance les requêtes réseau via `AccountService`, et met à jour l'état de session global (`SessionManager`)
/// en cas de succès. Elle inclut également des accès rapides vers la page d'inscription et la politique de confidentialité.
struct SignInView: View {
    // État pour stocker les entrées de l'utilisateur
    @State private var username = ""
    @State private var password = ""
    
    // État pour gérer le chargement et les erreurs
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showPrivacyPolicy = false
    
    // On accède à notre SessionManager pour mettre à jour l'état de connexion
    @ObservedObject var sessionManager = SessionManager.shared
    
    // Instance de notre service API
    private let accountService = AccountService()
    
    /// Le contenu visuel (body) de la vue SwiftUI.
    var body: some View {
        NavigationView { // Permet d'avoir une barre de navigation
            VStack(spacing: 25) {
                
                Image("logo2")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 100, height: 100)
                    .foregroundColor(.blue)
                    .padding(.top, 50)
                
                Text("Connexion")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                
                VStack(spacing: 15) {
                    TextField("Nom d'utilisateur", text: $username)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(10)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                    
                    SecureField("Mot de passe", text: $password)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(10)
                }
                .padding(.horizontal)
                
                
                if let errorMessage = errorMessage {
                    Text(errorMessage)
                        .foregroundColor(.red)
                        .font(.footnote)
                }
                
            
                Button(action: {
                    Task {
                        await performLogin()
                    }
                }) {
                    HStack {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(.trailing, 5)
                        }
                        Text("Se Connecter")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .padding(.horizontal)
                .disabled(isLoading || username.isEmpty || password.isEmpty)
                
                Button(action: {
                        showPrivacyPolicy = true
                    }) {
                        Text("Politique de confidentialité")
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.blue)
                    }
                    .padding(.bottom, 32)
                    .sheet(isPresented: $showPrivacyPolicy) {
                        PrivacyPolicyView()
                    }
                
                Spacer()
        
                
                
                NavigationLink(destination: SignUpView()) {
                    Text("Pas encore de compte ? S'inscrire")
                        .font(.footnote)
                        .foregroundColor(.blue)
                }
            }
            // Permet de fermer le clavier quand on clique ailleurs
            .onTapGesture {
                UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
            }
        }
    }
    
    /// Procède à la vérification des identifiants et tente d'établir une session sécurisée.
    ///
    /// Cette méthode asynchrone est déclenchée lors de l'appui sur le bouton "Se Connecter".
    /// Elle encode les données dans `LoginRequest`, interroge l'API via `AccountService`,
    /// puis gère les états de l'interface (`isLoading`) ainsi que la sauvegarde du jeton local sur succès.
    private func performLogin() async {
        // Réinitialisation de l'état
        isLoading = true
        errorMessage = nil
        
        let request = LoginRequest(username: username, password: password)
        
        do {
            let response = try await accountService.login(credentials: request)
            
            // Succès : On sauvegarde le token, ce qui mettra à jour l'UI grâce à @Published
            sessionManager.saveToken(response.accessToken)
            
        } catch {
            // Échec : On affiche un message d'erreur
            errorMessage = "Identifiants incorrects ou problème de connexion."
            print("Erreur de connexion : \(error)")
        }
        
        isLoading = false
    }
}

/// Aperçu en direct (Canvas) de `SignInView` pour Xcode.
struct SignInView_Previews: PreviewProvider {
    static var previews: some View {
        SignInView()
    }
}
