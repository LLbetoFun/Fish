package com.fun.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class FComponent extends JComponent {
    public FLayout.Type type= FLayout.Type.Left;

    public FComponent() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FComponent.this.mouseClicked(e);
            }
            public void mouseEntered(MouseEvent e) {
                FComponent.this.mouseEntered(e);
            }
            public void mouseExited(MouseEvent e) {
                FComponent.this.mouseExited(e);
            }
            public void mousePressed(MouseEvent e) {
                FComponent.this.mousePressed(e);
            }
            public void mouseReleased(MouseEvent e) {
                FComponent.this.mouseReleased(e);
            }
            public void mouseDragged(MouseEvent e) {
                FComponent.this.mouseDragged(e);
            }

        });
        setVisible(true);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        draw((Graphics2D) g);
    }
    public void draw(Graphics2D g){

    }
    public void mousePressed(MouseEvent e){

    }
    public void mouseReleased(MouseEvent e){

    }
    public void mouseClicked(MouseEvent e){

    }
    public void mouseEntered(MouseEvent e){

    }
    public void mouseExited(MouseEvent e){

    }
    public void mouseDragged(MouseEvent e){}


}
