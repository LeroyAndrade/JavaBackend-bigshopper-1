package nl.hu.bep.shopping.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingList implements NamedObject {
    private String name;
    private Shopper owner;
    private static List<ShoppingList> allLists = new ArrayList<>();
    private List<Item> listItems;

    public ShoppingList(String nm, Shopper owner) {
        this.name = nm;
        this.owner = owner;
        allLists.add(this);
        listItems = new ArrayList<>();
    }

    public List<Item> getListItems() {
        return Collections.unmodifiableList(listItems);
    }

    public boolean addItem(Product p, int amount) {
        Item newItem = new Item(p, amount);
        if (!listItems.contains(newItem)) {
            return listItems.add(newItem);
        }
        return listItems.get(listItems.indexOf(newItem)).addAmount(amount);
    }

    public Item getItem(String name) {
        return listItems.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public Shopper getOwner() {
        return owner;
    }

    public static List<ShoppingList> getAllLists() {
        return Collections.unmodifiableList(allLists);
    }
}
