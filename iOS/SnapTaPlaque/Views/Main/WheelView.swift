//
//  WheelView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

struct WheelView: View {
    // Les lettres et chiffres autorisés selon votre code Java
    let letters = ["A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "W", "X", "Y", "Z"]
    let numbers = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
    
    // États pour stocker la sélection de chaque colonne
    @State private var l1 = "A"
    @State private var l2 = "A"
    @State private var n1 = "0"
    @State private var n2 = "0"
    @State private var n3 = "0"
    @State private var l3 = "A"
    @State private var l4 = "A"
    
    // États pour gérer les alertes et requêtes
    @State private var isLoading = false
    @State private var showAlert = false
    @State private var alertMessage = ""
        
    // NOUVELLES VARIABLES
    private let vehicleService = VehicleService()
    @State private var vehicleResult: Vehicle? = nil
    
    var body: some View {
        
        VStack(spacing: 30) {
            
            Text("Saisissez la plaque")
                .font(.title2)
                .fontWeight(.bold)
            // On centre le texte et on ajoute un padding pour le positionner plus bas, laissant de l'espace pour la roulette
                .padding(.top, 200)
            
            // Le conteneur des 7 roulettes
            HStack(spacing: 0) {
                // Bloc Lettres 1
                WheelPicker(selection: $l1, data: letters)
                WheelPicker(selection: $l2, data: letters)
                
                Text("-")
                    .font(.system(size: 30, weight: .bold))
                    .padding(.horizontal, 5)
                
                // Bloc Chiffres
                WheelPicker(selection: $n1, data: numbers)
                WheelPicker(selection: $n2, data: numbers)
                WheelPicker(selection: $n3, data: numbers)
                
                Text("-")
                    .font(.system(size: 30, weight: .bold))
                    .padding(.horizontal, 5)
                
                // Bloc Lettres 2
                WheelPicker(selection: $l3, data: letters)
                WheelPicker(selection: $l4, data: letters)
            }
            .frame(height: 180) // Hauteur équivalente à vos 150dp
            .padding(.horizontal, 10)
            .background(Color(.systemGray6)) // Fond gris clair
            .cornerRadius(15)
            .padding(.horizontal, 20)
            
            // Bouton de Recherche
            Button(action: {
                searchVehicle()
            }) {
                HStack {
                    if isLoading {
                        ProgressView().padding(.trailing, 5)
                    }
                    Text("RECHERCHER")
                        .fontWeight(.bold)
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(15)
            }
            .padding(.horizontal, 40)
            .disabled(isLoading)
            
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .contentShape(Rectangle())
        .alert(isPresented: $showAlert) {
            // Remplace vos Toast.makeText(...)
            Alert(title: Text("Attention"), message: Text(alertMessage), dismissButton: .default(Text("OK")))
        }
        .sheet(item: $vehicleResult) { vehicle in
                VehicleDetailView(vehicle: vehicle)
            }
    }
    
    
    // MARK: - Fonctions Logiques
    private func getPlateString() -> String {
        return "\(l1)\(l2)-\(n1)\(n2)\(n3)-\(l3)\(l4)"
    }
    
    private func plateComplianceVerification(plate: String) -> Bool {
        // La même Regex que dans WheelFragment.java
        let regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS)[A-HJ-NP-TV-Z]{2})"
        
        // Sous iOS, on utilise NSPredicate pour évaluer une Regex
        let predicate = NSPredicate(format: "SELF MATCHES %@", regex_1)
        
        if !predicate.evaluate(with: plate) {
            // Note : regex_2 (sans tirets) n'est pas utile ici puisque notre getPlateString() force les tirets.
            alertMessage = "Format de plaque invalide ou non conforme (ex: lettres SS, WW interdites)."
            return false
        }
        return true
    }
    
    private func searchVehicle() {
            let plate = getPlateString()
            
            // 1. Validation de la plaque
            if !plateComplianceVerification(plate: plate) {
                showAlert = true
                return
            }
            
            // 2. Lancement de la requête
            isLoading = true
            
            Task {
                do {
                    // Appel API
                    let response = try await vehicleService.getVehicleInfo(plate: plate)
                    
                    // Transformation en modèle Vehicle Swift
                    vehicleResult = response.toVehicle(isFavorite: false)                    
                    
                    
                } catch {
                    alertMessage = "Plaque introuvable ou erreur de connexion."
                    showAlert = true
                }
                isLoading = false
            }
        }
}

// MARK: - Composant réutilisable pour une seule colonne de la roulette
struct WheelPicker: View {
    @Binding var selection: String
    let data: [String]
    
    var body: some View {
        Picker("", selection: $selection) {
            ForEach(data, id: \.self) { item in
                Text(item)
                    .font(.title2)
                    .fontWeight(.bold)
                    .tag(item)
            }
        }
        .pickerStyle(.wheel)
        // On limite la largeur de chaque colonne pour qu'elles rentrent toutes à l'écran
        .frame(width: 35)
        .clipped()
    }
}
