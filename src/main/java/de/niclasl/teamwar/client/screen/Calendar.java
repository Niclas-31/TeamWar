package de.niclasl.teamwar.client.screen;

import de.niclasl.teamwar.api.calendar.CalendarClientCache;
import de.niclasl.teamwar.api.calendar.CalendarEventStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

public class Calendar extends Screen {

  private YearMonth displayedMonth;

  private static final String[] WEEKDAYS = {
          "gui.teamwar.calendar.weekday.monday",
          "gui.teamwar.calendar.weekday.tuesday",
          "gui.teamwar.calendar.weekday.wednesday",
          "gui.teamwar.calendar.weekday.thursday",
          "gui.teamwar.calendar.weekday.friday",
          "gui.teamwar.calendar.weekday.saturday",
          "gui.teamwar.calendar.weekday.sunday"
  };

  public Calendar() {
    super(Component.translatable("gui.teamwar.calendar"));

    this.displayedMonth = YearMonth.now();
  }

  @Override
  protected void init() {
    super.init();

    int centerX = width / 2;
    int yearWidth = font.width(String.valueOf(displayedMonth.getYear()));
    int spacing = 10;
    int buttonSize = 15;
    int buttonY = 15;

    int leftX = centerX - yearWidth / 2 - spacing - buttonSize - 10;
    int rightX = centerX + yearWidth / 2 + spacing + 10;

    addRenderableWidget(
            Button.builder(
                    Component.literal("<"),
                            _ -> displayedMonth = displayedMonth.minusYears(1)
                    )
                    .bounds(leftX, buttonY, buttonSize, buttonSize)
                    .build()
    );

    addRenderableWidget(
            Button.builder(
                    Component.literal(">"),
                            _ -> displayedMonth = displayedMonth.plusYears(1)
                    )
                    .bounds(rightX, buttonY, buttonSize, buttonSize)
                    .build()
    );

    buttonY += 20;

    addRenderableWidget(
            Button.builder(
                            Component.literal("<"),
                            _ -> displayedMonth = displayedMonth.minusMonths(1)
                    )
                    .bounds(leftX, buttonY, buttonSize, buttonSize)
                    .build()
    );

    addRenderableWidget(
            Button.builder(
                            Component.literal(">"),
                            _ -> displayedMonth = displayedMonth.plusMonths(1)
                    )
                    .bounds(rightX, buttonY, buttonSize, buttonSize)
                    .build()
    );

    addRenderableWidget(
            Button.builder(
                    Component.translatable("gui.teamwar.plan"),
                    _ -> Minecraft.getInstance().gui.setScreen(new PlanerScreen())
            ).bounds(
                    10,
                    this.height - 25,
                    100,
                    20
            ).build()
    );

    addRenderableWidget(
            Button.builder(
                    Component.translatable("gui.teamwar.cancel"),
                    _ -> {
                      // TODO: Auswahl eines geplanten Events zum Absagen öffnen
                    }
            ).bounds(
                    this.width - 110,
                    this.height - 25,
                    100,
                    20
            ).build()
    );
  }

  @Override
  public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    super.extractBackground(graphics, mouseX, mouseY, a);

    int centerX = width / 2;

    graphics.centeredText(
            this.font,
            String.valueOf(displayedMonth.getYear()),
            centerX,
            20,
            0xFFFFFFFF
    );

    graphics.centeredText(this.font, getMonthName(displayedMonth), centerX, 40, 0xFFFFFFFF);

    int cellWidth = 25;
    int cellHeight = 20;
    int columns = 7;
    int rows = 7;

    int calendarWidth = cellWidth * columns;
    int calendarHeight = cellHeight * rows;

    int startX = width / 2 - calendarWidth / 2;
    int startY = 60;

    for (int i = 0; i < columns; i++) {
      graphics.centeredText(
              this.font,
              Component.translatable(WEEKDAYS[i]),
              startX + i * cellWidth + cellWidth / 2,
              startY + 5,
              0xFFFFFFFF
      );
    }

    LocalDate today = LocalDate.now();

    int firstDayOffset = displayedMonth.atDay(1).getDayOfWeek().getValue() - 1;

    int daysInMonth = displayedMonth.lengthOfMonth();

