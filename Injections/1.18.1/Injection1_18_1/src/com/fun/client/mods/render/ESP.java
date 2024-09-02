package com.fun.client.mods.render;

import com.fun.client.mods.Category;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.client.utils.ColorUtils;
import com.fun.eventapi.event.events.EventRender3D;
import com.fun.utils.EntityUtils;
import com.fun.utils.RenderManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;

public class ESP extends VModule {
    public ESP() {
        super("ESP", Category.Render);
    }
    private final Setting playersValue = new Setting("Players", this, true);
    private final Setting mobsValue = new Setting("Mobs", this, false);
    private final Setting animalsValue = new Setting("Animals", this, false);
    private final Setting deadValue = new Setting("Dead", this, false);
    private final Setting invisibleValue = new Setting("Invisible", this, false);



    @Override
    public void onRender3D(EventRender3D event) {
        if (mc.player != null && mc.level != null) {
            PoseStack poseStack = RenderManager.currentPoseStack;

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)entity;
                    if (EntityUtils.isSelected(
                            entity,
                            this.playersValue.getValBoolean(),
                            this.mobsValue.getValBoolean(),
                            this.animalsValue.getValBoolean(),
                            this.deadValue.getValBoolean(),
                            this.invisibleValue.getValBoolean(),
                            true
                    )) {
                        RenderManager.renderEntityBoundingBox(poseStack, 0, livingEntity, rainbow(10, 1).getRGB(), true);
                    }
                }
            }
        }
    }
    public static Color rainbow(int speed, int index) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        float hue = (float)angle / 360.0F;
        return new Color(Color.HSBtoRGB(hue, 0.7F, 1.0F));
    }
}
