package dev.m8u.dubkovlabs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.time.Duration;
import java.time.Instant;

public class Frame extends JFrame implements ActionListener{
    JPanel canvas;
    JLabel label;

    int N1 = 3, N2 = 5;
    float K = 0.5f, P = 0.5f;

    Random random;
    float currentRandomFloat;

    Timer timer;
    long frameCount = 1;

    ArrayList<MamaBird> mamaBirds;
    ArrayList<ChildBird> childBirds;
    BufferedImage mamaBirdImage;
    BufferedImage childBirdImage;

    File[] mamaBirdSoundFiles;
    File[] childBirdSoundFiles;

    Instant startTime;

    long mamaLastSec = -1, childLastSec = -1;
    long randomFloatLastSec = -1;

    @Override
    protected void frameInit() {
        super.frameInit();
        init();
    }

    public void init() {
        startTime = Instant.now();
        random = new Random();
        mamaBirds = new ArrayList<>();
        childBirds = new ArrayList<>();

        try {
            mamaBirdImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/mama_bird.png" ));
            childBirdImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/child_bird.png" ));

            mamaBirdSoundFiles = new File[] {
                    new File(System.getProperty("user.dir") + "/resources/mama_bird1.wav"),
                    new File(System.getProperty("user.dir") + "/resources/mama_bird2.wav"),
                    new File(System.getProperty("user.dir") + "/resources/mama_bird3.wav"),
                    new File(System.getProperty("user.dir") + "/resources/mama_bird4.wav")
            };
            childBirdSoundFiles = new File[] {
                    new File(System.getProperty("user.dir") + "/resources/child_bird1.wav"),
                    new File(System.getProperty("user.dir") + "/resources/child_bird2.wav"),
                    new File(System.getProperty("user.dir") + "/resources/child_bird3.wav")
            };
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));

        Box left = Box.createVerticalBox();
        canvas = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

                for (MamaBird bird : mamaBirds) {
                    bird.animationStep();
                    if (bird.checkForBorders(canvas.getWidth(), canvas.getHeight())) {
                        bird.cluck(mamaBirdSoundFiles[random.nextInt(mamaBirdSoundFiles.length)]);
                    }
                    AffineTransform old = g2d.getTransform();
                    g2d.rotate(Math.toRadians(bird.angle),
                            bird.x-bird.getImageWidth()/2.0f, bird.y+bird.getImageWidth()/2.0f);
                    bird.draw(g2d, mamaBirdImage);
                    g2d.setTransform(old);
                }

                for (ChildBird bird : childBirds) {
                    bird.animationStep();
                    if (bird.checkForBorders(canvas.getWidth(), canvas.getHeight())) {
                        bird.cluck(childBirdSoundFiles[random.nextInt(childBirdSoundFiles.length)]);
                    }
                    AffineTransform old = g2d.getTransform();
                    g2d.rotate(Math.toRadians(bird.angle),
                            bird.x-bird.getImageWidth()/2.0f, bird.y+bird.getImageWidth()/2.0f);
                    bird.draw(g2d, childBirdImage);
                    g2d.setTransform(old);
                }
            }
        };

        canvas.setSize(512, 512);
        left.add(canvas);

        label = new JLabel();
        left.add(label);

        this.add(left);

        this.setSize(512, 512);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        timer = new Timer(16, this);
        timer.start();
    }

    public void actionPerformed(ActionEvent event) {
        long currentSec = Duration.between(startTime, Instant.now()).getSeconds();

        if (currentSec != randomFloatLastSec ) {
            currentRandomFloat = random.nextFloat();
            randomFloatLastSec = currentSec;
        }
        if (currentSec % N1 == 0 && currentRandomFloat <= P && currentSec != mamaLastSec) {
            mamaBirds.add(new MamaBird(100 + random.nextInt(canvas.getWidth() - 100),
                    random.nextInt(canvas.getHeight() - 100),
                    1 + random.nextInt(2),
                    1 + random.nextInt(2),
                    1 + random.nextInt(3)));
            mamaLastSec = currentSec;
        }
        if (currentSec % N2 == 0 && (float) childBirds.size() / mamaBirds.size() < K && currentSec != childLastSec) {
            childBirds.add(new ChildBird(100 + random.nextInt(canvas.getWidth() - 100),
                    random.nextInt(canvas.getHeight() - 100),
                    2 + random.nextInt(5),
                    2 + random.nextInt(5),
                    6 + random.nextInt(10)));
            childLastSec = currentSec;
        }
        label.setText(currentSec + "s elapsed");

        canvas.repaint();

        frameCount++;
    }
}
