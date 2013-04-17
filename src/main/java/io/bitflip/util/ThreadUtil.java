package io.bitflip.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
        List<Runnable> runnableList = new LinkedList<>();
        runnableList.addAll(spawnedThreads.keySet());
        Collections.reverse(runnableList);
        
        Runnable[] runnables = runnableList.toArray(new Runnable[runnableList.size()]);
        runnableList.clear();
        
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
