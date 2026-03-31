//
//  ImagePicker.swift
//  SnapTaPlaque
//
//  Created by Victor Poirier on 23/03/2026.
//

import SwiftUI
import UIKit

/// Un pont (`UIViewControllerRepresentable`) qui intègre `UIImagePickerController` dans SwiftUI.
///
/// Ce composant permet à l'utilisateur de prendre une photo à l'aide de l'appareil photo (`.camera`)
/// ou d'en choisir une dans sa galerie (`.photoLibrary`). Conçu pour le flux de bout en bout de
/// SnapTaPlaque.
///
/// **Attention iOS 26 :** Bien que `UIImagePickerController` soit supporté pour l'appareil photo,
/// Apple recommande l'API `PhotosPicker` (via `PhotosUI`) pour l'accès aux galeries.
struct ImagePicker: UIViewControllerRepresentable {
    
    /// L'image renvoyée par le sélecteur une fois que l'utilisateur a fait son choix.
    /// Renvoie `nil` tant qu'aucune image n'a été sélectionnée.
    @Binding var image: UIImage?
    
    /// Précise la source pour choisir l'image (système d'appareil photo ou galerie).
    var sourceType: UIImagePickerController.SourceType
    
    /// L'environnement permettant de fermer cette vue modale (déprécié au profit de `dismiss`,
    /// mais courant pour maintenir la rétrocompatibilité).
    @Environment(\.presentationMode) var presentationMode
    
    /// Crée l'instance de `UIImagePickerController` et l'attache au coordinateur.
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        
        picker.sourceType = sourceType
        
        picker.delegate = context.coordinator
        return picker
    }
    
    /// Met à jour le `UIViewController` (inutile ici puisque l'état est géré en interne par UIKit).
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    /// Crée le `Coordinator` chargé des interactions et du retour d'état du délégué `UIImagePickerController`.
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    /// L'objet délégué (`Delegate`) qui répond aux événements de `UIImagePickerController`.
    class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: ImagePicker
        
        /// Initialise le coordinateur en conservant une référence au `parent` (`ImagePicker`).
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        /// Méthode appelée lorsque l'utilisateur a fini de sélectionner, prendre ou recadrer l'image.
        ///
        /// Extrait l'image originale `UIImage` depuis le dictionnaire d'informations `info`, l'assigne
        /// à la propriété `@Binding` et ferme le contrôleur.
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let uiImage = info[.originalImage] as? UIImage {
                parent.image = uiImage
            }
            parent.presentationMode.wrappedValue.dismiss()
        }
    }
}
