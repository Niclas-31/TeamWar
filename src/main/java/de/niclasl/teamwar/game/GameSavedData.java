package de.niclasl.teamwar.game;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class GameSavedData extends SavedData {

    public static final Codec<GameSavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                GameSavedData instance = new GameSavedData();
                instance.read(tag);
                return instance;
            }, instance -> instance.write(new CompoundTag())
    );

    public static final SavedDataType<GameSavedData> TYPE =
            new  SavedDataType<>(
                    Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "game_data"),
                    GameSavedData::new, CODEC);

    public GameState gameState = GameState.LOBBY;
    public int currentWeek = 1;
    public int currentDay = 1;
    public long gameStartTime;

    public void read(CompoundTag tag) {
        String stateName = tag.getStringOr("gameState", GameState.LOBBY.name());
        try {
            gameState = GameState.valueOf(stateName);
        } catch (IllegalArgumentException e) {
            gameState = GameState.LOBBY;
        }

        currentWeek = tag.getIntOr("currentWeek", 1);
        currentDay = tag.getIntOr("currentDay", 1);
    }

    public CompoundTag write(CompoundTag tag) {
        tag.putString("gameState", gameState.name());
        tag.putInt("currentWeek", currentWeek);
        tag.putInt("currentDay", currentDay);
        return tag;
    }
}