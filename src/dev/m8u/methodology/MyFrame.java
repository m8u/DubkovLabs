package dev.m8u.methodology;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MyFrame extends JFrame implements ActionListener{
    static JPanel canvas;
    Timer timer;
    int rectPos = 0;

    @Override
    protected void frameInit() {
        super.frameInit();
        init();
    }

    public void init() {
        /*this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
            }
        });*/
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));

        JLabel label = new JLabel("JFrame By Example");
        JButton button = new JButton("Button");

        Box left = Box.createVerticalBox();
        canvas = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
                g2d.fillRect(rectPos, rectPos, 16, 16);
            }
        };
        canvas.setSize(256, 512);
        left.add(canvas);

        Box right = Box.createVerticalBox();
        right.add(label);
        right.add(button);

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
        rectPos++;
        canvas.repaint();
    }
}
