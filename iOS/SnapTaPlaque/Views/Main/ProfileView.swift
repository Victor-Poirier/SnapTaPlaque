//
//  ProfileView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

/// La vue principale affichant le profil de l'utilisateur avec ses informations personnelles et ses paramètres.
///
/// `ProfileView` permet de visualiser et gérer les données du compte connecté (nom, email, photo, localisation),
/// de consulter la liste des véhicules marqués en favoris et d'accéder aux réglages de compte.
/// Elle interagit de manière asynchrone avec l'`AccountService`, le `FavoritesService`, et le `LocationManager`.
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
    
    // Modales Paramètres & Alertes
    @State private var showSettings = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    
    /// Le contenu visuel (body) de la vue profil SwiftUI.
    var body: some View {
        NavigationView {
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
            .listStyle(.insetGrouped)
            .navigationTitle("Profil")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack(spacing: 15) {
                        // Le bouton "Engrenage"
                        Button(action: { showSettings = true }) {
                            Image(systemName: "gearshape.fill")
                                .foregroundColor(.gray)
                        }
                        
                        // Le bouton déconnexion
                        Button(action: logout) {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                                .foregroundColor(.red)
                        }
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
        .sheet(isPresented: $showSettings) {
            AccountSettingsView(
                userEmail: self.email,
                onExport: { exportUserData() },
                onDelete: { deleteUserAccount() }
            )
        }
        // 2. L'ALERTE DE RÉSULTAT (Pour dire "Export réussi" ou gérer les erreurs)
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Information"), message: Text(alertMessage), dismissButton: .default(Text("OK")))
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
            .frame(width: 80, height: 80)
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
                Button(action: {
                    self.selectedVehicle = vehicle
                }) {
                    FavoriteRow(vehicle: vehicle)
                }
                .buttonStyle(.plain)
            }
        }
    }
    
    // MARK: - Fonctions Métier
    
    /// Charge les informations du profil utilisateur depuis l'API et rafraîchit l'interface.
    ///
    /// Cette méthode asynchrone utilise l'`AccountService` pour récupérer les données `getMe()`
    /// et la photo de profil (si existante).
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
    
    /// Télécharge la liste globale et à jour des favoris de l'utilisateur actif.
    private func loadFavorites() async {
        isLoadingFavorites = true
        do {
            self.favorites = try await favoritesService.getAllFavorites()
        } catch {
            print("Erreur de chargement des favoris: \(error)")
        }
        isLoadingFavorites = false
    }
    
    /// Compresse, met en forme et sauvegarde l'image de profil ciblée au niveau du serveur.
    ///
    /// - Parameter image: L'objet complexe de type `UIImage` fourni depuis la sélection de la galerie système.
    private func uploadPicture(_ image: UIImage) {
        Task {
            do {
                try await accountService.uploadProfilePicture(image: image)
                await MainActor.run { self.profileImage = image }
            } catch { print("Erreur d'upload: \(error)") }
        }
    }
    
    /// Sollicite le serveur de base de données pour détruire la photo de profil de l'utilisateur actif.
    private func deletePicture() {
        Task {
            do {
                try await accountService.deleteProfilePicture()
                await MainActor.run { self.profileImage = nil }
            } catch { print("Erreur de suppression: \(error)") }
        }
    }
    
    /// Méthode d'appel local qui ferme brutalement la session et ramène l'utilisateur sur la vue des identifiants (Login).
    private func logout() {
        SessionManager.shared.logout()
    }
    
    // MARK: - Fonctions Paramètres
    
    /// Récupère l'intégralité de la base de données propre aux activités de cet utilisateur via `AccountService`
    /// puis propose un conteneur système de partage de fichiers iOS (`UIActivityViewController`).
    private func exportUserData() {
        Task {
            do {
                // 1. On télécharge le JSON depuis l'API
                let jsonData = try await accountService.exportData()
                
                // 2. On crée un fichier temporaire sur l'iPhone
                let tempDirectory = FileManager.default.temporaryDirectory
                let fileURL = tempDirectory.appendingPathComponent("SnapTaPlaque_Export_Donnees.json")
                
                // 3. On écrit les données dans ce fichier
                try jsonData.write(to: fileURL)
                
                // 4. On ouvre le menu de partage natif d'iOS sur le Thread Principal
                await MainActor.run {
                    let activityVC = UIActivityViewController(activityItems: [fileURL], applicationActivities: nil)
                    
                    if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                       let rootVC = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController {
                    
                        rootVC.present(activityVC, animated: true)
                    }
                }
            } catch {
                await MainActor.run {
                    self.alertMessage = "Erreur lors du téléchargement de vos données."
                    self.showAlert = true
                }
            }
        }
    }
    
    /// Engendre la suppression complète de l'utilisateur et de ses données associées avant de forcer une fermeture de session.
    private func deleteUserAccount() {
        Task {
            do {
                try await accountService.deleteAccount()
                // Si la suppression marche, on déconnecte de force l'utilisateur !
                await MainActor.run {
                    logout()
                }
            } catch {
                await MainActor.run {
                    self.alertMessage = "Impossible de supprimer le compte pour le moment."
                    self.showAlert = true
                }
            }
        }
    }
}

// MARK: - Le design d'une ligne de Favori

/// Un composant horizontal représentant une unique ligne au sein de la liste des favoris de l'utilisateur.
///
/// Fonctionnement pratiquement identique à `HistoryRow`,
/// celui-ci s'appuie néanmoins sur un objet `Vehicle` natif complet.
struct FavoriteRow: View {
    
    /// Le type générique formaté contenant l'info complète.
    let vehicle: Vehicle
    
    /// Construit l'URL externe à destination d'un repo GitHub distant pour rafraîchir un logo de marque automobile.
    ///
    /// - Note: Cette propriété génère un "slug" adapté (minuscules, tirets en remplacement d'espaces).
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
