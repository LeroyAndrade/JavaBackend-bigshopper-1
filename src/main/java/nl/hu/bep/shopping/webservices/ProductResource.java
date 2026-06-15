package nl.hu.bep.shopping.webservices;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.hu.bep.shopping.model.Product;
import nl.hu.bep.shopping.model.Shop;

import java.util.List;
import java.util.Map;


@Path("product")
public class ProductResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts() {

        List<Product> product =Shop.getShop().getAllProducts();
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "ShoppingLists not found"))
                    .build();
        }
        return Response.ok(product).build();
    }
}
