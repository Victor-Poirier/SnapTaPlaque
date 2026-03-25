//
//  PredictionService.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation
import Vision
import UIKit

class PredictionService {
    
    func predictLicensePlate(from image: UIImage) async throws -> String {
        guard let cgImage = image.cgImage else {
            throw URLError(.cannotDecodeRawData)
        }
        
        let orientation = CGImagePropertyOrientation(image.imageOrientation)
        let handler = VNImageRequestHandler(cgImage: cgImage, orientation: orientation, options: [:])
        
        return try await withCheckedThrowingContinuation { continuation in
            let textRequest = VNRecognizeTextRequest { request, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }
                
                guard let observations = request.results as? [VNRecognizedTextObservation] else {
                    continuation.resume(throwing: URLError(.cannotParseResponse))
                    return
                }
                
                // 1. On lit tout le texte visible sur la photo
                var fullText = ""
                for observation in observations {
                    if let topCandidate = observation.topCandidates(1).first {
                        fullText += topCandidate.string + " "
                    }
                }
                
                // 2. Nettoyage global (on enlève espaces et tirets)
                let cleanedText = fullText.uppercased()
                    .replacingOccurrences(of: " ", with: "")
                    .replacingOccurrences(of: "-", with: "")
                
                print("📝 OCR Brut Apple : \(cleanedText)")
                
                // 3. Le filtre magique : On cherche le format SIV (ex: AB123CD) n'importe où !
                if let regex = try? NSRegularExpression(pattern: "([A-Z]{2}[0-9]{3}[A-Z]{2})") {
                    let range = NSRange(location: 0, length: cleanedText.utf16.count)
                    
                    if let match = regex.firstMatch(in: cleanedText, options: [], range: range) {
                        if let swiftRange = Range(match.range(at: 1), in: cleanedText) {
                            let plate = String(cleanedText[swiftRange])
                            
                            // On reformate proprement avec les tirets pour votre API
                            let formattedPlate = "\(plate.prefix(2))-\(plate.dropFirst(2).prefix(3))-\(plate.suffix(2))"
                            
                            print("🟢 Plaque extraite avec succès : \(formattedPlate)")
                            continuation.resume(returning: formattedPlate)
                            return
                        }
                    }
                }
                
                print("🔴 Aucune plaque valide trouvée dans l'image.")
                continuation.resume(throwing: URLError(.cannotParseResponse))
            }
            
            // On demande à l'iPhone de prendre son temps pour être ultra-précis
            textRequest.recognitionLevel = .accurate
            textRequest.usesLanguageCorrection = false
            
            do {
                try handler.perform([textRequest])
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }
}

// Helper d'orientation toujours nécessaire
extension CGImagePropertyOrientation {
    init(_ orientation: UIImage.Orientation) {
        switch orientation {
        case .up: self = .up
        case .upMirrored: self = .upMirrored
        case .down: self = .down
        case .downMirrored: self = .downMirrored
        case .left: self = .left
        case .leftMirrored: self = .leftMirrored
        case .right: self = .right
        case .rightMirrored: self = .rightMirrored
        @unknown default: self = .up
        }
    }
}