    for (int day = 1; day <= daysInMonth; day++) {
      int index = firstDayOffset + day - 1;

      int column = index % columns;
      int row = index / columns + 1;

      int x = startX + column * cellWidth;
      int y = startY + row * cellHeight;

      LocalDate date = displayedMonth.atDay(day);

      if (date.equals(today)) {
        graphics.fill(
                x,
                y,
                x + cellWidth,
                y + cellHeight,
                0x80FF0000
        );
      }

      graphics.centeredText(
              this.font,
              String.valueOf(day),
              x + cellWidth / 2,
              y + 5,
              0xFFFFFFFF
      );

      renderDayEvents(graphics, date, x, y, cellWidth, cellHeight);
    }

    int gridColor = 0xFF777777;

    for (int column = 0; column <= columns; column++) {
      int x = startX + column * cellWidth;

      graphics.fill(
              x, startY,
              x + 1, startY + calendarHeight,
              gridColor
      );
    }

    for (int row = 0; row <= rows; row++) {
      int y = startY + row * cellHeight;

      graphics.fill(
              startX, y,
              startX + calendarWidth, y + 1,
              gridColor
      );
    }

    renderLegend(graphics, centerX, startY + calendarHeight + 12);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  private Component getMonthName(YearMonth month) {
    return Component.translatable(
            "gui.teamwar.calendar.month." + month.getMonth().getValue()
    );
  }

  private void renderLegend(GuiGraphicsExtractor graphics, int centerX, int y) {
    int boxSize = 7;
    int gap = 5;
    int itemGap = 14;

    String[] keys = {
            "gui.teamwar.calendar.planned",
            "gui.teamwar.calendar.running",
            "gui.teamwar.calendar.finished",
            "gui.teamwar.calendar.cancelled"
    };

    int[] colors = {
            0xFF0000FF,
            0xFF00FF00,
            0xFFC8C8C8,
            0xFFA00000
    };

    int totalWidth = 0;

    for (String key : keys) {
      totalWidth += boxSize + gap + font.width(
              Component.translatable(key).getString()
      ) + itemGap;
    }

    totalWidth -= itemGap;

    int x = centerX - totalWidth / 2;

    for (int i = 0; i < keys.length; i++) {
      String label = Component.translatable(keys[i]).getString();

      graphics.fill(
              x,
              y + 2,
              x + boxSize,
              y + 2 + boxSize,
              colors[i]
      );

      x += boxSize + gap;

      graphics.text(
              this.font,
              label,
              x,
              y,
              0xFFFFFFFF
      );

      x += font.width(label) + itemGap;
    }
  }

  private void renderDayEvents(
          GuiGraphicsExtractor graphics,
          LocalDate date,
          int x,
          int y,
          int cellWidth,
          int cellHeight
  ) {
    List<CalendarClientCache.CalendarEventData> events =
            CalendarClientCache.getEvents().stream()
                    .filter(event -> occursOnDay(event, date))
                    .toList();

    int maxBars = 3;
    int barHeight = 3;
    int gap = 2;
    int startY = y + 13;

    for (int i = 0; i < Math.min(events.size(), maxBars); i++) {
      CalendarClientCache.CalendarEventData event = events.get(i);

      int barY = startY + i * (barHeight + gap);

      graphics.fill(
              x + 3,
              barY,
              x + cellWidth - 3,
              barY + barHeight,
              getStatusColor(event.status())
      );
    }

    if (events.size() > maxBars) {
      graphics.text(
              font,
              "+" + (events.size() - maxBars),
              x + 2,
              y + cellHeight - 8,
              0xFFFFFFFF
      );
    }
  }

  private boolean occursOnDay(
          CalendarClientCache.CalendarEventData event,
          LocalDate day
  ) {
    ZoneId zone = ZoneId.systemDefault();

    long dayStart = day.atStartOfDay(zone)
            .toInstant()
            .toEpochMilli();

    long nextDayStart = day.plusDays(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli();

    return event.startTime() < nextDayStart
            && event.endTime() > dayStart;
  }

  private int getStatusColor(CalendarEventStatus status) {
    return switch (status) {
      case PLANNED -> 0xFF0000FF;
      case RUNNING -> 0xFF00FF00;
      case FINISHED -> 0xFFC8C8C8;
      case CANCELLED -> 0xFFA00000;
    };
  }
}