package de.niclasl.teamwar.api.events;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.api.calendar.CalendarClientCache;
import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class ScheduledEventsSavedData extends SavedData {

  public static final Codec<ScheduledEventsSavedData> CODEC = CompoundTag.CODEC.xmap(
          tag -> {
            ScheduledEventsSavedData instance = new ScheduledEventsSavedData();
            instance.read(tag);
            return instance;
          }, instance -> instance.write(new CompoundTag())
  );
  public static final SavedDataType<ScheduledEventsSavedData> TYPE =
          new SavedDataType<>(
                  Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "events"),
                  ScheduledEventsSavedData::new, CODEC
          );

  private final Map<UUID, PlannedEventEntry> events = new HashMap<>();

  public static ScheduledEventsSavedData get(MinecraftServer server) {
    return get(server.overworld());
  }

  public static ScheduledEventsSavedData get(ServerLevel level) {
    return level.getDataStorage().computeIfAbsent(TYPE);
  }

  public void read(CompoundTag tag) {
    events.clear();

    CompoundTag savedEvents = tag.getCompoundOrEmpty("events");

    for (String key : savedEvents.keySet()) {
      try {
        UUID id = UUID.fromString(key);
        CompoundTag eventTag = savedEvents.getCompoundOrEmpty(key);

        PlannedEventEntry event = readEvent(id, eventTag);
        events.put(id, event);
      } catch (IllegalArgumentException exception) {
        TeamWar.LOGGER.error(
                "Invalid event ID during loading: {}", key
        );
      }
    }
  }

  public CompoundTag write(CompoundTag tag) {
    CompoundTag eventTag = new CompoundTag();

    for (Map.Entry<UUID, PlannedEventEntry> mapEntry : events.entrySet()) {

      PlannedEventEntry event = mapEntry.getValue();

      CompoundTag data = new CompoundTag();
      data.putString("event_type", event.getEventType().toString());
      data.putLong("start_time", event.getStartTime());
      data.putLong("end_time", event.getEndTime());
      data.putString("status", event.getStatus().name());

      eventTag.put(mapEntry.getKey().toString(), data);
    }

    tag.put("events", eventTag);
    return tag;
  }

  private PlannedEventEntry readEvent(UUID id, CompoundTag tag) {
    PlannedEventEntry event = new PlannedEventEntry(id);

    event.setEventType(Identifier.parse(tag.getStringOr("event_type", "")));
    event.setStartTime(tag.getLongOr("start_time", 0));
    event.setEndTime(tag.getLongOr("end_time", 0));
    event.setStatus(CalendarEventStatus.valueOf(tag.getStringOr("status", "PLANNED")));

    return event;
  }

  public Collection<PlannedEventEntry> getEvents() {
    return List.copyOf(events.values());
  }

  public PlannedEventEntry getEvent(UUID id) {
    return events.get(id);
  }

  public void addEvent(PlannedEventEntry event) {
    events.put(event.getId(), event);
    setDirty();
  }

  public void updateStatus(UUID id, CalendarEventStatus status) {
    PlannedEventEntry event = events.get(id);

    if (event == null) {
      return;
    }

    if (event.getStatus() != status) {
      event.setStatus(status);
      setDirty();
    }

  }

  public List<CalendarClientCache.CalendarEventData> getCalendarEventData() {
    return events.entrySet()
            .stream()
            .map(entry -> {
              UUID id = entry.getKey();
              PlannedEventEntry event = entry.getValue();

              return new CalendarClientCache.CalendarEventData(
                      id,
                      event.getEventType(),
                      event.getStartTime(),
                      event.getEndTime(),
                      event.getStatus()
              );
            })
            .toList();
  }
}