package espada.spacex.aurora.modules.renderplus;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.MathUtils;
import espada.spacex.aurora.utils.RenderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

public class Particles extends Modules {
   private final SettingGroup sgFireFiles;
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> fireFilesSet;
   private final Setting<Integer> ffcount;
   private final Setting<Double> ffsize;
   private final Setting<Mode> mode;
   private final Setting<Integer> count;
   private final Setting<Double> size;
   private final Setting<SettingColor> color;
   private final Identifier star;
   private final Identifier snowflake;
   private final Identifier vanillaSnowflake;
   private final Identifier firefly;
   private final ArrayList<ParticleBase> fireFlies;
   private final ArrayList<ParticleBase> particles;

   public Particles() {
      super(Aurora.RenderPlus, "Particles", "Render some particles to make your game look better.");
      this.sgFireFiles = this.settings.createGroup("FireFlies");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.fireFilesSet = this.sgFireFiles.add(((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("FireFiles")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgFireFiles;
      IntSetting.Builder var10002 = ((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("FFCount")).defaultValue(30)).min(20).max(200);
      Setting<Boolean> var10003 = this.fireFilesSet;
      Objects.requireNonNull(var10003);
      this.ffcount = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgFireFiles;
      DoubleSetting.Builder var2 = ((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("FFSize")).defaultValue((double)1.0F).min(0.1).max((double)2.0F);
      var10003 = this.fireFilesSet;
      Objects.requireNonNull(var10003);
      this.ffsize = var10001.add(((DoubleSetting.Builder)var2.visible(var10003::get)).build());
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).defaultValue(Particles.Mode.Snowflake)).build());
      this.count = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Count")).min(20).max(8000).defaultValue(100)).build());
      this.size = this.sgGeneral.add(((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Size")).defaultValue((double)1.0F).min(0.1).max((double)6.0F).build());
      this.color = this.sgGeneral.add(((ColorSetting.Builder)(new ColorSetting.Builder()).name("Color")).defaultValue(new SettingColor(255, 255, 255)).build());
      this.star = Identifier.of("aurora", "textures/star.png");
      this.snowflake = Identifier.of("aurora", "textures/snowflake.png");
      this.vanillaSnowflake = Identifier.of("textures/environment/snow.png");
      this.firefly = Identifier.of("aurora", "textures/firefly.png");
      this.fireFlies = new ArrayList();
      this.particles = new ArrayList();
   }

   @EventHandler
   public void onTick(TickEvent.Pre event) {
      this.fireFlies.removeIf(ParticleBase::tick);
      this.particles.removeIf(ParticleBase::tick);

      for(int i = this.fireFlies.size(); i < (Integer)this.ffcount.get(); ++i) {
         if ((Boolean)this.fireFilesSet.get()) {
            this.fireFlies.add(new FireFly((float)(this.mc.player.getX() + (double)MathUtils.random(-25.0F, 25.0F)), (float)(this.mc.player.getY() + (double)MathUtils.random(2.0F, 15.0F)), (float)(this.mc.player.getZ() + (double)MathUtils.random(-25.0F, 25.0F)), MathUtils.random(-0.2F, 0.2F), MathUtils.random(-0.1F, 0.1F), MathUtils.random(-0.2F, 0.2F)));
         }
      }

      for(int j = this.particles.size(); j < (Integer)this.count.get(); ++j) {
         if (this.mode.get() != Particles.Mode.Off) {
            this.particles.add(new ParticleBase((float)(this.mc.player.getX() + (double)MathUtils.random(-48.0F, 48.0F)), (float)(this.mc.player.getY() + (double)MathUtils.random(2.0F, 48.0F)), (float)(this.mc.player.getZ() + (double)MathUtils.random(-48.0F, 48.0F)), MathUtils.random(-0.4F, 0.4F), MathUtils.random(-0.1F, 0.1F), MathUtils.random(-0.4F, 0.4F)));
         }
      }

   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if ((Boolean)this.fireFilesSet.get()) {
         event.matrices.push();
         RenderSystem.setShaderTexture(0, this.firefly);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(() -> RenderUtils.TEXTURE_COLOR_PROGRAM.backingProgram);
         BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         this.fireFlies.forEach((p) -> p.render(bufferBuilder));
         BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
         RenderSystem.disableBlend();
         event.matrices.pop();
      }

      if (this.mode.get() != Particles.Mode.Off) {
         event.matrices.push();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(() -> RenderUtils.TEXTURE_COLOR_PROGRAM.backingProgram);
         BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         this.particles.forEach((p) -> p.render(bufferBuilder));
         BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
         RenderSystem.disableBlend();
         event.matrices.pop();
      }

   }

   public static enum Mode {
      Off,
      Star,
      Snowflake,
      VanillaSnowflake,
      Firefly;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Off, Star, Snowflake, VanillaSnowflake, Firefly};
      }
   }

   public class FireFly extends ParticleBase {
      private final List<Trail> trails = new ArrayList();

      public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
         super(posX, posY, posZ, motionX, motionY, motionZ);
      }

      public boolean tick() {
         double var10001 = (double)this.posX;
         double var10002 = (double)this.posY;
         if (Particles.this.mc.player.squaredDistanceTo(var10001, var10002, (double)this.posZ) > (double)100.0F) {
            this.age -= 4;
         } else if (!Particles.this.mc.world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).isAir()) {
            this.age -= 8;
         } else {
            --this.age;
         }

         if (this.age < 0) {
            return true;
         } else {
            this.trails.removeIf(Trail::update);
            this.prevposX = this.posX;
            this.prevposY = this.posY;
            this.prevposZ = this.posZ;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.trails.add(Particles.this.new Trail(new Vec3d((double)this.prevposX, (double)this.prevposY, (double)this.prevposZ), new Vec3d((double)this.posX, (double)this.posY, (double)this.posZ), (Color)Particles.this.color.get()));
            this.motionX *= 0.99F;
            this.motionY *= 0.99F;
            this.motionZ *= 0.99F;
            return false;
         }
      }

      public void render(BufferBuilder bufferBuilder) {
         RenderSystem.setShaderTexture(0, Particles.this.firefly);
         if (!this.trails.isEmpty()) {
            Camera camera = Particles.this.mc.gameRenderer.getCamera();

            for(Trail ctx : this.trails) {
               Vec3d pos = ctx.interpolate(1.0F);
               MatrixStack matrices = new MatrixStack();
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
               matrices.translate(pos.x, pos.y, pos.z);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
               Matrix4f matrix = matrices.peek().getPositionMatrix();
               float size = ((Double)Particles.this.ffsize.get()).floatValue();
               bufferBuilder.vertex(matrix, 0.0F, -size, 0.0F).texture(0.0F, 1.0F).color(RenderUtils.injectAlpha(ctx.color(), (int)((double)(255.0F * ((float)this.age / (float)this.maxAge)) * ctx.animation(Particles.this.mc.getRenderTickCounter().getTickDelta(true)))).getPacked());
               bufferBuilder.vertex(matrix, -size, -size, 0.0F).texture(1.0F, 1.0F).color(RenderUtils.injectAlpha(ctx.color(), (int)((double)(255.0F * ((float)this.age / (float)this.maxAge)) * ctx.animation(Particles.this.mc.getRenderTickCounter().getTickDelta(true)))).getPacked());
               bufferBuilder.vertex(matrix, -size, 0.0F, 0.0F).texture(1.0F, 0.0F).color(RenderUtils.injectAlpha(ctx.color(), (int)((double)(255.0F * ((float)this.age / (float)this.maxAge)) * ctx.animation(Particles.this.mc.getRenderTickCounter().getTickDelta(true)))).getPacked());
               bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(RenderUtils.injectAlpha(ctx.color(), (int)((double)(255.0F * ((float)this.age / (float)this.maxAge)) * ctx.animation(Particles.this.mc.getRenderTickCounter().getTickDelta(true)))).getPacked());
            }
         }

      }
   }

