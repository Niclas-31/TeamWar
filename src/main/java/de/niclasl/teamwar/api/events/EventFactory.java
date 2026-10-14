package de.niclasl.teamwar.api.events;

import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

@FunctionalInterface
public interface EventFactory {
    Event create(UUID id, ServerLevel level);
}