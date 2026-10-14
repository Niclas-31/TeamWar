package de.niclasl.teamwar.network;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.api.calendar.CalendarClientCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record SyncCalendarPayload(
        List<CalendarClientCache.CalendarEventData> events
) implements CustomPacketPayload {

  public static final Type<SyncCalendarPayload> TYPE =
          new Type<>(Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "sync_calendar"));

  public static final StreamCodec<ByteBuf, SyncCalendarPayload> STREAM_CODEC =
          CalendarClientCache.CalendarEventData.STREAM_CODEC
                  .apply(ByteBufCodecs.list(4096))
                  .map(SyncCalendarPayload::new, SyncCalendarPayload::events);

  public static void handle(SyncCalendarPayload msg, IPayloadContext ctx) {
    if (ctx.flow().isClientbound()) {
      ctx.enqueueWork(() -> CalendarClientCache.replaceAll(msg.events()));
    }
  }

  @Override
  public @NonNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}