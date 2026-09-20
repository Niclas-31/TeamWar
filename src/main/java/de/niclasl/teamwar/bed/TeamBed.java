package de.niclasl.teamwar.bed;

import net.minecraft.core.BlockPos;

public class TeamBed {

    public String world;

    public BlockPos foot;
    public BlockPos head;

    public float yaw;

    public TeamBed(String world, BlockPos foot, BlockPos head, float yaw) {
        this.world = world;
        this.foot = foot;
        this.head = head;
        this.yaw = yaw;
    }
}