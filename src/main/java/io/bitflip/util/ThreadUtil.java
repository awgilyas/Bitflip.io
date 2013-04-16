package io.bitflip.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadUtil {
    
    private static final Map<Runnable, Thread> spawnedThreads = new HashMap<>();
    
    public static Thread spawn(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        
        spawnedThreads.put(runnable, thread);
        return thread;
    }
    
    public static void despawn(Runnable runnable, boolean waitForEnd) {
        if (spawnedThreads.containsKey(runnable)) {
            if (waitForEnd) {
                ThreadUtil.join(runnable);
            }
            
            spawnedThreads.remove(runnable);
        }
    }
    
    public static void despawnAll(boolean waitForEnd) {
        Runnable[] runnables = spawnedThreads.keySet().toArray(new Runnable[] { });
        
        for (Runnable r : runnables) {
            if (waitForEnd) {
                ThreadUtil.join(r);
            }

            spawnedThreads.remove(r);
        }
    }
    
    public static void join(Runnable runnable) {
        if (spawnedThreads.containsKey(runnable)) {
            try {
                ((Thread)spawnedThreads.get(runnable)).join();
            } catch (InterruptedException ex) {
            }
        }
    }
    
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
        }
    }

}
