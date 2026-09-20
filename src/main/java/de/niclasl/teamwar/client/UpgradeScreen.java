package de.niclasl.teamwar.client;

import de.niclasl.teamwar.menu.UpgradeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {

    public UpgradeScreen(UpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xFF202020
        );

        for (int i = 0; i < 9; i++) {
            int x = this.leftPos + 8 + i * 18;
            int y = this.topPos + 20;

            graphics.fill(
                    x,
                    y,
                    x + 18,
                    y + 18,
                    0xFF404040
            );
        }
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);

        graphics.text(
                this.font,
                this.title,
                8,
                6,
                0xFFFFFFFF,
                false
        );
    }
}