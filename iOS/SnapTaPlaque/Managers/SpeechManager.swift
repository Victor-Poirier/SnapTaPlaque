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

class SpeechManager: NSObject, ObservableObject {
    
    @Published var isRecording = false
    @Published var recognizedText = ""
    @Published var errorMessage: String?
    
    private var audioEngine = AVAudioEngine()
    // On force la reconnaissance en français
    private var speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "fr-FR"))
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    
    // Demande les autorisations à iOS
    func requestPermission() {
        SFSpeechRecognizer.requestAuthorization { status in
            DispatchQueue.main.async {
                if status != .authorized {
                    self.errorMessage = "L'accès à la reconnaissance vocale a été refusé. Veuillez l'activer dans les réglages."
                }
            }
        }
    }
    
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
