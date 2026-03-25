//
//  LocationManager.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 25/03/2026.
//

import Foundation
import CoreLocation
import MapKit // Ajouté pour calmer le bug d'Xcode
import Combine

@MainActor // Garantit que l'interface utilisateur se met à jour sur le bon thread
class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    
    @Published var locationString: String = "Recherche en cours..."
    
    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }
    
    func requestLocation() {
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()
    }
    
    // L'ajout de "nonisolated" est nécessaire car la classe est @MainActor
    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        manager.stopUpdatingLocation()
        
        // On lance la tâche asynchrone moderne
        Task {
            await reverseGeocode(location: location)
        }
    }
    
    private func reverseGeocode(location: CLLocation) async {
        let geocoder = CLGeocoder()
        do {
            // Utilisation de la méthode moderne async (sans completionHandler)
            let placemarks = try await geocoder.reverseGeocodeLocation(location)
            if let placemark = placemarks.first {
                let city = placemark.locality ?? "Ville inconnue"
                let country = placemark.country ?? ""
                
                self.locationString = "\(city), \(country)"
            }
        } catch {
            self.locationString = "Position introuvable"
        }
    }
    
    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            self.locationString = "Localisation désactivée"
        }
    }
}
