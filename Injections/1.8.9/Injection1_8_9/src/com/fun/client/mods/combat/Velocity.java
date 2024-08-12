package com.fun.client.mods.combat;

import com.fun.client.settings.Setting;
import com.fun.eventapi.event.events.EventMoment;
import com.fun.eventapi.event.events.EventPacket;
import com.fun.inject.injection.wrapper.impl.entity.EntityWrapper;
import com.fun.inject.injection.wrapper.impl.network.packets.server.S12PacketEntityVelocityWrapper;
import com.fun.utils.version.clazz.Classes;

import java.util.Random;

import static com.fun.client.FunGhostClient.registerManager;
import com.fun.client.mods.Module;

public class Velocity extends Module {
    private final Random random = new Random();
    public EntityWrapper target = null;

    public Setting mode = new Setting("Mode", this, "Vanilla", new String[]{"Vanilla", "JumpReset"});
    public Setting horizontalMin = new Setting("Horizontal Min", this, 80.0, 0.0, 100.0, false) {
        @Override
        public boolean isVisible() {
            return mode.getValString().equalsIgnoreCase("Vanilla");
        }
    };
    public Setting horizontalMax = new Setting("Horizontal Max", this, 100.0, 0.0, 100.0, false) {
        @Override
        public boolean isVisible() {
            return mode.getValString().equalsIgnoreCase("Vanilla");
        }
    };
    public Setting verticalMin = new Setting("Vertical Min", this, 80.0, 0.0, 100.0, false) {
        @Override
        public boolean isVisible() {
            return mode.getValString().equalsIgnoreCase("Vanilla");
        }
    };
    public Setting verticalMax = new Setting("Vertical Max", this, 100.0, 0.0, 100.0, false) {
        @Override
        public boolean isVisible() {
            return mode.getValString().equalsIgnoreCase("Vanilla");
        }
    };

    public Setting chance = new Setting("Chance", this, 100.0, 0.0, 100.0, true);
    public Setting waterCheck = new Setting("WaterCheck", this, true);
    public Setting fov = new Setting("FOV", this, 90.0, 0.0, 180.0, false);

    private double getRandomMultiplier(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private boolean isTargetInFOV(EntityWrapper target) {
        double dx = target.getX() - mc.getPlayer().getX();
        double dz = target.getZ() - mc.getPlayer().getZ();
        double angle = Math.atan2(dz, dx) * (180 / Math.PI);
        double playerYaw = mc.getPlayer().getYaw();

        double angleDifference = Math.abs(playerYaw - angle);
        return angleDifference <= fov.getValDouble() / 2; // 正确检查视野范围内
    }

    @Override
    public void onPacket(EventPacket packet) {
        super.onPacket(packet);
        if (Classes.S12PACKET_ENTITY_VELOCITY.isInstanceof(packet.packet)) {
            try {
                S12PacketEntityVelocityWrapper packetVelocity = new S12PacketEntityVelocityWrapper(packet.packet);
                if (packetVelocity.getEntityID() == mc.getPlayer().getEntityID()) {
                    target = registerManager.vModuleManager.target.target;
                    if (target != null && isTargetInFOV(target)) { // 仅在目标存在且在视野范围内继续执行
                        if (this.mode.getValString().equalsIgnoreCase("Vanilla")) {
                            if (waterCheck.getValBoolean() && mc.getPlayer().isInWater()) {
                                return;
                            }
                            if (random.nextDouble() * 100 < chance.getValDouble()) {
                                double horizontalMultiplier = getRandomMultiplier(horizontalMin.getValDouble(), horizontalMax.getValDouble()) / 100.0;
                                double verticalMultiplier = getRandomMultiplier(verticalMin.getValDouble(), verticalMax.getValDouble()) / 100.0;
                                packetVelocity.setMotionX((int) (packetVelocity.getMotionX() * horizontalMultiplier));
                                packetVelocity.setMotionY((int) (packetVelocity.getMotionY() * verticalMultiplier));
                                packetVelocity.setMotionZ((int) (packetVelocity.getMotionZ() * horizontalMultiplier));
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMoment(EventMoment event) {
        super.onMoment(event);
        target = registerManager.vModuleManager.target.target;
        if (target != null && isTargetInFOV(target)) { // 仅在目标存在且在视野范围内继续执行
            if (mode.getValString().equalsIgnoreCase("JumpReset")) {
                if (mc.getPlayer().getHurtTime() == 9 && mc.getPlayer().isOnGround()) {
                    if (waterCheck.getValBoolean() && mc.getPlayer().isInWater()) {
                        return;
                    }
                    if (random.nextDouble() * 100 < chance.getValDouble()) {
                        mc.getPlayer().getMovementInputObj().setJump(true);
                    }
                }
            }
        }
    }
}

