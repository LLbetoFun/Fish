package com.fun.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CustomPaintComponent extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 设置一些绘制属性
        g2d.setColor(Color.BLUE); // 设置绘制颜色
        g2d.setStroke(new BasicStroke(2)); // 设置线条粗细

        // 绘制矩形
        g2d.drawRect(10, 10, 200, 100); // 矩形位置和大小

        // 绘制文本
        String text = "Hello, World!";
        Font font = new Font("Serif", Font.BOLD, 20); // 设置文本字体
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(text, g2d);
        float x = (200 - (float) bounds.getWidth()) / 2;
        float y = 60 + (float) (bounds.getHeight() / 2) - metrics.getMaxDescent();
        g2d.setColor(Color.WHITE); // 文本颜色
        g2d.drawString(text, x, y);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Custom Paint Component");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new CustomPaintComponent());
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}