package de.niclasl.teamwar.network;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import de.niclasl.teamwar.api.events.PlannedEventEntry;
import de.niclasl.teamwar.api.events.ScheduledEventsSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record CreateEventPayload(
        String eventType,
        long startTime,
        long endTime
) implements CustomPacketPayload {

  public static final Type<CreateEventPayload> TYPE =
          new Type<>(Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "create_event"));

  public static final StreamCodec<ByteBuf, CreateEventPayload> STREAM_CODEC =
          StreamCodec.composite(
                  ByteBufCodecs.STRING_UTF8,
                  CreateEventPayload::eventType,
                  ByteBufCodecs.VAR_LONG,
                  CreateEventPayload::startTime,
                  ByteBufCodecs.VAR_LONG,
                  CreateEventPayload::endTime,
                  CreateEventPayload::new
          );

  public static void handle(CreateEventPayload msg, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      if (!(ctx.player().level() instanceof ServerLevel level)) {
        return;
      }

      if (msg.endTime <= msg.startTime) {
        return;
      }

      ScheduledEventsSavedData savedData = level.getServer().getDataStorage().computeIfAbsent(ScheduledEventsSavedData.TYPE);

      UUID eventId = UUID.randomUUID();

      PlannedEventEntry entry = new PlannedEventEntry(eventId);

      entry.setEventType(Identifier.parse(msg.eventType));
      entry.setStartTime(msg.startTime);
      entry.setEndTime(msg.endTime);
      entry.setStatus(CalendarEventStatus.PLANNED);

      savedData.addEvent(entry);
    });
  }

  @Override
  public @NonNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}