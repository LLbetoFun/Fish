package com.fun.gui.impl.settings;

import com.fun.client.settings.Setting;
import com.fun.gui.impl.ModuleComponent;
import com.fun.gui.impl.SettingComponent;

import java.awt.*;
import java.awt.event.MouseEvent;

public class StringSetting extends SettingComponent {
    public int index;
    public Font defaultFont=new Font("微软雅黑",Font.PLAIN,5);
    public Font font=new Font("微软雅黑",Font.PLAIN+Font.BOLD,5);
    public StringSetting(Setting setting, ModuleComponent parent) {
        super(setting, parent);
        index=setting.getOptions().indexOf(setting.getValString());
        setFont(defaultFont);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        g.setColor(new Color(225, 242, 255, 135));
        g.drawRoundRect(this.getX()+1,this.getY()+1,this.getWidth()-1,this.getHeight()-1,2,2);
        g.setColor(new Color(29, 29, 51, 135));
        g.drawString(setting.getName(),this.getX()+this.getWidth()/4,this.getY()+this.getHeight()/4);
        g.setFont(font);
        g.setColor(new Color(113, 66, 119, 135));
        g.drawString(setting.getValString(),this.getWidth()/4+3+
                g.getFontMetrics(defaultFont).stringWidth(setting.getName()),this.getHeight()/4);
        g.setColor(new Color(29, 29, 51, 135));
        g.drawLine(this.getX(),this.getY(),this.getX()+this.getWidth(),this.getY());

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        index++;
        if (index >= setting.getOptions().size()) {
            index=0;
        }
        setting.setValString(setting.getOptions().get(index));
        repaint();
    }
}
