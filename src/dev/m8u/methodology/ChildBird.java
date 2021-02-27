package dev.m8u.methodology;

public class ChildBird extends Bird {

    ChildBird(int x, int y, int xVel, int yVel) {
        super(x, y, xVel, yVel);
    }

    @Override
    public int getImageWidth() {
        return 60;
    }
}
