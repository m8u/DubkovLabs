package dev.m8u.dubkovlabs;

public class ClientBird {
    int x, y, angle, secondsAlive, imageWidth;
    boolean isStuck, shouldCluck;

    public ClientBird(boolean isMama, int x, int y, int angle, int secondsAlive, boolean isStuck, boolean shouldCluck) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.isStuck = isStuck;
        this.shouldCluck = shouldCluck;
        this.secondsAlive = secondsAlive;
        this.imageWidth = isMama ? 115 : 40;
    }
}
