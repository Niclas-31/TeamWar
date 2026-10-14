package de.niclasl.teamwar.api.events;

import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class PlannedEventEntry {

  private final UUID id;
  private Identifier eventType;
  private long startTime;
  private long endTime;
  private CalendarEventStatus status;

  public PlannedEventEntry(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public void setEventType(Identifier eventType) {
    this.eventType = eventType;
  }

  public Identifier getEventType() {
    return eventType;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setStatus(CalendarEventStatus status) {
    this.status = status;
  }

  public CalendarEventStatus getStatus() {
    return status;
  }
}