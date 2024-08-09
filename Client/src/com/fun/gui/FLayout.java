package com.fun.gui;

import javax.swing.*;
import java.awt.*;

public class FLayout implements LayoutManager {
    @Override
    public void addLayoutComponent(String name, Component comp) {
        // 可以在这里根据名称对组件进行分类或特殊处理，本例中未使用
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // 组件从容器移除时不需要特别操作
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        int width = parent.getWidth();
        int height = parent.getHeight();
        // 计算左侧区域的宽度和高度


        // 计算首选大小，这里简化处理，只考虑宽度和高度

        return new Dimension(width, height);
    }

    @Override
    public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        int height = parent.getHeight();

        int leftWidth = width / 3;
        int rightWidth = width - leftWidth;
        for (Component comp : parent.getComponents()) {
            if (comp instanceof FComponent && ((FComponent) comp).type==Type.Left) {
                comp.setBounds(0, 0, leftWidth, height);
            }
            else if (comp instanceof FComponent &&((FComponent) comp).type==Type.Right) {
                comp.setBounds(leftWidth, 0, rightWidth, height);
            }
        }

    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        // 可以返回preferredLayoutSize或者根据组件的最小尺寸进行计算
        return preferredLayoutSize(parent);
    }

    public static enum Type{
        Left,Right
    }
}
