package de.niclasl.teamwar.client.screen;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.menu.UpgradeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {

    private static final Identifier UPGRADES_LOCATION =
            Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "textures/gui/container/upgrades.png");

    public UpgradeScreen(UpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 131);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int xo = (this.width -  this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, UPGRADES_LOCATION, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
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