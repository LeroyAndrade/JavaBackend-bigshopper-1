//MySecurityContext vertelt Jersey:
//Wie is ingelogd?
//        Welke rol heeft deze gebruiker?

package nl.hu.bep.shopping.security;

import nl.hu.bep.shopping.model.Shopper;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class MySecurityContext implements SecurityContext {
    private Shopper user;
    private String scheme;

    public MySecurityContext(Shopper user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    @Override
    public Principal getUserPrincipal() {
        // Geeft de ingelogde gebruiker terug.
        // Shopper implementeert Principal, dus dit mag.
        return this.user;
    }

    @Override
    public boolean isUserInRole(String s) {
        // Als er geen user is, is de gebruiker niet ingelogd.
        // Dan heeft die dus ook geen rol.
        if (user == null) {
            return false;
        }

        // Als de user geen rol heeft, mag die nergens doorheen.
        if (user.getRole() == null) {
            return false;
        }

        // Vergelijk de gevraagde rol met de rol van de ingelogde shopper.
        // Bijvoorbeeld: @RolesAllowed("admin") vergelijkt "admin" met user.getRole().
        System.out.printf("%s equals %s%n", s, user.getRole());

        return s.equals(user.getRole());
    }

    @Override
    public boolean isSecure() {
        // Controleert of de request via HTTPS binnenkomt.
        return "https".equals(this.scheme);
    }

    @Override
    public String getAuthenticationScheme() {
        // In de bijlage staat BASIC_AUTH.
        // We nemen die benaming over, ook al gebruiken we JWT tokens.
        return SecurityContext.BASIC_AUTH;
    }
}