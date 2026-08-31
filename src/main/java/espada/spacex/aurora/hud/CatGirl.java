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

public class CatGirl extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<Double> girlScale;
   private final Setting<SideMode> side;
   private final Identifier catgirl;
   public static final HudElementInfo<CatGirl> INFO;

   public CatGirl() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.girlScale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Catgirl Scale")).description("Modify the size of the Catgirl.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.side = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Kill Message Mode")).description("What kind of messages to send.")).defaultValue(CatGirl.SideMode.Right)).build());
      this.catgirl = Identifier.of("spacex", "catgirl.png");
   }

   public void render(HudRenderer renderer) {
      this.setSize((double)450.0F * (Double)this.girlScale.get(), (double)755.0F * (Double)this.girlScale.get());
      MatrixStack matrixStack = new MatrixStack();
      GL.bindTexture(this.catgirl);
      Renderer2D.TEXTURE.begin();
      Renderer2D.TEXTURE.texQuad((double)this.x + (this.side.get() == CatGirl.SideMode.Left ? (Double)this.girlScale.get() * (double)450.0F : (double)0.0F), (double)this.y, (Double)this.girlScale.get() * (this.side.get() == CatGirl.SideMode.Left ? (Double)this.girlScale.get() * (double)-450.0F : (double)450.0F), (Double)this.girlScale.get() * (double)755.0F, new Color(255, 255, 255, 255));
      Renderer2D.TEXTURE.render(matrixStack);
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "catgirl", "It's a Cat girl what do you want", CatGirl::new);
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
