package io.bitflip.engine;

import com.mongodb.DBObject;
import io.bitflip.db.Database;
import io.bitflip.util.Console;
import io.bitflip.util.ThreadUtil;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class TradeHandler implements Runnable {
    
    public static final int UPDATE_INTERVAL = 100;
    
    private Engine engine;
    @Getter @Setter private boolean running;
    
    public TradeHandler(Engine engine) {
        this.engine = engine;
    }
    
    @Override
    public void run() {
        running = true;
        Console.log(this, "TradeHandler started!");
        
        List<Order> orders;
        Iterator orderIterator, otherOrderIterator;
        
        while (running) {
            orders = fetchOpenOrders();
            if (orders.size() > 0) {
                orderIterator = orders.iterator();
                otherOrderIterator = orders.iterator();

                Order order, otherOrder;
                while (orderIterator.hasNext()) {
                    order = (Order)orderIterator.next();

                    if (order.getStatus().equals(Order.Status.Complete)) {
                        continue;
                    }

                    while (otherOrderIterator.hasNext()) {
                        otherOrder = (Order)otherOrderIterator.next();
                        
                        if (order.getId() == otherOrder.getId()) continue;

                        if (otherOrder.getStatus().equals(Order.Status.Complete)) {
                            continue;
                        }

                        if (Order.match(order, otherOrder)) {
                            executeOrders(order, otherOrder);
                        }
                    }
                }
                
                saveOrders(orders);
            }
            
            ThreadUtil.sleep(UPDATE_INTERVAL);
        }
        
        running = false;
        Console.log(this, "TradeHandler shutting down...");
    }
    
    private List<Order> fetchOpenOrders() {
        List<Order> orders = new LinkedList<>();
        
        Database database = engine.getDatabase();
        Map<String, Object> query = Collections.singletonMap("status", (Object)"open");
        List<DBObject> results = database.find("orders", Database.mapToDBObject(query));
        
        for (DBObject obj : results) {
            orders.add(Order.fromDBObject(obj));
        }
        
        return orders;
    }
    
    private void saveOrders(List<Order> orders) {
        List<DBObject> objects = new LinkedList<>();
        
        for (Order o : orders) {
            objects.add(o.toDBObject());
        }
        
        engine.getDatabase().update("orders", objects);
    }
    
    private void executeOrders(Order one, Order two) {
        Console.log(this, "Executing orders #" + one.getId() + " and #" + two.getId() + "!");
        
        if (one.getType().equals(two.getType())) return;
        Order bid = (one.getType().equals(Order.Type.Bid) ? one : two);
        Order ask = (one.equals(bid) ? two : one);
        
        // TODO verify account balances
        
        double bidAmount = bid.getAmount(), askAmount = ask.getAmount();
        if (bidAmount <= askAmount) {
            bid.setAmount(0D);
            bid.setStatus(Order.Status.Complete);
            ask.setAmount(askAmount - bidAmount);
        } else {
            bid.setAmount(bidAmount - askAmount);
            ask.setAmount(0D);
            ask.setStatus(Order.Status.Complete);
        }
    }

}
