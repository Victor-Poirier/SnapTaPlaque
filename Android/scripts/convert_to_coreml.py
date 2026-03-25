#!/usr/bin/env python3
"""
Script pour convertir le modèle YOLOv12 (ONNX) au format CoreML (.mlpackage).
Ce script télécharge le modèle depuis Hugging Face et utilise onnx2torch + coremltools.

ATTENTION :
La conversion ONNX -> CoreML n'est pas triviale pour les modèles complexes comme YOLO.
Ce script fournit une tentative de conversion directe.
Il est probable que le modèle généré nécessite un post-traitement (NMS) personnalisé sur iOS.
Le modèle CoreML généré prend en entrée un tenseur (1, 3, 640, 640).

Prérequis :
    pip install -r scripts/requirements.txt

Usage :
    python3 scripts/convert_to_coreml.py
"""

import os
import sys
import torch
import onnx
import coremltools as ct
from huggingface_hub import hf_hub_download
from onnx2torch import convert
from onnxsim import simplify
from onnx import numpy_helper

# Configuration
REPO_ID = "0xnu/european-license-plate-recognition"
MODEL_FILENAME = "model.onnx"
OUTPUT_FILENAME = "PlateDetector.mlpackage"
INPUT_SHAPE = (1, 3, 640, 640)

def fix_onnx_split_nodes(model):
    """
    Répare les noeuds 'Split' qui utilisent une entrée pour 'split' (Opset >= 11)
    au lieu d'un attribut (Opset < 11), car onnx2torch préfère souvent la version attribut.
    """
    print("   [Fix] Recherche de noeuds 'Split' problématiques...")
    
    # Créer un map des initializers pour accès rapide
    initializers = {init.name: init for init in model.graph.initializer}
    nodes_to_remove = []
    
    for node in model.graph.node:
        if node.op_type == "Split":
            # Si le noeud a 2 entrées, la 2ème est le tensor 'split'
            if len(node.input) == 2:
                split_tensor_name = node.input[1]
                
                # On vérifie si c'est une constante connue (initializer)
                if split_tensor_name in initializers:
                    # Récup les valeurs du split
                    split_init = initializers[split_tensor_name]
                    split_values = numpy_helper.to_array(split_init).tolist()
                    
                    # Convertir en attribut 'split'
                    new_attr = onnx.helper.make_attribute("split", split_values)
                    node.attribute.extend([new_attr])
                    
                    # Supprimer la 2ème entrée
                    del node.input[1]
                    
                    print(f"      - Noeud Split '{node.name}' converti (Input -> Attribute: {split_values})")
                else:
                    print(f"      - Attention: Noeud Split '{node.name}' a une entrée dynamique non constante, impossible à fixer.")

    return model

def fix_onnx_reshape_nodes(model):
    """
    Répare les noeuds 'Reshape' qui utilisent l'attribut 'allowzero' (Opset >= 14).
    Si on retrograde vers Opset 12, cet attribut est invalide.
    """
    print("   [Fix] Recherche de noeuds 'Reshape' problématiques (allowzero)...")
    
    count = 0
    for node in model.graph.node:
        if node.op_type == "Reshape":
            # Chercher l'attribut allowzero
            allowzero_attr = next((a for a in node.attribute if a.name == "allowzero"), None)
            if allowzero_attr:
                node.attribute.remove(allowzero_attr)
                count += 1
                
    if count > 0:
        print(f"      - {count} noeuds Reshape corrigés (suppression de 'allowzero')")
    else:
        print("      - Aucun noeud Reshape avec 'allowzero' trouvé.")

    return model

