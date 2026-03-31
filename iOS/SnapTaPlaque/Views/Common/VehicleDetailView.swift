//
//  VehicleDetailView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import SwiftUI

/// La vue détaillée d'un véhicule permettant d'afficher ses caractéristiques complètes et de gérer son statut "Favori".
///
/// `VehicleDetailView` affiche la plaque formatée, la marque, le modèle, la motorisation ainsi qu'un logo
/// dynamique (téléchargé de façon asynchrone). Elle embarque des actions interactives
/// (ajout et retrait des favoris) gérées via `FavoritesService`.
struct VehicleDetailView: View {
    // Permet de fermer la vue (modale ou retour de navigation)
    @Environment(\.dismiss) var dismiss
    
    /// Le modèle de données du véhicule à afficher.
    let vehicle: Vehicle
    
    /// État local du favori, actualisé en fonction de l'interaction de l'utilisateur ou des données réseau.
    @State private var isFavorite: Bool
    
    /// Un indicateur (spinner) spécifiant si une requête réseau sur le statut des favoris est actuellement en transit.
    @State private var isUpdatingFavorite = false
    private let favoritesService = FavoritesService()
    
    /// Initialise la vue avec un véhicule spécifique.
    ///
    /// - Parameter vehicle: L'objet `Vehicle` à présenter. Configure également l'état
    ///   local du favori via l'attribut natif défini dans l'objet.
    init(vehicle: Vehicle) {
        self.vehicle = vehicle
        _isFavorite = State(initialValue: vehicle.isFavorite)
    }
    
    /// Construit l'URL externe à destination d'un repo GitHub distant pour rafraîchir un logo de marque automobile.
    ///
    /// - Note: Cette propriété génère un "slug" adapté (minuscules, tirets en remplacement d'espaces).
    var logoURL: URL? {
        guard let brand = vehicle.brand else { return nil }
        // On transforme "Alfa Romeo" en "alfa-romeo"
        let slug = brand.lowercased().trimmingCharacters(in: .whitespaces).replacingOccurrences(of: " ", with: "-").replacingOccurrences(of: "ë", with: "e")
        return URL(string: "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/logos/optimized/\(slug).png")
    }
    
    /// Le contenu visuel (body) de la vue affichant les détails du véhicule.
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                
                // 1. En-tête avec le Logo de la Marque
                AsyncImage(url: logoURL) { phase in
                    switch phase {
                    case .empty:
                        // Pendant le chargement
                        ProgressView()
                            .frame(width: 80, height: 80)
                    case .success(let image):
                        // Image chargée avec succès
                        image
                            .resizable()
                            .scaledToFit()
                            .frame(width: 100, height: 100)
                    case .failure(_):
                        // En cas d'erreur (ou si la marque n'est pas dans le repo Github)
                        Image(systemName: "car.circle.fill")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 80, height: 80)
                            .foregroundColor(.gray)
                    @unknown default:
                        EmptyView()
                    }
                }
                .padding(.top, 20)
                
                // 2. La plaque d'immatriculation
                Text(vehicle.immatriculation)
                    .font(.system(size: 34, weight: .black, design: .monospaced))
                    .padding(.vertical, 10)
                    .padding(.horizontal, 20)
                    .background(Color.white)
                    .foregroundColor(.black)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.black, lineWidth: 3)
                    )
                
                // 3. Les informations du véhicule
                VStack(spacing: 15) {
                    DetailRow(title: "Marque", value: vehicle.brand ?? "Inconnue")
                    DetailRow(title: "Modèle", value: vehicle.model ?? "Inconnu")
                    DetailRow(title: "Énergie", value: vehicle.energy ?? "Non spécifiée")
                    DetailRow(title: "Info", value: vehicle.info ?? "Non spécifié")
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(15)
                .padding(.horizontal)
                
                Spacer()
            }
            .navigationTitle("Détails du véhicule")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Fermer") { dismiss() }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        Task { await toggleFavorite() }
                    }) {
                        if isUpdatingFavorite {
                            ProgressView() // Petit loader à la place de l'étoile pendant l'appel API
                        } else {
                            Image(systemName: isFavorite ? "star.fill" : "star")
                                .foregroundColor(isFavorite ? .yellow : .gray)
                        }
                    }
                    .disabled(isUpdatingFavorite)
                }
            }
            .task {
                // Se lance tout seul quand la modale glisse vers le haut
                await checkFavoriteStatus()
            }
        }
    }
    
    /// Inverse l'état du "Favori" en effectuant un appel réseau par alternance (`DELETE` / `POST`).
    ///
    /// Basé sur la variable d'état locale `isFavorite`, cette fonction appelle le `FavoritesService`
    /// tout en prévenant l'interface (`isUpdatingFavorite` = true) pendant l'attente du backend.
    private func toggleFavorite() async {
        isUpdatingFavorite = true
        do {
            if isFavorite {
                try await favoritesService.removeFavorite(plate: vehicle.immatriculation)
                isFavorite = false
            } else {
                try await favoritesService.addFavorite(plate: vehicle.immatriculation)
                isFavorite = true
            }
        } catch {
            print("Erreur lors de la modification du favori: \(error)")
        }
        isUpdatingFavorite = false
    }
    
    /// Interroge le serveur silencieusement à l'ouverture de la vue pour confirmer la validité du statut favori du véhicule.
    private func checkFavoriteStatus() async {
        do {
            // On récupère la liste à jour des favoris
            let allFavorites = try await favoritesService.getAllFavorites()
            
            // On vérifie si notre plaque actuelle est dans cette liste
            let alreadyFavorited = allFavorites.contains(where: { $0.immatriculation == vehicle.immatriculation })
            
            // On met à jour l'étoile
            self.isFavorite = alreadyFavorited
        } catch {
            print("Impossible de vérifier l'état du favori.")
        }
    }
}

/// Un composant horizontal (ligne) permettant d'afficher uniformément un titre à gauche et sa valeur d'information à droite.
///
/// Ce composant interne à `VehicleDetailView` est particulièrement recommandé pour
/// rendre l'empilement d'informations (Marque, Modèle, etc.) fluide et lisible.
struct DetailRow: View {
    
    /// L'en-tête (le label) de la caractéristique à renseigner.
    let title: String
    
    /// La propriété dynamique extraite du véhicule (modèle, essence, etc.).
    let value: String
    
    var body: some View {
        HStack {
            Text(title)
                .fontWeight(.bold)
                .foregroundColor(.gray)
            Spacer()
            Text(value)
                .fontWeight(.semibold)
                .multilineTextAlignment(.trailing)
        }
        Divider()
    }
}
