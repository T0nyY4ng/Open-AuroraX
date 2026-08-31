package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;

public class Rsr extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<Double> girlScale;
   private final Setting<SideMode> side;
   private final Identifier textureId;
   public static final HudElementInfo<Rsr> INFO;

   public Rsr() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.girlScale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Rsr Scale")).description("Modify the size of the Rsr.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.side = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Kill Message Mode")).description("What kind of messages to send.")).defaultValue(Rsr.SideMode.Right)).build());
      this.textureId = Identifier.of("spacex", "rsr.png");
   }

   public void render(HudRenderer renderer) {
      this.setSize((double)832.0F * (Double)this.girlScale.get(), (double)1248.0F * (Double)this.girlScale.get());
      MatrixStack matrixStack = new MatrixStack();
      GL.bindTexture(this.textureId);
      Renderer2D.TEXTURE.begin();
      Renderer2D.TEXTURE.texQuad((double)this.x + (this.side.get() == SideMode.Left ? (Double)this.girlScale.get() * (double)832.0F : (double)0.0F), (double)this.y, (Double)this.girlScale.get() * (this.side.get() == SideMode.Left ? (Double)this.girlScale.get() * (double)-832.0F : (double)832.0F), (Double)this.girlScale.get() * (double)1248.0F, new Color(255, 255, 255, 255));
      Renderer2D.TEXTURE.render(matrixStack);
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "Rsr", "It's a Cat girl what do you want", Rsr::new);
   }

   public static enum SideMode {
      Right,
      Left;

      // $FF: synthetic method
      private static SideMode[] $values() {
         return new SideMode[]{Right, Left};
      }
   }
}
