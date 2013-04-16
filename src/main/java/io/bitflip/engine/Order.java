package io.bitflip.engine;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.Getter;
import lombok.Setter;

public class Order {
    
    @Getter @Setter private Type type;
    @Getter @Setter private Status status;
    @Getter @Setter private double amount;
    @Getter private String account;
    @Getter private int id;
    @Getter private double price;
    @Getter private long createdOn, completedOn;
    
    public Order(Type type, String account, int id, double amount, double price) {
        this.type = type;
        this.account = account;
        this.id = id;
        this.amount = amount;
        this.price = price;
    }
    
    private Order() {
    }
    
    public static boolean match(Order one, Order two) {
        // Check if both orders are open
        if (one.status.equals(two.status) && one.status.equals(Status.Open)) {
            // Check if one is a bid and one is an ask
            if (!one.type.equals(two.type)) {
                // Check if they're the same price
                if (one.price == two.price) {
                    // Check for enough coins to satisfy either
                    if (one.type.equals(Type.Ask) && one.amount >= two.amount) {
                        return true;
                    } else if (one.type.equals(Type.Bid) && one.amount <= two.amount) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public static Order fromDBObject(DBObject obj) {
        Order order = new Order();
        
        if (!obj.containsField("id")) return null;
        
        Object id_obj = obj.get("id");
        if (id_obj instanceof Integer) {
            order.id = ((Integer)id_obj).intValue();
        } else if (id_obj instanceof Double) {
            order.id = ((Double)obj.get("id")).intValue();
        } else {
            return null;
        }
        
        if (!obj.containsField("type")) return null;
        order.type = Type.fromString((String)obj.get("type"));

        if (!obj.containsField("status")) return null;
        order.status = Status.fromString((String)obj.get("status"));
        
        if (!obj.containsField("account")) return null;
        order.account = (String)obj.get("account");
        
        if (!obj.containsField("price")) return null;
        order.price = ((Double)obj.get("price")).doubleValue();
        
        if (!obj.containsField("amount")) return null;
        order.amount = ((Double)obj.get("amount")).doubleValue();
        
        return order;
    }
    
    public DBObject toDBObject() {
        DBObject obj = new BasicDBObject();
        
        obj.put("id", id);
        obj.put("type", type.value);
        obj.put("status", status.value);
        obj.put("account", account);
        obj.put("price", price);
        obj.put("amount", amount);
        
        return obj;
    }
    
    public enum Type {
        Bid("bid"),
        Ask("ask");
        
        private String value;
        Type(String value) {
            this.value = value;
        }
        
        public static Type fromString(String value) {
            for (Type t : Type.values()) {
                if (t.value.equalsIgnoreCase(value)) {
                    return t;
                }
            }

            return null;
        }
    }
    
    public enum Status {
        Open("open"),
        Cancelled("cancelled"),
        Complete("complete");
        
        private String value;
        Status(String value) {
            this.value = value;
        }
        
        public static Status fromString(String value) {
            for (Status s : Status.values()) {
                if (s.value.equalsIgnoreCase(value)) {
                    return s;
                }
            }

            return null;
        }
    }
    
}
