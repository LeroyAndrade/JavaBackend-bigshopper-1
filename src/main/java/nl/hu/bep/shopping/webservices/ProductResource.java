package nl.hu.bep.shopping.webservices;

import javax.annotation.security.PermitAll;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.hu.bep.shopping.model.Product;
import nl.hu.bep.shopping.model.Shop;

import java.util.List;
import java.util.Map;

//Iedereen mag lezen welke producten er zijn
@PermitAll
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


//        http://localhost:8082/restservices/product/patchName/Cola%20Zero
    //    http://localhost:8082/restservices/product/patchName/
//      http://localhost:8082/restservices/product/addShopper/

    //    Delete shopper
//    {
//        "name" : "initialList"
//    }

//Wijzig productnaam
//    http://localhost:8082/restservices/product/patchName/MyListGeslaagd
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("patchName/{oldName}")
    public Response patchProduct(@PathParam("oldName") String oldName, Map<String, String> brunoBody){
        Product product = null;

        for(Product p : Product.getAllProducts()) {
            if (p.getName().equals(oldName)) {
                product = p;
            }
        }

        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("Error", "Product with that name not found"))
                    .build();
        }


        String newName = brunoBody.get("name");
        for(Product p : Product.getAllProducts()) {
            if (p.getName().equals(newName)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("Error", "Product with that name already exists, can't modify."))
                        .build();
            }
        }

        product.setName(newName);
        return Response.ok(product)
                .build();
    }


//    Patch Change Owner



}
