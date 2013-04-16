package io.bitflip.util;

import lombok.Getter;

public abstract class ConsoleCommand {
    
    @Getter private String name;
    
    protected ConsoleCommand(String name) {
        this.name = name;
    }

    public abstract void call(String[] args);
    
}
