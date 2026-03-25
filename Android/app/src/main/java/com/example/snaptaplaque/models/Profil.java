package com.example.snaptaplaque.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente le profil d'un utilisateur de l'application SnapTaPlaque.
 *
 * <p>Cette classe encapsule les informations personnelles de l'utilisateur
 * (nom d'utilisateur, prénom, nom, mot de passe, email) ainsi que sa liste
 * de véhicules favoris.</p>
 *
 * <p>L'instanciation directe est interdite (constructeur privé). La création
 * d'un profil se fait exclusivement via la méthode fabrique
 * {@link #createProfil(String, String, String, String, String)}.</p>
 *
 * <p>La liste {@link #favoriteVehicule} est synchronisée automatiquement par le
 * {@link com.example.snaptaplaque.viewmodels.SharedViewModel} lorsqu'un véhicule
 * est marqué ou démarqué comme favori depuis l'un des fragments de l'application.</p>
 *
 * <h3>Exemple d'utilisation :</h3>
 * <pre>{@code
 * Profil profil = Profil.createProfil("jdupont", "Jean", "Dupont", "motdepasse", "jean@email.fr");
 * String pseudo = profil.getUsername(); // "jdupont"
 * List<Vehicle> favoris = profil.favoriteVehicule; // liste vide à la création
 * }</pre>
 *
 * @see Vehicle
 * @see com.example.snaptaplaque.viewmodels.SharedViewModel
 */
public class Profil {

    /**
     * Nom d'utilisateur unique identifiant le profil.
     */
    private String username;

    /**
     * Prénom de l'utilisateur.
     */
    private String fisrtName;

    /**
     * Nom de famille de l'utilisateur.
     */
    private String name;

    /**
     * Mot de passe du compte utilisateur.
     */
    private String password;

    /**
     * Adresse email associée au profil.
     */
    private String email;

    /**
     * Liste des véhicules marqués comme favoris par l'utilisateur.
     *
     * <p>Cette liste est initialisée vide à la création du profil et est mise à jour
     * par le {@link com.example.snaptaplaque.viewmodels.SharedViewModel} lors de chaque
     * appel à {@code toggleFavorite()}. Elle contient uniquement les {@link Vehicle}
     * dont la propriété {@code isFavorite()} vaut {@code true}.</p>
     */
    public List<Vehicle> favoriteVehicule;

    /**
     * Constructeur privé du profil utilisateur.
     *
     * <p>Ce constructeur est privé afin d'imposer l'utilisation de la méthode fabrique
     * {@link #createProfil(String, String, String, String, String)} pour toute
     * instanciation. La liste des véhicules favoris est initialisée comme une
     * {@link ArrayList} vide.</p>
     *
     * @param username  le nom d'utilisateur unique ; ne doit pas être {@code null} ou vide
     * @param fisrtName le prénom de l'utilisateur ; ne doit pas être {@code null}
     * @param name      le nom de famille de l'utilisateur ; ne doit pas être {@code null}
     * @param password  le mot de passe du compte ; ne doit pas être {@code null} ou vide
     * @param email     l'adresse email associée au profil ; ne doit pas être {@code null}
     */
    private Profil(String username, String fisrtName, String name, String password, String email) {
        this.username = username;
        this.fisrtName = fisrtName;
        this.name = name;
        this.password = password;
        this.email = email;
        this.favoriteVehicule = new ArrayList<>();
    }

    /**
     * Méthode fabrique (factory method) pour créer une nouvelle instance de {@link Profil}.
     *
     * <p>Cette méthode constitue le seul point d'entrée pour instancier un profil
     * utilisateur. Elle délègue la construction au constructeur privé
     * {@link #Profil(String, String, String, String, String)}.</p>
     *
     * @param username  le nom d'utilisateur unique ; ne doit pas être {@code null} ou vide
     * @param fisrtName le prénom de l'utilisateur ; ne doit pas être {@code null}
     * @param name      le nom de famille de l'utilisateur ; ne doit pas être {@code null}
     * @param password  le mot de passe du compte ; ne doit pas être {@code null} ou vide
     * @param email     l'adresse email associée au profil ; ne doit pas être {@code null}
     * @return une nouvelle instance de {@link Profil} initialisée avec les paramètres fournis
     * et une liste de favoris vide
     */
    public static Profil createProfil(String username, String fisrtName, String name, String password, String email) {
        return new Profil(username, fisrtName, name, password, email);
    }

    /**
     * Retourne le nom d'utilisateur du profil.
     *
     * @return le nom d'utilisateur unique associé à ce profil ; ne retourne jamais {@code null}
     * si le profil a été correctement initialisé
     */
    public String getUsername() {
        return username;
    }

    public void setFavoriteVehicule(List<Vehicle> favorites) {
        this.favoriteVehicule = favorites;
    }
}