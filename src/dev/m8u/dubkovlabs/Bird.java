package dev.m8u.dubkovlabs;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class Bird {
    private int imageWidth;
    int x, y,
        xVel, yVel,
        angle, angleVel,
        collisionsInRow,
        secondsAlive;
    boolean isStuck;

    Bird(int x, int y, int xVel, int yVel, int angle, int angleVel) {
        this.x = x;
        this.y = y;
        this.xVel = xVel;
        this.yVel = yVel;
        this.angle = angle;
        this.angleVel = angleVel;
        this.collisionsInRow = 0;
        this.isStuck = false;
        this.secondsAlive = 0;
    }

    public void animationStep() {
        this.x += this.xVel;
        this.y += this.yVel;
        this.angle = (this.angle + angleVel < 360) ?
                this.angle + this.angleVel : this.angleVel - (360 - this.angle);
    }

    public boolean checkForBorders(int width, int height) {
        boolean collided = false;
        if ((this.x - getImageWidth() < 0 || this.x > width) && !this.isStuck) {
            xVel = -xVel;
            angleVel = -angleVel;
            collided = true;
        } else if (this.isStuck && this.collisionsInRow > 0) {
            xVel = (int) (((width/2.0f - this.x) * 2) / (width / 2));
            yVel = (int) (((height/2.0f - this.y) * 2) / (height / 2));
            this.collisionsInRow = 0;
        }

        if ((this.y < 0 || this.y + getImageWidth() > height) && !this.isStuck) {
            yVel = -yVel;
            collided = true;
        } else if (this.isStuck && this.collisionsInRow > 0) {
            xVel = (int) (((width/2.0f - this.x) * 2) / (width / 2));
            yVel = (int) (((height/2.0f - this.y) * 2) / (height / 2));
            System.out.println(width/2.0f + " " + this.x);
            System.out.println(xVel + " " + yVel);
            this.collisionsInRow = 0;
        }

        if (!(this.x - getImageWidth() < 0 || this.x > width)
                && !(this.y < 0 || this.y + getImageWidth() > height)) {
            this.isStuck = false;
        }

        if (collided) {
            this.collisionsInRow++;
            if (collisionsInRow > 10)
                this.isStuck = true;
        }
        else
            this.collisionsInRow = 0;

        return collided;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void draw(Graphics2D g2d, BufferedImage image) {
        g2d.drawImage(image, this.x, this.y, -getImageWidth(), getImageWidth(), null);
    }

    public void cluck(File soundFile) {
        new Thread(() -> {
            Clip clip;
            try {
                clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(soundFile));
                clip.start();
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                //e.printStackTrace();
            }
        }).start();
    }
}
