package nl.hu.bep.shopping.webservices;


//JWT
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
//JWT

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

    // JWT Haalt de naam van de ingelogde gebruiker uit de SecurityContext.
// Deze naam komt uiteindelijk uit de JWT.
    private String getLoggedInUsername(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return null;
        }

        return securityContext.getUserPrincipal().getName();
    }

    // JWT Controleert of de ingelogde gebruiker eigenaar is van de boodschappenlijst.
// Zo mag een gebruiker alleen zijn eigen lijsten bewerken.
    private boolean isOwner(ShoppingList shoppingList, SecurityContext securityContext) {
        String loggedInUser = getLoggedInUsername(securityContext);

        if (loggedInUser == null) {
            return false;
        }

        if (shoppingList == null || shoppingList.getOwner() == null) {
            return false;
        }

        return loggedInUser.equals(shoppingList.getOwner().getName());
    }

    // JWT Controleert of de gebruiker eigenaar is OF admin is.
// Dit is de plek waar je zegt: admin mag ook andermans lijsten bewerken.
    private boolean isOwnerOrAdmin(ShoppingList shoppingList, SecurityContext securityContext) {
        return isOwner(shoppingList, securityContext) || isAdmin(securityContext);
    }

    // JWT Controleert of de ingelogde gebruiker admin is.
    private boolean isAdmin(SecurityContext securityContext) {
        if (securityContext == null) {
            return false;
        }

        return securityContext.isUserInRole("admin");
    }

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
    @RolesAllowed({"user", "admin"}) // Alleen ingelogde gebruikers mogen lijsten maken
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("createShoppingList/{ownerName}")
    public Response createShoppingList(@PathParam("ownerName") String ownerName, Map<String, String> body, @Context SecurityContext securityContext) {
// zet Jackson de JSON automatisch om naar een Java Map.
//        Map<String, String>      betekent:
// een verzameling key-value paren, // waarbij de key een String is en de value ook een String.


//        JWT
        // Haal de naam van de ingelogde gebruiker uit de JWT/SecurityContext.
        String loggedInUser = getLoggedInUsername(securityContext);

        // Niet ingelogd betekent: geen lijst maken.
        if (loggedInUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // De ingelogde gebruiker mag alleen een lijst voor zichzelf maken.
        // Dus leroy mag niet createShoppingList/admin aanroepen.
        if (!loggedInUser.equals(ownerName) && !isAdmin(securityContext)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Je mag alleen je eigen lijsten bewerken"))
                    .build();
        }
//        JWT

//        Als lijst bestaat, maak geen nieuwe aan
        String listName = body.get("name");
        if (Shop.getShop().getShoppingListByName(listName) != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("Error", "Lijst bestaat al"))
                    .build();
        }


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
    @RolesAllowed({"user", "admin"}) // Alleen ingelogde gebruikers mogen producten aan een lijst toevoegen
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addProduct/{listName}")
    public Response addProductToShoppingList(
            @PathParam("listName") String listName,
            Map<String, Object> body,  @Context SecurityContext securityContext) {

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


//        JWT
        // Controleer of de ingelogde gebruiker eigenaar is van deze lijst.
        if (!isOwnerOrAdmin(shoppingList, securityContext)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Je mag alleen je eigen lijsten bewerken"))
                    .build();
        }


        shoppingList.addItem(product, amount);

        return Response.ok(shoppingList).build();
    }


    //   http://localhost:8082/restservices/list/boodschappenlijstReset/initialList
    //    http://localhost:8082/restservices/list/addProduct/initialList

    @PUT
    @RolesAllowed({"user", "admin"}) // Alleen ingelogde gebruikers mogen hun eigen lijst resetten
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("boodschappenlijstReset/{listName}")
    public Response boodschappenlijstReset(@PathParam("listName") String listName,
                                           @Context SecurityContext securityContext) {
        ShoppingList shoppingList = Shop.getShop().getShoppingListByName(listName);

        if (shoppingList == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Boodschappenlijst niet gevonden"))
                    .build();
        }

        //JWT  Controleer of de ingelogde gebruiker eigenaar is van deze lijst.
        // Zo mag leroy niet de lijst van admin resetten.
        if (!isOwnerOrAdmin(shoppingList, securityContext)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Je mag alleen je eigen lijsten bewerken"))
                    .build();
        }


        shoppingList.reset();

        return Response.ok(shoppingList).build();
    }


    //Patch voor de shopperProduct aanpassen
// http://localhost:8082/restservices/shopper/patchCustomername/Dum-Dum
    @PATCH
    @RolesAllowed({"user", "admin"}) // Alleen ingelogde gebruikers mogen hun eigen lijstnaam wijzigen
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("patchCustomername/{oldName}")
    public Response patchCustomerProduct(@PathParam("oldName") String oldName, Map<String, String> brunoBody,
                                         @Context SecurityContext securityContext) {

        Shopper shopperName = null;
        List<ShoppingList> shoppingPersons = Shop.getShop().getAllShoppingLists();

        String newName = brunoBody.get("name");

        ShoppingList gevondenLijst = null;


        for (ShoppingList s : shoppingPersons) {
            if (s.getName().equals(oldName)) {
                gevondenLijst = s;
                shopperName = s.getOwner();
                System.out.println(s);
                System.out.println(s.getClass().getName());
                break;
            }
        }

        if (gevondenLijst == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("Error", "Boodschappenlijst is niet gevonden"))
                    .build();
        }

//        JWT
        // Controleer of de ingelogde gebruiker eigenaar is van deze lijst.
        if (!isOwnerOrAdmin(gevondenLijst, securityContext)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Je mag alleen je eigen lijsten bewerken"))
                    .build();
        }

        gevondenLijst.setName(brunoBody.get("name"));

        return Response.ok(shopperName)
                .build();
    }





