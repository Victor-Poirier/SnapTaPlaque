//
//  ProfileView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

struct ProfileView: View {
    // Services
    @StateObject private var locationManager = LocationManager()
    private let accountService = AccountService()
    private let favoritesService = FavoritesService()
    
    // Variables d'état
    @State private var username: String = "Chargement..."
    @State private var email: String = "..."
    @State private var profileImage: UIImage? = nil
    
    // Favoris
    @State private var favorites: [Vehicle] = []
    @State private var isLoadingFavorites = true
    @State private var selectedVehicle: Vehicle? = nil
    
    // Gestion de la photo
    @State private var showPhotoMenu = false
    @State private var showImagePicker = false
    @State private var imageSourceType: UIImagePickerController.SourceType = .photoLibrary
    
    var body: some View {
        NavigationView {
            // NOUVEAU : On utilise une List au lieu d'une ScrollView pour le style "Grouped"
            List {
                
                // 1. L'en-tête (Photo et infos utilisateur)
                Section {
                    profileHeader
                }
                
                // 2. La liste des favoris
                Section(header: Text("Mes Favoris")) {
                    favoritesList
                }
                
            }
            .listStyle(.insetGrouped) // C'est CA qui donne le même style que l'Historique !
            .navigationTitle("Profil")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: logout) {
                        Image(systemName: "rectangle.portrait.and.arrow.right")
                            .foregroundColor(.red)
                    }
                }
            }
        }
        .confirmationDialog("Photo de profil", isPresented: $showPhotoMenu, titleVisibility: .visible) {
            Button("Prendre une photo") {
                imageSourceType = .camera
                showImagePicker = true
            }
            Button("Choisir dans la galerie") {
                imageSourceType = .photoLibrary
                showImagePicker = true
            }
            Button("Supprimer la photo", role: .destructive) { deletePicture() }
            Button("Annuler", role: .cancel) {}
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(image: Binding(
                get: { self.profileImage },
                set: { newImage in
                    if let img = newImage { uploadPicture(img) }
                }
            ), sourceType: imageSourceType)
        }
        .sheet(item: $selectedVehicle) { vehicle in
            VehicleDetailView(vehicle: vehicle)
        }
        .task {
            locationManager.requestLocation()
            await loadProfile()
            await loadFavorites()
        }
    }
    
    // MARK: - Sous-Vues
    
    private var profileHeader: some View {
        HStack(spacing: 20) {
            ZStack {
                if let image = profileImage {
                    Image(uiImage: image).resizable().scaledToFill()
                } else {
                    Image(systemName: "person.crop.circle.fill")
                        .resizable().scaledToFit().foregroundColor(.gray.opacity(0.3))
                }
            }
            .frame(width: 80, height: 80) // Légèrement réduit pour bien rentrer dans la cellule de la List
            .clipShape(Circle())
            .overlay(Circle().stroke(Color.blue, lineWidth: 3))
            .shadow(radius: 5)
            .onTapGesture { showPhotoMenu = true }
            
            VStack(alignment: .leading, spacing: 8) {
                Text(username).font(.title3).fontWeight(.bold)
                
                HStack {
                    Image(systemName: "envelope.fill").foregroundColor(.gray).frame(width: 20)
                    Text(email).font(.subheadline).foregroundColor(.secondary)
                }
                
                HStack {
                    Image(systemName: "mappin.and.ellipse").foregroundColor(.red).frame(width: 20)
                    Text(locationManager.locationString).font(.subheadline).foregroundColor(.secondary)
                }
            }
            Spacer()
        }
        .padding(.vertical, 8)
    }
    
    @ViewBuilder
    private var favoritesList: some View {
        if isLoadingFavorites {
            HStack {
                Spacer()
                ProgressView()
                Spacer()
            }
        } else if favorites.isEmpty {
            Text("Aucun véhicule dans vos favoris.")
                .foregroundColor(.gray)
        } else {
            ForEach(favorites) { vehicle in
                // NOUVEAU : On rend la ligne cliquable
                Button(action: {
                    self.selectedVehicle = vehicle
                }) {
                    FavoriteRow(vehicle: vehicle)
                }
                // Pour que le bouton n'ait pas le style "texte bleu" par défaut
                .buttonStyle(.plain)
            }
        }
    }
    
    // MARK: - Fonctions Métier
    
    private func loadProfile() async {
        do {
            let meInfo = try await accountService.getMe()
            self.username = meInfo.username
            self.email = meInfo.email
            self.profileImage = try await accountService.getProfilePicture()
        } catch {
            print("Erreur de chargement du profil: \(error)")
        }
    }
    
    private func loadFavorites() async {
        isLoadingFavorites = true
        do {
            self.favorites = try await favoritesService.getAllFavorites()
        } catch {
            print("Erreur de chargement des favoris: \(error)")
        }
        isLoadingFavorites = false
    }
    
    private func uploadPicture(_ image: UIImage) {
        Task {
            do {
                try await accountService.uploadProfilePicture(image: image)
                await MainActor.run { self.profileImage = image }
            } catch { print("Erreur d'upload: \(error)") }
        }
    }
    
    private func deletePicture() {
        Task {
            do {
                try await accountService.deleteProfilePicture()
                await MainActor.run { self.profileImage = nil }
            } catch { print("Erreur de suppression: \(error)") }
        }
    }
    
    private func logout() {
        SessionManager.shared.logout()
    }
}

// MARK: - Le design d'une ligne de Favori (Identique à HistoryRow)
struct FavoriteRow: View {
    let vehicle: Vehicle
    
    // Logique du logo adaptée au modèle Vehicle
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
                Text(vehicle.immatriculation)
                    .font(.headline)
                    .fontWeight(.black)
                    .fontDesign(.monospaced) // Style plaque
                
                // Note : si votre modèle Vehicle n'a pas de ".model" remplacez-le par ce que vous avez, ou retirez-le !
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
