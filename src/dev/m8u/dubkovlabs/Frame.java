package dev.m8u.dubkovlabs;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

public class Frame extends JFrame implements ActionListener {
    String hostname = "217.71.129.139";
    int port = 4502;
    //String hostname = "localhost";
    //int port = 3369;

    InetAddress address;
    DatagramSocket socket;

    JPanel canvas;
    JSpinner mamaSpinner;
    JSlider mamaSlider;
    JSpinner childSpinner;
    JSlider childSlider;
    JSpinner lifespanSpinner;
    JButton pauseButton;
    JLabel mamaBirdCountLabel;
    JLabel childBirdCountLabel;
    JLabel timerLabel;

    int N1 = 3, N2 = 1, birdsLifespan = 30;
    float K = 0.5f, P = 0.5f;

    Random random;

    Timer timer;
    long currentSec;

    boolean isPaused = false;

    ArrayList<ClientBird> birds;

    BufferedImage mamaBirdImage;
    BufferedImage eggImage;
    BufferedImage childBirdEggImage;
    BufferedImage childBirdImage;
    BufferedImage birdSoulImage;
    BufferedImage hayBackgroundImage;

    File[] mamaBirdSoundFiles;
    File[] childBirdSoundFiles;

    Instant startTime;

    @Override
    protected void frameInit() {
        super.frameInit();
        init();
    }

