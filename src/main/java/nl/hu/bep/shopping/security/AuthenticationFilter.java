//De filter leest straks de JWT uit de Authorization header, valideert die token, haalt de username eruit en vult daarna
//        jouw MySecurityContext. Dat is dezelfde constructie als in de bijlage.
//

package nl.hu.bep.shopping.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import nl.hu.bep.shopping.model.Shopper;
import nl.hu.bep.shopping.webservices.AuthenticationResource;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestCtx) {
        // Haal het scheme op, bijvoorbeeld "http" of "https".
        // Dit wordt gebruikt door MySecurityContext.isSecure().
        String scheme = requestCtx.getUriInfo().getRequestUri().getScheme();

        // Iedereen wordt eerst behandeld als guest.
        // Dat betekent: geen ingelogde Shopper.
        MySecurityContext msc = new MySecurityContext(null, scheme);

        // Haal de Authorization header op.
        // Bijvoorbeeld:
        // Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
        String authHeader = requestCtx.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Controleer of er een Bearer token is meegestuurd.
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Haal alleen de JWT uit de header.
            String token = authHeader.substring("Bearer".length()).trim();

            try {
                // Maak een JWT parser met dezelfde key als waarmee de token is gemaakt.
                JwtParser parser = Jwts.parser().setSigningKey(AuthenticationResource.key);

                // Controleer en parse de JWT.
                // Als de token ongeldig of verlopen is, komt er een JwtException.
                Claims claims = parser.parseClaimsJws(token).getBody();

                // De subject is de username die we in AuthenticationResource hebben gezet.
                String user = claims.getSubject();

                // Zoek de Shopper op basis van de username.
                Shopper shopper = Shopper.getUserByName(user);

                // Maak een SecurityContext met de ingelogde shopper.
                msc = new MySecurityContext(shopper, scheme);

            } catch (JwtException | IllegalArgumentException e) {
                // Als de JWT niet klopt, verlopen is of niet gelezen kan worden,
                // behandelen we de gebruiker als guest.
                System.out.println("Invalid JWT, processing as guest!");
            }
        }

        // Geef onze SecurityContext aan Jersey.
        // Jersey gebruikt deze context later bij bijvoorbeeld @RolesAllowed.
        requestCtx.setSecurityContext(msc);
    }
}