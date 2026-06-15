package nl.hu.bep.shopping.webservices;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.hu.bep.shopping.model.Item;
import nl.hu.bep.shopping.model.Shop;
import nl.hu.bep.shopping.model.ShoppingList;

import java.util.List;

import java.util.Map;

@Path("list")
public class ListResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShoppingLists() {
        List<ShoppingList> shoppingLists = Shop.getShop().getAllShoppingLists();

        if (shoppingLists.isEmpty()) {
//                    Json.createObjectBuilder().add("error", "no lists present").build().toString();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "ShoppingLists not found"))
                    .build();
        }
        return Response.ok(shoppingLists).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}")
    public Response getShoppingListByName(@PathParam("name") String name) {
        Shop shop = Shop.getShop();
        ShoppingList list = shop.getShoppingListByName(name);
        //very specific JSON output to illustrate full control of parameters from domain to outside world

        if (list == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("Error", "Geen lijst"))
                    .build();
        }

        return Response.ok(list).build();

    }
}
