package de.niclasl.teamwar.api.events;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class ShadowKnight extends Event {

  public ShadowKnight(UUID id, ServerLevel level) {
    super(id, level);

    TeamWar.LOGGER.info("UUID: {}", id);
  }

  @Override
  public void start() {
    if (getData() == null || getData().getStatus() != CalendarEventStatus.PLANNED) {
      return;
    }

    setStatus(CalendarEventStatus.RUNNING);
  }

  @Override
  public void finish() {
    if (getData() == null || getData().getStatus() != CalendarEventStatus.RUNNING) {
      return;
    }

    setStatus(CalendarEventStatus.FINISHED);
  }

  @Override
  public void resume() {

  }
}