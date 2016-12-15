package io.nithril.pizzeria;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A customer which already know which pizza he will order
 * <p>
 * Created by nlabrot on 15/12/16.
 */
public class Customer {

  private static final Logger LOG = LoggerFactory.getLogger(Customer.class);

  protected static final Customer POISON = new Customer();

  private final Pizzeria pizzeria;
  private final Pizza pizza;
  private final long id;

  private boolean leaved = false;

  private Customer() {
    this.pizzeria = null;
    this.pizza = null;
    this.id = 0;
  }

  public Customer(long id, Pizzeria pizzeria, Pizza pizza) {
    this.id = id;
    this.pizzeria = requireNonNull(pizzeria);
    this.pizza = requireNonNull(pizza);
  }

  public void seatAndOrder() {
    LOG.info("I'm [{}] seated and ready to order [{}]", id, pizza);
    pizzeria.submitOrder(new Order(this, pizza));
  }


  public void receiveOrder(Order order) {
    try {
      LOG.info("I'm [{}] eating the pizza [{}]", id, order.getPizza());
      Thread.sleep(1000);
      LOG.info("I [{}] have ate the pizza [{}]", id, order.getPizza());
      pizzeria.customerLeave(this);
      leaved = true;
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public boolean isLeft() {
    return leaved;
  }

  public long getId() {
    return id;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Customer customer = (Customer) o;

    return id == customer.id;

  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
