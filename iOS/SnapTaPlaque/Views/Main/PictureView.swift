//
//  PictureView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

/// La vue dédiée à l'analyse d'image (OCR) et à la recherche automatisée de véhicule par immatriculation.
///
/// `PictureView` orchestre l'ensemble du flux photographique de l'application :
/// - Sélection d'une image (depuis la caméra ou la photothèque locale).
/// - Extration asynchrone du texte via `PredictionService` (basé sur le framework Vision d'Apple).
/// - Affichage du résultat, et appel ultérieur éventuel au `VehicleService` pour
///   récupérer les détails complets de la plaque si celle-ci a été identifiée sur le réseau.
struct PictureView: View {
    
    /// Déclenche l'apparition de la modale réclamant à l'utilisateur s'il souhaite prendre une nouvelle photo
    /// ou utiliser une image existante depuis sa photothèque.
    @State private var showActionSheet = false
    
    /// Lance l'affichage du widget système natif `UIImagePickerController`.
    @State private var showImagePicker = false
    
    /// Retient la source du média désirée par l'utilisateur (caméra / photothèque).
    @State private var imageSourceType: UIImagePickerController.SourceType = .camera
    
    /// La photo récupérée avec succès une fois le processus de l'ImagePicker finalisé.
    @State private var capturedImage: UIImage? = nil
    
    /// Détermine si le service d'analyse (`Vision API`) est actuellement en train de traiter l'image.
    @State private var isPredicting = false
    
    /// Résultat stringifié de l'analyse (numéro de plaque "nettoyé").
    @State private var predictedPlate: String = ""
    
    private let vehicleService = VehicleService()
    private let predictionService = PredictionService()
    
    /// Object `Vehicle` récupéré si la recherche sur l'API distante avec le numéro de plaque a réussi.
    /// Il déclenchera automatiquement l'affichage modal `.sheet` de `VehicleDetailView`.
    @State private var vehicleResult: Vehicle? = nil
    
    /// Le corps du message d'erreur éventuel lors de l'étude (image corrompue, plaque introuvable).
    @State private var errorMessage: String?
    
    /// Déclencheur conditionnel d'une carte d'alerte `Alert` à l'écran concernant l'attribut `errorMessage`.
    @State private var showAlert = false
    
    /// Le rendu déclaratif SwiftUI représentant le scanner dynamique de la vue.
    var body: some View {
        VStack(spacing: 30) {
            
            if let image = capturedImage {
                
                
                Image(uiImage: image)
                    .resizable()
                    .scaledToFit()
                    .frame(height: 300)
                    .cornerRadius(15)
                    .shadow(radius: 5)
                
                if isPredicting {
                    ProgressView("Analyse de la plaque en cours...")
                        .padding()
                } else if !predictedPlate.isEmpty {
                    VStack {
                        Text("Plaque détectée :")
                            .font(.headline)
                        Text(predictedPlate)
                            .font(.system(size: 34, weight: .black, design: .monospaced))
                            .padding()
                            .background(Color.white)
                            .foregroundColor(.black)
                            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.black, lineWidth: 3))
                        
                        Button(action: { searchDetectedPlate() }) {
                            Text("Rechercher ce véhicule")
                                .fontWeight(.bold)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(15)
                        }
                        .padding(.horizontal, 40)
                        .padding(.top, 10)
                    }
                }
                
                // On réouvre le menu de choix au lieu de forcer la caméra
                Button("Analyser une autre photo") {
                    capturedImage = nil
                    predictedPlate = ""
                    showActionSheet = true
                }
                .foregroundColor(.red)
                
            } else {
                // État initial
                VStack(spacing: 20) {
                    Image(systemName: "viewfinder.circle.fill")
                        .font(.system(size: 80))
                        .foregroundColor(.blue)
                    
                    Text("Recherche par Image")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text("Prenez une photo ou choisissez-en une dans votre galerie pour détecter la plaque.")
                        .multilineTextAlignment(.center)
                        .foregroundColor(.gray)
                        .padding(.horizontal)
                    
                    // Ce bouton ouvre le menu de sélection
                    Button(action: { showActionSheet = true }) {
                        Text("Sélectionner une image")
                            .fontWeight(.bold)
                            .padding()
                            .frame(maxWidth: .infinity)
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(15)
                    }
                    .padding(.horizontal, 40)
                    .padding(.top, 20)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .contentShape(Rectangle())
        
        // 1. LE MENU DE SÉLECTION (Action Sheet)
        .confirmationDialog("Choisir une image", isPresented: $showActionSheet, titleVisibility: .visible) {
            Button("Prendre une photo") {
                imageSourceType = .camera
                showImagePicker = true
            }
            Button("Choisir depuis la galerie") {
                imageSourceType = .photoLibrary
                showImagePicker = true
            }
            Button("Annuler", role: .cancel) {}
        }
        
        // 2. LE SÉLECTEUR D'IMAGE (Modifié pour passer le sourceType)
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(image: $capturedImage, sourceType: imageSourceType)
        }
        
        // 3. LA PRÉDICTION
        .onChange(of: capturedImage) { oldValue, newValue in
                    if let img = newValue {
                        runPrediction(on: img)
                    }
                }
        
        // 4. LES MODALES D'ERREUR ET DE RÉSULTAT
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Erreur"), message: Text(errorMessage ?? ""), dismissButton: .default(Text("OK")))
        }
        .sheet(item: $vehicleResult) { vehicle in
            VehicleDetailView(vehicle: vehicle)
        }
    }
    
    // MARK: - Logique
    
    /// Délègue le travail d'extraction visuelle de l'image (OCR) à notre couche de service asynchrone.
    ///
    /// - Parameter image: L'objet complexe en mémoire (`UIImage`) issu directement du sélecteur `ImagePicker`.
    ///
    /// - Note: Bascule la variable d'état local `isPredicting` à `true` pendant la phase de traitement et met
    ///         à jour la vue exclusivement sur le `$MainActor` afin de respecter la boucle SwiftUI.
    private func runPrediction(on image: UIImage) {
        isPredicting = true
        predictedPlate = ""
        
        Task {
            do {
                let result = try await predictionService.predictLicensePlate(from: image)
                // On met à jour l'UI sur le thread principal
                await MainActor.run {
                    self.predictedPlate = result
                    self.isPredicting = false
                }
            } catch {
                await MainActor.run {
                    self.errorMessage = "Impossible de lire la plaque."
                    self.showAlert = true
                    self.isPredicting = false
                }
            }
        }
    }
    
    /// Recherche la plaque récemment extraite pour obtenir la "carte d'identification" complète du véhicule via le serveur.
    ///
    /// Cette méthode asynchrone prend le relais de l'OCR. Elle traduit la réponse distante JSON en modèle natif (`Vehicle`)
    /// de façon à déclencher immédiatement la vue de détail liée.
    private func searchDetectedPlate() {
        Task {
            do {
                let response = try await vehicleService.getVehicleInfo(plate: predictedPlate)
                vehicleResult = response.toVehicle(isFavorite: false)
            } catch {
                errorMessage = "Véhicule introuvable dans la base de données."
                showAlert = true
            }
        }
    }
}

/// Aperçu dans l'éditeur (Canvas) pour la vue `PictureView`.
struct PictureView_Previews: PreviewProvider {
    static var previews: some View {
        PictureView()
    }
}
