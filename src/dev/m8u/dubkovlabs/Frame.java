package dev.m8u.dubkovlabs;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.time.Duration;
import java.time.Instant;

public class Frame extends JFrame implements ActionListener {
    JPanel canvas;
    JLabel timerLabel;
    JLabel mamaBirdCountLabel;
    JLabel childBirdCountLabel;

    int N1 = 3, N2 = 1, birdsLifespan = 30;
    float K = 0.5f, P = 0.5f;

    Random random;
    float currentRandomFloat;

    Timer timer;
    long frameCount = 1;

    ArrayList<MamaBird> mamaBirds;
    ArrayList<ChildBird> childBirds;
    ArrayList<MamaBird> deadBirds;

    BufferedImage mamaBirdImage;
    BufferedImage eggImage;
    BufferedImage childBirdEggImage;
    BufferedImage childBirdImage;
    BufferedImage birdSoulImage;
    BufferedImage hayBackgroundImage;

    File[] mamaBirdSoundFiles;
    File[] childBirdSoundFiles;

    Instant startTime;
    Instant pauseTime;

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
        deadBirds = new ArrayList<>();

        try {
            mamaBirdImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/mama_bird.png" ));
            eggImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/egg.png" ));
            childBirdEggImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/child_bird_egg.png" ));
            childBirdImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/child_bird.png" ));
            birdSoulImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/bird_soul.png" ));
            hayBackgroundImage = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/hay.png" ));


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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(1.0);


        Box left = new Box(BoxLayout.Y_AXIS);
        canvas = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);


                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
                for (int y = 0; y < canvas.getHeight(); y+=hayBackgroundImage.getHeight())
                    for (int x = 0; x < canvas.getWidth(); x+=hayBackgroundImage.getWidth())
                        g2d.drawImage(hayBackgroundImage, x, y, null);

                for (MamaBird bird : mamaBirds) {
                    AffineTransform old = g2d.getTransform();
                    g2d.rotate(Math.toRadians(bird.angle),
                            bird.x-bird.getImageWidth()/2.0f, bird.y+bird.getImageWidth()/2.0f);
                    bird.draw(g2d, mamaBirdImage);
                    g2d.setTransform(old);
                }

                for (ChildBird bird : childBirds) {
                    AffineTransform old = g2d.getTransform();
                    g2d.rotate(Math.toRadians(bird.angle),
                            bird.x-bird.getImageWidth()/2.0f, bird.y+bird.getImageWidth()/2.0f);
                    if (bird.secondsAlive < birdsLifespan / 10.0f)
                        bird.draw(g2d, eggImage);
                    else if (bird.secondsAlive < birdsLifespan / 6.0f)
                        bird.draw(g2d, childBirdEggImage);
                    else
                        bird.draw(g2d, childBirdImage);
                    g2d.setTransform(old);
                }

