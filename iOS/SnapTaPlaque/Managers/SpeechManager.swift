//
//  SpeechManager.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import Foundation
import AVFoundation
import Speech
import SwiftUI
import Combine

/// Un gestionnaire responsable de l'enregistrement audio et de la reconnaissance vocale en temps réel.
///
/// `SpeechManager` utilise `AVFoundation` pour capturer l'audio depuis le microphone et `Speech`
/// pour retranscrire la parole en texte (configuré nativement pour la langue française).
/// Conçu pour SwiftUI, il expose des propriétés `@Published` pour mettre à jour dynamiquement l'interface utilisateur.
class SpeechManager: NSObject, ObservableObject {
    
    /// Indique si l'enregistrement audio et la reconnaissance vocale sont actuellement en cours.
    @Published var isRecording = false
    
    /// Le texte transcrit en temps réel par le moteur de reconnaissance vocale.
    @Published var recognizedText = ""
    
    /// Un message descriptif en cas d'erreur (ex: permissions refusées, erreur technique du micro).
    @Published var errorMessage: String?
    
    private var audioEngine = AVAudioEngine()
    // On force la reconnaissance en français
    private var speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "fr-FR"))
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    
    /// Demande à l'utilisateur les autorisations requises par iOS pour utiliser la reconnaissance vocale.
    ///
    /// Si l'autorisation n'est pas accordée, la propriété `errorMessage` est mise à jour avec un message
    /// explicatif destiné à l'interface utilisateur.
    func requestPermission() {
        SFSpeechRecognizer.requestAuthorization { status in
            DispatchQueue.main.async {
                if status != .authorized {
                    self.errorMessage = "L'accès à la reconnaissance vocale a été refusé. Veuillez l'activer dans les réglages."
                }
            }
        }
    }
    
    /// Prépare la session audio, configure le microphone et démarre la transcription vocale en temps réel.
    ///
    /// Cette méthode suit plusieurs étapes critiques :
    /// 1. Configuration de l'`AVAudioSession` pour optimiser l'enregistrement vocal.
    /// 2. Initialisation d'une requête `SFSpeechAudioBufferRecognitionRequest`.
    /// 3. Branchement du flux audio du bus d'entrée de l'`AVAudioEngine` au moteur de reconnaissance.
    /// 4. Lancement de la tâche asynchrone qui met régulièrement à jour `recognizedText` au fil de la parole.
    func startRecording() {
        // 1. Configuration de la session audio
        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
            try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
            self.errorMessage = "Impossible de configurer le micro."
            return
        }
        
        // 2. Préparation de la requête
        request = SFSpeechAudioBufferRecognitionRequest()
        guard let request = request, let recognizer = speechRecognizer, recognizer.isAvailable else {
            self.errorMessage = "La reconnaissance vocale n'est pas disponible pour le moment."
            return
        }
        
        // 3. Connexion du micro au moteur de reconnaissance
        let inputNode = audioEngine.inputNode
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
            self.request?.append(buffer)
        }
        
        audioEngine.prepare()
        do {
            try audioEngine.start()
            DispatchQueue.main.async {
                self.isRecording = true
                self.recognizedText = "..."
            }
        } catch {
            self.errorMessage = "Erreur au démarrage de l'écoute."
            return
        }
        
        // 4. Lancement de la transcription en temps réel
        recognitionTask = recognizer.recognitionTask(with: request) { result, error in
            if let result = result {
                DispatchQueue.main.async {
                    self.recognizedText = result.bestTranscription.formattedString
                }
            }
            
            if error != nil || result?.isFinal == true {
                self.stopRecording()
            }
        }
    }
    
    /// Met fin à l'enregistrement audio et arrête la tâche de reconnaissance vocale en cours.
    ///
    /// Cette méthode nettoie les flux audio (retrait du « tap ») et désactive la session `AVAudioSession`
    /// pour rendre proprement le contrôle de l'audio aux autres applications du système (musique, etc.).
    func stopRecording() {
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        request?.endAudio()
        recognitionTask?.cancel()
        
        DispatchQueue.main.async {
            self.isRecording = false
            // On désactive la session audio pour rendre le contrôle aux autres applications (musique, etc.)
            try? AVAudioSession.sharedInstance().setActive(false)
        }
    }
}
