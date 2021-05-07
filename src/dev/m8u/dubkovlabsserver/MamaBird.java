package dev.m8u.dubkovlabsserver;

public class MamaBird extends Bird {

    MamaBird(int x, int y, int xVel, int yVel, int angle, int angleVel, int lifespan) {
        super(x, y, xVel, yVel, angle, angleVel, lifespan);
    }

    MamaBird(int x, int y, int xVel, int yVel, int angle, int angleVel, int lifespan, int secondsAlive) {
        super(x, y, xVel, yVel, angle, angleVel, lifespan);
        this.secondsAlive = secondsAlive;
    }

    @Override
    public int getImageWidth() {
        return 115;
    }
}
