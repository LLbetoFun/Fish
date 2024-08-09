package com.fun.gui.impl;

import com.fun.client.mods.Module;
import com.fun.gui.FComponent;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ModuleComponent extends FComponent {
    private final Module module;
    private boolean isOpen;
    public ArrayList<SettingComponent> settings=new ArrayList<>();
    public CategoryComponent parent;
    public ModuleComponent(Module module,CategoryComponent parent) {
        super();
        this.module = module;
        this.parent = parent;
        this.parent.children.add(this);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(new Color(225, 242, 255, 135));
        g.drawRoundRect(this.getX(),this.getY(),this.getWidth(),this.getHeight(),4,4);
        g.setColor(module.isRunning()?new Color(124, 181, 124, 153):new Color(29, 29, 51, 135));
        g.drawString(module.getName(),this.getX()+this.getWidth()/4,this.getY()+this.getHeight()/4);
        g.setColor(new Color(29, 29, 51, 135));
        g.drawLine(this.getX(),this.getY(),this.getX()+this.getWidth(),this.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if(e.getButton()==MouseEvent.BUTTON1){
            module.setRunning(!module.isRunning());
        }
        if(e.getButton()==MouseEvent.BUTTON3){
            setOpen(!isOpen);
        }
        repaint();
    }
    public void setOpen(boolean open) {
        isOpen = open;
        for(SettingComponent s : settings){
            s.setVisible(isOpen&&s.setting.isVisible());
        }
    }
}
