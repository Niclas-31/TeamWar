package de.niclasl.teamwar.api.events;

import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class EventType {
  private final EventFactory factory;

  public EventType(EventFactory factory) {
    this.factory = factory;
  }

  public Event create(UUID id, ServerLevel level) {
    return factory.create(id, level);
  }
}