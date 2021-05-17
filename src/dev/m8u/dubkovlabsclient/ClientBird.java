package dev.m8u.dubkovlabsclient;

public class ClientBird {
    int x, y, angle, lifespan, secondsAlive, imageWidth;
    boolean isStuck, shouldCluck;

    public ClientBird(boolean isMama, int x, int y, int angle, int lifespan, int secondsAlive, boolean isStuck, boolean shouldCluck) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.isStuck = isStuck;
        this.shouldCluck = shouldCluck;
        this.lifespan = lifespan;
        this.secondsAlive = secondsAlive;
        this.imageWidth = isMama ? 115 : 40;
    }
}