//Patch voor de shopperProduct aanpassen
//    http://localhost:8082/restservices/list/patchOwner/initialList

//    {
//        "name": "anotherList",
//            "owner": {
//        "name": "Andrade Leroy"
//        }
//    }
    @PATCH
    @RolesAllowed({"user", "admin"}) // Alleen ingelogde gebruikers mogen eigenaar van hun eigen lijst wijzigen
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("patchOwner/{oldCustomerName}")
    public Response patchCustomer(@PathParam("oldCustomerName") String oldCustomerName, Map<String, Object> brunoBody,
                                  @Context SecurityContext securityContext) {

        ShoppingList gevondenLijst = null;
        List<ShoppingList> shoppingPersons = Shop.getShop().getAllShoppingLists();

        for (ShoppingList s : shoppingPersons) {
            if (s.getName().equals(oldCustomerName)) {
                gevondenLijst = s;
                break;
            }
        }

        if (gevondenLijst == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("Error", "Boodschappenlijst is niet gevonden"))
                    .build();
        }

        // JWT Controleer of de ingelogde gebruiker eigenaar is van deze lijst.
// Alleen de huidige eigenaar mag de lijst aanpassen.
        if (!isOwnerOrAdmin(gevondenLijst, securityContext)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Je mag alleen je eigen lijsten bewerken"))
                    .build();
        }


        Map<String, String> owner = (Map<String, String>) brunoBody.get("owner");

        if (owner == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("Error", "Boodschappenlijst is niet gevonden"))
                    .build();
        }


        String newOwner = owner.get("name");
        Shopper shopperName = null;

        for (Shopper s : Shop.getShop().getAllPersons()){
            if (s.getName().equals(newOwner)) {
                shopperName = s;
                break;
            }
        }

        if (shopperName == null) {
            shopperName = new Shopper(newOwner);
        }

        gevondenLijst.setOwner(shopperName);
        shopperName.addList(gevondenLijst);


        return Response.ok(gevondenLijst)
                .build();
    }
}