   private class Trail {
      private final Vec3d from;
      private final Vec3d to;
      private final Color color;
      private int ticks;
      private int prevTicks;

      public Trail(Vec3d from, Vec3d to, Color color) {
         this.from = from;
         this.to = to;
         this.ticks = 10;
         this.color = color;
      }

      public Vec3d interpolate(float pt) {
         double x = this.from.x + (this.to.x - this.from.x) * (double)pt - Particles.this.mc.getEntityRenderDispatcher().camera.getPos().getX();
         double y = this.from.y + (this.to.y - this.from.y) * (double)pt - Particles.this.mc.getEntityRenderDispatcher().camera.getPos().getY();
         double z = this.from.z + (this.to.z - this.from.z) * (double)pt - Particles.this.mc.getEntityRenderDispatcher().camera.getPos().getZ();
         return new Vec3d(x, y, z);
      }

      public double animation(float pt) {
         return (double)((float)this.prevTicks + (float)(this.ticks - this.prevTicks) * pt) / (double)10.0F;
      }

      public boolean update() {
         this.prevTicks = this.ticks;
         return this.ticks-- <= 0;
      }

      public Color color() {
         return this.color;
      }
   }

   public class ParticleBase {
      protected float prevposX;
      protected float prevposY;
      protected float prevposZ;
      protected float posX;
      protected float posY;
      protected float posZ;
      protected float motionX;
      protected float motionY;
      protected float motionZ;
      protected int age;
      protected final int maxAge;

