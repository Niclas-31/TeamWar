package de.niclasl.teamWar.market.manager;

import de.niclasl.teamWar.TeamWar;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class MarketManager {

    private final TeamWar plugin;
    private File marketFile;
    private FileConfiguration marketConfig;

    public Map<String, MarketItem> getItemsByCategory(Category category) {
        return itemsByCategory.getOrDefault(category, Collections.emptyMap());
    }

    public enum Category {
        FOOD,
        ARMOR,
        TOOLS,
        COMBAT,
        ENCHANTMENTS
    }

    private static final Map<Category, Map<String, MarketItem>> itemsByCategory = new HashMap<>();

    public MarketManager(TeamWar plugin) {
        this.plugin = plugin;
        setupMarketFile();
        loadMarketConfig();
        loadItems(Category.ENCHANTMENTS);
        loadItems(Category.ARMOR);
        loadItems(Category.COMBAT);
        loadItems(Category.FOOD);
        loadItems(Category.TOOLS);
    }

    public ItemStack getCategoryItem(Category category) {
        Material mat = switch (category) {
            case FOOD -> Material.PORKCHOP;
            case COMBAT -> Material.DIAMOND_SWORD;
            case ARMOR -> Material.DIAMOND_CHESTPLATE;
            case TOOLS -> Material.DIAMOND_PICKAXE;
            case ENCHANTMENTS -> Material.ENCHANTED_BOOK;
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(category.name());
        item.setItemMeta(meta);
        return item;
    }

    public void setupMarketFile() {
        marketFile = new File(plugin.getDataFolder(), "market.yml");
        if (!marketFile.exists()) {
            plugin.saveResource("market.yml", false);
        }
        marketConfig = YamlConfiguration.loadConfiguration(marketFile);
    }

    public void loadMarketConfig() {
        marketConfig = YamlConfiguration.loadConfiguration(marketFile);
    }

    public void loadItems(Category category) {
        String categoryKey = category.name().toLowerCase();
        String categoryPath = "categories." + categoryKey;

        if (!marketConfig.isConfigurationSection(categoryPath)) {
            plugin.getLogger().warning("Market category not found in config: " + categoryKey);
            return;
        }

        List<Map<Object, Object>> items = (List<Map<Object, Object>>) (Object) marketConfig.getMapList(categoryPath + ".items");
        Map<String, MarketItem> loadedItems = new HashMap<>();

        for (Map<Object, Object> itemMap : items) {

            Number priceNum = (Number) itemMap.getOrDefault("price", itemMap.getOrDefault("preis", 0));
            double price = priceNum != null ? priceNum.doubleValue() : 0.0;

            String prerequisite = (String) itemMap.get("prerequisite");
            if (category == Category.ENCHANTMENTS) {
                String enchantName = (String) itemMap.get("enchantment");
                Number levelNum = (Number) itemMap.getOrDefault("level", 1);
                int level = levelNum != null ? levelNum.intValue() : 1;

                if (enchantName == null) {
                    plugin.getLogger().warning("Missing enchantment name in market.yml for category: " + categoryKey);
                    continue;
                }

                Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
                if (enchantment == null) {
                    plugin.getLogger().warning("Invalid enchantment name in market.yml: " + enchantName);
                    continue;
                }

                MarketItem marketItem = new MarketItem(
                        enchantName.toLowerCase() + "_" + level,
                        Material.ENCHANTED_BOOK,
                        level,
                        price,
                        category,
                        prerequisite
                );
                marketItem.setEnchantment(enchantment);
                loadedItems.put(marketItem.getKey(), marketItem);

            } else {
                String materialName = (String) itemMap.get("material");
                if (materialName == null) {
                    plugin.getLogger().warning("Missing material for item in category " + categoryKey);
                    continue;
                }

                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    plugin.getLogger().warning("Invalid material name in market.yml: " + materialName);
                    continue;
                }

                MarketItem marketItem = new MarketItem(
                        materialName.toLowerCase(),
                        material,
                        0,
                        price,
                        category,
                        prerequisite
                );
                loadedItems.put(marketItem.getKey(), marketItem);
            }
        }

        itemsByCategory.put(category, loadedItems);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public MarketItem getItemByKey(String key) {
        for (Map<String, MarketItem> map : itemsByCategory.values()) {
            if (map.containsKey(key)) return map.get(key);
        }
        return null;
    }

    public static class MarketItem {
        private final String key;
        private final Material material;
        private final int level;
        private final double price;
        private final Category category;
        private Enchantment enchantment;

        private final String prerequisiteKey;

        public MarketItem(String key, Material material, int level, double price, Category category, String prerequisiteKey) {
            this.key = key;
            this.material = material;
            this.level = level;
            this.price = price;
            this.category = category;
            this.prerequisiteKey = prerequisiteKey;
        }

        public Category getCategory() {
            return category;
        }

        public String getKey() {
            return key;
        }

        public Material getMaterial() {
            return material;
        }

        public int getLevel() {
            return level;
        }

        public double getPrice() {
            return price;
        }

        public String getDisplayName() {
            if (enchantment != null && material == Material.ENCHANTED_BOOK) {
                return capitalize(enchantment.getKey().getKey()) + " " + level;
            } else {
                return capitalize(material.name().replace('_', ' '));
            }
        }

        public Enchantment getEnchantment() {
            return enchantment;
        }

        public void setEnchantment(Enchantment enchantment) {
            this.enchantment = enchantment;
        }

        public ItemStack toItemStack() {
            ItemStack item;

            if (enchantment != null && material == Material.ENCHANTED_BOOK) {
                item = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof EnchantmentStorageMeta enchantMeta) {
                    enchantMeta.addStoredEnchant(enchantment, level, true);
                }
                assert meta != null;
                meta.setDisplayName("§e" + enchantment.getKey().getKey() + " " + level);
                List<String> lore = new ArrayList<>();
                lore.add("§7Price: §a" + String.format("%.2f", price));
                meta.setLore(lore);
                item.setItemMeta(meta);
            } else {
                item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName("§e" + capitalize(material.name().replace('_', ' ')));
                List<String> lore = new ArrayList<>();
                lore.add("§7Price: §a" + String.format("%.2f", price));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            return item;
        }

        public String getPrerequisiteKey() {
            return prerequisiteKey;
        }
    }
}