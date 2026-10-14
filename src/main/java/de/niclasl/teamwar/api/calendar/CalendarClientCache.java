package de.niclasl.teamwar.api.calendar;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CalendarClientCache {

  private static final Map<UUID, CalendarEventData> EVENTS = new HashMap<>();

  private CalendarClientCache() {}

  public static void replaceAll(Collection<CalendarEventData> events) {
    EVENTS.clear();

    for (CalendarEventData event : events) {
      EVENTS.put(event.id(), event);
    }
  }

  public static Collection<CalendarEventData> getEvents() {
    return java.util.List.copyOf(EVENTS.values());
  }

  public static CalendarEventData getEvent(UUID id) {
    return EVENTS.get(id);
  }

  public static void clear() {
    EVENTS.clear();
  }

  public record CalendarEventData(
          UUID id,
          Identifier eventType,
          long startTime,
          long endTime,
          CalendarEventStatus status
  ) {
    private static final StreamCodec<ByteBuf, UUID> UUID_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    UUID::fromString,
                    UUID::toString
            );

    private static final StreamCodec<ByteBuf, Identifier> IDENTIFIER_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    Identifier::parse,
                    Identifier::toString
            );

    private static final StreamCodec<ByteBuf, CalendarEventStatus> STATUS_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    CalendarEventStatus::valueOf,
                    Enum::name
            );

    public static final StreamCodec<ByteBuf, CalendarEventData> STREAM_CODEC =
            StreamCodec.composite(
                    UUID_CODEC,
                    CalendarEventData::id,

                    IDENTIFIER_CODEC,
                    CalendarEventData::eventType,

                    ByteBufCodecs.VAR_LONG,
                    CalendarEventData::startTime,

                    ByteBufCodecs.VAR_LONG,
                    CalendarEventData::endTime,

                    STATUS_CODEC,
                    CalendarEventData::status,

                    CalendarEventData::new
            );
  }
}