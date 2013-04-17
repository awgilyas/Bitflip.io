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
    private static Engine instance = null;

    @Getter @Setter private boolean running;
    @Getter private boolean ready;
    @Getter private Database database;
    private TradeHandler tradeHandler;
    
    public Engine() {
        tradeHandler = new TradeHandler(this);
        database = new Database();
    }
    
    public static Engine create() {
        if (instance == null) {
            return (instance = new Engine());
        } else {
            Console.log(instance, "Engine instance already created; cannot create new instance.");
            return null;
        }
    }
    
    public static Engine getInstance() {
        return instance;
    }

    public boolean init() {
        Console.log(this, "Engine initializing...");
        
        if (!database.init()) {
            return false;
        }
        
        Console.log(this, "Engine initialized!");
        return (ready = true);
    }
    
    @Override
    public void run() {
        if (!ready) {
            Console.log(this, "Engine not initialized; cannot run!");
            return;
        }
        
        ThreadUtil.spawn(tradeHandler);
        while (!tradeHandler.isRunning()) { }
        
        Console.log(this, "Engine started!");
        running = true;
        while (running && tradeHandler.isRunning()) {
            tradeHandler.setRunning(running);
            ThreadUtil.sleep(10);
        }
        
        running = false;
        tradeHandler.setRunning(false);
        Console.log(this, "Engine shutting down...");
    }
    
}
