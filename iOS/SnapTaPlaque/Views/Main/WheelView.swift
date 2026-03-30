//
//  WheelView.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 21/03/2026.
//

import SwiftUI

/// La vue de recherche manuelle permettant de composer une plaque via une mécanique de "roulettes" (Pickers).
///
/// `WheelView` propose un sélecteur rotatif s'assurant que l'utilisateur saisit uniquement des
/// caractères admis par le système d'immatriculation SIV français. Chaque colonne met à jour son état propre
/// pour finalement concaténer les valeurs dans la méthode asynchrone `searchVehicle()`.
struct WheelView: View {
    /// Le dictionnaire des lettres autorisées pour contourner les lettres non reconnues ou interdites (SIV).
    let letters = ["A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "W", "X", "Y", "Z"]
    
    /// Le dictionnaire des unités numériques admissibles (en format chaîne de caractères).
    let numbers = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
    
    // États pour stocker la sélection de chaque colonne
    @State private var l1 = "A"
    @State private var l2 = "A"
    @State private var n1 = "0"
    @State private var n2 = "0"
    @State private var n3 = "0"
    @State private var l3 = "A"
    @State private var l4 = "A"
    
    /// Prévient l'interface que l'API est en cours d'interrogation.
    @State private var isLoading = false
    
    /// Déclencheur permettant l'affichage de la carte d'alerte `Alert` à l'écran.
    @State private var showAlert = false
    
    /// Le motif ou l'erreur affiché à l'intérieur de la carte d'alerte.
    @State private var alertMessage = ""
        
    private let vehicleService = VehicleService()
    
    /// Cible de données déclenchant, si trouvée, la modale (sheet) détaillée de `VehicleDetailView`.
    @State private var vehicleResult: Vehicle? = nil
    
    /// Le rendu architectural imbriqué pour la vue contenant ses blocs Picker et son bouton de démarrage de recherche.
    var body: some View {
        
        VStack(spacing: 30) {
            
            Text("Saisissez la plaque")
                .font(.title2)
                .fontWeight(.bold)
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
            .frame(height: 180)
            .padding(.horizontal, 10)
            .background(Color(.systemGray6))
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
            Alert(title: Text("Attention"), message: Text(alertMessage), dismissButton: .default(Text("OK")))
        }
        .sheet(item: $vehicleResult) { vehicle in
                VehicleDetailView(vehicle: vehicle)
            }
    }
    
    
    // MARK: - Fonctions Logiques
    
    /// Concatène les sept états `@State` représentant les valeurs isolées des roulettes.
    ///
    /// - Returns: Une chaîne formatée standard, prête à être soumise aux testeurs et à l'API (ex: *"AB-123-CD"*).
    private func getPlateString() -> String {
        return "\(l1)\(l2)-\(n1)\(n2)\(n3)-\(l3)\(l4)"
    }
    
    /// Vérifie que la syntaxe globale de la plaque obéit très précisément aux restrictions légales françaises (SIV).
    ///
    /// - Parameter plate: La chaîne de la plaque concaténée retournée par `getPlateString()`.
    /// - Returns: `true` si le motif regex correspond entièrement, `false` sinon. Change également le comportement de la modale en interne.
    private func plateComplianceVerification(plate: String) -> Bool {
        // La même Regex que dans WheelFragment.java
        let regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS)[A-HJ-NP-TV-Z]{2})"
        
        let predicate = NSPredicate(format: "SELF MATCHES %@", regex_1)
        
        if !predicate.evaluate(with: plate) {
            alertMessage = "Format de plaque invalide ou non conforme (ex: lettres SS, WW interdites)."
            return false
        }
        return true
    }
    
    /// Extrait la composition des roulettes (`getPlateString()`), procède à sa validation locale (Regex SIV)
    /// puis interroge l'API via le composant asynchrone `VehicleService`.
    ///
    /// Une fois la tentative aboutie, charge le type formatté Swift (`Vehicle`) entraînant la modale.
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

/// Un composant individualisé agissant comme isoloir (colonne) de sélection via `.wheel`.
///
/// L'encapsulation de `Picker` dans cet élément propre permet de manipuler avec précision le binding `$selection`
/// mais aussi la limite horizontale pour que 7 exemplaires puissent résider côte à côte sur tous les écrans d'iPhone.
struct WheelPicker: View {
    
    /// Une liaison de donnée modifiable attachant la colonne à l'un des sept `@State` du haut (ex: `$l1` ou `$n3`).
    @Binding var selection: String
    
    /// Le tableau constant alimentant les choix de la liste.
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
        .frame(width: 35)
        .clipped()
    }
}
