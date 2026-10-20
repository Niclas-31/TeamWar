package de.niclasl.teamwar.repair;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class WorldBalanceSavedData extends SavedData {

  private WorldBalanceState state = WorldBalanceState.BALANCED;

  public static final Codec<WorldBalanceSavedData> CODEC =
          Codec.STRING
                  .xmap(WorldBalanceState::valueOf,
                          WorldBalanceState::name)
                  .xmap(WorldBalanceSavedData::new,
                          WorldBalanceSavedData::getState);

  public static final SavedDataType<WorldBalanceSavedData> TYPE =
          new SavedDataType<>(
                  Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "world_balance"),
                  WorldBalanceSavedData::new, CODEC
          );

  private WorldBalanceSavedData() {
  }

  private WorldBalanceSavedData(WorldBalanceState state) {
    this.state = state;
  }

  public WorldBalanceState getState() {
    return state;
  }

  public void setState(WorldBalanceState state) {
    if (this.state == state) {
      return;
    }

    this.state = state;
    setDirty();
  }
}