//  Voor JWT - login
package nl.hu.bep.shopping.webservices;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import nl.hu.bep.shopping.model.Shopper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Key;
import java.util.Calendar;
import java.util.Map;

@Path("/authentication")
public class AuthenticationResource {

    // Geheime sleutel waarmee de JWT wordt ondertekend.
    // Dezezelfde key heb je later nodig om de JWT te controleren.
    final static public Key key = MacProvider.generateKey();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticateUser(LogonRequest req) {
        try {
            // Controleer username en password.
            // Als deze kloppen, krijgen we de role terug.
            String role = Shopper.validateLogin(req.username, req.password);

            // Geen role betekent: login is mislukt.
            if (role == null) {
                throw new IllegalArgumentException("No user found!");
            }

            // Maak een JWT voor deze shopper.
            String jwt_token = createToken(req.username, role);

            // Geef de JWT terug aan de frontend.
            return Response.ok(Map.of("JWT", jwt_token)).build();

        } catch (JwtException | IllegalArgumentException e) {
            // Bij verkeerde login sturen we 401 Unauthorized terug.
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private String createToken(String username, String role) {
        // Token verloopt na 30 minuten.
        Calendar expires = Calendar.getInstance();
        expires.add(Calendar.MINUTE, 30);

        return Jwts.builder()
                // De subject is de naam van de ingelogde shopper.
                .setSubject(username)

                // Verlooptijd van de token.
                .setExpiration(expires.getTime())

                // Rol van de gebruiker, bijvoorbeeld "user" of "admin".
                .claim("role", role)

                // Token ondertekenen met HS512 en de geheime key.
                .signWith(SignatureAlgorithm.HS512, key)

                // Maak de uiteindelijke JWT-string.
                .compact();
    }
}