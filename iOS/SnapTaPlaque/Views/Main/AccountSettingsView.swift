//
//  AccountSettingsView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 30/03/2026.
//

import SwiftUI

/// La vue de gestion et de configuration des paramètres du compte utilisateur.
///
/// `AccountSettingsView` est destinée à regrouper toutes les options d'administration
/// propres à un profil, affichée sous forme de modale depuis le `ProfileView`.
/// Elle permet notamment d'exporter les données personnelles (règlementation RGPD)
/// et d'initier la suppression irréversible du compte.
struct AccountSettingsView: View {
    
    /// Proriété d'environnement permettant de fermer la modale et de revenir à la vue parente (`ProfileView`).
    @Environment(\.dismiss) var dismiss
    
    /// L'adresse courriel de l'utilisateur concerné, affichée à titre informatif dans la vue.
    let userEmail: String
    
    /// Une closure (action asynchrone ou non) déclenchée lorsque l'exportation des données est demandée.
    let onExport: () -> Void
    
    /// Une closure (action critique) déclenchée lorsque l'utilisateur valide la suppression définitive de son profil.
    let onDelete: () -> Void
    
    /// État booléen contrôlant l'apparition de la modale de sécurité (Alerte) avant de lancer un processus destructeur.
    @State private var showingDeleteConfirmation = false
    
    /// Le rendu déclaratif de l'interface qui expose les options du compte via une structure de liste (`List`).
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Informations")) {
                    HStack {
                        Text("Compte rattaché")
                        Spacer()
                        Text(userEmail)
                            .foregroundColor(.secondary)
                    }
                }
                
                Section(header: Text("Mes données (RGPD)"), footer: Text("Le fichier généré contiendra l'historique de vos requêtes ainsi que vos paramètres de profil associés.")) {
                    Button(action: {
                        onExport()
                        dismiss()
                    }) {
                        Label("Exporter toutes mes données", systemImage: "square.and.arrow.up")
                    }
                }
                
                Section(header: Text("Supprimer votre compte"), footer: Text("La suppression de votre compte est irréversible. Toutes vos données seront définitivement perdues.")) {
                    Button(role: .destructive, action: {
                        showingDeleteConfirmation = true
                    }) {
                        Label("Supprimer définitivement le compte", systemImage: "trash")
                            .foregroundColor(.red)
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Paramètres")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Terminer") {
                        dismiss()
                    }
                    .fontWeight(.bold)
                }
            }
            .alert("Confirmer la suppression ?", isPresented: $showingDeleteConfirmation) {
                Button("Annuler", role: .cancel) { }
                Button("Supprimer", role: .destructive) {
                    onDelete()
                    dismiss()
                }
            } message: {
                Text("Cette action est irréversible et supprimera l'intégralité de vos plaques sauvegardées sur le serveur.")
            }
        }
    }
}

/// Aperçu en direct (Canvas) utilisé dans Xcode pour visualiser la configuration de compte.
#Preview {
    AccountSettingsView(
        userEmail: "victor@snaptaplaque.fr",
        onExport: { print("Export mock") },
        onDelete: { print("Delete mock") }
    )
}
