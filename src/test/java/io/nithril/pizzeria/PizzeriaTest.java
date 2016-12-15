package io.nithril.pizzeria;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by nlabrot on 15/12/16.
 */
public class PizzeriaTest {

  @Test
  public void name() throws Exception {

    int nbOfPizzaiolo = 5;
    int nbOfPlaces = 50;
    int nbOfCustomers = 100;


    Pizzeria pizzeria = new Pizzeria(nbOfPizzaiolo, nbOfPlaces);
    pizzeria.open();


    List<Customer> customers = new ArrayList<>();

    for (int i = 0; i < nbOfCustomers; i++) {
      Customer customer = pizzeria.createCustomer(Pizza.values()[ThreadLocalRandom.current().nextInt(Pizza.values().length)]);
      customers.add(customer);
      pizzeria.customerEnter(customer);
    }


    while (!pizzeria.isDone()) {
      Thread.sleep(500);
    }

    pizzeria.close();

    for (Customer customer : customers) {
      Assert.assertTrue(customer.isLeft());
    }


  }
}