def main():
    print(f"--- Démarrage de la conversion : {MODEL_FILENAME} ({REPO_ID}) ---")

    # 1. Télécharger le modèle
    print(f"1. Téléchargement du modèle ONNX...")
    try:
        model_path = hf_hub_download(repo_id=REPO_ID, filename=MODEL_FILENAME)
        print(f"   Modèle téléchargé à : {model_path}")
    except Exception as e:
        print(f"   Erreur de téléchargement : {e}")
        sys.exit(1)

    # 2. Convertir ONNX -> PyTorch
    print(f"2. Simplification et Conversion ONNX -> PyTorch...")
    
    # Étape 2a: Simplifier le modèle avec onnx-simplifier
    try:
        onnx_model = onnx.load(model_path)
        print("   Simplification du modèle ONNX (onnx-simplifier)...")
        
        # NOTE: On applique la simplification SUR LE MODÈLE ORIGINAL (Opset 19)
        # On ne force pas le downgrade avant, sinon le validateur ONNX crash sur les noeuds Resize (v11+)
        model_simp, check = simplify(onnx_model)
        if not check:
            print("   Attention: La validation du modèle simplifié a échoué, mais on tente quand même.")
        else:
            print("   Modèle simplifié avec succès.")
        
        # Étape 2b: Appliquer les correctifs manuels sur le modèle simplifié
        # Cela permet de rendre le graphe compatible avec onnx2torch (qui préfère les vieux formats Split/Reshape)
        print("   Application des correctifs manuels (Split, Reshape)...")
        try:
            model_simp = fix_onnx_split_nodes(model_simp)
            model_simp = fix_onnx_reshape_nodes(model_simp)
        except Exception as e:
             print(f"   Erreur lors du fix manuel : {e}")

        # Étape 2c: Downgrade final du numéro de version Opset
        # On ment à onnx2torch en disant que c'est du Opset 12, maintenant que les noeuds sont propres.
        if model_simp.opset_import[0].version > 12:
            print(f"   Forçage metadata Opset {model_simp.opset_import[0].version} -> 12 pour onnx2torch...")
            model_simp.opset_import[0].version = 12
        
        # Étape 2d: Conversion finale
        print("   Conversion via onnx2torch...")
        torch_model = convert(model_simp)
        torch_model.eval()
        print(f"   Modèle PyTorch créé avec succès.")

    except Exception as e:
        print(f"   Erreur critique lors de la conversion ONNX -> PyTorch : {e}")
        print("   Veuillez vérifier les logs ci-dessus.")
        sys.exit(1)

    # 3. Tracer le modèle (Tracing via JIT)
    print(f"3. Traçage du modèle PyTorch (JIT)...")
    try:
        # Création d'un input dummy avec la bonne dimension
        dummy_input = torch.rand(INPUT_SHAPE)
        # On force le traçage sur CPU pour éviter les soucis de device
        traced_model = torch.jit.trace(torch_model, dummy_input)
        print(f"   Modèle tracé.")
    except Exception as e:
        print(f"   Erreur lors du traçage JIT : {e}")
        sys.exit(1)

    # 4. Convertir PyTorch -> CoreML
    print(f"4. Conversion finale vers CoreML ({OUTPUT_FILENAME})...")
    try:
        # On utilise TensorType par défaut. Si l'input doit être une Image, on pourrait utiliser ImageType.
        # Pour YOLO, souvent l'input est normalisé [0,1]. ImageType gère la normalisation si configuré (scale=1/255).
        # Ici on reste générique pour éviter d'ajouter une double normalisation si le modèle ONNX l'a déjà.
        # On remplace TensorType par ImageType, et on ajoute le scale pour YOLO
        image_input = ct.ImageType(
            name="images",
            shape=INPUT_SHAPE,
            scale=1.0,  # 👈 AUCUNE DIVISION ! On laisse les pixels de 0 à 255.
            color_layout=ct.colorlayout.RGB  # 👈 On force le bon ordre des couleurs
        )

        mlmodel = ct.convert(
            traced_model,
            inputs=[image_input],
            minimum_deployment_target=ct.target.iOS16,
        )
        
        # Sauvegarde
        output_path = os.path.join(os.path.dirname(__file__), "..", OUTPUT_FILENAME)
        # Normalisation du chemin absolu
        output_path = os.path.abspath(output_path)

        mlmodel.save(output_path)
        print(f"--- Terminé ! ---")
        print(f"Le fichier .mlpackage a été généré ici : {output_path}")
        print("Note : Intégrez ce fichier dans votre projet Xcode (cible iOS).")
        
    except Exception as e:
        print(f"   Erreur lors de la conversion CoreML : {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()

