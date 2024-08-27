package com.fun.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.fun.client.FunGhostClient;
import com.fun.client.config.ConfigModule;
import com.fun.client.mods.Category;
import com.fun.client.mods.Module;
import com.fun.client.settings.Setting;
import com.fun.gui.impl.CategoryComponent;
import com.fun.gui.impl.ModuleComponent;
import com.fun.gui.impl.settings.BoolSetting;
import com.fun.gui.impl.settings.NumberSetting;
import com.fun.gui.impl.settings.StringSetting;
import com.fun.inject.Bootstrap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FGui extends JFrame {
    public Map<JComponent, FLayout.Type> typeMap = new HashMap<>();
    public ArrayList<CategoryComponent> categoryComponents = new ArrayList<>();

    public FComponent leftChose = null;
    public static FGui instance;
    public static void init(){
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {

        }
        instance = new FGui();
    }
    public FGui() {
        setTitle("Fish");
        Container container = getContentPane();
        container.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        JPanel rightPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(rightPanel);
        BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
        BoxLayout bl2 = new BoxLayout(rightPanel, BoxLayout.Y_AXIS);
        panel.setLayout(bl);
        rightPanel.setLayout(bl2);

        addComponent(panel);
        addComponent(scrollPane);
        for(Category c:Category.values()){
            CategoryComponent cc=new CategoryComponent(c,this);
            panel.add(cc);
            for(Module m: FunGhostClient.registerManager.mods){
                if(m.category == c){
                    ModuleComponent mc=new ModuleComponent(m,cc);
                    rightPanel.add(mc);
                    for(Setting set:FunGhostClient.settingsManager.getSettingsByMod(m)){
                        if(set.isCheck()){
                            mc.add(new BoolSetting(set,mc));
                            System.out.println("addbool");
                        }
                        if(set.isCombo()){
                            mc.add(new StringSetting(set,mc));
                            System.out.println("addstring");
                        }
                        if(set.isSlider()){
                            mc.add(new NumberSetting(set,mc));
                            System.out.println("adddouble");
                        }
                    }
                }
            }
            cc.setChose(true);
        }

        setVisible(true);
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                ConfigModule.saveConfig();
                try {
                    Bootstrap.destroyClient();
                }
                catch (Exception ex){

                }
                System.exit(0);


            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        setSize(1000,618);
        container.setBackground(Color.darkGray);
        SwingUtilities.updateComponentTreeUI(container);
    }
    public void addComponent(JComponent component) {
        //typeMap.put(component, type);
        this.getContentPane().add(component);
    }
}
