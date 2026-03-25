//
//  PrivacyPolicyView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

struct PrivacyPolicyView: View {
    @Environment(\.dismiss) var dismiss
    
    @State private var policyResponse: RgpdResponse?
    @State private var isLoading = true
    @State private var errorMessage: String?
    
    private let rootService = RootService()
    
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
    
    private func loadPolicy() async {
        isLoading = true
        errorMessage = nil
        
        do {
            // Optionnel : Passer la langue du téléphone si votre API gère l'internationalisation
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

/// Vue pour afficher un titre et un texte simple
struct SectionView: View {
    let title: String
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

/// Vue pour afficher un titre et une liste à puces (Tableaux de chaînes)
struct SectionListView: View {
    let title: String
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

/// Vue pour afficher le détail d'un droit spécifique
struct RightDetailView: View {
    let title: String
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
