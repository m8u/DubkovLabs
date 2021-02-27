package dev.m8u.methodology;

public class MamaBird extends Bird {

    MamaBird(int x, int y, int xVel, int yVel) {
        super(x, y, xVel, yVel);
    }

    @Override
    public int getImageWidth() {
        return 100;
    }
}
