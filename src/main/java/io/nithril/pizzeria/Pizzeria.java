package io.nithril.pizzeria;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by nlabrot on 15/12/16.
 */
public class Pizzeria {

  private AtomicLong counterId = new AtomicLong();

  private static final Logger LOG = LoggerFactory.getLogger(Pizzeria.class);

  private final int nbOfPizzaiolo;

  // Hold the submitted orders
  private final BlockingDeque<Order> submittedOrders = new LinkedBlockingDeque<>();
  // Hold the ready orders
  private final BlockingDeque<Order> readyOrders = new LinkedBlockingDeque<>();

  // Hold the seated customers
  private final BlockingDeque<Customer> seatedCustomers;

  // Hold the awaiting customers
  private final BlockingDeque<Customer> awaitingCustomers;

  private final ExecutorService dispatcherExecutor = Executors.newFixedThreadPool(2);
  private final ExecutorService pizzaioloExecutor;
  private final ExecutorService customerExecutor;


  public Pizzeria(int nbOfPizzaiolo, int nbOfPlaces) {
    checkArgument(nbOfPizzaiolo > 0);
    checkArgument(nbOfPlaces > 0);

    this.nbOfPizzaiolo = nbOfPizzaiolo;
    this.pizzaioloExecutor = Executors.newFixedThreadPool(nbOfPizzaiolo);

    this.seatedCustomers = new LinkedBlockingDeque<>(nbOfPlaces);
    this.awaitingCustomers = new LinkedBlockingDeque<>();

    // At most contains nbOfPlaces active thread
    this.customerExecutor = Executors.newFixedThreadPool(nbOfPlaces);
  }

  public void open() {
    // Spawn the pizzaiolo
    for (int i = 0; i < nbOfPizzaiolo; i++) {
      pizzaioloExecutor.execute(new Pizzaiolo(i, submittedOrders, readyOrders));
    }
    // Execute the customer dispatcher
    dispatcherExecutor.execute(this::customerDispatcher);
    // Execute the delivery dispatcher
    dispatcherExecutor.execute(this::deliveryDispatcher);
  }


  public boolean isDone() {
    return this.seatedCustomers.isEmpty() && this.awaitingCustomers.isEmpty();
  }

  /**
   * Close and release the threads
   */
  public void close() {
    awaitingCustomers.add(Customer.POISON);
    readyOrders.add(Order.POISON);

    for (int i = 0; i < nbOfPizzaiolo; i++) {
      submittedOrders.add(Order.POISON);
    }
  }

  /**
   * A customer enters the pizzeria
   *
   * @param customer
   * @throws InterruptedException
   */
  public void customerEnter(Customer customer) {
    awaitingCustomers.add(customer);
  }

  /**
   * A customer leaves the pizzeria
   *
   * @param customer the customer must be a "seated customer"
   * @throws IllegalStateException if the customer is not a "seated customer"
   */
  public void customerLeave(Customer customer) {
    checkState(seatedCustomers.remove(customer));
  }


  /**
   * A customer
   *
   * @param order the customer order must submitted by a "seated customer"
   * @throws IllegalStateException if the customer is not a "seated customer"
   */
  public void submitOrder(Order order) {
    checkState(seatedCustomers.contains(order.getCustomer()));
    submittedOrders.add(order);
  }


  public Customer createCustomer(Pizza pizza) {
    return new Customer(counterId.incrementAndGet(), this, pizza);
  }

  /**
   * Seat the awaiting customer when a seat is released
   */
  private void customerDispatcher() {
    try {
      while (true) {
        Customer customer = awaitingCustomers.take();
        if (Customer.POISON == customer) {
          return;
        }
        seatedCustomers.put(customer);

        customerExecutor.execute(() -> {
          customer.seatAndOrder();
        });
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Dispatch ready orders to customers
   */
  private void deliveryDispatcher() {
    try {
      while (true) {
        Order order = readyOrders.take();

        if (Order.POISON == order) {
          return;
        }
        customerExecutor.execute(() -> order.getCustomer().receiveOrder(order));
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
