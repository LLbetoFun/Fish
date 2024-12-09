package com.fun.client.mods.combat;

import com.fun.client.mods.Category;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.eventapi.event.events.EventStrafe;
import com.fun.eventapi.event.events.EventUpdate;
import com.fun.inject.Mappings;
import com.fun.inject.injection.wrapper.impl.entity.EntityPlayerSPWrapper;
import com.fun.inject.injection.wrapper.impl.entity.EntityPlayerWrapper;
import com.fun.inject.injection.wrapper.impl.entity.EntityWrapper;
import com.fun.inject.injection.wrapper.impl.world.WorldClientWrapper;
import com.fun.utils.version.clazz.Classes;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class Target extends VModule {
    public Entity target = null;
    public ArrayList<Entity> bots = new ArrayList<>();
    public ArrayList<Entity> ts = new ArrayList<>();
    public Setting onlyPlayer = new Setting("OnlyPlayer", this, false);
    public Setting antiBot = new Setting("AntiBot", this, false);
    public Setting range = new Setting("Range", this, 6.0, 0, 6.0, false);
    public Setting invisible = new Setting("Invisible", this, false);
    public Setting teams = new Setting("Teams", this, false);
    public Setting armorColor = new Setting("ArmorColor", this, false){
        @Override
        public boolean isVisible() {
            return teams.getValBoolean();
        }
    };
    //armorColor
    public double dist = Double.MAX_VALUE;

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
        dist = Double.MAX_VALUE;
    }


    @Override
    public void onUpdate(EventUpdate event) {
        target = null;
        dist = Double.MAX_VALUE;
        LocalPlayer playersp = mc.player;

        for (Entity player : onlyPlayer.getValBoolean() ? mc.level.players() : mc.level.entitiesForRendering()) {
            double d1 = mc.player.distanceTo(player);
            if (player != playersp && d1 < range.getValDouble() && d1 < dist && player.isAlive()
                    && player instanceof LivingEntity && (invisible.getValBoolean() || !player.isInvisible())) {
                target = player;
                dist = d1;
            }
        }

        try {
            if (antiBot.getValBoolean()) {
                bots.clear();
                for (Player p : mc.level.players()) {
                    if (p == null) continue;
                    if (Classes.EntityPlayerSP.isInstanceof(p)) continue;

                    if (p.getDisplayName().getContents().startsWith(mc.player.getDisplayName().getContents().substring(1, 3))) {
                        bots.add(p);
                        continue;
                    }
                    if (mc.getConnection().getPlayerInfo(p.getUUID()) == null) {
                        bots.add(p);
                        continue;
                    }
                    if (p.isInvisible() && !invisible.getValBoolean()) {
                        bots.add(p);
                        continue;
                    }
                    if (p.getTeam() != null && mc.player.getTeam() != null && p.getTeam().isAlliedTo(mc.player.getTeam())) {
                        bots.add(p);
                    }
                }
                if (target != null && bots.contains(target)) target = null;
            }
            if(teams.getValBoolean()){
                ts.clear();
                for (Player p : mc.level.players()) {
                    if (p == null) continue;
                    if(p instanceof LocalPlayer)continue;
                    if(isSameTeam(p))ts.add(p);
                }
                if (target != null && ts.contains(target)) target = null;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Target() {
        super("Target", Category.Combat);
    }
    public boolean isSameTeam(LivingEntity entity) {
        if (this.armorColor.getValBoolean() && entity instanceof Player entityPlayer) {
            ItemStack myHead = (ItemStack)mc.player.getInventory().armor.get(3);
            ItemStack entityHead = (ItemStack)entityPlayer.getInventory().armor.get(3);
            if (!myHead.isEmpty() && !entityHead.isEmpty() && myHead.getItem() instanceof ArmorItem && entityHead.getItem() instanceof ArmorItem) {
                return this.getArmorColor(myHead) == this.getArmorColor(entityHead);
            }
        }

        return false;
    }

    private int getArmorColor(ItemStack stack) {
        return stack.getItem() instanceof DyeableLeatherItem ? ((DyeableLeatherItem)stack.getItem()).getColor(stack) : -1;
    }


}
