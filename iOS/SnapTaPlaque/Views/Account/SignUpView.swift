//
//  SignUpView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//

import SwiftUI

struct SignUpView: View {
    // Permet de revenir en arrière (vers la page de connexion)
    @Environment(\.dismiss) var dismiss
    
    // États pour les champs
    @State private var username = ""
    @State private var fullName = ""
    @State private var email = ""
    @State private var password = ""
    @State private var gdprConsent = false
    
    // États de gestion
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showPrivacyPolicy = false
    
    private let accountService = AccountService()
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                
                // 1. Logo
                Image("logo") // Remplacez par Image("logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 140, height: 100)
                    .foregroundColor(.blue)
                    .padding(.top, 24)
                
                // 2. Conteneur principal (équivalent de votre LinearLayout @drawable/cadre_formulaire)
                VStack(spacing: 16) {
                    Text("Rejoignez-nous")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                    
                    // Champs de saisie
                    Group {
                        TextField("Identifiant", text: $username)
                            .autocapitalization(.none)
                            .onChange(of: username) { _, newValue in
                                // Équivalent de replaceAll("[^a-zA-Z0-9]", "")
                                username = newValue.filter { $0.isLetter || $0.isNumber }
                            }
                        
                        TextField("Nom complet", text: $fullName)
                        
                        TextField("Adresse e-mail", text: $email)
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                        
                        SecureField("Mot de passe", text: $password)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
                    
                    // RGPD (équivalent du SwitchCompat)
                    Toggle(isOn: $gdprConsent) {
                        Text("J'accepte la politique de confidentialité")
                            .font(.footnote)
                            .foregroundColor(gdprConsent ? .primary : .red)
                    }
                    .tint(.red)
                    
                    // Affichage d'erreur
                    if let errorMessage = errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .font(.footnote)
                            .multilineTextAlignment(.center)
                    }
                    
                    // Boutons
                    VStack(spacing: 12) {
                        Button(action: {
                            Task {
                                await performRegister()
                            }
                        }) {
                            HStack {
                                if isLoading { ProgressView().padding(.trailing, 5) }
                                Text("S'INSCRIRE")
                                    .fontWeight(.bold)
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(15)
                        }
                        .disabled(isLoading)
                    }
                    .padding(.top, 10)
                }
                .padding(20)
                .background(Color(.systemBackground)) // Fond blanc/noir selon thème
                .cornerRadius(20)
                .shadow(radius: 5)
                .padding(.horizontal, 24)
                
                // 3. Lien Politique de confidentialité
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
                
                NavigationLink(destination: SignInView()) {
                    Text("Déjà inscrit ? Se connecter")
                        .font(.footnote)
                        .foregroundColor(.blue)
                }
            }
        }
        .background(Color(.systemGray5).edgesIgnoringSafeArea(.all)) // Fond gris global
        .navigationBarHidden(true) // On cache la barre pour avoir l'effet plein écran
        // Pour cacher le clavier
        .onTapGesture {
            UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
        }
    }
    
    // Logique d'inscription
    private func performRegister() async {
        let cleanUsername = username.trimmingCharacters(in: .whitespaces)
        let cleanEmail = email.trimmingCharacters(in: .whitespaces)
        let cleanFullName = fullName.trimmingCharacters(in: .whitespaces)
        
        // Vérification des champs
        if cleanUsername.isEmpty || cleanEmail.isEmpty || password.isEmpty || cleanFullName.isEmpty || !gdprConsent {
            errorMessage = "Veuillez remplir tous les champs et accepter la politique."
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        let request = RegisterRequest(
            email: cleanEmail,
            username: cleanUsername,
            password: password,
            fullName: cleanFullName,
            isAdmin: false,
            gdprConsent: gdprConsent
        )
        
        do {
            // 1. Inscription
            let _ = try await accountService.register(requestData: request)
            
            // 2. Connexion automatique
            let loginResponse = try await accountService.login(credentials: LoginRequest(username: cleanUsername, password: password))
            
            // 3. Sauvegarde du token (C'est ceci qui va déclencher le changement d'écran !)
            SessionManager.shared.saveToken(loginResponse.accessToken)
            
        } catch {
            errorMessage = "Erreur lors de l'inscription. L'email ou l'identifiant existe peut-être déjà."
            print("Erreur : \(error)")
        }
        isLoading = false
    }
}
