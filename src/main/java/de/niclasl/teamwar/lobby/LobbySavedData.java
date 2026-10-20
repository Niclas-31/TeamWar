package de.niclasl.teamwar.lobby;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LobbySavedData extends SavedData {

    public static final Codec<LobbySavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                LobbySavedData instance = new LobbySavedData();
                instance.read(tag);
                return instance;
            }, instance -> instance.write(new CompoundTag())
    );
    public static final SavedDataType<LobbySavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "lobby"),
                    LobbySavedData::new, CODEC);

    public ResourceKey<Level> dimension;
    public BlockPos spawn;
    public float yaw;
    public float pitch;

    public Set<UUID> players = new HashSet<>();

    public void read(CompoundTag tag) {
        String dimensionName = tag.getStringOr("dimension", "");

        if (!dimensionName.isEmpty()) {
            Identifier id = Identifier.parse(dimensionName);

            dimension = ResourceKey.create(Registries.DIMENSION, id);
        }

        if (tag.contains("x") && tag.contains("y") && tag.contains("z")) {

            int x = tag.getIntOr("x", 0);
            int y = tag.getIntOr("y", 0);
            int z = tag.getIntOr("z", 0);

            spawn = new BlockPos(x, y, z);
        }

        yaw = tag.getFloatOr("yaw", 0.0F);
        pitch = tag.getFloatOr("pitch", 0.0F);

        players.clear();

        ListTag playersTag = tag.getListOrEmpty("players");

        for (int i = 0; i < playersTag.size(); i++) {

            String uuidString = playersTag.getStringOr(i, "");

            try {
                players.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public CompoundTag write(CompoundTag tag) {
        if (dimension != null) {
            tag.putString("dimension", dimension.identifier().toString());
        }

        if (spawn != null) {
            tag.putInt("x", spawn.getX());
            tag.putInt("y", spawn.getY());
            tag.putInt("z", spawn.getZ());
        }

        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);

        ListTag playersTag = new ListTag();

        for (UUID uuid : players) {
            playersTag.add(StringTag.valueOf(uuid.toString()));
        }

        tag.put("players", playersTag);
        return tag;
    }
}