                for (MamaBird bird : deadBirds) {
                    bird.draw(g2d, birdSoulImage);
                }

            }
        };

        left.add(canvas);

        splitPane.setLeftComponent(left);

        JPanel right = new JPanel(new GridBagLayout());
        right.setMaximumSize(new Dimension(64, 512));
        right.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel paramsPanel = new JPanel();
        paramsPanel.setBorder(new TitledBorder("Parameters"));
        GridLayout paramsGridLayout = new GridLayout(5, 0);
        paramsGridLayout.setVgap(5);
        paramsPanel.setLayout(paramsGridLayout);

        JPanel mamaSpinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSpinner mamaSpinner = new JSpinner();
        mamaSpinner.setModel(new SpinnerNumberModel(3, 1, 10, 1));
        mamaSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        mamaSpinner.addChangeListener(e -> {
            N1 = (int) mamaSpinner.getValue();
        });
        mamaSpinner.setToolTipText("Chicken spawn interval (s)");
        mamaSpinnerPanel.add(new JLabel("N1:"));
        mamaSpinnerPanel.add(mamaSpinner);


        JPanel mamaSliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSlider mamaSlider = new JSlider();
        mamaSlider.setModel(new DefaultBoundedRangeModel(50, 1, 0, 100+1));
        JLabel mamaSliderValueLabel = new JLabel(mamaSlider.getValue() + "%");
        mamaSliderValueLabel.setPreferredSize(new Dimension(mamaSliderValueLabel.getFontMetrics(mamaSliderValueLabel.getFont()).stringWidth("100%"), mamaSliderValueLabel.getPreferredSize().height));
        mamaSlider.addChangeListener(e -> {
            P = mamaSlider.getValue() / 100.0f;
            mamaSliderValueLabel.setText(mamaSlider.getValue() + "%");
        });
        mamaSlider.setToolTipText("Chicken spawn probability (%)");
        mamaSliderPanel.add(new JLabel("P:"));
        mamaSliderPanel.add(mamaSlider);
        mamaSliderPanel.add(mamaSliderValueLabel);


        JPanel childSpinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSpinner childSpinner = new JSpinner();
        childSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        childSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
        childSpinner.addChangeListener(e -> {
            N2 = (int) childSpinner.getValue();
        });
        childSpinner.setToolTipText("Baby chick spawn interval (s)");
        childSpinnerPanel.add(new JLabel("N2:"));
        childSpinnerPanel.add(childSpinner);


        JPanel childSliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSlider childSlider = new JSlider();
        childSlider.setModel(new DefaultBoundedRangeModel(50, 1, 0, 100+1));
        JLabel childSliderValueLabel = new JLabel(childSlider.getValue() + "%");
        childSliderValueLabel.setPreferredSize(new Dimension(childSliderValueLabel.getFontMetrics(childSliderValueLabel.getFont()).stringWidth("100%"), childSliderValueLabel.getPreferredSize().height));
        childSlider.addChangeListener(e -> {
            K = childSlider.getValue() / 100.0f;
            childSliderValueLabel.setText(childSlider.getValue() + "%");
        });
        childSlider.setToolTipText("Baby chicks ratio (%)");
        childSliderPanel.add(new JLabel("K:"));
        childSliderPanel.add(childSlider);
        childSliderPanel.add(childSliderValueLabel);


        JPanel lifespanSpinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSpinner lifespanSpinner = new JSpinner();
        lifespanSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        lifespanSpinner.setModel(new SpinnerNumberModel(30, 1, 60, 1));
        lifespanSpinner.addChangeListener(e -> {
            birdsLifespan = (int) lifespanSpinner.getValue();
        });
        lifespanSpinner.setToolTipText("Birds lifespan (s)");
        lifespanSpinnerPanel.add(new JLabel("Lifespan:"));
        lifespanSpinnerPanel.add(lifespanSpinner);


        paramsPanel.add(mamaSpinnerPanel);
        paramsPanel.add(mamaSliderPanel);
        paramsPanel.add(childSpinnerPanel);
        paramsPanel.add(childSliderPanel);
        paramsPanel.add(lifespanSpinnerPanel);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setBorder(new TitledBorder("Actions"));
        GridLayout actionGridLayout = new GridLayout(1, 0);
        actionGridLayout.setVgap(5);
        actionsPanel.setLayout(actionGridLayout);

        JButton pauseButton = new JButton("Pause simulation");
        pauseButton.addActionListener((e) -> {
            if (timer.isRunning()) {
                timer.stop();
                pauseTime = Instant.now();
                pauseButton.setText("Resume simulation");
            } else {
                startTime = startTime.plus(Duration.between(pauseTime, Instant.now()));
                timer.start();
                pauseButton.setText("Pause simulation");
            }
        });

        actionsPanel.add(pauseButton);

        JPanel statsPanel = new JPanel();
        statsPanel.setBorder(new TitledBorder("Stats"));
        GridLayout statsGridLayout = new GridLayout(3, 0);
        statsGridLayout.setVgap(5);
        statsPanel.setLayout(statsGridLayout);

        mamaBirdCountLabel = new JLabel("Chicken count: " + mamaBirds.size());
        childBirdCountLabel = new JLabel("Baby chick count: " + childBirds.size());
        timerLabel = new JLabel();
        mamaBirdCountLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        childBirdCountLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        timerLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

        statsPanel.add(mamaBirdCountLabel);
        statsPanel.add(childBirdCountLabel);
        statsPanel.add(timerLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        right.add(paramsPanel, gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 1;
        right.add(actionsPanel, gbc);
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.gridx = 0;
        gbc.gridy = 2;
        right.add(statsPanel, gbc);

        splitPane.setRightComponent(right);

        splitPane.addPropertyChangeListener("dividerLocation", e -> {
            if (splitPane.getLastDividerLocation() <= 0)
                splitPane.setDividerLocation(splitPane.getLastDividerLocation());
        });

        this.add(splitPane);

        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        timer = new Timer(16, this);
        timer.start();
    }

    public void actionPerformed(ActionEvent event) {

        if (canvas.getWidth() == 0)
            canvas.setSize(512, 512);

        long currentSec = Duration.between(startTime, Instant.now()).getSeconds();


        if (currentSec != randomFloatLastSec ) {
            currentRandomFloat = random.nextFloat();
            randomFloatLastSec = currentSec;
        }
        if (currentSec != mamaLastSec) {
            for (MamaBird bird : mamaBirds) {
                bird.secondsAlive++;
            }
            if (currentSec % N1 == 0 && currentRandomFloat <= P) {
                mamaBirds.add(new MamaBird(170 + random.nextInt(canvas.getWidth() - 170*2),
                        170 + random.nextInt(canvas.getHeight() - 170*2),
                        1 + random.nextInt(2),
                        1 + random.nextInt(2),
                        0,
                        1 + random.nextInt(3)));
                mamaBirdCountLabel.setText("Chicken count: " + mamaBirds.size());
            }
            mamaLastSec = currentSec;
        }
        if (currentSec != childLastSec) {
            for (ChildBird bird : childBirds) {
                bird.secondsAlive++;
            }
            if (currentSec % N2 == 0 && (float) childBirds.size() / mamaBirds.size() < K) {
                childBirds.add(new ChildBird((canvas.getWidth()/2 + (random.nextInt(50) - 25)),
                        (canvas.getHeight()/2 + (random.nextInt(50) - 25)),
                        2 + random.nextInt(5),
                        2 + random.nextInt(5),
                        0,
                        6 + random.nextInt(10)));
                childLastSec = currentSec;
                childBirdCountLabel.setText("Baby chick count: " + childBirds.size());
            }
            childLastSec = currentSec;
        }
        timerLabel.setText(String.format("Time elapsed: %02d:%02d", currentSec/60, currentSec%60));

        for (Iterator<MamaBird> iterator = mamaBirds.iterator(); iterator.hasNext();) {
            MamaBird bird = iterator.next();
            if (bird.secondsAlive > birdsLifespan) {
                iterator.remove();
                deadBirds.add(new MamaBird(bird.x, bird.y, 0, -4, 0, 0));
                mamaBirdCountLabel.setText("Chicken count: " + mamaBirds.size());
            }
            bird.animationStep();
            if (bird.checkForBorders(canvas.getWidth(), canvas.getHeight()) && bird.collisionsInRow < 50) {
                bird.cluck(mamaBirdSoundFiles[random.nextInt(mamaBirdSoundFiles.length)]);
            }
        }
        for (Iterator<ChildBird> iterator = childBirds.iterator(); iterator.hasNext();) {
            ChildBird bird = iterator.next();
            if (bird.secondsAlive >= birdsLifespan / 3.0f) {
                iterator.remove();
                childBirdCountLabel.setText("Baby chick count: " + childBirds.size());
                mamaBirds.add(new MamaBird(bird.x, bird.y,
                        (bird.xVel > 0 ? 1 + random.nextInt(2) : (1 + random.nextInt(2)) * -1),
                        (bird.yVel > 0 ? 1 + random.nextInt(2) : 1 + (random.nextInt(2)) * -1),
                        bird.angle,
                        (bird.angleVel > 0 ? 1 + random.nextInt(3) : (1 + random.nextInt(3)) * -1)));
                mamaBirdCountLabel.setText("Chicken count: " + mamaBirds.size());
            }
            if (bird.secondsAlive >= birdsLifespan / 6.0f)
                bird.animationStep();
            if (bird.checkForBorders(canvas.getWidth(), canvas.getHeight()) && bird.collisionsInRow < 50) {
                bird.cluck(childBirdSoundFiles[random.nextInt(childBirdSoundFiles.length)]);
            }
        }
        for (Iterator<MamaBird> iterator = deadBirds.iterator(); iterator.hasNext();) {
            MamaBird bird = iterator.next();
            if (bird.y + bird.getImageWidth() < 0) {
                iterator.remove();
            }
            bird.animationStep();
        }

        canvas.repaint();

        frameCount++;
    }
}
