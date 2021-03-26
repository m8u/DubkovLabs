package dev.m8u.dubkovlabs;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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
    JLabel timerLabel;
    JLabel mamaBirdCountLabel;
    JLabel childBirdCountLabel;

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(1.0);

        Box left = new Box(BoxLayout.Y_AXIS);
        canvas = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
                g2d.setColor(Color.WHITE);

                for (MamaBird bird : mamaBirds) {
                    bird.animationStep();
                    if (bird.checkForBorders(canvas.getWidth(), canvas.getHeight()) && bird.collisionsInRow < 50) {
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
                    if (bird.checkForBorders(canvas.getWidth(), canvas.getHeight()) && bird.collisionsInRow < 50) {
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

        left.add(canvas);

        splitPane.setLeftComponent(left);

        JPanel right = new JPanel(new GridBagLayout());
        right.setMaximumSize(new Dimension(64, 512));
        right.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel paramsPanel = new JPanel();
        paramsPanel.setBorder(new TitledBorder("Parameters"));
        GridLayout paramsGridLayout = new GridLayout(4, 0);
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
        childSpinner.setModel(new SpinnerNumberModel(5, 1, 10, 1));
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

        paramsPanel.add(mamaSpinnerPanel);
        paramsPanel.add(mamaSliderPanel);
        paramsPanel.add(childSpinnerPanel);
        paramsPanel.add(childSliderPanel);

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
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.gridx = 0;
        gbc.gridy = 1;
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
        if (currentSec % N1 == 0 && currentRandomFloat <= P && currentSec != mamaLastSec) {
            mamaBirds.add(new MamaBird(100 + random.nextInt(canvas.getWidth() - 100),
                    random.nextInt(canvas.getHeight() - 100),
                    1 + random.nextInt(2),
                    1 + random.nextInt(2),
                    1 + random.nextInt(3)));
            mamaLastSec = currentSec;
            mamaBirdCountLabel.setText("Chicken count: " + mamaBirds.size());
        }
        if (currentSec % N2 == 0 && (float) childBirds.size() / mamaBirds.size() < K && currentSec != childLastSec) {
            childBirds.add(new ChildBird(100 + random.nextInt(canvas.getWidth() - 100),
                    random.nextInt(canvas.getHeight() - 100),
                    2 + random.nextInt(5),
                    2 + random.nextInt(5),
                    6 + random.nextInt(10)));
            childLastSec = currentSec;
            childBirdCountLabel.setText("Baby chick count: " + childBirds.size());
        }
        timerLabel.setText(String.format("Time elapsed: %02d:%02d", currentSec/60, currentSec%60));

        canvas.repaint();

        frameCount++;
    }
}
