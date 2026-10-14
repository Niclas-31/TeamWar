package de.niclasl.teamwar.menu;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.classes.ClassManager;
import de.niclasl.teamwar.classes.ClassUpgrade;
import de.niclasl.teamwar.classes.PlayerClass;
import de.niclasl.teamwar.classes.PlayerUpgrades;
import de.niclasl.teamwar.economy.MoneyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMenu extends AbstractContainerMenu {

    private final Container container;
    private final PlayerUpgrades playerUpgrades;
    private final ClassManager classManager;
    private final MoneyManager moneyManager;

    public UpgradeMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.UPGRADE_MENU.get(), containerId);

        this.playerUpgrades = new PlayerUpgrades(inventory.player.getUUID());
        this.classManager = null;
        this.moneyManager = null;

        this.container = new SimpleContainer(9);

        for (int i = 0; i < 9; i++) {
            addSlot(new UpgradeSlot(
                    container,
                    i,
                    8 + i * 18,
                    18
            ));
        }
    }

    public UpgradeMenu(int containerId, PlayerUpgrades playerUpgrades, ClassManager classManager) {
        super(ModMenuTypes.UPGRADE_MENU.get(), containerId);

        this.playerUpgrades = playerUpgrades;
        this.classManager = classManager;
        this.moneyManager = TeamWar.getMoneyManager();

        this.container = new SimpleContainer(9);

        setupItems();

        for (int i = 0; i < 9; i++) {
            addSlot(new UpgradeSlot(
                    container,
                    i,
                    8 + i * 18,
                    20
            ));
        }
    }

    private void setupItems() {

        for (int i = 0; i < 9; i++) {
            container.setItem(i, ItemStack.EMPTY);
        }

        if (playerUpgrades == null) {
            return;
        }

        PlayerClass playerClass = classManager.getClass(playerUpgrades.getPlayerId());

        if (playerClass == null) {
            return;
        }

        List<ClassUpgrade> upgrades = playerClass.getAvailableUpgrades();

        for (int i = 0; i < Math.min(upgrades.size(), 9); i++) {

            ClassUpgrade upgrade = upgrades.get(i);

            int level = playerUpgrades.getLevel(upgrade);

            int percent = level * upgrade.getPercentPerLevel();

            ItemStack stack = new ItemStack(Items.BOOK);

            stack.set(DataComponents.CUSTOM_NAME, Component.literal(upgrade.name() + " (" + percent + "%)"));

            List<Component> lore = new ArrayList<>();

            lore.add(Component.translatable("gui.teamwar.upgrade.level", level));

            lore.add(Component.translatable("gui.teamwar.upgrade.next", upgrade.getPercentPerLevel()));

            lore.add(Component.translatable("gui.teamwar.upgrade.cost", MoneyManager.formatMoney(upgrade.getCost(level))));

            stack.set(DataComponents.LORE, new ItemLore(lore));

            container.setItem(i, stack);
        }
    }

    @Override
    public void clicked(int slotId, int button, @NonNull ContainerInput input, @NonNull Player player) {
        if (slotId >= 0 && slotId < 9) {

            if (player instanceof ServerPlayer serverPlayer) {
                handleUpgradeClick(
                        serverPlayer,
                        slotId
                );
            }

            return;
        }

        super.clicked(slotId, button, input, player);
    }

    private void handleUpgradeClick(ServerPlayer player, int slotId) {
        if (playerUpgrades == null) {
            return;
        }

        List<ClassUpgrade> available = classManager.getClass(player.getUUID()).getAvailableUpgrades();

        if (slotId >= available.size()) {
            return;
        }

        ClassUpgrade upgrade = available.get(slotId);

        purchaseUpgrade(player, upgrade);
    }

    private void purchaseUpgrade(ServerPlayer player, ClassUpgrade upgrade) {
        int level = playerUpgrades.getLevel(upgrade);

        int cost = upgrade.getCost(level);

        int totalPercent = playerUpgrades.getTotalPercent(upgrade);

        if (totalPercent >= 100) {

            player.sendSystemMessage(Component.translatable("message.upgrade.maximum")
                    .withStyle(ChatFormatting.GREEN)
            );

            return;
        }

        String teamName = TeamWar.getTeamManager().getTeamOfPlayer(player.getUUID());

        if (teamName == null) {

            player.sendSystemMessage(Component.translatable("message.not_in_team")
                    .withStyle(ChatFormatting.RED)
            );

            return;
        }

        double money = moneyManager.getTeamMoney(teamName);

        if (money < cost) {

            player.sendSystemMessage(Component.translatable("message.upgrade.deny", cost));

            return;
        }

        moneyManager.setTeamMoney(teamName, money - cost);

        classManager.upgradePlayer(player.getUUID(), upgrade);

        PlayerClass playerClass = classManager.getClass(player.getUUID());

        float maxHealth = playerClass.getMaxHealth(playerUpgrades);

        var attribute = player.getAttribute(Attributes.MAX_HEALTH);

        if (attribute != null) {
            attribute.setBaseValue(maxHealth);

            if (player.getHealth() > maxHealth) {
                player.setHealth(maxHealth);
            }
        }

        player.sendSystemMessage(
                Component.translatable(
                        "message.upgrade.successful",
                        upgrade.getDisplayName(),
                        playerUpgrades.getLevel(upgrade
                        )
                )
        );

        refreshItems();
    }

    private void refreshItems() {

        for (int i = 0; i < container.getContainerSize(); i++) {
            container.setItem(i, ItemStack.EMPTY);
        }

        setupItems();

        broadcastChanges();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static class UpgradeSlot extends Slot {

        public UpgradeSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@NonNull Player player) {
            return false;
        }
    }
}