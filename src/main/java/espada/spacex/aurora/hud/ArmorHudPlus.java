package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.utils.RenderUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;

public class ArmorHudPlus extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<Double> scale;
   private final Setting<Integer> rounding;
   private final Setting<Boolean> bg;
   private final Setting<SettingColor> bgColor;
   private final Setting<SettingColor> durColor;
   private final Setting<DurMode> durMode;
   public static final HudElementInfo<ArmorHudPlus> INFO;

   public ArmorHudPlus() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("Scale to render at.")).defaultValue((double)1.0F).range((double)0.0F, (double)5.0F).sliderRange((double)0.0F, (double)5.0F).build());
      this.rounding = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Rounding")).description("How rounded should the background be.")).defaultValue(50)).range(0, 100).sliderRange(0, 100).build());
      this.bg = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Background")).description("Renders a background behind armor pieces.")).defaultValue(false)).build());
      this.bgColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Background Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(0, 0, 0, 150)).build());
      this.durColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Durability Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.durMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Durability Mode")).description("Where should durability be rendered at.")).defaultValue(ArmorHudPlus.DurMode.Bottom)).build());
   }

   public void render(HudRenderer renderer) {
      if (MeteorClient.mc.player != null) {
         this.setSize((double)100.0F * (Double)this.scale.get() * (double)2.0F, (double)28.0F * (Double)this.scale.get() * (double)2.0F);
         MatrixStack stack = new MatrixStack();
         stack.translate((float)this.x, (float)this.y, 0.0F);
         stack.scale((float)((Double)this.scale.get() * (double)2.0F), (float)((Double)this.scale.get() * (double)2.0F), 1.0F);
         if ((Boolean)this.bg.get()) {
            RenderUtils.rounded(stack, (float)(Integer)this.rounding.get() * 0.14F, (float)(Integer)this.rounding.get() * 0.14F, 100.0F - (float)(Integer)this.rounding.get() * 0.28F, 28.0F - (float)(Integer)this.rounding.get() * 0.28F, (float)(Integer)this.rounding.get() * 0.14F, 10, ((SettingColor)this.bgColor.get()).getPacked());
         }

         MatrixStack drawStack = renderer.drawContext.getMatrices();
         drawStack.push();
         drawStack.translate((float)this.x, (float)this.y, 0.0F);
         drawStack.scale((float)((Double)this.scale.get() * (double)2.0F), (float)((Double)this.scale.get() * (double)2.0F), 1.0F);

         for(int i = 3; i >= 0; --i) {
            ItemStack itemStack = (ItemStack)MeteorClient.mc.player.getInventory().armor.get(i);
            renderer.drawContext.drawItem(itemStack, i * 20 + 12, this.durMode.get() == ArmorHudPlus.DurMode.Top ? 10 : 0);
            if (!itemStack.isEmpty()) {
               this.centeredText(stack, String.valueOf(Math.round(100.0F - (float)itemStack.getDamage() / (float)itemStack.getMaxDamage() * 100.0F)), i * 20 + 20, this.durMode.get() == ArmorHudPlus.DurMode.Top ? 3 : 17, ((SettingColor)this.durColor.get()).getPacked());
            }
         }

         drawStack.pop();
      }
   }

   private void centeredText(MatrixStack stack, String text, int x, int y, int color) {
      RenderUtils.text(text, stack, (float)x - (float)MeteorClient.mc.textRenderer.getWidth(text) / 2.0F, (float)y, color);
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "ArmorHud+", "A target hud the fuck you thinkin bruv.", ArmorHudPlus::new);
   }

   public static enum DurMode {
      Top,
      Bottom;

      // $FF: synthetic method
      private static DurMode[] $values() {
         return new DurMode[]{Top, Bottom};
      }
   }
}
