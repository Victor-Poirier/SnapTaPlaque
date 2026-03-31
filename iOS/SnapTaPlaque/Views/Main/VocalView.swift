//
//  VocalView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

/// La vue dédiée à l'acquisition d'une plaque d'immatriculation par commande vocale.
///
/// `VocalView` fait appel à `SpeechManager` pour utiliser la reconnaissance vocale d'Apple.
/// Elle inclut un bouton interactif (micro) dont le comportement dicte l'enregistrement, puis
/// reformate automatiquement ("A B 123 C D" en "AB-123-CD") la chaîne avant de l'envoyer
/// au `VehicleService` pour trouver le véhicule ciblé.
struct VocalView: View {
    
    /// Le gestionnaire d'état asynchrone rattaché au framework `Speech` pour capter le microphone et gérer les autorisations.
    @StateObject private var speechManager = SpeechManager()
    
    /// Le résultat vocal "nettoyé" et formaté selon le modèle SIV (Système d'Immatriculation des Véhicules).
    @State private var formattedPlate = ""
    
    /// Indicateur de chargement asynchrone lors de la requête finale à destination de l'API.
    @State private var isLoading = false
    
    /// Déclencheur conditionnel d'une carte d'alerte `Alert` en cas d'erreur de reconnaissance ou d'API.
    @State private var showAlert = false
    
    /// Le message de l'alerte destiné à l'utilisateur.
    @State private var alertMessage = ""
    
    // Pour l'appel API post-prédiction
    private let vehicleService = VehicleService()
    
    /// Object `Vehicle` récupéré si la recherche sur l'API distante avec le numéro dicté a réussi.
    /// Il déclenchera l'apparition de la modale `.sheet` avec les détails de la plaque.
    @State private var vehicleResult: Vehicle? = nil
    
    /// L'arborescence UI permettant la dictée et son interprétation.
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
    
    /// Reformate brutalement le texte identifié par la dictée vocale en une plaque SIV standard ("AB-123-CD").
    ///
    /// - Parameter rawText: Une phrase extraite d'une diction, comme `"A B 123 C D"`.
    /// - Returns: Une chaîne formatée pour convenir aux spécifications d'entrée de notre base de données.
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
    
    /// Interroge le serveur réseau pour télécharger à la volée le véhicule si les données vocales forment une plaque légitime.
    ///
    /// Cette méthode asynchrone met à jour la variable d'affichage `isLoading`, déclenche le chargement puis
    /// manipule `vehicleResult` pour invoquer et passer le relais à `VehicleDetailView`.
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

/// Aperçu en direct (Canvas) utilisé dans Xcode pour la page de recherche vocale.
struct VocalView_Previews: PreviewProvider {
    static var previews: some View {
        VocalView()
    }
}