      public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
         this.posX = posX;
         this.posY = posY;
         this.posZ = posZ;
         this.prevposX = posX;
         this.prevposY = posY;
         this.prevposZ = posZ;
         this.motionX = motionX;
         this.motionY = motionY;
         this.motionZ = motionZ;
         this.age = (int)MathUtils.random(100.0F, 300.0F);
         this.maxAge = this.age;
      }

      public boolean tick() {
         double var10001 = (double)this.posX;
         double var10002 = (double)this.posY;
         if (Particles.this.mc.player.squaredDistanceTo(var10001, var10002, (double)this.posZ) > (double)4096.0F) {
            this.age -= 8;
         } else {
            --this.age;
         }

         if (this.age < 0) {
            return true;
         } else {
            this.prevposX = this.posX;
            this.prevposY = this.posY;
            this.prevposZ = this.posZ;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.motionX *= 0.9F;
            this.motionY *= 0.9F;
            this.motionZ *= 0.9F;
            this.motionY -= 0.001F;
            return false;
         }
      }

      public void render(BufferBuilder bufferBuilder) {
         switch (((Mode)Particles.this.mode.get()).ordinal()) {
            case 1 -> RenderSystem.setShaderTexture(0, Particles.this.star);
            case 2 -> RenderSystem.setShaderTexture(0, Particles.this.snowflake);
            case 3 -> RenderSystem.setShaderTexture(0, Particles.this.vanillaSnowflake);
            case 4 -> RenderSystem.setShaderTexture(0, Particles.this.firefly);
         }

         MatrixStack matrices = new MatrixStack();
         Camera camera = Particles.this.mc.gameRenderer.getCamera();
         Vec3d pos = RenderUtils.interpolatePos(this.prevposX, this.prevposY, this.prevposZ, this.posX, this.posY, this.posZ);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
         matrices.translate(pos.x, pos.y, pos.z);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
         Matrix4f matrix1 = matrices.peek().getPositionMatrix();
         float fSize = ((Double)Particles.this.size.get()).floatValue();
         bufferBuilder.vertex(matrix1, 0.0F, -fSize, 0.0F).texture(0.0F, 1.0F).color(RenderUtils.injectAlpha((Color)Particles.this.color.get(), (int)(255.0F * ((float)this.age / (float)this.maxAge))).getPacked());
         bufferBuilder.vertex(matrix1, -fSize, -fSize, 0.0F).texture(1.0F, 1.0F).color(RenderUtils.injectAlpha((Color)Particles.this.color.get(), (int)(255.0F * ((float)this.age / (float)this.maxAge))).getPacked());
         bufferBuilder.vertex(matrix1, -fSize, 0.0F, 0.0F).texture(1.0F, 0.0F).color(RenderUtils.injectAlpha((Color)Particles.this.color.get(), (int)(255.0F * ((float)this.age / (float)this.maxAge))).getPacked());
         bufferBuilder.vertex(matrix1, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(RenderUtils.injectAlpha((Color)Particles.this.color.get(), (int)(255.0F * ((float)this.age / (float)this.maxAge))).getPacked());
      }
   }
}
