package de.niclasl.teamwar.api.events;

import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.time.Instant;
import java.util.UUID;

public abstract class Event {

    private final UUID id;
    protected final MinecraftServer server;

    protected Event(UUID id, ServerLevel level) {
        this.id = id;
        this.server = level.getServer();
    }

    public UUID getId() {
        return id;
    }

    protected ScheduledEventsSavedData getSavedData() {
        return server.getDataStorage().computeIfAbsent(ScheduledEventsSavedData.TYPE);
    }

    protected PlannedEventEntry getData() {
        ScheduledEventsSavedData savedData = getSavedData();

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