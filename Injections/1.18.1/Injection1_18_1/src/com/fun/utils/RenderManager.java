package com.fun.utils;

import com.fun.inject.injection.wrapper.impl.render.DefaultVertexFormats;
import com.fun.inject.injection.wrapper.impl.render.GlStateManagerWrapper;
import com.fun.inject.injection.wrapper.impl.render.Tessellator;
import com.fun.inject.injection.wrapper.impl.render.WorldRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.function.Supplier;

import static com.fun.utils.PacketUtils.mc;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR;

public class RenderManager {
    public static PoseStack currentPoseStack;
    public static void drawRoundedRect(int left, int top, int right, int bottom, int radius, int color) {
        left += radius;
        top += radius;
        bottom -= radius;
        right -= radius;
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (color >> 24 & 0xFF) / 255.0F;
        float f = (color >> 16 & 0xFF) / 255.0F;
        float f1 = (color >> 8 & 0xFF) / 255.0F;
        float f2 = (color & 0xFF) / 255.0F;
        setupRender();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuilder();

        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);//tryBlendFuncSeparate (IIII)V func_179120_a
        RenderSystem.setShaderColor(f, f1, f2, f3);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        worldrenderer.begin(VertexFormat.Mode.TRIANGLE_FAN, POSITION_COLOR);//POSITION field_181705_e
        //begin (ILnet/minecraft/client/renderer/vertex/VertexFormat;)V func_181668_a
        for (int cornerId = 0; cornerId < 4; cornerId++) {
            int ky = (cornerId + 1) / 2 % 2;
            int kx = cornerId / 2;
            double x = (kx != 0) ? right : left;
            double y = (ky != 0) ? bottom : top;
            for (int a = 0; a <= 8; a++)
                worldrenderer
                        .vertex(currentPoseStack.last().pose(), (float) (x + Math.sin(0.19634954084936207D * a + Math.PI * cornerId / 2.0D) * radius), (float) (y + Math.cos(0.19634954084936207D * a + Math.PI * cornerId / 2.0D) * radius), 0.0F)
                        .color(color).endVertex();
        }

