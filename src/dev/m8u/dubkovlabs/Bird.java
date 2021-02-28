package dev.m8u.dubkovlabs;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Bird {
    private int imageWidth;
    int x, y, xVel, yVel, angle, angleVel;

    Bird(int x, int y, int xVel, int yVel, int angleVel) {
        this.x = x;
        this.y = y;
        this.xVel = xVel;
        this.yVel = yVel;
        this.angle = 0;
        this.angleVel = angleVel;
    }

    public void animationStep() {
        this.x += this.xVel;
        this.y += this.yVel;
        this.angle = (this.angle + angleVel < 360) ?
                this.angle + this.angleVel : this.angleVel - (360 - this.angle);
    }

    public void checkForBorders(int width, int height) {
        if (this.x - getImageWidth()< 0 || this.x > width) {
            xVel = -xVel;
            angleVel = -angleVel;
        }
        if (this.y < 0 || this.y + getImageWidth() > height) {
            yVel = -yVel;
        }
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void draw(Graphics2D g2d, BufferedImage image) {
        g2d.drawImage(image, this.x, this.y, -getImageWidth(), getImageWidth(), null);
    }
}
