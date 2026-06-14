package nl.hu.bep.shopping.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Product implements NamedObject {
    private String name;
    private static List<Product> allProducts = new ArrayList<>();

    public Product(String nm) {
        this.name = nm;
        if (!allProducts.contains(this)) allProducts.add(this);
    }

    public String getName() {
        return name;
    }

    public static List<Product> getAllProducts() {
        return Collections.unmodifiableList(allProducts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return name.equals(product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
