package io.nithril.pizzeria;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;

/**
 * A pizzaiolo. He deque the order and queue the pizza once ready
 * <p>
 * Created by nlabrot on 15/12/16.
 */
public class Pizzaiolo implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Pizzaiolo.class);

  private final int id;
  private final BlockingDeque<Order> orders;
  private final BlockingDeque<Order> ordersReady;


  public Pizzaiolo(int id, BlockingDeque<Order> orders, BlockingDeque<Order> ordersReady) {
    this.id = requireNonNull(id);
    this.orders = requireNonNull(orders);
    this.ordersReady = requireNonNull(ordersReady);
  }

  @Override
  public void run() {

    try {
      while (true) {
        Order order = orders.take();

        if (Order.POISON == order) {
          // propagate
          ordersReady.add(order);
          return;
        }
        LOG.info("I'm [{}] preparing the pizza [{}] for the customer [{}]", id, order.getPizza(), order.getCustomer().getId());
        Thread.sleep(500);
        LOG.info("I [{}] have finished preparing the pizza [{}] for the customer [{}]", id, order.getPizza(), order.getCustomer().getId());

        ordersReady.add(order);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }

  }
}
