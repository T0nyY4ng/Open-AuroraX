package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class PlaceRender extends Module {
   private final SettingGroup sgRender;
   private final Setting<render_type> type;
   private final Setting<Integer> renderTime;
   private final Setting<Integer> smoothness;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final Setting<Integer> fade;
   private final Setting<ShapeMode> shapeMode;
   private Box renderBoxOne;
   private Box renderBoxTwo;
   public static final List<Render> render = new ArrayList();

   public PlaceRender() {
      super(Aurora.RenderPlus, "PlaceRender", "Render Places");
      this.sgRender = this.settings.createGroup("Render");
      this.type = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Render Type")).description("Render type.")).defaultValue(PlaceRender.render_type.Fade)).build());
      this.renderTime = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("render-time")).description("How long to render placements.")).defaultValue(10)).min(0).sliderMax(20).visible(() -> this.type.get() == PlaceRender.render_type.Smooth)).build());
      this.smoothness = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Smoothness")).description("How smoothly the render should move around.")).defaultValue(10)).min(0).sliderMax(20).visible(() -> this.type.get() == PlaceRender.render_type.Smooth)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 50)).build());
      this.fade = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Fade time")).description("time")).defaultValue(1000)).max(5000).min(500).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
   }

   @EventHandler
   public void onEvent(PlaceBlockEvent event) {
      render.add(new Render(event.blockPos, System.currentTimeMillis()));
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if (this.type.get() == PlaceRender.render_type.Fade) {
         render.removeIf((r) -> System.currentTimeMillis() - r.time > (long)(Integer)this.fade.get());
         render.forEach((r) -> {
            double progress = (double)1.0F - (double)Math.min(System.currentTimeMillis() - r.time, 500L) / (double)500.0F;
            event.renderer.box(r.pos, new Color(((SettingColor)this.sideColor.get()).r, ((SettingColor)this.sideColor.get()).g, ((SettingColor)this.sideColor.get()).b, (int)Math.round((double)((SettingColor)this.sideColor.get()).a * progress)), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * progress)), (ShapeMode)this.shapeMode.get(), 0);
         });
      }

      if (this.type.get() == PlaceRender.render_type.Smooth) {
         render.removeIf((r) -> System.currentTimeMillis() - r.time > (long)(Integer)this.fade.get());
         if (render.size() == 0) {
            return;
         }

         for(Render renderp : render) {
            BlockPos renderPos = renderp.pos;
            if ((Integer)this.renderTime.get() <= 0) {
               return;
            }

            if (this.renderBoxOne == null) {
               this.renderBoxOne = new Box(renderPos);
            }

            if (this.renderBoxTwo == null) {
               this.renderBoxTwo = new Box(renderPos);
            } else {
               ((IBox)this.renderBoxTwo).set(renderPos);
            }

            double offsetX = (this.renderBoxTwo.minX - this.renderBoxOne.minX) / (double)(Integer)this.smoothness.get();
            double offsetY = (this.renderBoxTwo.minY - this.renderBoxOne.minY) / (double)(Integer)this.smoothness.get();
            double offsetZ = (this.renderBoxTwo.minZ - this.renderBoxOne.minZ) / (double)(Integer)this.smoothness.get();
            ((IBox)this.renderBoxOne).set(this.renderBoxOne.minX + offsetX, this.renderBoxOne.minY + offsetY, this.renderBoxOne.minZ + offsetZ, this.renderBoxOne.maxX + offsetX, this.renderBoxOne.maxY + offsetY, this.renderBoxOne.maxZ + offsetZ);
            event.renderer.box(this.renderBoxOne, (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
         }
      }

   }

   public static enum render_type {
      Fade,
      Smooth;

      // $FF: synthetic method
      private static render_type[] $values() {
         return new render_type[]{Fade, Smooth};
      }
   }

   public static record Render(BlockPos pos, long time) {
   }
}
