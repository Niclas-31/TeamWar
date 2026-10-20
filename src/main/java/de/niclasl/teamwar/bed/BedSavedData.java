package de.niclasl.teamwar.bed;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

public class BedSavedData extends SavedData {

    public static final Codec<BedSavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> {
                BedSavedData instance = new BedSavedData();
                instance.read(tag);
                return instance;
            }, instance -> instance.write(new CompoundTag())
    );
    public static final SavedDataType<BedSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "beds"),
                    BedSavedData::new, CODEC);

    public final Map<String, TeamBed> teamBeds = new HashMap<>();

    public void read(CompoundTag tag) {
        teamBeds.clear();

        for (String teamName : tag.keySet()) {
            CompoundTag bedTag = tag.getCompoundOrEmpty(teamName);

            String world = bedTag.getStringOr("world", "");

            CompoundTag footTag = bedTag.getCompoundOrEmpty("foot");
            CompoundTag headTag = bedTag.getCompoundOrEmpty("head");

            BlockPos foot = new BlockPos(
                    footTag.getIntOr("x", 0),
                    footTag.getIntOr("y", 0),
                    footTag.getIntOr("z", 0)
            );

            BlockPos head = new BlockPos(
                    headTag.getIntOr("x", 0),
                    headTag.getIntOr("y", 0),
                    headTag.getIntOr("z", 0)
            );

            float yaw = bedTag.getFloatOr("yaw", 0.0F);

            teamBeds.put(
                    teamName,
                    new TeamBed(world, foot, head, yaw)
            );
        }
    }

    public CompoundTag write(CompoundTag tag) {
        for (Map.Entry<String, TeamBed> entry : teamBeds.entrySet()) {

            String teamName = entry.getKey();
            TeamBed bed = entry.getValue();

            CompoundTag bedTag = new CompoundTag();

            bedTag.putString("world", bed.world);

            CompoundTag footTag = new CompoundTag();
            footTag.putInt("x", bed.foot.getX());
            footTag.putInt("y", bed.foot.getY());
            footTag.putInt("z", bed.foot.getZ());

            CompoundTag headTag = new CompoundTag();
            headTag.putInt("x", bed.head.getX());
            headTag.putInt("y", bed.head.getY());
            headTag.putInt("z", bed.head.getZ());

            bedTag.put("foot", footTag);
            bedTag.put("head", headTag);

            bedTag.putFloat("yaw", bed.yaw);

            tag.put(teamName, bedTag);
        }

        return tag;
    }
}