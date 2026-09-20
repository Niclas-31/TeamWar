package de.niclasl.teamwar.player;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSavedData extends SavedData {

    public static final Codec<PlayerSavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                PlayerSavedData instance = new PlayerSavedData();
                instance.read(tag);
                return instance;
            }, instance -> instance.write(new CompoundTag())
    );
    public static final SavedDataType<PlayerSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "playtime"),
                    PlayerSavedData::new, CODEC);

    public static PlayerSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Map<UUID, Integer> totalTime = new HashMap<>();

    public void read(CompoundTag tag) {
        totalTime.clear();

        for (String key : tag.keySet()) {
            try {
                UUID uuid = UUID.fromString(key);

                CompoundTag playerTag = tag.getCompoundOrEmpty(key);

                int time = playerTag.getIntOr("totalTime", 0);

                totalTime.put(uuid, time);

            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public CompoundTag write(CompoundTag tag) {
        for (Map.Entry<UUID, Integer> entry : totalTime.entrySet()) {
            CompoundTag playerTag = new CompoundTag();

            playerTag.putInt("totalTime", entry.getValue());

            tag.put(entry.getKey().toString(), playerTag);
        }
        return tag;
    }
}