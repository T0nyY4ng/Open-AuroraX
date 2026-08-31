package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CrystalESP extends Modules {
   private final SettingGroup sgRender;
   private final Setting<RenderMode> renderMode;
   private final Setting<Boolean> outline;
   private final Setting<Boolean> box;
   private final Setting<SettingColor> lineColor;
   public final Setting<SettingColor> sideColor;
   private final Setting<Double> animationExponent;
   private long lastTime;
   private double renderProgress;
   private RenderMode sgGeneral;

   public CrystalESP() {
      super(Aurora.RenderPlus, "CrystalESP", "Test TorllHack Render");
      this.sgRender = this.settings.createGroup("Render");
      this.renderMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Render Mode")).description("The mode to render in.")).defaultValue(CrystalESP.RenderMode.Normal)).build());
      this.outline = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("outline")).description("")).defaultValue(false)).visible(() -> this.renderMode.get() == CrystalESP.RenderMode.Normal)).build());
      this.box = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("box")).description("")).defaultValue(false)).visible(() -> this.renderMode.get() == CrystalESP.RenderMode.Normal)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Line color of rendered stuff")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Side color of rendered stuff")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.animationExponent = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Exponent")).description("How fast should boze mode box grow.")).defaultValue((double)3.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> this.renderMode.get() == CrystalESP.RenderMode.Test)).build());
      this.lastTime = 0L;
      this.renderProgress = (double)0.0F;
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      for(Entity crystal : this.mc.world.getEntities()) {
         if (crystal instanceof EndCrystalEntity) {
            this.EntityEsp(crystal, event);
         }
      }

   }

   public void EntityEsp(Entity entity, Render3DEvent event) {
      double delta = (double)((float)(System.currentTimeMillis() - this.lastTime) / 1000.0F);
      this.lastTime = System.currentTimeMillis();
      BlockPos pos = new BlockPos(entity.getBlockX(), entity.getBlockY() - 1, entity.getBlockZ());
      BlockPos axisAlignedBB = new BlockPos(pos);
      this.renderProgress = Math.max((double)0.0F, this.renderProgress - delta);
      double r = (double)0.5F - Math.pow((double)1.0F - this.renderProgress, (Double)this.animationExponent.get()) / (double)2.0F;
      Box box = new Box((double)axisAlignedBB.getX() + (double)0.5F - r, (double)axisAlignedBB.getY() + (double)-0.5F - r + (double)1.0F, (double)axisAlignedBB.getZ() + (double)0.5F - r, (double)axisAlignedBB.getX() + (double)0.5F + r, (double)axisAlignedBB.getY() + (double)-0.5F + r + (double)1.0F, (double)axisAlignedBB.getZ() + (double)0.5F + r);
      if (this.renderMode.get() == CrystalESP.RenderMode.Normal) {
         if ((Boolean)this.outline.get()) {
            event.renderer.box(axisAlignedBB, (Color)this.sideColor.get(), (Color)this.lineColor.get(), ShapeMode.Lines, 0);
         }

         if ((Boolean)this.box.get()) {
            event.renderer.box(axisAlignedBB, (Color)this.sideColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
         }
      } else {
         if ((Boolean)this.outline.get()) {
            event.renderer.box(box, (Color)this.sideColor.get(), (Color)this.lineColor.get(), ShapeMode.Lines, 0);
         }

         if ((Boolean)this.box.get()) {
            event.renderer.box(box, (Color)this.sideColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
         }
      }

   }

   public static enum RenderMode {
      Normal,
      Test;

      // $FF: synthetic method
      private static RenderMode[] $values() {
         return new RenderMode[]{Normal, Test};
      }
   }
}