    public void init() {
        try {
            address = InetAddress.getByName(hostname);
            socket = new DatagramSocket();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        startTime = Instant.now();
        random = new Random();

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

        birds = new ArrayList<>();

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

                for (ClientBird bird : birds) {
                    AffineTransform old = g2d.getTransform();
                    g2d.rotate(Math.toRadians(bird.angle),
                            bird.x-bird.imageWidth/2.0f, bird.y+bird.imageWidth/2.0f);
                    if (bird.secondsAlive < (int) (bird.lifespan / 10.0f))
                        g2d.drawImage(eggImage, bird.x, bird.y, -bird.imageWidth, bird.imageWidth, null);
                    else if (bird.secondsAlive < (int) (bird.lifespan / 6.0f))
                        g2d.drawImage(childBirdEggImage, bird.x, bird.y, -bird.imageWidth, bird.imageWidth, null);
                    else if (bird.secondsAlive < (int) (bird.lifespan / 3.0f) || bird.imageWidth == 40) {
                        g2d.drawImage(childBirdImage, bird.x, bird.y, -bird.imageWidth, bird.imageWidth, null);
                        if (bird.shouldCluck)
                            playSound(childBirdSoundFiles[random.nextInt(childBirdSoundFiles.length)]);
                    } else if (bird.secondsAlive <= bird.lifespan) {
                        g2d.drawImage(mamaBirdImage, bird.x, bird.y, -bird.imageWidth, bird.imageWidth, null);
                        if (bird.shouldCluck)
                            playSound(mamaBirdSoundFiles[random.nextInt(mamaBirdSoundFiles.length)]);
                    } else
                        g2d.drawImage(birdSoulImage, bird.x, bird.y, -bird.imageWidth, bird.imageWidth, null);
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
        GridLayout paramsGridLayout = new GridLayout(5, 0);
        paramsGridLayout.setVgap(5);
        paramsPanel.setLayout(paramsGridLayout);

        JPanel mamaSpinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mamaSpinner = new JSpinner();
        mamaSpinner.setModel(new SpinnerNumberModel(3, 1, 10, 1));
        mamaSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        mamaSpinner.addChangeListener(e -> {
            if (isPaused) {
                mamaSpinner.setValue(N1);
                return;
            }
            N1 = (int) mamaSpinner.getValue();
            submitParamValue("N1", N1);
        });
        mamaSpinner.setToolTipText("Chicken spawn interval (s)");
        mamaSpinnerPanel.add(new JLabel("N1:"));
        mamaSpinnerPanel.add(mamaSpinner);

        JPanel mamaSliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mamaSlider = new JSlider();
        mamaSlider.setModel(new DefaultBoundedRangeModel(50, 1, 0, 100+1));
        JLabel mamaSliderValueLabel = new JLabel(mamaSlider.getValue() + "%");
        mamaSliderValueLabel.setPreferredSize(new Dimension(mamaSliderValueLabel.getFontMetrics(mamaSliderValueLabel.getFont()).stringWidth("100%"), mamaSliderValueLabel.getPreferredSize().height));
        mamaSlider.addChangeListener(e -> {
            if (isPaused) {
                mamaSlider.setValue((int) (P*100));
                return;
            }
            P = mamaSlider.getValue() / 100.0f;
            mamaSliderValueLabel.setText(mamaSlider.getValue() + "%");
            submitParamValue("P", (int) (P*100));
        });
        mamaSlider.setToolTipText("Chicken spawn probability (%)");
        mamaSliderPanel.add(new JLabel("P:"));
        mamaSliderPanel.add(mamaSlider);
        mamaSliderPanel.add(mamaSliderValueLabel);

        JPanel childSpinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        childSpinner = new JSpinner();
        childSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        childSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
        childSpinner.addChangeListener(e -> {
            if (isPaused) {
                childSpinner.setValue(N2);
                return;
            }
            N2 = (int) childSpinner.getValue();
            submitParamValue("N2", N2);
        });
        childSpinner.setToolTipText("Baby chick spawn interval (s)");
        childSpinnerPanel.add(new JLabel("N2:"));
        childSpinnerPanel.add(childSpinner);

        JPanel childSliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        childSlider = new JSlider();
        childSlider.setModel(new DefaultBoundedRangeModel(50, 1, 0, 100+1));
        JLabel childSliderValueLabel = new JLabel(childSlider.getValue() + "%");
        childSliderValueLabel.setPreferredSize(new Dimension(childSliderValueLabel.getFontMetrics(childSliderValueLabel.getFont()).stringWidth("100%"), childSliderValueLabel.getPreferredSize().height));
        childSlider.addChangeListener(e -> {
            if (isPaused) {
                childSlider.setValue((int) (K*100));
                return;
            }
            K = childSlider.getValue() / 100.0f;
            childSliderValueLabel.setText(childSlider.getValue() + "%");
            submitParamValue("K", (int) (K*100));
        });
        childSlider.setToolTipText("Baby chicks ratio (%)");
        childSliderPanel.add(new JLabel("K:"));
        childSliderPanel.add(childSlider);
        childSliderPanel.add(childSliderValueLabel);

        JPanel lifespanSpinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lifespanSpinner = new JSpinner();
        lifespanSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        lifespanSpinner.setModel(new SpinnerNumberModel(30, 1, 60, 1));
        lifespanSpinner.addChangeListener(e -> {
            if (isPaused) {
                lifespanSpinner.setValue(birdsLifespan);
                return;
            }
            birdsLifespan = (int) lifespanSpinner.getValue();
            submitParamValue("birdsLifespan", birdsLifespan);
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
        GridLayout actionGridLayout = new GridLayout(3, 0);
        actionGridLayout.setVgap(5);
        actionsPanel.setLayout(actionGridLayout);

        pauseButton = new JButton("Submit pause");
        pauseButton.addActionListener((e) -> {
            submitAction("togglePause");
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Submit resume" : "Submit pause");
        });
        actionsPanel.add(pauseButton);

        JButton saveButton = new JButton("Submit save");
        saveButton.addActionListener((e) -> {
            submitAction("save");
        });
        actionsPanel.add(saveButton);

        JButton loadButton = new JButton("Submit load");
        loadButton.addActionListener((e) -> {
            submitAction("load");
        });
        actionsPanel.add(loadButton);

        JPanel statsPanel = new JPanel();
        statsPanel.setBorder(new TitledBorder("Stats"));
        GridLayout statsGridLayout = new GridLayout(3, 0);
        statsGridLayout.setVgap(5);
        statsPanel.setLayout(statsGridLayout);

        mamaBirdCountLabel = new JLabel();
        childBirdCountLabel = new JLabel();
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
            canvas.setSize(485, 559);

        new Thread(() -> {
            JSONObject data = requestHenhouseData();
            applyHenhouseJSONData(data);
        }).start();

        timerLabel.setText(String.format("Time elapsed: %02d:%02d", currentSec/60, currentSec%60));

        canvas.repaint();
    }

    JSONObject requestHenhouseData() {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("", "");
            byte[] requestBuffer = jsonData.toString().getBytes();
            DatagramPacket request = new DatagramPacket(requestBuffer, requestBuffer.length, address, port);
            socket.send(request);

            byte[] responseBuffer = new byte[2000];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);

            socket.receive(response);
            String responseData = new String(responseBuffer, 0, response.getLength());
            return new JSONObject(responseData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void applyHenhouseJSONData(JSONObject data) {
        currentSec = data.getLong("currentSec");
        N1 = data.getInt("N1");
        mamaSpinner.setValue(N1);
        N2 = data.getInt("N2");
        childSpinner.setValue(N2);
        P = (float) data.getDouble("P");
        mamaSlider.setValue((int) (P * 100));
        K = (float) data.getDouble("K");
        childSlider.setValue((int) (K * 100));
        birdsLifespan = data.getInt("birdsLifespan");
        lifespanSpinner.setValue(birdsLifespan);
        isPaused = data.getBoolean("isPaused");
        birds = new ArrayList<>();
        JSONArray mamaBirds = data.getJSONArray("mamaBirds");
        JSONArray childBirds = data.getJSONArray("childBirds");
        JSONArray deadBirds = data.getJSONArray("deadBirds");

        for (int i = 0; i < mamaBirds.length(); i++) {
            JSONObject jsonBird = mamaBirds.getJSONObject(i);
            birds.add(new ClientBird((jsonBird.getInt("secondsAlive") >= jsonBird.getInt("lifespan") / 3.0f),
                    jsonBird.getInt("x"),
                    jsonBird.getInt("y"),
                    jsonBird.getInt("angle"),
                    jsonBird.getInt("lifespan"),
                    jsonBird.getInt("secondsAlive"),
                    jsonBird.getBoolean("isStuck"),
                    jsonBird.getBoolean("shouldCluck")));
        }
        for (int i = 0; i < childBirds.length(); i++) {
            JSONObject jsonBird = childBirds.getJSONObject(i);
            birds.add(new ClientBird((jsonBird.getInt("secondsAlive") >= jsonBird.getInt("lifespan") / 3.0f),
                    jsonBird.getInt("x"),
                    jsonBird.getInt("y"),
                    jsonBird.getInt("angle"),
                    jsonBird.getInt("lifespan"),
                    jsonBird.getInt("secondsAlive"),
                    jsonBird.getBoolean("isStuck"),
                    jsonBird.getBoolean("shouldCluck")));
        }
        for (int i = 0; i < deadBirds.length(); i++) {
            JSONObject jsonBird = deadBirds.getJSONObject(i);
            birds.add(new ClientBird((jsonBird.getInt("secondsAlive") >= jsonBird.getInt("lifespan") / 3.0f),
                    jsonBird.getInt("x"),
                    jsonBird.getInt("y"),
                    jsonBird.getInt("angle"),
                    jsonBird.getInt("lifespan"),
                    jsonBird.getInt("secondsAlive"),
                    jsonBird.getBoolean("isStuck"),
                    jsonBird.getBoolean("shouldCluck")));
        }

        mamaBirdCountLabel.setText("Chicken count: " + mamaBirds.length());
        childBirdCountLabel.setText("Baby chick count: " + childBirds.length());
    }

    void submitParamValue(String paramName, int value) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("parameter", paramName);
            jsonData.put("value", value);
            byte[] buffer = jsonData.toString().getBytes();

            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void submitAction(String actionName) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("action", actionName);
            byte[] buffer = jsonData.toString().getBytes();

            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void playSound(File soundFile) {
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
