package com.example.snaptaplaque.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire de gestion des logos de marques automobiles.
 * <p>Cette classe permet de récupérer l'URL d'un logo au format PNG en haute résolution
 * à partir du nom d'une marque de voiture, en s'appuyant sur le dataset open-source
 * "car-logos-dataset".</p>
 */
public class BrandLogoHelper {

    /**
     * Dictionnaire faisant correspondre le nom d'une marque (clé) à son "slug" (identifiant URL).
     * <p>Utilisé pour les marques dont le nom commercial diffère du nom de fichier
     * (ex: "MERCEDES" -> "mercedes-benz").</p>
     */
    private static final Map<String, String> BRAND_LOGO_MAP = new HashMap<>();

    static {
        BRAND_LOGO_MAP.put("RENAULT",     "renault");
        BRAND_LOGO_MAP.put("AUDI",        "audi");
        BRAND_LOGO_MAP.put("TOYOTA",      "toyota");
        BRAND_LOGO_MAP.put("MERCEDES",    "mercedes-benz");
        BRAND_LOGO_MAP.put("BMW",         "bmw");
        BRAND_LOGO_MAP.put("HONDA",       "honda");
        BRAND_LOGO_MAP.put("OPEL",        "opel");
        BRAND_LOGO_MAP.put("PEUGEOT",     "peugeot");
        BRAND_LOGO_MAP.put("CITROËN",     "citroen");
        BRAND_LOGO_MAP.put("FIAT",        "fiat");
        BRAND_LOGO_MAP.put("DACIA",       "dacia");
        BRAND_LOGO_MAP.put("VOLKSWAGEN",  "volkswagen");
        BRAND_LOGO_MAP.put("KIA",         "kia");
        BRAND_LOGO_MAP.put("FORD",        "ford");
        BRAND_LOGO_MAP.put("SEAT",        "seat");
        BRAND_LOGO_MAP.put("CUPRA",       "cupra");
        BRAND_LOGO_MAP.put("SKODA",       "skoda");
        BRAND_LOGO_MAP.put("VOLVO",       "volvo");
        BRAND_LOGO_MAP.put("HYUNDAI",     "hyundai");
        BRAND_LOGO_MAP.put("NISSAN",      "nissan");
        BRAND_LOGO_MAP.put("SUZUKI",      "suzuki");
        BRAND_LOGO_MAP.put("JEEP",        "jeep");
        BRAND_LOGO_MAP.put("MAZDA",       "mazda");
        BRAND_LOGO_MAP.put("TESLA",       "tesla");
        BRAND_LOGO_MAP.put("ALFA ROMEO",  "alfa-romeo");
        BRAND_LOGO_MAP.put("LAND ROVER",  "land-rover");
        BRAND_LOGO_MAP.put("LEXUS",       "lexus");
        BRAND_LOGO_MAP.put("MITSUBISHI",  "mitsubishi");
        BRAND_LOGO_MAP.put("PORSCHE",     "porsche");
    }

    /** URL de base du CDN hébergeant les logos optimisés. */
    private static final String BASE_URL =
            "https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@latest/logos/optimized/";

    /**
     * Génère l'URL complète du logo correspondant à la marque fournie.
     * * <p>Logique de résolution :</p>
     * <ol>
     * <li>Normalisation du nom de la marque (suppression des espaces, passage en majuscules).</li>
     * <li>Recherche dans le dictionnaire {@link #BRAND_LOGO_MAP}.</li>
     * <li>Si absent, génération d'un slug par défaut (minuscules et remplacement des espaces par des tirets).</li>
     * </ol>
     *
     * @param brand Le nom de la marque (ex: "Renault", "Alfa Romeo", "Toyota").
     * @return L'URL directe vers l'image PNG, ou {@code null} si la marque est vide.
     */
    public static String getLogoUrl(String brand) {
        if (brand == null || brand.isEmpty()) return null;

        String key = brand.trim().toUpperCase();

        // Si la marque est dans notre Map, on prend le slug spécifique
        // Sinon, on génère un slug par défaut (minuscule + tirets)
        String slug = BRAND_LOGO_MAP.containsKey(key)
                ? BRAND_LOGO_MAP.get(key)
                : brand.trim().toLowerCase().replace(" ", "-");

        return BASE_URL + slug + ".png";
    }
}