package dev.m8u.dubkovlabs;

public class MamaBird extends Bird {

    MamaBird(int x, int y, int xVel, int yVel, int angleVel) {
        super(x, y, xVel, yVel, angleVel);
    }

    @Override
    public int getImageWidth() {
        return 100;
    }
}
