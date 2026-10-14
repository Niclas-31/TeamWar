package de.niclasl.teamwar.client.util;

import de.niclasl.teamwar.client.screen.Calendar;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT)
public class ModKeyMappings {
  public static final KeyMapping CALENDAR = new KeyMapping("key.teamwar.calendar", GLFW.GLFW_KEY_K, KeyMapping.Category.GAMEPLAY) {
    private boolean isDownOld = false;

    @Override
    public void setDown(boolean isDown) {
      super.setDown(isDown);
      if (isDownOld != isDown && isDown) {
        Minecraft.getInstance().gui.setScreen(new Calendar());
      }
      isDownOld = isDown;
    }
  };

  @SubscribeEvent
  public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(CALENDAR);
  }

  @SubscribeEvent
  public static void onClientTick(ClientTickEvent.Post event) {
    if (Minecraft.getInstance().gui.screen() == null) {
      CALENDAR.consumeClick();
    }
  }
}