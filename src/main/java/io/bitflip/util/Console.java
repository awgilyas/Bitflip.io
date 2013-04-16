package io.bitflip.util;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class Console implements Runnable {
    
    private static final Calendar calendar = Calendar.getInstance();
    private static Thread instanceThread = null;
    @Getter private static Console instance = null;

    @Getter @Setter private boolean running;
    private List<Character> lineBuffer;
    private Map<String, ConsoleCommand> registeredCommands;
    @Getter private boolean locked;
    
    public Console() {
        lineBuffer = new LinkedList<>();
        registeredCommands = new HashMap<>();
    }
    
    @Override
    public void run() {
        printPrompt();
        running = true;
        
        String line;
        char c;
        
        try {
            while (running && (c = (char)System.in.read()) != -1) {
                lineBuffer.add(c);
                if (c == '\n') {
                    if (!isLineBufferEmpty()) {
                        line = getLineBuffer().replace("\r", "").replace("\n", "");
                        lineBuffer.clear();
                    
                        if (line.length() > 0) {
                            handleCommands(parseCommand(line));
                        } else {
                        }
                    } else {
                        clearLine(false, true);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("\r\n" + ex);
        }
        
        running = false;
    }
    
    public boolean registerCommand(String name, ConsoleCommand command) {
        if (!registeredCommands.containsKey(name)) {
            registeredCommands.put(name, command);
        } else {
            return false;
        }
        
        return true;
    }
    
    protected Map<String, String[]> parseCommand(String line) {
        String cmd = line.toLowerCase();
        String[] args = new String[0];
        
        if (line.indexOf(" ") > 0) {
            cmd = line.substring(0, line.indexOf(" ")).toLowerCase();
            args = line.substring(line.indexOf(" ") + 1).split(" ");
        }
        
        return Collections.singletonMap(cmd, args);
    }
    
    protected void handleCommands(Map<String, String[]> commands) {
        String[] args;
        for (String cmd : commands.keySet()) {
            args = commands.get(cmd);

            if (registeredCommands.containsKey(cmd)) {
                ((ConsoleCommand)registeredCommands.get(cmd)).call(args);
            } else {
                System.out.println("Unknown command '" + cmd + "'");
            }
        }
    }
    
    public void clearLine(boolean clearBuffer, boolean clearPrompt) {
        for (int i = 0; i < lineBuffer.size(); i++) {
            System.out.print("\b");
        }
        
        if (clearBuffer) {
            lineBuffer.clear();
        }
        
        if (clearPrompt) {
            String prompt = getPrompt();
            for (int i = 0; i < prompt.length(); i++) {
                System.out.print("\b");
            }
        }
    }
    
    protected String getLineBuffer() {
        String line = "";
        for (Character chr : lineBuffer) {
            line += chr.charValue();
        }
        
        return line;
    }
    
    protected boolean isLineBufferEmpty() {
        return lineBuffer.isEmpty();
    }
    
    public String getPrompt() {
        return "";
    }
    
    public void printPrompt() {
        System.out.print(getPrompt());
    }
    
    protected void logMessage(Object sender, Object message, boolean noPrompt) {
        String formattedMessage = "";
        formattedMessage += Console.getTimestamp() + " ";
        
        if (sender != null) {
            formattedMessage += "[" + sender.getClass().getSimpleName() + "] ";
        }
        
        if (message instanceof Exception) {
            formattedMessage += ((Exception)message).getMessage();
        } else {
            formattedMessage += message.toString();
        }
        
        if (isLineBufferEmpty()) {
            clearLine(false, true);
            System.out.println(formattedMessage);
            if (!noPrompt) printPrompt();
        } else {
            String inputLine = getLineBuffer();
            clearLine(false, false);

            System.out.println(formattedMessage);
            if (!noPrompt) printPrompt();
            System.out.println(inputLine);
        }
    }
    
    public void lock() {
        locked = true;
    }
    
    public void unlock() {
        locked = false;
    }
    
    public static void start() {
        if (instance == null) {
            instance = new Console();
        }
        
        instanceThread = new Thread(instance);
        instanceThread.start();
        while (!instance.isRunning()) { }
    }
    
    public static void stopAsync() {
        if (instance == null) {
            return;
        }
        
        instance.setRunning(false);
    }
    
    public static boolean canLog() {
        if (instance == null) return false;
        if (!instance.running) return false;
        
        return true;
    }
    
    public static void log(Object sender, Object message) {
        Console.log(sender, message, false);
    }
    
    public static void log(Object sender, Object message, boolean noPrompt) {
        if (!Console.canLog()) {
            System.out.println("Console not running; cannot log!");
            return;
        }
        
        while (instance.isLocked()) { }
        
        instance.lock();
        instance.logMessage(sender, message, noPrompt);
        instance.unlock();
    }
    
    public static String getTimestamp() {
        int h, m, s;
        h = calendar.get(Calendar.HOUR_OF_DAY);
        m = calendar.get(Calendar.MINUTE);
        s = calendar.get(Calendar.SECOND);
        
        return (h > 10 ? h : "0" + h) + ":" + 
             (m > 10 ? m : "0" + m) + ":" + 
            (s > 10 ? s : "0" + s);
    }

}
