package dev.m8u.methodology;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Frame extends JFrame implements ActionListener{
    static JPanel canvas;

    int N1 = 100, N2 = 80;
    float K = 0.5f, P = 0.5f;

    Random random;

    int frameCount = 1;
    Timer timer;

    ArrayList<MamaBird> mamaBirds;
    ArrayList<ChildBird> childBirds;
    BufferedImage mamaBirdImage;
    BufferedImage childBirdImage;

    @Override
    protected void frameInit() {
        super.frameInit();
        init();
    }

    public void init() {
        random = new Random();
        mamaBirds = new ArrayList<>();
        childBirds = new ArrayList<>();

        try {
            mamaBirdImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\mama_bird.png" ));
            childBirdImage = ImageIO.read(new File(System.getProperty("user.dir") + "\\resources\\child_bird.png" ));
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
                    bird.dvdAnimationStep();
                    bird.checkForBorders(canvas.getWidth(), canvas.getHeight());
                   //g2d.rotate(Math.toRadians(frameCount % 360), bird.x, bird.y);
                    bird.draw(g2d, mamaBirdImage);
                }

                for (ChildBird bird : childBirds) {
                    bird.dvdAnimationStep();
                    bird.checkForBorders(canvas.getWidth(), canvas.getHeight());
                    //g2d.rotate(Math.toRadians(frameCount % 360), bird.x, bird.y);
                    bird.draw(g2d, childBirdImage);
                }
            }
        };

        canvas.setSize(256, 512);
        left.add(canvas);

        Box right = Box.createVerticalBox();

        this.add(left);
        this.add(right);

        this.setSize(512, 512);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        timer = new Timer(16, this);
        timer.start();
    }

    public void actionPerformed(ActionEvent event) {
        if (frameCount % N1 == 0 && random.nextFloat() <= P) {
            mamaBirds.add(new MamaBird(100 + random.nextInt(canvas.getWidth() - 100),
                    random.nextInt(canvas.getHeight() - 100),
                    1 + random.nextInt(2), 1 + random.nextInt(2)));
        }

        if (frameCount % N2 == 0 && (float) childBirds.size() / mamaBirds.size() < K) {
            childBirds.add(new ChildBird(100 + random.nextInt(canvas.getWidth() - 100),
                    random.nextInt(canvas.getHeight() - 100),
                    2 + random.nextInt(5), 2 + random.nextInt(5)));
        }
        canvas.repaint();
        frameCount++;
    }
}