        tessellator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        endRender();

    }
    public static double roundToDecimal(double n, int point) {
        if (point == 0) {
            return Math.floor(n);
        }
        double factor = Math.pow(10, point);
        return Math.round(n * factor) / factor;
    }
    public static void renderRoundedQuadInternal2(Matrix4f matrix, float cr, float cg, float cb, float ca, float cr1, float cg1, float cb1, float ca1, float cr2, float cg2, float cb2, float ca2, float cr3, float cg3, float cb3, float ca3, double fromX, double fromY, double toX, double toY, double radC1) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, POSITION_COLOR);

        double[][] map = new double[][]{new double[]{toX - radC1, toY - radC1, radC1}, new double[]{toX - radC1, fromY + radC1, radC1}, new double[]{fromX + radC1, fromY + radC1, radC1}, new double[]{fromX + radC1, toY - radC1, radC1}};

        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90; r < (90 + i * 90); r += 10) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                switch (i) {
                    case 0 ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr1, cg1, cb1, ca1);
                    case 1 ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
                    case 2 ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr2, cg2, cb2, ca2);
                    default ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr3, cg3, cb3, ca3);
                }
            }
        }
        Tesselator.getInstance().end();
        //BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
    public static void drawRound(PoseStack matrices, float x, float y, float width, float height, float radius, Color color) {
        renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius, 4);
    }

    public static void renderRoundedQuad(PoseStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        renderRoundedQuadInternal(matrices.last().pose(), c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }
    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        Tesselator tl=Tesselator.getInstance();
        BufferBuilder bufferBuilder = tl.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, POSITION_COLOR);
        double[][] map = new double[][]{new double[]{toX - radius, toY - radius, radius}, new double[]{toX - radius, fromY + radius, radius}, new double[]{fromX + radius, fromY + radius, radius}, new double[]{fromX + radius, toY - radius, radius}};
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
            }
            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
        }
        tl.end();
    }
    private static void endRender() {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
    private static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
    public static void renderEntityBoundingBox(PoseStack poseStack, int type, Entity entity, int color, boolean damage) {
        if (entity instanceof LivingEntity) {
            EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
            double x = entity.xOld + (entity.getX() - entity.xOld) * (double)mc.getFrameTime() - renderManager.camera.getPosition().x();
            double y = entity.yOld + (entity.getY() - entity.yOld) * (double)mc.getFrameTime() - renderManager.camera.getPosition().y();
            double z = entity.zOld + (entity.getZ() - entity.zOld) * (double)mc.getFrameTime() - renderManager.camera.getPosition().z();
            float scale = 0.03F;
            if (entity instanceof Player && damage && ((Player)entity).hurtTime > 0) {
                color = Color.RED.getRGB();
            }

            RenderSystem.disableDepthTest();
            switch (type) {
                case 0:
                    poseStack.pushPose();
                    poseStack.translate(x, y, z);
                    poseStack.mulPose(new Quaternion(0.0F, -renderManager.camera.getYRot(), 0.0F, true));
                    poseStack.scale(scale, scale, scale);
                    int outline = Color.BLACK.getRGB();
                    drawRect(poseStack, -20, -1, -26, 75, outline);
                    drawRect(poseStack, 20, -1, 26, 75, outline);
                    drawRect(poseStack, -20, -1, 21, 5, outline);
                    drawRect(poseStack, -20, 70, 21, 75, outline);
                    if (color != 0) {
                        drawRect(poseStack, -21, 0, -25, 74, color);
                        drawRect(poseStack, 21, 0, 25, 74, color);
                        drawRect(poseStack, -21, 0, 24, 4, color);
                        drawRect(poseStack, -21, 71, 25, 74, color);
                    } else {
                        int startColor = rainbowDraw(2L, 0L);
                        int endColor = rainbowDraw(2L, 1000L);
                        drawGradientRect(poseStack, -21, 0, -25, 74, startColor, endColor);
                        drawGradientRect(poseStack, 21, 0, 25, 74, startColor, endColor);
                        drawRect(poseStack, -21, 0, 21, 4, endColor);
                        drawRect(poseStack, -21, 71, 21, 74, startColor);
                    }

                    RenderSystem.enableDepthTest();
                    poseStack.popPose();
            }
        }
    }
    public static void drawRect(PoseStack poseStack, int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int j = left;
            left = right;
            right = j;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.setShader(new Supplier<ShaderInstance>() {
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float)left, (float)bottom, 0.0F).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF).endVertex();
        bufferbuilder.vertex(matrix, (float)right, (float)bottom, 0.0F)
                .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF)
                .endVertex();
        bufferbuilder.vertex(matrix, (float)right, (float)top, 0.0F).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF).endVertex();
        bufferbuilder.vertex(matrix, (float)left, (float)top, 0.0F).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF).endVertex();
        tessellator.end();
        RenderSystem.enableTexture();
    }
    public static void drawGradientRect(PoseStack poseStack, int left, int top, int right, int bottom, int startColor, int endColor) {
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.setShader(new Supplier<ShaderInstance>() {
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float startAlpha = (float)(startColor >> 24 & 0xFF) / 255.0F;
        float startRed = (float)(startColor >> 16 & 0xFF) / 255.0F;
        float startGreen = (float)(startColor >> 8 & 0xFF) / 255.0F;
        float startBlue = (float)(startColor & 0xFF) / 255.0F;
        float endAlpha = (float)(endColor >> 24 & 0xFF) / 255.0F;
        float endRed = (float)(endColor >> 16 & 0xFF) / 255.0F;
        float endGreen = (float)(endColor >> 8 & 0xFF) / 255.0F;
        float endBlue = (float)(endColor & 0xFF) / 255.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float)right, (float)top, 0.0F).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        bufferbuilder.vertex(matrix, (float)left, (float)top, 0.0F).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        bufferbuilder.vertex(matrix, (float)left, (float)bottom, 0.0F).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        bufferbuilder.vertex(matrix, (float)right, (float)bottom, 0.0F).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
    public static int rainbowDraw(long speed, long... delay) {
        long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
        return Color.getHSBColor((float)(time % (15000L / speed)) / (15000.0F / (float)speed), 1.0F, 1.0F).getRGB();
    }


}
