package nl.hu.bep.shopping.model;

import java.util.Objects;

public class Item implements NamedObject {
    private Product product;
    private int amount;

    public Item(Product p, int amount) {
        this.product = p;
        this.amount = amount;
    }

    public String getName() {
        return product.getName();
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return product.equals(item.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product);
    }

    public boolean addAmount(int amount) {
        if (amount > 0) {
            this.amount += amount;
            return true;
        }
        return false;
    }
}
