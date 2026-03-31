//
//  LocationManager.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 25/03/2026.
//

import Foundation
import CoreLocation
import MapKit
import Combine

/// Un gestionnaire de localisation asynchrone optimisé pour iOS 26.
///
/// `LocationManager` gère les demandes d'autorisation avec `CoreLocation`,
/// récupère la position actuelle (one-shot) avec un système de timeout,
/// et convertit les coordonnées reçues en adresse lisible (Ville, Pays) via les API modernes `MapKit`.
/// Totalement compatible SwiftUI, il garantit la cohérence des mises à jour sur le `@MainActor`.
@MainActor
class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var timeoutTask: Task<Void, Never>?
    private var isRequestInProgress = false

    /// L'adresse locale au format texte ou un message décrivant le statut actuel
    /// (ex: "Recherche en cours...", erreurs d'autorisation, etc.).
    @Published var locationString: String = "Recherche en cours..."

    /// Initialise le gestionnaire en définissant le délégué et la précision à une centaine de mètres
    /// afin d'économiser la batterie pour une simple récupération de ville.
    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        manager.distanceFilter = kCLDistanceFilterNone
    }

    deinit {
        timeoutTask?.cancel()
    }

    /// Point d'entrée principal pour demander ou rafraîchir la position.
    ///
    /// Interroge l'état de l'autorisation système:
    /// - S'il n'est pas déterminé, affiche un message adéquat et demande l'autorisation.
    /// - S'il est autorisé, lance la requête automatique via `startSingleLocationRequest()`.
    /// - Sinon, affiche le motif du refus dans `locationString`.
    func requestLocation() {
        let status = manager.authorizationStatus

        switch status {
        case .notDetermined:
            locationString = "Autorisation de localisation requise"
            manager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways:
            startSingleLocationRequest()
        case .denied:
            locationString = "Localisation refusée (réglages iOS)"
        case .restricted:
            locationString = "Localisation restreinte"
        @unknown default:
            locationString = "Statut de localisation inconnu"
        }
    }

    /// Démarre de manière sécurisée une requête de positionnement unique (`requestLocation`).
    ///
    /// Lance un minuteur d'annulation automatique (timeout) de 12 secondes en arrière-plan
    /// pour éviter le blocage de l'interface en cas de non-réponse du système GPS ou de mauvaise couverture.
    private func startSingleLocationRequest() {
        guard !isRequestInProgress else { return }
        isRequestInProgress = true
        locationString = "Recherche en cours..."


        manager.requestLocation()

        timeoutTask?.cancel()
        timeoutTask = Task {
            try? await Task.sleep(nanoseconds: 12_000_000_000)
            guard !Task.isCancelled else { return }
            if isRequestInProgress {
                manager.stopUpdatingLocation()
                isRequestInProgress = false
                locationString = "Position introuvable"
            }
        }
    }

    /// Callback `CLLocationManagerDelegate` invoqué automatiquement lors du changement d'autorisation.
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            switch manager.authorizationStatus {
            case .authorizedWhenInUse, .authorizedAlways:
                startSingleLocationRequest()
            case .denied:
                isRequestInProgress = false
                timeoutTask?.cancel()
                locationString = "Localisation refusée (réglages iOS)"
            case .restricted:
                isRequestInProgress = false
                timeoutTask?.cancel()
                locationString = "Localisation restreinte"
            case .notDetermined:
                break
            @unknown default:
                isRequestInProgress = false
                timeoutTask?.cancel()
                locationString = "Statut de localisation inconnu"
            }
        }
    }

    /// Callback `CLLocationManagerDelegate` invoqué lorsque de nouvelles coordonnées sont disponibles.
    ///
    /// Filtre les positions obsolètes (les anciennes valeurs mises en cache de plus de 15s) avant de
    /// lancer le géocodage inversé.
    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        Task { @MainActor in
            // Ignore les positions trop anciennes
            let age = -location.timestamp.timeIntervalSinceNow
            guard age < 15 else { return }

            timeoutTask?.cancel()
            isRequestInProgress = false
            await reverseGeocode(location: location)
        }
    }

    /// Réalise un géocodage inversé de la localisation géographique via `MKReverseGeocodingRequest`.
    ///
    /// Utilise les dernières propriétés d'iOS 26 depuis `MKMapItem.addressRepresentations`
    /// pour extraire le nom de la ville (`cityName`) et du pays/région (`regionName`).
    ///
    /// - Parameter location: La position géographique exacte reçue par CoreLocation.
    private func reverseGeocode(location: CLLocation) async {
        guard let request = MKReverseGeocodingRequest(location: location) else {
            locationString = "Position introuvable"
            return
        }

        do {
            let response = try await request.mapItems
            if let item = response.first {
                let city = item.addressRepresentations?.cityName ?? "Ville inconnue"
                let country = item.addressRepresentations?.regionName ?? ""
                locationString = country.isEmpty ? city : "\(city), \(country)"
            } else {
                locationString = "Position introuvable"
            }
        } catch {
            locationString = "Position introuvable"
        }
    }

    /// Callback `CLLocationManagerDelegate` interceptant les erreurs lors de la mise à jour GPS.
    ///
    /// Gère de lui-même les erreurs transitoires comme `.locationUnknown` en amorçant un fallback.
    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            if let clError = error as? CLError, clError.code == .locationUnknown, isRequestInProgress {
                manager.startUpdatingLocation()
                return
            }

            timeoutTask?.cancel()
            manager.stopUpdatingLocation()
            isRequestInProgress = false
            locationString = "Localisation indisponible"
        }
    }
}
