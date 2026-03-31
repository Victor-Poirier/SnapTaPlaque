//
//  HistoryView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

/// La vue principale affichant l'historique complet des véhicules scannés par l'utilisateur.
///
/// `HistoryView` utilise le `VehicleService` pour interroger l'API distante de manière asynchrone
/// et dresse une liste chronologique (`.reversed()`) des différentes requêtes passées via un composant SwiftUI orienté `List`.
/// Un clic sur une cellule ouvre le véhicule concerné pour afficher des détails approfondis.
struct HistoryView: View {
    
    /// Un tableau stockant l'ensemble de l'historique téléchargé depuis l'API.
    @State private var historyList: [HistoryVehicleItem] = []
    
    /// Détermine si un appel réseau (rafraîchissement ou accès initial) est en cours de téléchargement.
    @State private var isLoading = true
    
    /// Une chaîne de caractères contenant une erreur métier si le réseau échoue.
    @State private var errorMessage: String?
    
    /// Un pointeur vers le véhicule que l'utilisateur souhaite scruter en détail.
    /// Si cette donnée est renseignée, la modale (`sheet`) s'ouvre automatiquement.
    @State private var selectedVehicle: Vehicle? = nil
    
    private let vehicleService = VehicleService()
    
    /// Le contenu visuel (body) de la vue d'historique `HistoryView`.
    var body: some View {
        NavigationView {
            ZStack {
                Color(.systemGroupedBackground)
                    .ignoresSafeArea()
                
                if isLoading {
                    ProgressView("Chargement de vos recherches...")
                } else if let errorMessage = errorMessage {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 40))
                            .foregroundColor(.orange)
                        Text(errorMessage)
                            .multilineTextAlignment(.center)
                        Button("Réessayer") {
                            Task { await loadHistory() }
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding()
                } else if historyList.isEmpty {
                    VStack(spacing: 15) {
                        Image(systemName: "clock.badge.xmark")
                            .font(.system(size: 50))
                            .foregroundColor(.gray)
                        Text("Aucun historique")
                            .font(.headline)
                        Text("Vos futures recherches apparaîtront ici.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                } else {
                    List(historyList) { historyItem in
                        Button(action: {
                            // On convertit l'historique en véhicule
                            selectedVehicle = historyItem.toVehicle()
                        }) {
                            HistoryRow(vehicle: historyItem)
                        }
                        .buttonStyle(.plain)
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Historique")
            .task {
                await loadHistory()
            }
            .sheet(item: $selectedVehicle) { vehicle in
                VehicleDetailView(vehicle: vehicle)
            }
        }
    }
    
    /// Lance un rafraîchissement global de l'historique de recherche en sollicitant l'API réseau.
    ///
    /// Extrait la grappe JSON via le composant asynchrone `getHistory()`,
    /// manipule (`.reversed()`) les résultats pour présenter les dernières immatriculations en premier,
    /// puis clôture l'indicateur de chargement (`isLoading = false`).
    private func loadHistory() async {
        if historyList.isEmpty { isLoading = true }
        errorMessage = nil
        
        do {
            let response = try await vehicleService.getHistory()
            
            historyList = response.history.reversed()
            
        } catch {
            errorMessage = "Impossible de charger l'historique."
            print("Erreur historique : \(error)")
        }
        
        isLoading = false
    }
}

// MARK: - Le design d'une ligne (Cellule) de la liste

/// Un composant horizontal représentant une unique ligne (cellule) au sein du tableau d'historique global.
///
/// Modèle structuré pour dévoiler le logo (téléchargé asynchronement depuis l'URL formatée GitHub à l'aide de
/// la méthode de nommage par slug), ainsi que le numéro de plaque avec la police `.monospaced` et le nom du modèle.
struct HistoryRow: View {
    
    /// La structure de données provenant de l'historique API.
    let vehicle: HistoryVehicleItem
    
    /// Construit l'URL externe à destination d'un repo GitHub distant pour rafraîchir un logo de marque automobile.
    ///
    /// - Note: Cette propriété génère un "slug" adapté (minuscules, tirets en remplacement d'espaces).
    var logoURL: URL? {
        guard let brand = vehicle.brand else { return nil }
        let slug = brand.lowercased().trimmingCharacters(in: .whitespaces).replacingOccurrences(of: " ", with: "-").replacingOccurrences(of: "ë", with: "e")
        return URL(string: "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/logos/optimized/\(slug).png")
    }
    
    var body: some View {
        HStack(spacing: 15) {
            
            // Le Logo
            AsyncImage(url: logoURL) { phase in
                if let image = phase.image {
                    image
                        .resizable()
                        .scaledToFit()
                        .frame(width: 40, height: 40)
                } else {
                    // Placeholder si pas de logo
                    Image(systemName: "car.circle.fill")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 40, height: 40)
                        .foregroundColor(.gray)
                }
            }
            
            // Les Textes
            VStack(alignment: .leading, spacing: 4) {
                Text(vehicle.licensePlate)
                    .font(.headline)
                    .fontWeight(.black)
                    .fontDesign(.monospaced) // Style plaque
                
                Text("\(vehicle.brand ?? "Inconnu") \(vehicle.model ?? "")")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            
            Spacer()
            
            // Une petite flèche à droite pour indiquer qu'on peut cliquer
            Image(systemName: "chevron.right")
                .font(.footnote)
                .foregroundColor(.gray)
        }
        .padding(.vertical, 4)
    }
}
