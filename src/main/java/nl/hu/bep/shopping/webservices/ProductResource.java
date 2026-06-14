package nl.hu.bep.shopping.webservices;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import nl.hu.bep.shopping.model.Shop;



@Path("product")
public class ProductResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getProducts() {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        //tip: oneliners seem awesome, but what part exactly gives us the error if any occurs on line 23?
        Shop.getShop().getAllProducts().forEach(p-> jab.add(Json.createObjectBuilder().add("name", p.getName())));
        return jab.build().toString();

    }
}
