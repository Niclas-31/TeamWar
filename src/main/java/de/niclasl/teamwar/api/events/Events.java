package de.niclasl.teamwar.api.events;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.api.TWRegistry;
import net.minecraft.resources.Identifier;

public class Events {
    public static final TWRegistry<EventType> EVENTS = new TWRegistry<>();

    public static void register() {
        EVENTS.register(
                Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "shadow_knight"),
                new EventType(ShadowKnight::new));
    }

    public static EventType getEventType(Identifier id) {
        return EVENTS.get(id);
    }
}