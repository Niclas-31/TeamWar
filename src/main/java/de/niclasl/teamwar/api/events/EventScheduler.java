package de.niclasl.teamwar.api.events;

import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventScheduler {

  private static final Map<UUID, Event> ACTIVE_EVENTS = new HashMap<>();

  private static MinecraftServer currentServer;
  private static int tickCounter;

  private EventScheduler() {}

  @SubscribeEvent
  public static void onServerTick(ServerTickEvent.Post event) {
    MinecraftServer server = event.getServer();

    if (currentServer != server) {
      ACTIVE_EVENTS.clear();
      currentServer = server;
      tickCounter = 0;
      restoreRunningEvents(server);
    }

    if (++tickCounter < 20) {
      return;
    }

    tickCounter = 0;
    tick(server);
  }

  private static void tick(MinecraftServer server) {
    long now = System.currentTimeMillis();

    var savedData = server.getDataStorage().computeIfAbsent(ScheduledEventsSavedData.TYPE);

    if (!ACTIVE_EVENTS.isEmpty()) {
      var iterator = ACTIVE_EVENTS.entrySet().iterator();
      var entry = iterator.next();

      Event activeEvent = entry.getValue();

      if (now >= activeEvent.getEndTime().toEpochMilli()) {
        activeEvent.finish();
        savedData.updateStatus(entry.getKey(), CalendarEventStatus.FINISHED);

        iterator.remove();
      }
    }

    if (!ACTIVE_EVENTS.isEmpty()) {
      return;
    }

    PlannedEventEntry next = savedData.getEvents().stream()
            .filter(e -> e.getStatus() == CalendarEventStatus.PLANNED)
            .filter(e -> e.getStartTime() <= now)
            .min(Comparator.comparingLong(PlannedEventEntry::getStartTime))
            .orElse(null);

    if (next == null) {
      return;
    }

    if (now >= next.getEndTime()) {
      savedData.updateStatus(next.getId(), CalendarEventStatus.FINISHED);
      return;
    }

    Identifier eventTypeId = next.getEventType();
    EventType eventType = Events.getEventType(eventTypeId);

    if (eventType == null) {
      return;
    }

    ServerLevel level = server.overworld();

    Event instance = eventType.create(next.getId(), level);
    instance.start();

    if (savedData.getEvent(next.getId()).getStatus() == CalendarEventStatus.RUNNING) {
      ACTIVE_EVENTS.put(next.getId(), instance);
    }
  }

  private static void restoreRunningEvents(MinecraftServer server) {
    var savedData = server.getDataStorage().computeIfAbsent(ScheduledEventsSavedData.TYPE);
    long now = System.currentTimeMillis();

    for (PlannedEventEntry entry : savedData.getEvents()) {
      if (entry.getStatus() != CalendarEventStatus.RUNNING) {
        continue;
      }

      if (now >= entry.getEndTime()) {
        savedData.updateStatus(entry.getId(), CalendarEventStatus.FINISHED);
        continue;
      }

      if (!ACTIVE_EVENTS.isEmpty()) {
        savedData.updateStatus(entry.getId(), CalendarEventStatus.PLANNED);
        continue;
      }

      EventType type = Events.getEventType(entry.getEventType());

      if (type == null) {
        continue;
      }

      Event instance = type.create(entry.getId(), server.overworld());
      ACTIVE_EVENTS.put(entry.getId(), instance);
      instance.resume();
    }
  }
}