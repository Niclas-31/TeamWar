package de.niclasl.teamwar.classes;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PlayerUpgradeSavedData extends SavedData {

    public static final Codec<PlayerUpgradeSavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                PlayerUpgradeSavedData instance = new PlayerUpgradeSavedData();
                instance.read(tag);
                return instance;
            }, instance -> instance.write(new CompoundTag())
    );
    public static final SavedDataType<PlayerUpgradeSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "upgrades"),
                    PlayerUpgradeSavedData::new, CODEC);

    public static PlayerUpgradeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public final Map<UUID, PlayerUpgrades> playerUpgrades = new HashMap<>();

    public void read(CompoundTag tag) {
        playerUpgrades.clear();

        for (String key : tag.keySet()) {

            UUID uuid;

            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            CompoundTag playerTag = tag.getCompoundOrEmpty(key);

            PlayerUpgrades upgrades = new PlayerUpgrades(uuid);

            ListTag upgradesTag = playerTag.getListOrEmpty("upgrades");

            for (int i = 0; i < upgradesTag.size(); i++) {

                CompoundTag upgradeTag = upgradesTag.getCompoundOrEmpty(i);

                String upgradeName = upgradeTag.getStringOr("name", "");

                int level = upgradeTag.getIntOr("level", 0);

                if (upgradeName.isEmpty()) {
                    continue;
                }

                try {
                    ClassUpgrade upgrade = ClassUpgrade.valueOf(upgradeName.toUpperCase(Locale.ROOT));

                    upgrades.setLevel(upgrade, level);
                } catch (IllegalArgumentException ignored) {
                }
            }

            playerUpgrades.put(uuid, upgrades);
        }
    }

    public CompoundTag write(CompoundTag tag) {
        for (Map.Entry<UUID, PlayerUpgrades> entry : playerUpgrades.entrySet()) {

            UUID uuid = entry.getKey();
            PlayerUpgrades playerData = entry.getValue();

            CompoundTag playerTag = new CompoundTag();

            ListTag upgradesTag = new ListTag();

            for (Map.Entry<ClassUpgrade, Integer> upgradeEntry : playerData.getUpgrades().entrySet()) {

                CompoundTag upgradeTag = new CompoundTag();

                upgradeTag.putString("name", upgradeEntry.getKey().getDisplayName());

                upgradeTag.putInt("level", upgradeEntry.getValue());

                upgradesTag.add(upgradeTag);
            }

            playerTag.put("upgrades", upgradesTag);

            tag.put(uuid.toString(), playerTag);
        }

        return tag;
    }
}