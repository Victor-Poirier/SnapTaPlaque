//
//  PictureView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

struct PictureView: View {
    // Gestion du menu et du sélecteur
    @State private var showActionSheet = false
    @State private var showImagePicker = false
    @State private var imageSourceType: UIImagePickerController.SourceType = .camera
    
    @State private var capturedImage: UIImage? = nil
    
    @State private var isPredicting = false
    @State private var predictedPlate: String = ""
    
    private let vehicleService = VehicleService()
    private let predictionService = PredictionService()
    
    @State private var vehicleResult: Vehicle? = nil
    @State private var errorMessage: String?
    @State private var showAlert = false
    
    var body: some View {
        VStack(spacing: 30) {
            
            if let image = capturedImage {
                
                
                // ... (GARDEZ TOUT LE BLOC D'AFFICHAGE DE L'IMAGE ET DES RÉSULTATS ICI) ...
                
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

struct PictureView_Previews: PreviewProvider {
    static var previews: some View {
        PictureView()
    }
}

