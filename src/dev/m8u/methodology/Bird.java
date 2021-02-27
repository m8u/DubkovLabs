package dev.m8u.methodology;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Bird {
    int imageWidth;
    int x, y, xVel, yVel;

    Bird(int x, int y, int xVel, int yVel) {
        this.x = x;
        this.y = y;
        this.xVel = xVel;
        this.yVel = yVel;
    }

    public void dvdAnimationStep() {
        this.x += this.xVel;
        this.y += this.yVel;
    }

    public void checkForBorders(int width, int height) {
        if (this.x < 0 || this.x > width) {
            xVel = -xVel;
            this.x += xVel > 0 ? getImageWidth() : -getImageWidth();
        }

        if (this.y < 0 || this.y + getImageWidth() > height) {
            yVel = -yVel;
        }
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void draw(Graphics2D g2d, BufferedImage image) {
        if (xVel > 0) {
            g2d.drawImage(image, this.x, this.y, -getImageWidth(), getImageWidth(), null);
        } else {
            g2d.drawImage(image, this.x, this.y, getImageWidth(), getImageWidth(), null);
        }
    }
}
