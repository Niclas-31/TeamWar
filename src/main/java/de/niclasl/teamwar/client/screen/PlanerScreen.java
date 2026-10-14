package de.niclasl.teamwar.client.screen;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.api.events.Events;
import de.niclasl.teamwar.network.CreateEventPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PlanerScreen extends Screen {

  private static final DateTimeFormatter FORMATTER =
          DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private CycleButton<Identifier> eventTypeButton;
  private EditBox startTimeBox;
  private EditBox endTimeBox;

  public PlanerScreen() {
    super(Component.translatable("gui.teamwar.planer"));
  }

  @Override
  protected void init() {
    int centerX = this.width / 2;
    int startY = this.height / 2 - 80;
    int fieldWidth = 220;
    int fieldX = centerX - fieldWidth / 2;

    List<Identifier> eventTypes = Events.EVENTS.ids()
            .stream()
            .toList();

    if (!eventTypes.isEmpty()) {
      eventTypeButton = CycleButton
              .builder(this::getEventTypeName, eventTypes.getFirst())
              .withValues(eventTypes)
              .create(
                      fieldX,
                      startY,
                      fieldWidth,
                      20,
                      Component.translatable("gui.teamwar.planer.event_type")
              );

      addRenderableWidget(eventTypeButton);
    }

    startTimeBox = new EditBox(
            font, fieldX, startY + 45,
            fieldWidth, 20,
            Component.translatable("gui.teamwar.planer.start_time")
    );
    startTimeBox.setHint(Component.literal("dd.MM.yyyy HH:mm"));
    addRenderableWidget(startTimeBox);

    endTimeBox = new EditBox(
            font, fieldX, startY + 90,
            fieldWidth, 20,
            Component.translatable("gui.teamwar.planer.end_time")
    );
    endTimeBox.setHint(Component.literal("dd.MM.yyyy HH:mm"));
    addRenderableWidget(endTimeBox);

    addRenderableWidget(
            Button.builder(
                    Component.translatable("gui.teamwar.plan"),
                    _ -> confirmPlan()
            ).bounds(
                    centerX - 110,
                    startY + 135,
                    105,
                    20
            ).build()
    );

    addRenderableWidget(
            Button.builder(
                    Component.translatable("gui.teamwar.cancel"),
                    _ -> onClose()
            ).bounds(
                    centerX + 5,
                    startY + 135,
                    105,
                    20
            ).build()
    );
  }

  private Component getEventTypeName(Identifier id) {
    String name = id.getPath().replace('_', ' ');

    return Component.literal(name.substring(0, 1).toUpperCase() + name.substring(1));
  }

  private void confirmPlan() {
    if (eventTypeButton == null) {
      return;
    }

    try {
      LocalDateTime start = LocalDateTime.parse(startTimeBox.getValue(), FORMATTER);

      LocalDateTime end = LocalDateTime.parse(endTimeBox.getValue(), FORMATTER);

      if (!end.isAfter(start)) {
        TeamWar.LOGGER.warn("Invalid end time!");
        return;
      }

      Identifier selectedType = eventTypeButton.getValue();

      long startMillis = start
              .atZone(ZoneId.systemDefault())
              .toInstant()
              .toEpochMilli();

      long endMillis = end
              .atZone(ZoneId.systemDefault())
              .toInstant()
              .toEpochMilli();

      ClientPacketDistributor.sendToServer(
              new CreateEventPayload(
                      selectedType.toString(),
                      startMillis,
                      endMillis
              )
      );

      onClose();
    } catch (DateTimeParseException exception) {
      TeamWar.LOGGER.error(exception.getMessage());
    }
  }

  @Override
  public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    super.extractBackground(graphics, mouseX, mouseY, a);

    int centerX = this.width / 2;
    int startY = this.height / 2 - 80;

    graphics.centeredText(
            this.font,
            Component.translatable("gui.teamwar.planer.start_time"),
            centerX,
            startY + 31,
            0xFFFFFFFF
    );

    graphics.centeredText(
            this.font,
            Component.translatable("gui.teamwar.planer.end_time"),
            centerX,
            startY + 76,
            0xFFFFFFFF
    );
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}