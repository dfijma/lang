package net.fijma;

import net.fijma.value.IntValue;

import java.util.HashMap;
import java.util.Map;

public class Memory {


    private final Map<String, IntValue> memory = new HashMap<>();

    public IntValue get(String key) {
        return memory.get(key);
    }

    public void set(String key, IntValue value) {
        memory.put(key, value);
    }
}
