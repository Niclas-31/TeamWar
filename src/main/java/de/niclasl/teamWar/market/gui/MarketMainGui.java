package de.niclasl.teamWar.market.gui;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.market.manager.MarketManager;
import de.niclasl.teamWar.market.manager.MarketManager.Category;
import de.niclasl.teamWar.market.manager.MarketManager.MarketItem;
import de.niclasl.teamWar.money.manager.MoneyManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MarketMainGui implements Listener {

    private static MarketManager marketManager;
    private static final String title = "§6Market: ";
    private final Set<UUID> clickCooldown = new HashSet<>();

    private static final List<Integer> categorySlots = Arrays.asList(0, 9, 18, 27, 36, 45);

    private static final List<Integer> borderSlots = Arrays.asList(
            1, 2, 3, 4, 5, 6, 7, 8,
            10, 17, 19, 26, 28, 35, 37, 44,
            46, 47, 49, 51, 52, 53
    );

    private static final List<Integer> itemSlots = Arrays.asList(
            11, 12, 13, 14, 15, 16,
            20, 21, 22, 23, 24, 25,
            29, 30, 31, 32, 33, 34,
            38, 39, 40, 41, 42, 43
    );

    private final Map<UUID, Category> currentCategory = new HashMap<>();
    private final Map<UUID, Integer> currentPage = new HashMap<>();
    private final TeamWar plugin;
    private static MarketMainGui instance;

    public MarketMainGui(TeamWar plugin, MarketManager marketManager) {
        instance = this;
        this.plugin = plugin;
        MarketMainGui.marketManager = marketManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void open(Player player, Category category, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Market");

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = blackPane.getItemMeta();
        assert meta != null;
        meta.setDisplayName(" ");
        blackPane.setItemMeta(meta);
        for (int slot : borderSlots) {
            inv.setItem(slot, blackPane);
        }

        Category[] categories = Category.values();
        for (int i = 0; i < categories.length && i < categorySlots.size(); i++) {
            inv.setItem(categorySlots.get(i), marketManager.getCategoryItem(categories[i]));
        }

        fillCategoryItems(inv, category, page);

        int totalItems = marketManager.getItemsByCategory(category).size();
        int totalPages = (int) Math.ceil((double) totalItems / itemSlots.size());

        if (page > 0) {
            inv.setItem(48, createButton("§a← Previous Page"));
        }
        if (page < totalPages - 1) {
            inv.setItem(50, createButton("§aNext Page →"));
        }

        instance.currentCategory.put(player.getUniqueId(), category);
        instance.currentPage.put(player.getUniqueId(), page);

        player.openInventory(inv);
    }

    private static void fillCategoryItems(Inventory inv, Category category, int page) {
        Map<String, MarketItem> itemsMap = marketManager.getItemsByCategory(category);
        List<MarketItem> items = new ArrayList<>(itemsMap.values());

        items.sort((a, b) -> {
            String nameA = a.getDisplayName().toLowerCase();
            String nameB = b.getDisplayName().toLowerCase();

            if (category == Category.COMBAT || category == Category.TOOLS || category == Category.ARMOR) {
                int tierA = getMaterialTier(a.getMaterial());
                int tierB = getMaterialTier(b.getMaterial());
                if (tierA != tierB) {
                    return Integer.compare(tierA, tierB);
                }
            }
            return nameA.compareTo(nameB);
        });

        int start = page * itemSlots.size();
        int end = Math.min(start + itemSlots.size(), items.size());

        int index = 0;
        for (int i = start; i < end; i++) {
            inv.setItem(itemSlots.get(index), items.get(i).toItemStack());
            index++;
        }
    }

    private static ItemStack createButton(String name) {
        ItemStack button = new ItemStack(Material.ARROW);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            button.setItemMeta(meta);
        }
        return button;
    }

    private static int getMaterialTier(Material mat) {
        String name = mat.name();
        if (name.startsWith("WOODEN")) return 1;
        if (name.startsWith("STONE")) return 2;
        if (name.startsWith("LEATHER")) return 3;
        if (name.startsWith("CHAINMAIL")) return 4;
        if (name.startsWith("IRON")) return 5;
        if (name.startsWith("DIAMOND")) return 6;
        if (name.startsWith("NETHERITE")) return 7;
        return 999;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(title)) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;

        e.setCancelled(true);
        UUID uuid = player.getUniqueId();

        if (!clickCooldown.add(uuid)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> clickCooldown.remove(uuid), 1);

        if (e.getClick().isShiftClick() || e.getClick().isKeyboardClick() || e.getClick().isCreativeAction()) return;

        int slot = e.getRawSlot();
        MarketManager marketManager = plugin.getMarketManager();

        if (categorySlots.contains(slot)) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta() && Objects.requireNonNull(clicked.getItemMeta()).hasDisplayName()) {
                try {
                    Category category = Category.valueOf(clicked.getItemMeta().getDisplayName());
                    open(player, category, 0);
                } catch (IllegalArgumentException ignored) {}
            }
            return;
        }

        if (slot == 48 || slot == 50) {
            Category category = currentCategory.get(uuid);
            int page = currentPage.getOrDefault(uuid, 0);
            int totalItems = marketManager.getItemsByCategory(category).size();
            int totalPages = (int) Math.ceil((double) totalItems / itemSlots.size());

            if (slot == 48 && page > 0) open(player, category, page - 1);
            if (slot == 50 && page < totalPages - 1) open(player, category, page + 1);
            return;
        }

        if (!itemSlots.contains(slot)) return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        MarketItem marketItem = marketManager.getItemsByCategory(currentCategory.get(uuid))
                .values()
                .stream()
                .filter(mi -> {
                    if (mi.getMaterial() == Material.ENCHANTED_BOOK && mi.getEnchantment() != null) {
                        String display = "§e" + mi.getEnchantment().getKey().getKey() + " " + mi.getLevel();
                        return clickedItem.hasItemMeta() && Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().equals(display);
                    } else {
                        return mi.getMaterial() == clickedItem.getType();
                    }
                })
                .findFirst()
                .orElse(null);

        if (marketItem == null) return;

        if (marketItem.getPrerequisiteKey() != null) {
            MarketItem prereqItem = marketManager.getItemByKey(marketItem.getPrerequisiteKey());
            if (prereqItem != null) {
                boolean hasPrereq = player.getInventory().containsAtLeast(
                        new ItemStack(prereqItem.getMaterial(), 1), 1
                );
                if (!hasPrereq) {
                    player.sendMessage("§cYou must first buy §e" + prereqItem.getDisplayName() + "§c!");
                    return;
                }
            }
        }

        double price = marketItem.getPrice();
        String teamName = TeamManager.getTeamOfPlayer(uuid);
        double balance;

        if (teamName != null) {
            balance = MoneyManager.getTeamMoney(teamName);
        } else {
            balance = MoneyManager.getPlayerMoney(uuid);
        }

        BigDecimal priceBD = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal balanceBD = BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP);

        if (balanceBD.compareTo(priceBD) < 0) {
            player.sendMessage("§cYou don't have enough money to buy this item!");
            return;
        }

        if (teamName != null) {
            MoneyManager.removeTeamMoney(teamName, priceBD.doubleValue());
        } else {
            MoneyManager.removePlayerMoney(uuid, priceBD.doubleValue());
        }

        ItemStack boughtItem;
        if (marketItem.getMaterial() == Material.ENCHANTED_BOOK && marketItem.getEnchantment() != null) {
            boughtItem = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) boughtItem.getItemMeta();
            assert meta != null;
            meta.addStoredEnchant(marketItem.getEnchantment(), marketItem.getLevel(), true);
            boughtItem.setItemMeta(meta);
        } else {
            boughtItem = new ItemStack(marketItem.getMaterial(), 1);
        }
        player.getInventory().addItem(boughtItem);

        String moneyFormatted;
        if (teamName != null) {
            moneyFormatted = MoneyManager.getTeamMoneyFormatted(teamName);
        } else {
            moneyFormatted = MoneyManager.getPlayerMoneyFormatted(uuid);
        }

        player.sendMessage("§aYou bought §e" + marketItem.getDisplayName()
                + "§a for §6" + priceBD + "$§a!");
        player.sendMessage("§7Your new balance: §6" + moneyFormatted + "$");
    }
}