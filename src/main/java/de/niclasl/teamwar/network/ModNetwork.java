package de.niclasl.teamwar.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class ModNetwork {

  public static void register(RegisterPayloadHandlersEvent event) {
    var registrar = event.registrar("1");

    registrar.playToServer(
            CreateEventPayload.TYPE,
            CreateEventPayload.STREAM_CODEC,
            CreateEventPayload::handle
    );
    registrar.playToClient(
            SyncCalendarPayload.TYPE,
            SyncCalendarPayload.STREAM_CODEC,
            SyncCalendarPayload::handle
    );
  }
}