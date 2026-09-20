package de.niclasl.teamwar.team;

public class TeamLocation {

    public String world;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public TeamLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}