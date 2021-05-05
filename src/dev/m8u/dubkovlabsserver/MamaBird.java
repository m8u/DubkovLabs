package dev.m8u.dubkovlabsserver;

public class MamaBird extends Bird {

    MamaBird(int x, int y, int xVel, int yVel, int angle, int angleVel) {
        super(x, y, xVel, yVel, angle, angleVel);
    }

    MamaBird(int x, int y, int xVel, int yVel, int angle, int angleVel, int secondsAlive) {
        super(x, y, xVel, yVel, angle, angleVel);
        this.secondsAlive = secondsAlive;
    }

    @Override
    public int getImageWidth() {
        return 115;
    }
}
