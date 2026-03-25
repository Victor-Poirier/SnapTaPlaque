//
//  HistoryView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

struct HistoryView: View {
    @State private var historyList: [HistoryVehicleItem] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    
    // NOUVEAU : Variables pour gérer la modale
    @State private var selectedVehicle: Vehicle? = nil
    
    
    private let vehicleService = VehicleService()
    
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
                        // NOUVEAU : On rend la ligne cliquable
                        Button(action: {
                            // On convertit l'historique en véhicule
                            selectedVehicle = historyItem.toVehicle()
                        }) {
                            HistoryRow(vehicle: historyItem)
                        }
                        // Pour que le bouton n'ait pas le style "texte bleu" par défaut
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
    
    private func loadHistory() async {
        if historyList.isEmpty { isLoading = true }
        errorMessage = nil
        
        do {
            let response = try await vehicleService.getHistory()
            
            // NOUVEAU : On utilise .reversed() pour mettre la dernière recherche tout en haut !
            historyList = response.history.reversed()
            
        } catch {
            errorMessage = "Impossible de charger l'historique."
            print("Erreur historique : \(error)")
        }
        
        isLoading = false
    }
}

// MARK: - Le design d'une ligne (Cellule) de la liste
struct HistoryRow: View {
    let vehicle: HistoryVehicleItem
    
    // On réutilise la même logique que pour la modale
    var logoURL: URL? {
        guard let brand = vehicle.brand else { return nil }
        let slug = brand.lowercased().trimmingCharacters(in: .whitespaces).replacingOccurrences(of: " ", with: "-")
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
