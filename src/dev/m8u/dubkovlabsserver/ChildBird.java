package dev.m8u.dubkovlabsserver;

public class ChildBird extends Bird {

    ChildBird(int x, int y, int xVel, int yVel, int angle, int angleVel, int lifespan) {
        super(x, y, xVel, yVel, angle, angleVel, lifespan);
    }

    @Override
    public int getImageWidth() {
        return 40;
    }
}
