package de.niclasl.teamwar.api.events;

import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import net.minecraft.server.level.ServerLevel;

import java.time.Instant;
import java.util.UUID;

public abstract class Event {

    private final UUID id;
    protected final ServerLevel level;

    protected Event(UUID id, ServerLevel level) {
        this.id = id;
        this.level = level;
    }

    public UUID getId() {
        return id;
    }

    public ServerLevel getLevel() {
        return level;
    }

    protected ScheduledEventsSavedData getSavedData() {
        return ScheduledEventsSavedData.get(this.level.getServer().overworld());
    }

    protected PlannedEventEntry getData() {
        ScheduledEventsSavedData savedData = ScheduledEventsSavedData.get(level.getServer().overworld());

        return savedData.getEvent(id);
    }

    public Instant getEndTime() {
        PlannedEventEntry data = getData();

        return data == null ? null : Instant.ofEpochMilli(data.getEndTime());
    }

    protected void setStatus(CalendarEventStatus status) {
        getSavedData().updateStatus(id, status);
    }

    public abstract void start();

    public abstract void finish();

    public abstract void resume();
}