package dev.m8u.dubkovlabs;

public class MamaBird extends Bird {

    MamaBird(int x, int y, int xVel, int yVel, int angle, int angleVel) {
        super(x, y, xVel, yVel, angle, angleVel);
    }

    @Override
    public int getImageWidth() {
        return 115;
    }
}
