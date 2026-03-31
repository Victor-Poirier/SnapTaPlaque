//
//  Vehicle.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 20/03/2026.
//
import Foundation

/// Le modèle de données métier principal représentant un véhicule.
///
/// `Vehicle` encapsule les données signalétiques extraites d'une plaque d'immatriculation ou récupérées 
/// depuis l'API et est hautement exploitable dans les vues listes (grâce à `Identifiable`).
/// Conforme à `Codable`, il peut facilement être échangé sur le réseau ou mis en cache localement.
struct Vehicle: Codable, Identifiable{
    
    /// Un identifiant unique exigé par le protocole `Identifiable` pour le moteur de rendu `SwiftUI`.
    ///
    /// - Note: L'identifiant s'appuie directement sur l'`immatriculation` du véhicule, qui garantit l'unicité du rendu pour les boucles `ForEach`.
    var id: String { immatriculation }
    
    /// Le numéro officiel de la plaque d'immatriculation de la voiture.
    let immatriculation: String
    
    /// La marque constructeur (ex: "Renault", "Tesla"), s'il est parvenu à être identifié.
    let brand: String?
    
    /// Le type de modèle ou la gamme du constructeur, optionnel en fonction du résultat retourné.
    let model: String?
    
    /// Toutes données complémentaires non répertoriées ailleurs décrivant le véhicule.
    let info: String?
    
    /// Le type d'énergie (carburant classique, hybride ou moteur électrique) propulsant de ce véhicule.
    let energy: String?
    
    /// Un indicateur (mutable) définissant l'appartenance de ce véhicule aux Favoris de l'utilisateur.
    /// Il sert à déclencher ou retirer une étoile de sélection visuelle.
    var isFavorite: Bool
}
