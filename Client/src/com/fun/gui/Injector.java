package com.fun.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.fun.inject.Bootstrap;
import com.fun.inject.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Injector extends JFrame {
    public boolean injected = false;

    public Injector() throws HeadlessException {
        super();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace(); // 打印异常信息，便于调试
        }
        init();
        this.setSize(716, 403); // 初始窗口大小
        this.setVisible(true);
    }

    private void init() {
        setTitle("Fish" + Bootstrap.VERSION);
        setIconImage(new ImageIcon(getClass().getResource("/assets/texture/fishico2.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new GridLayout());
        this.setBackground(Color.WHITE);

        JButton jb = new JButton("Inject");

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制背景图片
                Image backgroundImage = new ImageIcon(Injector.class.getResource("/assets/texture/injector.png")).getImage();
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };

        // 设置面板不透明
        panel.setOpaque(false);

        // 设置布局管理器为 null
        panel.setLayout(null);

        // 设置按钮的位置和大小
        jb.setBounds(300, 300, 100, 30); // x, y, width, height
        panel.add(jb);

        // 将面板添加到窗体
        this.add(panel);

        jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 隐藏窗口
                Injector.this.setVisible(false);
                // 调用 Main.start() 方法
                Main.start();
            }
        });
    }
}
