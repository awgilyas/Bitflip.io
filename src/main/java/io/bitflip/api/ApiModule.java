package io.bitflip.api;

import io.bitflip.db.Database;
import io.bitflip.engine.Order;
import java.util.Collections;
import java.util.List;

public abstract class ApiModule {
    
    protected ApiServer server;
    
    protected ApiModule(ApiServer server) {
        this.server = server;
    }
    
    public abstract ApiRequestResult call(List<Object> args);
    
    protected Order getLastOrder() {
        return Order.fromDBObject(
            server.getDatabase().findOneLast("orders", 
                Database.mapToDBObject(Collections.singletonMap("status", (Object)"complete"))));
    }

}
