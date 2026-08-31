package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.util.math.MatrixStack;

public class GearHud extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<List<Item>> items;
   private final Setting<Double> scale;
   private final Setting<SettingColor> color;
   private final Setting<Boolean> shadow;
   private final Setting<Boolean> experienceInfo;
   public static final HudElementInfo<GearHud> INFO;

   public GearHud() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.items = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)(new ItemListSetting.Builder()).name("Items")).description("Items to show.")).defaultValue(new Item[]{Items.END_CRYSTAL, Items.EXPERIENCE_BOTTLE, Items.OBSIDIAN, Items.TOTEM_OF_UNDYING}).build());
      this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("The scale.")).defaultValue((double)1.5F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Shadow")).description("Renders a shadow behind the chars.")).defaultValue(true)).build());
      this.experienceInfo = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Experience Info")).description("Displays mend percentage for armor next to experience bottles.")).defaultValue(true)).build());
   }

   public void render(HudRenderer renderer) {
      this.setSize((double)55.0F * (Double)this.scale.get() * (Double)this.scale.get(), (double)20.0F * (Double)this.scale.get() * (Double)this.scale.get() * (double)((List)this.items.get()).size());

      for(int i = 0; i < ((List)this.items.get()).size(); ++i) {
         int posY = (int)Math.round((double)this.y + (double)(i * 20) * (Double)this.scale.get() * (Double)this.scale.get());
         MatrixStack drawStack = renderer.drawContext.getMatrices();
         drawStack.push();
         drawStack.translate((float)this.x, (float)this.y, 0.0F);
         drawStack.scale((float)((Double)this.scale.get() * (Double)this.scale.get()), (float)((Double)this.scale.get() * (Double)this.scale.get()), 1.0F);
         renderer.drawContext.drawItem(((Item)((List)this.items.get()).get(i)).getDefaultStack(), this.x, posY);
         drawStack.pop();
         renderer.text(this.getText(((Item)((List)this.items.get()).get(i)).asItem()), (double)this.x + (double)25.0F * (Double)this.scale.get() * (Double)this.scale.get(), (double)posY, (Color)this.color.get(), (Boolean)this.shadow.get(), (Double)this.scale.get());
      }

   }

   private int amountOf(Item item) {
      return InvUtils.find((itemStack) -> itemStack.getItem().equals(item)).count();
   }

   private String getText(Item item) {
      if (item == Items.EXPERIENCE_BOTTLE && this.armorDur() > (double)0.0F && (Boolean)this.experienceInfo.get()) {
         int var10000 = this.amountOf(item);
         return var10000 + "  " + Math.round((double)(this.amountOf(item) * 14) / this.armorDur() * (double)100.0F) + "%";
      } else {
         return String.valueOf(this.amountOf(item));
      }
   }

   private double armorDur() {
      double rur = (double)0.0F;
      if (MeteorClient.mc.player != null) {
         for(int i = 0; i < 4; ++i) {
            rur += (double)((ItemStack)MeteorClient.mc.player.getInventory().armor.get(i)).getMaxDamage();
         }
      }

      return rur;
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "GearHud", "Gear.", GearHud::new);
   }
}
