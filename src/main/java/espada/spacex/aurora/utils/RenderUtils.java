package espada.spacex.aurora.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
   public static final Matrix4f lastProjMat = new Matrix4f();
   public static final Matrix4f lastModMat = new Matrix4f();
   public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
   public static TextureColorProgram TEXTURE_COLOR_PROGRAM;
   public static GradientGlowProgram GRADIENT_GLOW_PROGRAM;
   private static float prevCircleStep;
   private static float circleStep;
   private static final VertexConsumerProvider.Immediate vertex;

   public static void initShaders() {
      if (GRADIENT_GLOW_PROGRAM == null) {
         GRADIENT_GLOW_PROGRAM = new GradientGlowProgram();
      }

      if (TEXTURE_COLOR_PROGRAM == null) {
         TEXTURE_COLOR_PROGRAM = new TextureColorProgram();
      }

   }

   public static Color injectAlpha(Color color, int alpha) {
      return new Color(color.r, color.g, color.b, MathHelper.clamp(alpha, 0, 255));
   }

   public static void drawTexture(DrawContext context, Identifier icon, int x, int y, int width, int height) {
      RenderSystem.blendEquation(32774);
      RenderSystem.blendFunc(770, 1);
      RenderSystem.enableBlend();
      RenderSystem.texParameter(3553, 10240, 9729);
      RenderSystem.texParameter(3553, 10241, 9987);
      context.drawTexture(icon, x, y, 0.0F, 0.0F, width, height, width, height);
   }

   public static void rounded(MatrixStack stack, float x, float y, float w, float h, float radius, int p, int color) {
      Matrix4f matrix4f = stack.peek().getPositionMatrix();
      float a = (float)Argb.getAlpha(color) / 255.0F;
      float r = (float)Argb.getRed(color) / 255.0F;
      float g = (float)Argb.getGreen(color) / 255.0F;
      float b = (float)Argb.getBlue(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
      corner(x + w, y, radius, 360, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      corner(x, y, radius, 270, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      corner(x, y + h, radius, 180, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      corner(x + w, y + h, radius, 90, (float)p, r, g, b, a, bufferBuilder, matrix4f);
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      RenderSystem.disableBlend();
   }

   public static void corner(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
      for(float i = (float)angle; i > (float)(angle - 90); i -= 90.0F / p) {
         bufferBuilder.vertex(matrix4f, (float)((double)x + Math.cos(Math.toRadians((double)i)) * (double)radius), (float)((double)y + Math.sin(Math.toRadians((double)i)) * (double)radius), 0.0F).color(r, g, b, a);
      }

   }

   public static void text(String text, MatrixStack stack, float x, float y, int color) {
      MeteorClient.mc.textRenderer.draw(text, x, y, color, false, stack.peek().getPositionMatrix(), vertex, TextLayerType.NORMAL, 0, 15728880);
      vertex.draw();
   }

   public static void quad(MatrixStack stack, float x, float y, float w, float h, int color) {
      Matrix4f matrix4f = stack.peek().getPositionMatrix();
      float a = (float)Argb.getAlpha(color) / 255.0F;
      float r = (float)Argb.getRed(color) / 255.0F;
      float g = (float)Argb.getGreen(color) / 255.0F;
      float b = (float)Argb.getBlue(color) / 255.0F;
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(matrix4f, x + w, y, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix4f, x, y + h, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix4f, x + w, y + h, 0.0F).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      RenderSystem.disableBlend();
   }

   public static Vec3d interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
      double x = (double)(prevposX + (posX - prevposX) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = (double)(prevposY + (posY - prevposY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY();
      double z = (double)(prevposZ + (posZ - prevposZ) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ();
      return new Vec3d(x, y, z);
   }

   public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
      Vec3d camera = MeteorClient.mc.getEntityRenderDispatcher().camera.getPos();
      int displayHeight = MeteorClient.mc.getWindow().getHeight();
      int[] viewport = new int[4];
      GL11.glGetIntegerv(2978, viewport);
      Vector3f target = new Vector3f();
      float deltaX = (float)(pos.x - camera.x);
      float deltaY = (float)(pos.y - camera.y);
      float deltaZ = (float)(pos.z - camera.z);
      Vector4f transformedCoordinates = (new Vector4f(deltaX, deltaY, deltaZ, 1.0F)).mul(lastWorldSpaceMatrix);
      Matrix4f matrixProj = new Matrix4f(lastProjMat);
      Matrix4f matrixModel = new Matrix4f(lastModMat);
      matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
      return new Vec3d((double)target.x / MeteorClient.mc.getWindow().getScaleFactor(), (double)((float)displayHeight - target.y) / MeteorClient.mc.getWindow().getScaleFactor(), (double)target.z);
   }

   public static void updateJello() {
      prevCircleStep = circleStep;
      circleStep += 0.15F;
   }

   public static void drawJello(MatrixStack matrix, Entity target, Color color) {
      double cs = (double)(prevCircleStep + (circleStep - prevCircleStep) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true));
      double prevSinAnim = absSinAnimation(cs - (double)0.45F);
      double sinAnim = absSinAnimation(cs);
      double x = target.prevX + (target.getX() - target.prevX) * (double)MeteorClient.mc.getRenderTickCounter().getTickDelta(true) - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = target.prevY + (target.getY() - target.prevY) * (double)MeteorClient.mc.getRenderTickCounter().getTickDelta(true) - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY() + prevSinAnim * (double)target.getHeight();
      double z = target.prevZ + (target.getZ() - target.prevZ) * (double)MeteorClient.mc.getRenderTickCounter().getTickDelta(true) - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ();
      double nextY = target.prevY + (target.getY() - target.prevY) * (double)MeteorClient.mc.getRenderTickCounter().getTickDelta(true) - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY() + sinAnim * (double)target.getHeight();
      matrix.push();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

      for(int i = 0; i <= 30; ++i) {
         float cos = (float)(x + Math.cos((double)i * 6.28 / (double)30.0F) * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * (double)0.5F);
         float sin = (float)(z + Math.sin((double)i * 6.28 / (double)30.0F) * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * (double)0.5F);
         bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float)nextY, sin).color(color.getPacked());
         bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float)y, sin).color(injectAlpha(color, 0).getPacked());
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      matrix.pop();
   }

   public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, int color) {
      float f = (float)(color >> 24 & 255) / 255.0F;
      float g = (float)(color >> 16 & 255) / 255.0F;
      float h = (float)(color >> 8 & 255) / 255.0F;
      float k = (float)(color & 255) / 255.0F;
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y + height, 0.0F).color(g, h, k, f);
      bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y + height, 0.0F).color(g, h, k, f);
      bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y, 0.0F).color(g, h, k, f);
      bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y, 0.0F).color(g, h, k, f);
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
   }

   private static double absSinAnimation(double input) {
      return Math.abs((double)1.0F + Math.sin(input)) / (double)2.0F;
   }

   static {
      vertex = MeteorClient.mc.getBufferBuilders().getEntityVertexConsumers();
   }
}
