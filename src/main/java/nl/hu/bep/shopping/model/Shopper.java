package nl.hu.bep.shopping.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Shopper implementeert Principal zodat Jersey deze klasse kan gebruiken
public class Shopper implements NamedObject, Principal {
    private String name;
    // Extra velden voor authenticatie/autorisatie

//    Voorkom dat password in
    @JsonIgnore
    private String password;
    private String role;

    private static List<Shopper> allShoppers = new ArrayList<>();
    private List<ShoppingList> allLists = new ArrayList<>();

    public Shopper(String nm) {
        this.name = nm;
        // Standaardwaarden voor shoppers die zonder wachtwoord worden aangemaakt.
        this.password = null;
        this.role = "user";
        if (!allShoppers.contains(this)) allShoppers.add(this);
    }

    // Nieuwe constructor voor shoppers die kunnen inloggen.
    public Shopper(String nm, String password, String role) {
        this.name = nm;
        this.password = password;
        this.role = role;

        if (!allShoppers.contains(this)) allShoppers.add(this);
    }

    // Voorbeeldgebruikers.
    // In een echte applicatie halen uit db

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shopper shopper = (Shopper) o;
        return name.equals(shopper.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public static List<Shopper> getAllShoppers() {
        return Collections.unmodifiableList(allShoppers);
    }

    public boolean addList(ShoppingList newList) {
        if (!allLists.contains(newList)) {
            return allLists.add(newList);
        }
        return false;
    }




//    JWT
    // Rol van de gebruiker, bijvoorbeeld "user" of "admin".
    public String getRole() {
        return role;
    }

    // Methode om een shopper op naam terug te vinden.
    // Deze is later handig bij het controleren van een JWT.
    public static Shopper getUserByName(String name) {
        for (Shopper shopper : allShoppers) {
            if (shopper.getName().equals(name)) {
                return shopper;
            }
        }

        return null;
    }

    // Methode om login-credentials te controleren.
    // Bij een goede login geven we de role terug.
    // Bij een foute login geven we null terug.
    public static String validateLogin(String username, String password) {
        Shopper shopper = getUserByName(username);

        // Geen shopper gevonden
        if (shopper == null) {
            return null;
        }

        // Shopper heeft geen wachtwoord, dus kan niet inloggen
        if (shopper.password == null) {
            return null;
        }

        // Wachtwoord klopt niet
        if (!shopper.password.equals(password)) {
            return null;
        }

        // Login klopt, dus geef de rol terug
        return shopper.getRole();
    }



    public void setBoodschappenlijstNaam(String name){
        this.name = name;
    }

    @JsonIgnore
    public List<ShoppingList> getAllLists() {
        return Collections.unmodifiableList(allLists);
    }

    public int getAmountOfLists() {
        return allLists.size();
    }
}
