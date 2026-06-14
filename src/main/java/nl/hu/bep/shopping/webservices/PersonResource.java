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
import nl.hu.bep.shopping.model.Shop;
import nl.hu.bep.shopping.model.Shopper;
import nl.hu.bep.shopping.model.ShoppingList;

import java.util.List;

@Path("shopper")
public class PersonResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getShoppers() {
        Shop shop = Shop.getShop();
        JsonArrayBuilder jab = Json.createArrayBuilder();

        for (Shopper p : shop.getAllPersons()) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("name", p.getName());
            job.add("numberOfLists", p.getAmountOfLists());
            jab.add(job);
        }

        JsonArray array = jab.build();
        return array.toString();

    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getShoppingListsFromPerson(@PathParam("name") String name) {
        Shop shop = Shop.getShop();
        JsonArrayBuilder jab = Json.createArrayBuilder();
        List<ShoppingList> allListsFromPerson = shop.getListFromPerson(name); //warning: might return null!
        if (allListsFromPerson == null)
            return Json.createObjectBuilder()
                    .add("error", "No owner with that name appearantly")
                    .build()
                    .toString();
        else
            allListsFromPerson.forEach(
                    sl -> jab.add(
                            Json.createObjectBuilder()
                                    .add("name", sl.getName())));
        return jab.build().toString();
    }
}
