package nl.hu.bep.shopping.webservices;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.hu.bep.shopping.model.Shop;
import nl.hu.bep.shopping.model.Shopper;
import nl.hu.bep.shopping.model.ShoppingList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;

@Path("shopper")
public class PersonResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShoppers() {
        Shop shop = Shop.getShop();

        if (shop == null){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "ShoppingLists not found"))
                    .build();
        }
        return Response.ok(shop).build();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShoppingListsFromPerson(@PathParam("name") String name) {
        Shop shop = Shop.getShop();

        List<ShoppingList> allListsFromPerson = shop.getListFromPerson(name); //warning: might return null!

        if (allListsFromPerson == null){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("Error", "No owner with that name appearantly"))
                    .build();
        }
         return Response.ok(allListsFromPerson).build();
    }
}
