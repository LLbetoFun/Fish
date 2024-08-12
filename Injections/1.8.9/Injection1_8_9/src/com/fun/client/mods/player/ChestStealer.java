package com.fun.client.mods.player;

import com.fun.client.mods.Category;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.eventapi.event.events.EventUpdate;
import com.fun.inject.mapper.Mapper;
import com.fun.utils.math.Timer;
import com.fun.utils.version.fields.Fields;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.*;



public class ChestStealer extends VModule {
    public ChestStealer() {
        super("ChestStealer", Category.Player);
    }
    public Setting scaffoldBlocks=new Setting("ScaffoldBlocks",this,true);
    public Setting swords=new Setting("Swords",this,true);
    public Setting axe=new Setting("Axe",this,true);
    public Setting shovel=new Setting("Shovel",this,true);
    public Setting hoe=new Setting("Hoe",this,true);
    public Setting food=new Setting("Foods",this,true);
    public Setting armor=new Setting("Armor",this,true);
    public Setting drinks=new Setting("Drinks",this,true);
    public Setting delay=new Setting("Delay",this,175,0,300,true);
    public Timer timer=new Timer();
    private static final Field GuiChest_lowerChestInventory;


    static {
        try {
            GuiChest_lowerChestInventory = GuiChest.class.getDeclaredField(Mapper.getObfField( "lowerChestInventory","net/minecraft/client/gui/inventory/GuiChest"));
            GuiChest_lowerChestInventory.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdate(EventUpdate event) {
        super.onUpdate(event);
        if (mc.currentScreen instanceof GuiChest) {
            GuiChest chest=(GuiChest)mc.currentScreen;
            Map<Integer, ItemStack> itemStackMap=new HashMap<>();
            IInventory lowerChestInventory= null;
            try {
                lowerChestInventory = ((IInventory)GuiChest_lowerChestInventory.get(chest));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            String name = lowerChestInventory.getDisplayName().getUnformattedText().toLowerCase();
            String[] black_list = new String[]{"menu", "selector", "game", "gui", "server", "inventory", "play", "teleporter", "shop", "melee", "armor",
                    "block", "castle", "mini", "warp", "teleport", "user", "team", "tool", "sure", "trade", "cancel", "accept", "soul", "book", "recipe",
                    "profile", "tele", "port", "map", "kit", "select", "lobby", "vault", "lock", "anticheat", "travel", "settings", "user", "preference",
                    "compass", "cake", "wars", "buy", "upgrade", "ranged", "potions", "utility"};

            for (String str : black_list)
                if (name.contains(str))
                    return;
            for(int i=0;i<lowerChestInventory.getSizeInventory();i++){
                Slot slot = chest.inventorySlots.getSlot(i);
                if(slot.getHasStack())itemStackMap.put(i, slot.getStack());
            }
            Set<Integer> set=itemStackMap.keySet();
            List<Object> list=Arrays.asList(set.toArray());
            Collections.shuffle(list);
            if(timer.every((long) ((long) delay.getValDouble()+Math.random()*50L-25))&& !list.isEmpty()){
                click((Integer) list.get(0),chest.inventorySlots.windowId);
            }
            //ItemStack itemStack=chest.getSlot((In teger) list.get(0)).getItem();

        }
    }
    public void put(int slot1,int slot2,int ID){
        mc.playerController.windowClick(ID,slot1,slot2,2,mc.thePlayer);
    }
    public void click(int slot,int ID){
        mc.playerController.windowClick(ID,slot,0,1,mc.thePlayer);
    }
    public void drop(int slot,int ID){
        mc.playerController.windowClick(ID,slot,0,4,mc.thePlayer);
    }
    public void change(int slot1,int slot2,int ID){

        mc.playerController.windowClick(ID,slot1,0,0,mc.thePlayer);
        mc.playerController.windowClick(ID,slot2,0,0,mc.thePlayer);
        mc.playerController.windowClick(ID,slot1,0,0,mc.thePlayer);

    }
}
