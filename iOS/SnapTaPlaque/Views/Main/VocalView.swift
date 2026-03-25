//
//  VocalView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

struct VocalView: View {
    @StateObject private var speechManager = SpeechManager()
    
    @State private var formattedPlate = ""
    @State private var isLoading = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    
    // Pour l'appel API post-prédiction
    private let vehicleService = VehicleService()
    @State private var vehicleResult: Vehicle? = nil
    
    var body: some View {
        VStack(spacing: 30) {
                
            Text("Recherche Vocale")
                .font(.title2)
                .fontWeight(.bold)
                .padding(.bottom, 50)
            
            // Le texte brut compris par Siri
            Text(speechManager.recognizedText.isEmpty ? "Appuyez pour dicter la plaque" : speechManager.recognizedText)
                .font(.headline)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            
            // Le texte formaté en Plaque d'immatriculation (affiché dès qu'on arrête d'enregistrer)
            if !formattedPlate.isEmpty && !speechManager.isRecording {
                Text(formattedPlate)
                    .font(.system(size: 34, weight: .black, design: .monospaced))
                    .padding()
                    .background(Color.white)
                    .foregroundColor(.black)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.black, lineWidth: 3))
                    .transition(.scale)
            }
            
            // Le bouton Micro
            Button(action: {
                if speechManager.isRecording {
                    speechManager.stopRecording()
                    // Quand on s'arrête, on nettoie et on formate le texte
                    formattedPlate = formatPlate(speechManager.recognizedText)
                } else {
                    formattedPlate = ""
                    speechManager.startRecording()
                }
            }) {
                ZStack {
                    Circle()
                        .fill(speechManager.isRecording ? Color.red : Color.blue)
                        .frame(width: 100, height: 100)
                        .shadow(radius: speechManager.isRecording ? 10 : 0)
                    
                    Image(systemName: speechManager.isRecording ? "stop.fill" : "mic.fill")
                        .font(.system(size: 40))
                        .foregroundColor(.white)
                }
            }
            // Animation de "pulsation" pendant l'enregistrement
            .scaleEffect(speechManager.isRecording ? 1.1 : 1.0)
            .animation(.easeInOut(duration: 0.5).repeatCount(speechManager.isRecording ? .max : 0, autoreverses: true), value: speechManager.isRecording)
            .padding(.bottom, 50)
            
            
            // Le bouton de Recherche
            Button(action: {
                searchVehicle()
            }) {
                HStack {
                    if isLoading { ProgressView().padding(.trailing, 5) }
                    Text("RECHERCHER")
                        .fontWeight(.bold)
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(formattedPlate.isEmpty ? Color.gray : Color.blue)
                .foregroundColor(.white)
                .cornerRadius(15)
                
            }
            .padding(.horizontal, 40)
            .disabled(formattedPlate.isEmpty || isLoading)
                
        }
        // 👇 TRES IMPORTANT : Maintient le Swipe Vertical 👇
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .contentShape(Rectangle())
        // Demande la permission dès que la vue s'affiche
        .onAppear {
            speechManager.requestPermission()
        }
        // Gestion des erreurs vocales
        .onChange(of: speechManager.errorMessage) { oldValue, newValue in
            if let error = newValue {
                alertMessage = error
                showAlert = true
            }
        }
        // Modales d'erreur et de résultat
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Attention"), message: Text(alertMessage), dismissButton: .default(Text("OK")))
        }
        .sheet(item: $vehicleResult) { vehicle in
            VehicleDetailView(vehicle: vehicle)
        }
        
    }
    
    // MARK: - Logique Métier
    
    /// Transforme "A B 123 C D" en "AB-123-CD"
    private func formatPlate(_ rawText: String) -> String {
        // Enlève les espaces, les tirets dictés vocalement, et met en majuscules
        let cleaned = rawText.replacingOccurrences(of: " ", with: "")
                             .replacingOccurrences(of: "-", with: "")
                             .uppercased()
        
        // Si la longueur est de 7 caractères (Plaque standard SIV)
        if cleaned.count == 7 {
            let p1 = cleaned.prefix(2)
            let p2 = cleaned.dropFirst(2).prefix(3)
            let p3 = cleaned.suffix(2)
            return "\(p1)-\(p2)-\(p3)"
        }
        
        // Sinon on retourne brut, la regex de l'API s'en occupera
        return cleaned
    }
    
    private func searchVehicle() {
        isLoading = true
        Task {
            do {
                let response = try await vehicleService.getVehicleInfo(plate: formattedPlate)
                vehicleResult = response.toVehicle(isFavorite: false)
            } catch {
                alertMessage = "Véhicule introuvable ou erreur de format."
                showAlert = true
            }
            isLoading = false
        }
    }
}

struct VocalView_Previews: PreviewProvider {
    static var previews: some View {
        VocalView()
    }
}

