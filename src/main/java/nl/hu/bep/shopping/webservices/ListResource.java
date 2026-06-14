package nl.hu.bep.shopping.webservices;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import nl.hu.bep.shopping.model.Item;
import nl.hu.bep.shopping.model.Shop;
import nl.hu.bep.shopping.model.ShoppingList;


import java.util.List;

@Path("list")
public class ListResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getShoppingLists() {
        List<ShoppingList> shoppingLists = Shop.getShop().getAllShoppingLists();
        if (shoppingLists.isEmpty()) {
            return Json.createObjectBuilder().add("error", "no lists present").build().toString();
        }
        //java stream example, see @getShoppingListByName for a step by step output example
        JsonArrayBuilder jab = Json.createArrayBuilder();
        shoppingLists.forEach(
                sl -> jab.add(
                        Json.createObjectBuilder()
                                .add("id", sl.getName())
                                .add("owner", sl.getOwner().getName())
                ));
        return jab.build().toString();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{name}")
    public String getShoppingListByName(@PathParam("name") String name) {
        Shop shop = Shop.getShop();
        JsonObjectBuilder listjob = Json.createObjectBuilder();
        ShoppingList list = shop.getShoppingListByName(name);
        //very specific JSON output to illustrate full control of parameters from domain to outside world
        try {
            listjob.add("name", list.getName());
            listjob.add("owner", list.getOwner().getName());
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (Item e : list.getListItems()) {
                JsonObjectBuilder itemjob = Json.createObjectBuilder();
                itemjob.add("itemName", e.getName());
                itemjob.add("itemAmount", e.getAmount());
                jab.add(itemjob);
            }
            listjob.add("items", jab);
        } catch (NullPointerException e) {
            listjob.add("error", "No list by that name");
        }
        return listjob.build().toString();

    }
}
