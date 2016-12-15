package io.nithril.pizzeria;

import static java.util.Objects.requireNonNull;

/**
 * Hold a customer order
 * <p>
 * Created by nlabrot on 15/12/16.
 */
public class Order {

  protected static final Order POISON = new Order();

  private final Customer customer;
  private final Pizza pizza;

  private Order() {
    this.customer = null;
    this.pizza = null;
  }

  public Order(Customer customer, Pizza pizza) {
    this.customer = requireNonNull(customer);
    this.pizza = requireNonNull(pizza);
  }

  public Customer getCustomer() {
    return customer;
  }

  public Pizza getPizza() {
    return pizza;
  }
}
