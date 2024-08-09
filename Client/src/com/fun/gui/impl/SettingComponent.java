package com.fun.gui.impl;

import com.fun.client.settings.Setting;
import com.fun.gui.FComponent;

import java.awt.*;

public class SettingComponent extends FComponent {
    public Setting setting;
    private ModuleComponent parent;
    public SettingComponent(Setting setting, ModuleComponent parent) {
        super();
        this.setting = setting;
        this.parent = parent;
        this.parent.settings.add(this);
    }


}
