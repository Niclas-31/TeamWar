package de.niclasl.teamwar.economy;

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

public class MoneySavedData extends SavedData {

    public final Map<String, Double> teamBalances = new HashMap<>();
    public final Map<UUID, Double> playerBalances = new HashMap<>();

    public static final Codec<MoneySavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                MoneySavedData instance = new MoneySavedData();
                instance.read(tag);
                return instance;
            }, instance -> instance.write(new CompoundTag())
    );
    public static final SavedDataType<MoneySavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "money"),
                    MoneySavedData::new, CODEC);

    public static MoneySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void read(CompoundTag tag) {
        CompoundTag teams = tag.getCompoundOrEmpty("teamBalances");

        for (String key : teams.keySet()) {
            teamBalances.put(key, teams.getDoubleOr(key, 0.0));
        }

        CompoundTag players = tag.getCompoundOrEmpty("playerBalances");

        for (String key : players.keySet()) {
            try {
                UUID uuid = UUID.fromString(key);

                playerBalances.put(uuid, players.getDoubleOr(key, 0.0));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public CompoundTag write(CompoundTag tag) {
        CompoundTag teams = new CompoundTag();

        for (Map.Entry<String, Double> entry : teamBalances.entrySet()) {

            teams.putDouble(entry.getKey(), entry.getValue());
        }

        tag.put("teamBalances", teams);

        CompoundTag players = new CompoundTag();

        for (Map.Entry<UUID, Double> entry : playerBalances.entrySet()) {

            players.putDouble(entry.getKey().toString(), entry.getValue());
        }

        tag.put("playerBalances", players);

        return tag;
    }
}