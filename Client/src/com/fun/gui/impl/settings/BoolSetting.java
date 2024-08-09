package com.fun.gui.impl.settings;

import com.fun.client.settings.Setting;
import com.fun.gui.FComponent;
import com.fun.gui.impl.ModuleComponent;
import com.fun.gui.impl.SettingComponent;

import java.awt.*;
import java.awt.event.MouseEvent;

public class BoolSetting extends SettingComponent {
    public BoolSetting(Setting setting, ModuleComponent parent) {
        super(setting, parent);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        setting.setValBoolean(!setting.getValBoolean());
        repaint();
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(new Color(225, 242, 255, 135));
        g.drawRoundRect(this.getX()+1,this.getY()+1,this.getWidth()-1,this.getHeight()-1,2,2);
        g.setColor(new Color(29, 29, 51, 135));
        g.drawString(setting.getName(),this.getX()+this.getWidth()/4,this.getY()+this.getHeight()/4);
        g.setColor(new Color(113, 66, 119, 135));
        g.drawRoundRect(getX()+getWidth()-10,getY(),10,getHeight(),1,1);
        g.setColor(new Color(29, 29, 51, 135));
        g.drawLine(this.getX(),this.getY(),this.getX()+this.getWidth(),this.getY());
    }
}
