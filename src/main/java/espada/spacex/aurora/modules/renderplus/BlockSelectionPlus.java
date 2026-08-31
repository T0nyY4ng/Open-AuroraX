package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.RenderUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;

public class BlockSelectionPlus extends Modules {
   long time = 0L;
   private final SettingGroup sgGeneral;
   private long lastMillis;
   BlockPos bp2;
   private final Setting<ShapeMode> shapeMode;
   public final Setting<SettingColor> color;
   private final Setting<SettingColor> lineColor;
   private final Setting<Double> animationMoveExponent;
   private final Setting<Double> animationSpeed;
   private final Setting<FadeMode> fade;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private Vec3d renderTarget;
   private double renderProgress;
   private Vec3d renderPos;

   public BlockSelectionPlus() {
      super(Aurora.RenderPlus, "block-selection-plus", "Modifies how your block selection is rendered.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.lastMillis = System.currentTimeMillis();
      this.bp2 = null;
      this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
      this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Side color of rendered boxes")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.animationMoveExponent = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Move Exponent")).description("Moves faster when longer away from the target.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.animationSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Move Speed")).description("How fast should aurora mode box move.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fade = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("FadeMode")).description("FadeMode")).defaultValue(BlockSelectionPlus.FadeMode.Normal)).build());
      this.renderTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.renderTarget = null;
      this.renderProgress = (double)0.0F;
      this.renderPos = null;
   }

   public void onActivate() {
      super.onActivate();
      this.renderPos = null;
      this.renderProgress = (double)0.0F;
      this.lastMillis = System.currentTimeMillis();
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.crosshairTarget != null) {
         HitResult var3 = this.mc.crosshairTarget;
         if (var3 instanceof BlockHitResult) {
            BlockHitResult result = (BlockHitResult)var3;
            double var11 = (double)0.0F;
            if (result.isInsideBlock()) {
               return;
            }

            double delta = (double)((float)(System.currentTimeMillis() - this.lastMillis) / 1000.0F);
            this.lastMillis = System.currentTimeMillis();
            BlockPos bp = result.getBlockPos();
            BlockState state = this.mc.world.getBlockState(bp);
            VoxelShape shape = state.getOutlineShape(this.mc.world, bp);
            if (shape.isEmpty()) {
               if (this.fade.get() == BlockSelectionPlus.FadeMode.Normal) {
                  this.bp2 = bp;
               }

               if (System.currentTimeMillis() - this.time > 1000L) {
                  return;
               }

               if (this.time != 0L) {
                  var11 = (double)1.0F - Math.min((double)(System.currentTimeMillis() - this.time) + (Double)this.renderTime.get() * (double)1000.0F, (Double)this.fadeTime.get() * (double)1000.0F) / ((Double)this.fadeTime.get() * (double)1000.0F);
               }
            } else {
               this.bp2 = bp;
               this.time = System.currentTimeMillis();
            }

            this.renderProgress = Math.min((double)1.0F, this.renderProgress + delta);
            this.renderTarget = (new Vec3d((double)this.bp2.getX(), (double)this.bp2.getY(), (double)this.bp2.getZ())).add((double)0.0F, (double)1.0F, (double)0.0F);
            this.renderPos = this.smoothMove(this.renderPos, this.renderTarget, delta * (Double)this.animationSpeed.get() * (double)5.0F);
            Box box = new Box(this.renderPos.getX(), this.renderPos.getY() - (double)1.0F, this.renderPos.getZ(), this.renderPos.getX() + (double)1.0F, this.renderPos.getY(), this.renderPos.getZ() + (double)1.0F);
            event.renderer.box(box, shape.isEmpty() ? RenderUtils.injectAlpha((Color)this.color.get(), (int)Math.round((double)((SettingColor)this.color.get()).a * var11)) : (Color)this.color.get(), shape.isEmpty() ? RenderUtils.injectAlpha((Color)this.lineColor.get(), (int)Math.round((double)((SettingColor)this.lineColor.get()).a * var11)) : (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
            return;
         }
      }

   }

   private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
      if (current == null) {
         return target;
      } else {
         double absX = Math.abs(current.x - target.x);
         double absY = Math.abs(current.y - target.y);
         double absZ = Math.abs(current.z - target.z);
         double x = (absX + Math.pow(absX, (Double)this.animationMoveExponent.get() - (double)1.0F)) * delta;
         double y = (absX + Math.pow(absY, (Double)this.animationMoveExponent.get() - (double)1.0F)) * delta;
         double z = (absX + Math.pow(absZ, (Double)this.animationMoveExponent.get() - (double)1.0F)) * delta;
         return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x), current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y), current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
      }
   }

   public static enum FadeMode {
      Normal,
      Disabled;

      // $FF: synthetic method
      private static FadeMode[] $values() {
         return new FadeMode[]{Normal, Disabled};
      }
   }
}
