//
//  PrivacyPolicyView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

/// La vue dédiée à l'affichage dynamique de la politique de confidentialité (RGPD).
///
/// `PrivacyPolicyView` récupère et affiche les informations légales relatives à la gestion
/// des données personnelles. Elle appelle de manière asynchrone le `RootService` pour charger
/// formellement les règles de l'API sous forme de JSON (`RgpdResponse`).
struct PrivacyPolicyView: View {
    // Environnement pour permettre la fermeture manuelle de la modale.
    @Environment(\.dismiss) var dismiss
    
    /// État interne pour stocker la réponse de la politique de confidentialité.
    @State private var policyResponse: RgpdResponse?
    
    /// Indicateur de chargement pour afficher un spinner pendant la récupération des données.
    @State private var isLoading = true
    
    /// Message d'erreur à afficher en cas d'échec de chargement des données.
    @State private var errorMessage: String?
    
    private let rootService = RootService()
    
    /// Le contenu visuel (body) de la vue d'affichage de politique de confidentialité.
    var body: some View {
        NavigationView {
            Group {
                if isLoading {
                    ProgressView("Chargement des politiques...")
                } else if let errorMessage = errorMessage {
                    // Affichage de l'erreur
                    VStack(spacing: 16) {
                        Text("Erreur")
                            .font(.headline)
                            .foregroundColor(.red)
                        Text(errorMessage)
                            .multilineTextAlignment(.center)
                        Button("Réessayer") {
                            Task { await loadPolicy() }
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding()
                } else if let policy = policyResponse {
                    // Affichage des données RGPD
                    ScrollView {
                        VStack(alignment: .leading, spacing: 24) {
                            
                            SectionView(title: "Responsable du traitement", content: policy.controller)
                            
                            SectionListView(title: "Contact", items: policy.contact)
                            
                            SectionView(title: "Finalité", content: policy.purpose)
                            
                            SectionView(title: "Base légale", content: policy.legalBasis)
                            
                            SectionListView(title: "Données collectées", items: policy.dataCollected)
                            
                            SectionView(title: "Durée de conservation", content: policy.retentionPeriod)
                            
                            // Affichage des droits des utilisateurs
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Vos droits")
                                    .font(.title3)
                                    .fontWeight(.bold)
                                    .foregroundColor(.primary)
                                
                                VStack(alignment: .leading, spacing: 8) {
                                    RightDetailView(title: "Droit d'accès", description: policy.userRights.access)
                                    RightDetailView(title: "Droit de rectification", description: policy.userRights.rectification)
                                    RightDetailView(title: "Droit à l'effacement", description: policy.userRights.erasure)
                                }
                                .padding(.bottom, 8)
                            }
                
                            
                            SectionView(title: "Partage des données", content: policy.dataSharing)
                            
                            SectionListView(title: "Mesures de sécurité", items: policy.securityMeasures)
                            
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Confidentialité")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Fermer") {
                        dismiss()
                    }
                    .fontWeight(.bold)
                }
            }
            .task {
                await loadPolicy()
            }
        }
    }
    
    /// Charge de manière asynchrone la politique de confidentialité locale depuis l'API backend.
    ///
    /// Interroge le `RootService` avec une demande `RgpdRequest` en précisant le code langue `"fr"`.
    /// À son aboutissement, la vue change d'état et le spinner de chargement (`isLoading`) laisse
    /// sa place à l'interface structurée. Si une erreur de traitement intervient, un bouton permet
    /// le re-téléchargement à la demande de l'utilisateur.
    private func loadPolicy() async {
        isLoading = true
        errorMessage = nil
        
        do {
            // Optionnel : Passer la langue du téléphone
            let request = RgpdRequest(language: "fr")
            policyResponse = try await rootService.fetchPrivacyPolicy(requestData: request)
        } catch {
            errorMessage = "Impossible de charger la politique de confidentialité. Veuillez vérifier votre connexion."
            print("Erreur RGPD : \(error)")
        }
        
        isLoading = false
    }
}

// MARK: - Sous-vues pour structurer l'affichage

/// Un composant visuel affichant un titre mis en gras suivi par un paragraphe de texte descriptif.
struct SectionView: View {
    
    /// L'en-tête de la section tel qu'il apparaîtra à l'écran.
    let title: String
    
    ///  Le texte descriptif, détaillé et long de la condition visée.
    let content: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            Text(content)
                .font(.body)
                .foregroundColor(.secondary)
        }
    }
}

/// Un composant visuel présentant un titre couplé à une liste de chaînes textuelles (bullet-points).
///
/// Cette structure permet un rendu lisible et aéré pour des conditions empilées.
struct SectionListView: View {
    
    /// L'en-tête thématique de cette liste.
    let title: String
    
    /// Un tableau de textes simples repéré par son contenu via le rendu automatique SwiftUI (`id: \.self`).
    let items: [String]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            ForEach(items, id: \.self) { item in
                HStack(alignment: .top) {
                    Text("•")
                    Text(item)
                }
                .font(.body)
                .foregroundColor(.secondary)
            }
        }
    }
}

/// Un composant local épuré visant à exposer les différents droits garantis à un utilisateur.
///
/// Utile pour bien distinguer à l'écran chaque type régalien du droit (comme
/// le *Droit d'accès*, le *Droit de rectification* ou le *Droit à l'effacement*).
struct RightDetailView: View {
    
    /// L'intitulé de nature du droit RGPD (ex: "Droit à l'effacement").
    let title: String
    
    ///  Les modalités et garanties concernant l'application dudit droit.
    let description: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            Text(description)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }
}
