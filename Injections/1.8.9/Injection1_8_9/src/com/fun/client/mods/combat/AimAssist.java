package com.fun.client.mods.combat;

import com.fun.client.settings.Setting;
import com.fun.client.utils.Rotation.Rotation;
import com.fun.eventapi.event.events.EventRender3D;
import com.fun.eventapi.event.events.EventUpdate;
import com.fun.inject.injection.wrapper.impl.entity.EntityPlayerSPWrapper;
import com.fun.inject.injection.wrapper.impl.entity.EntityWrapper;
import com.fun.utils.math.MathHelper;
import com.fun.utils.math.vecmath.Vec3;

import javax.vecmath.Vector2f;

import static com.fun.client.FunGhostClient.registerManager;
import static com.fun.utils.rotation.RotationUtils.getAngleDifference;

import com.fun.client.mods.Category;
import com.fun.client.mods.Module;

public class AimAssist extends Module {
    public AimAssist() {
        super("AimAssist", Category.Combat);
    }

    public EntityWrapper target = null;
    public Rotation lastRotations = null;
    public Rotation rotations = null;
    private long lastClickTime = 0;
    private boolean aimAssistActive = false;

    public Setting requireClicking = new Setting("RequireClick", this, true);
    public Setting onAttack = new Setting("OnAttack", this, false);
    public Setting onRotate = new Setting("OnRotate", this, false);
    public Setting speed = new Setting("Speed", this, 30, 0, 100, false);
    public Setting fov = new Setting("FOV", this, 90, 0, 360, true);
    public Setting minFov = new Setting("MinFOV", this, 10, 0, 60, true);

    @Override
    public void onUpdate(EventUpdate event) {
        super.onUpdate(event);
        lastRotations = rotations;
        rotations = null;

        if (requireClicking.getValBoolean() && !checkClickActivation()) {
            aimAssistActive = false;
            return;
        }

        if (onAttack.getValBoolean() && !mc.getGameSettings().getKey("key.attack").isPressed()) {
            return;
        }

        target = registerManager.vModuleManager.target.target;
        EntityPlayerSPWrapper playersp = mc.getPlayer();

        if (target == null || playersp.isOpenGui()) return;

        Vector2f v = aim(new Vec3(playersp.getX(), playersp.getY() + playersp.getEyeHeight(), playersp.getZ()),
                new Vec3(target.getX(), target.getY() + target.getEyeHeight(), target.getZ()));

        this.rotations = new Rotation(v);
        if (!isInFOV(rotations, playersp)) {
            rotations = null;
        }
    }

    private boolean checkClickActivation() {
        if (mc.getGameSettings().getKey("key.attack").isPressed()) {
            lastClickTime = System.currentTimeMillis();
            aimAssistActive = true;
            return true;
        } else if (System.currentTimeMillis() - lastClickTime > 200) {
            return false;
        }
        return aimAssistActive;
    }

    @Override
    public void onRender3D(EventRender3D event) {
        super.onRender3D(event);
        EntityPlayerSPWrapper playersp = mc.getPlayer();

        if (shouldSkipRender()) return;

        Vector2f newRotations = new Vector2f(MathHelper.wrapAngleTo180_float((float) (this.lastRotations.getYaw() +
                        (this.rotations.getYaw() - this.lastRotations.getYaw()) * mc.getTimer().getRenderPartialTicks())), 0);
        adjustPlayerAngles(playersp, newRotations);
    }

    private boolean shouldSkipRender() {
        return rotations == null || lastRotations == null ||
                (mc.getMouseHelper().getDeltaY() == 0 && mc.getMouseHelper().getDeltaX() == 0 && onRotate.getValBoolean());
    }

    private void adjustPlayerAngles(EntityPlayerSPWrapper playersp, Vector2f newRotations) {
        final float strength = (float) speed.getValDouble();
        final float f = mc.getGameSettings().geMouseSensitivity() * 0.6F + 0.2F;
        final float gcd = f * f * f * 8.0F;

        int i = mc.getGameSettings().isInvertMouse() ? -1 : 1;
        float f2 = mc.getMouseHelper().getDeltaX() +
                (MathHelper.wrapAngleTo180_float(newRotations.x - mc.getPlayer().getYaw()) * (strength / 100) -
                        mc.getMouseHelper().getDeltaX()) * gcd;
        float f3 = mc.getMouseHelper().getDeltaY() -
                mc.getMouseHelper().getDeltaY() * gcd;

        playersp.setAngles(f2, f3 * i);
    }

    public static Vector2f aim(Vec3 player, Vec3 target) {
        double x = target.xCoord - player.xCoord;
        double z = target.zCoord - player.zCoord;
        double xz = Math.sqrt(x * x + z * z);

        return new Vector2f(-(float) (Math.atan2(target.yCoord - player.yCoord, xz) * (180 / Math.PI)),
                -(float) (Math.atan2(target.xCoord - player.xCoord, target.zCoord - player.zCoord) * (180 / Math.PI)));
    }

    private boolean isInFOV(Rotation targetRotation, EntityPlayerSPWrapper player) {
        float yawDifference = getAngleDifference(player.getYaw(), targetRotation.getYaw());
        float maxFov = (float) fov.getValDouble() / 2;
        float minFovValue = (float) minFov.getValDouble() / 2;  // FOV
        return Math.abs(yawDifference) <= maxFov && Math.abs(yawDifference) >= minFovValue;
    }
}
