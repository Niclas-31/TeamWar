package de.niclasl.teamwar.api;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TWRegistry<T> {

  private final Map<Identifier, T> values = new LinkedHashMap<>();

  public void register(Identifier id, T value) {
    if (values.containsKey(id)) {
      throw new IllegalStateException("Duplicate registry id: " + id);
    }

    values.put(id, value);
  }

  public T get(Identifier id) {
    return values.get(id);
  }

  public Collection<T> values() {
    return values.values();
  }

  public boolean contains(Identifier id) {
    return values.containsKey(id);
  }

  public Collection<Identifier> ids() {
    return values.keySet();
  }
}