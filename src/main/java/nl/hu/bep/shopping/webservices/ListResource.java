package nl.hu.bep.shopping.webservices;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.bep.shopping.model.*;

import java.util.List;

import java.util.Map;

@Path("list")
public class ListResource {
    //Om te zeggen: dit is het Jackson object om JSON te kunnen lezen en schrijven
    private ObjectMapper mapper = new ObjectMapper();


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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("createShoppingList/{ownerName}")
    public Response createShoppingList(@PathParam("ownerName") String ownerName, Map<String, String> body) {
// zet Jackson de JSON automatisch om naar een Java Map.
//        Map<String, String>      betekent:
// een verzameling key-value paren, // waarbij de key een String is en de value ook een String.

        Shopper owner = null;
        // Voorbeeld JSON body:
//        Map<String, String> <-geen json
        // {
        //   "name": "Weekend boodschappen"
        // }
        // Omdat @Consumes(MediaType.APPLICATION_JSON), zet Jackson de JSON automatisch om naar een Java Map.

        for (Shopper s : Shopper.getAllShoppers()) {
            if (s.getName().equals(ownerName)) {
                owner = s;
                break;
            }
        }

//        controleren of er een shopper gevonden is
        // Als owner nog steeds null is, dan bestaat er geen shopper met de naam uit de URL.
        if (owner == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Shopper niet gevonden"))
                    .build();
        }
//  haal de naam van de nieuwe boodschappenlijst uit de JSON body.
        //
        // Voorbeeld body:
        // {
        //   "name": "Weekend boodschappen"
        // }

        // ShoppingList  model deze constructor:
        // new ShoppingList(String naam, Shopper owner
        // Daarom geef mee:
        // listName = naam uit de JSON body
        // owner    = Shopper uit de url haalt
        String listName = body.get("name");

        // Voorbeeld:
        // new ShoppingList("Weekend boodschappen", Dum-Dum)
        ShoppingList shoppingList = new ShoppingList(listName, owner);

        // De ShoppingList weet nu wie de owner is,
        // maar de Shopper moet ook weten dat hij deze lijst heeft.
        //
        // Dus:
        // ShoppingList -> owner
        // Shopper -> shoppingList
        owner.addList(shoppingList);

        // CREATED betekent HTTP status 201.
        return Response.ok(shoppingList)
                .build();
    }



//    Add product, wijzig de JSON inhoud bijvoorbeeld met +=1
//    http://localhost:8082/restservices/list/addProduct/initialList
//    http://localhost:8082/restservices/list/addProduct/anotherList

//{
//    "name": "Paracetamol 30stk",
//        "amount": 1
//}

//    Ik maak een nieuwe ShoppingList voor een shopper die al bestaat.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addProduct/{listName}")
    public Response addProductToShoppingList(
            @PathParam("listName") String listName,
            Map<String, Object> body) {

        // Dan zoekt deze regel naar de boodschappenlijst met naam "initialList".
        ShoppingList shoppingList = Shop.getShop().getShoppingListByName(listName);
        ShoppingList shoppingList2 = Shop.getShop().getShoppingListByName(listName);
        System.out.println(shoppingList2);

        if (shoppingList == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Boodschappenlijst niet gevonden"))
                    .build();
        }

        String productName = body.get("name").toString();
        int amount = (int) body.get("amount");

        // omdat het product nog niet gevonden is.
        Product product = null;

//        loop door alle bestaande producten,  addItem(..) heeft een echt Product-object nodig.
        for (Product p : Shop.getShop().getAllProducts()) {
//            if gevonden / true
            if (p.getName().equals(productName)) {
                product = p;
                break;
            }
        }

        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Product niet gevonden"))
                    .build();
        }

        shoppingList.addItem(product, amount);

        return Response.ok(shoppingList).build();
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("boodschappenlijstReset/{listName}")
    public Response boodschappenlijstReset(@PathParam("listName") String listName) {
        ShoppingList shoppingList = Shop.getShop().getShoppingListByName(listName);

        if (shoppingList == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Boodschappenlijst niet gevonden"))
                    .build();
        }

        shoppingList.reset();

        return Response.ok(shoppingList).build();
    }

}