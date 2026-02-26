"""
model — Sous-package du pipeline de reconnaissance de plaques d'immatriculation.

Ce sous-package contient les modules implémentant le pipeline LPR
(License Plate Recognition) utilisé par l'API SnapTaPlaque pour la
détection et la lecture des plaques d'immatriculation à partir
d'images soumises par les utilisateurs.

Modules :
    - ``lpr_engine.py``    — Pipeline principal combinant la détection
      de plaques via YOLO et la lecture des caractères via EasyOCR.

Version : 1.0.0
"""