package io.bitflip.engine;

import io.bitflip.db.Database;
import io.bitflip.util.Console;
import io.bitflip.util.ThreadUtil;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Engine implements Runnable {
    
    public static final List<Exception> errors = new LinkedList<>();
    
    @Getter @Setter private boolean running;
    @Getter private Database database;
    private TradeHandler tradeHandler;
    
    public Engine() {
        tradeHandler = new TradeHandler(this);
        database = new Database();
    }

    public boolean init() {
        Console.log(this, "Engine initializing...");
        
        if (!database.init()) {
            return false;
        }
        
        Console.log(this, "Engine initialized!");
        return true;
    }
    
    @Override
    public void run() {
        running = true;
        Console.log(this, "Engine started!");
        
        ThreadUtil.spawn(tradeHandler);
        
        while (!tradeHandler.isRunning()) { }
        while (running && tradeHandler.isRunning()) {
            tradeHandler.setRunning(running);
            ThreadUtil.sleep(10);
        }
        
        running = false;
        tradeHandler.setRunning(false);
        Console.log(this, "Engine shutting down...");
    }
    
}
