//
//  SignInView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import SwiftUI

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
    
    var body: some View {
        NavigationView { // Permet d'avoir une barre de navigation
            VStack(spacing: 25) {
                
                // 1. En-tête / Logo (Ajustez le nom de l'image selon vos assets)
                Image("logo") // Placeholder, remplacez par Image("VotreLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 100, height: 100)
                    .foregroundColor(.blue) // Utilisez votre couleur primaire
                    .padding(.top, 50)
                
                Text("Connexion")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                // 2. Champs de saisie
                VStack(spacing: 15) {
                    TextField("Nom d'utilisateur", text: $username)
                        .padding()
                        .background(Color(.systemGray6)) // Équivalent d'un fond gris clair (cadre_formulaire)
                        .cornerRadius(10)
                        .autocapitalization(.none) // Important pour les noms d'utilisateur
                        .disableAutocorrection(true)
                    
                    SecureField("Mot de passe", text: $password)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(10)
                }
                .padding(.horizontal)
                
                // 3. Affichage des erreurs (s'il y en a)
                if let errorMessage = errorMessage {
                    Text(errorMessage)
                        .foregroundColor(.red)
                        .font(.footnote)
                }
                
                // 4. Bouton de Connexion
                Button(action: {
                    Task {
                        await performLogin()
                    }
                }) {
                    HStack {
                        if isLoading {
                            ProgressView() // Spinner de chargement
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .padding(.trailing, 5)
                        }
                        Text("Se Connecter")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue) // Remplacez par votre couleur principale
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .padding(.horizontal)
                .disabled(isLoading || username.isEmpty || password.isEmpty) // Désactive le bouton si vide ou en cours
                
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
                
                Spacer() // Pousse le contenu vers le haut
        
                
                // 5. Lien vers l'inscription
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
    
    // Fonction qui exécute l'appel API
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

// Pour avoir un aperçu directement dans Xcode
struct SignInView_Previews: PreviewProvider {
    static var previews: some View {
        SignInView()
    }
}
