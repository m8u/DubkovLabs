package dev.m8u.dubkovlabs;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    public boolean checkForBorders(int width, int height) {
        boolean collided = false;
        if (this.x - getImageWidth() < 0 || this.x > width) {
            xVel = -xVel;
            angleVel = -angleVel;
            collided = true;
        }
        if (this.y < 0 || this.y + getImageWidth() > height) {
            yVel = -yVel;
            collided = true;
        }
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
                e.printStackTrace();
            }
        }).start();
    }
}
