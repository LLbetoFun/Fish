package com.fun.gui.impl;

import com.fun.client.mods.Category;
import com.fun.gui.FComponent;
import com.fun.gui.FGui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class CategoryComponent extends FComponent {
    private final Category category;
    public boolean isChose = false;
    public FGui parent;
    public ArrayList<ModuleComponent> children = new ArrayList<>();
    public CategoryComponent(Category c,FGui parent) {
        super();
        this.category = c;
        this.parent = parent;
        this.parent.categoryComponents.add(this);
    }
    public Category getCategory() {
        return category;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if(e.getButton()==MouseEvent.BUTTON1){
            for (CategoryComponent cc:parent.categoryComponents) {
                cc.setChose(cc.equals(this));
            }
        }
        repaint();
    }
    public void setChose(boolean chose){
        if(chose)parent.leftChose=this;
        isChose = chose;
        for(ModuleComponent m:children){
            m.setVisible(chose);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        System.out.println("draw");
        g.setColor(new Color(225, 242, 255, 135));
        g.drawRoundRect(this.getLocation().x,this.getLocation().y,this.getWidth(),this.getHeight(),4,4);
        g.setColor(new Color(29, 29, 51, 135));
        g.drawString(category.name(),this.getLocation().x+this.getWidth()/4,this.getLocation().y+this.getHeight()/4);
    }
}
