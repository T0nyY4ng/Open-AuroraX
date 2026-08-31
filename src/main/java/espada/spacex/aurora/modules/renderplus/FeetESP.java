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
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class FeetESP extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> friend;
   private final Setting<Boolean> other;
   private final Setting<Boolean> self;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final Setting<Double> range;

   public FeetESP() {
      super(Aurora.RenderPlus, "Feet ESP", "No, it doesn't show you pictures of feet.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.friend = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Friend")).description("Renders friends' feet.")).defaultValue(true)).build());
      this.other = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Other")).description("Renders other players' feet.")).defaultValue(true)).build());
      this.self = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Self")).description("Renders own feet.")).defaultValue(true)).build());
      this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of feet should be rendered")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color of the feet outlines.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.sideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color of the feet sides.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Range")).description("Renders feet inside this range.")).defaultValue((double)25.0F).min((double)0.0F).sliderRange((double)0.0F, (double)25.0F).build());
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         this.mc.world.getPlayers().forEach((player) -> {
            if (!((double)player.distanceTo(this.mc.player) > (Double)this.range.get())) {
               if ((Boolean)this.friend.get() || !Friends.get().isFriend(player)) {
                  if ((Boolean)this.other.get() || player == this.mc.player || Friends.get().isFriend(player)) {
                     if ((Boolean)this.self.get() || this.mc.player != player) {
                        this.render(event, new Vec3d(MathHelper.lerp((double)this.mc.getRenderTickCounter().getTickDelta(true), player.prevX, player.getX()), MathHelper.lerp((double)this.mc.getRenderTickCounter().getTickDelta(true), player.prevY, player.getY()), MathHelper.lerp((double)this.mc.getRenderTickCounter().getTickDelta(true), player.prevZ, player.getZ())));
                     }
                  }
               }
            }
         });
      }
   }

   private void render(Render3DEvent event, Vec3d vec) {
      event.renderer.sideHorizontal(vec.x - 0.3, vec.y, vec.z - 0.3, vec.x + 0.3, vec.z + 0.3, (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get());
   }
}
