package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.MathHelper;

public class Keys extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<Double> scale;
   private final Setting<SettingColor> textColor;
   private final Setting<SettingColor> cTextColor;
   private final Setting<Boolean> textBG;
   private final Setting<SettingColor> bgColor;
   private final Setting<SettingColor> cbgColor;
   private final Setting<Mode> mode;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private List<Key> keys;
   public static final HudElementInfo<Keys> INFO;

   public Keys() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("Scale to render at.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.textColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Key Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(30, 30, 30, 255)).build());
      this.cTextColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Clicked Key Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.textBG = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Key Background")).description("Should there be a background for keys.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgGeneral;
      ColorSetting.Builder var10002 = ((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("BG Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(50, 50, 50, 255));
      Setting<Boolean> var10003 = this.textBG;
      Objects.requireNonNull(var10003);
      this.bgColor = var10001.add(((ColorSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgGeneral;
      var10002 = ((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Clicked BG Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(50, 50, 50, 255));
      var10003 = this.textBG;
      Objects.requireNonNull(var10003);
      this.cbgColor = var10001.add(((ColorSetting.Builder)var10002.visible(var10003::get)).build());
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description("Mode for key locations.")).defaultValue(Keys.Mode.Basic)).build());
      this.renderTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("Seconds to keep full color before fading.")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.fadeTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How many seconds should fading take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).build());
      this.keys = null;
   }

   public void render(HudRenderer renderer) {
      if (this.keys == null) {
         this.keys = new ArrayList();
         KeyBinding[] binds = new KeyBinding[]{MeteorClient.mc.options.forwardKey, MeteorClient.mc.options.leftKey, MeteorClient.mc.options.backKey, MeteorClient.mc.options.rightKey};

         for(int i = 0; i < 4; ++i) {
            KeyBinding bind = binds[i];
            String key = bind.getBoundKeyLocalizedText().getString().toUpperCase();
            this.keys.add(new Key(key, bind, i));
         }
      }

      double var10001;
      switch (((Mode)this.mode.get()).ordinal()) {
         case 0 -> var10001 = (double)160.0F;
         case 1 -> var10001 = (double)40.0F;
         case 2 -> var10001 = (double)120.0F;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      var10001 = var10001 * (Double)this.scale.get() * (Double)this.scale.get();
      double var10002;
      switch (((Mode)this.mode.get()).ordinal()) {
         case 0 -> var10002 = (double)40.0F;
         case 1 -> var10002 = (double)160.0F;
         case 2 -> var10002 = (double)80.0F;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      this.setSize(var10001, var10002 * (Double)this.scale.get() * (Double)this.scale.get());
      this.keys.forEach((keyx) -> {
         keyx.updatePos();
         keyx.checkClick();
         if ((Boolean)this.textBG.get()) {
            renderer.quad(keyx.posX + (double)2.0F * (Double)this.scale.get() * (Double)this.scale.get(), keyx.posY + (double)2.0F * (Double)this.scale.get() * (Double)this.scale.get(), (double)36.0F * (Double)this.scale.get() * (Double)this.scale.get(), (double)36.0F * (Double)this.scale.get() * (Double)this.scale.get(), this.getBGColor(keyx));
         }

         renderer.text(keyx.key, keyx.posX + this.xOffset(keyx.key, renderer), keyx.posY + this.yOffset(renderer), this.getTextColor(keyx), false, (Double)this.scale.get());
      });
   }

   private Color getBGColor(Key k) {
      return this.lerpColor(MathHelper.clamp(((double)k.sinceClick() - (Double)this.renderTime.get() * (double)1000.0F) / (Double)this.fadeTime.get() / (double)1000.0F, (double)0.0F, (double)1.0F), (Color)this.cbgColor.get(), (Color)this.bgColor.get());
   }

   private Color getTextColor(Key k) {
      return this.lerpColor(MathHelper.clamp(((double)k.sinceClick() - (Double)this.renderTime.get() * (double)1000.0F) / (Double)this.fadeTime.get() / (double)1000.0F, (double)0.0F, (double)1.0F), (Color)this.cTextColor.get(), (Color)this.textColor.get());
   }

   private Color lerpColor(double delta, Color s, Color e) {
      return new Color((int)Math.round(MathHelper.lerp(delta, (double)s.r, (double)e.r)), (int)Math.round(MathHelper.lerp(delta, (double)s.g, (double)e.g)), (int)Math.round(MathHelper.lerp(delta, (double)s.b, (double)e.b)), (int)Math.round(MathHelper.lerp(delta, (double)s.a, (double)e.a)));
   }

   private double xOffset(String string, HudRenderer renderer) {
      return ((double)20.0F - renderer.textWidth(string, false) / (double)2.0F) * (Double)this.scale.get() * (Double)this.scale.get();
   }

   private double yOffset(HudRenderer renderer) {
      return ((double)20.0F - renderer.textHeight(false) / (double)2.0F) * (Double)this.scale.get() * (Double)this.scale.get();
   }

   private double getX(int i) {
      double var10000;
      switch (((Mode)this.mode.get()).ordinal()) {
         case 0 -> var10000 = (double)(i * 40);
         case 1 -> var10000 = (double)0.0F;
         case 2 -> var10000 = i == 0 ? (double)40.0F : (double)((i - 1) * 40);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private double getY(int i) {
      double var10000;
      switch (((Mode)this.mode.get()).ordinal()) {
         case 0 -> var10000 = (double)0.0F;
         case 1 -> var10000 = (double)(i * 40);
         case 2 -> var10000 = i == 0 ? (double)0.0F : (double)40.0F;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "Keys", "Draws pressed movement keys.", Keys::new);
   }

   private class Key {
      public final String key;
      public final KeyBinding bind;
      public final int i;
      public double posX = (double)0.0F;
      public double posY = (double)0.0F;
      public long lastClicked = 0L;

      public Key(String key, KeyBinding bind, int i) {
         this.key = key;
         this.bind = bind;
         this.i = i;
      }

      public void updatePos() {
         this.posX = (double)Keys.this.x + Keys.this.getX(this.i) * (Double)Keys.this.scale.get() * (Double)Keys.this.scale.get();
         this.posY = (double)Keys.this.y + Keys.this.getY(this.i) * (Double)Keys.this.scale.get() * (Double)Keys.this.scale.get();
      }

      public void checkClick() {
         if (this.bind.isPressed()) {
            this.lastClicked = System.currentTimeMillis();
         }

      }

      public long sinceClick() {
         return System.currentTimeMillis() - this.lastClicked;
      }
   }

   public static enum Mode {
      Horizontal,
      Vertical,
      Basic;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Horizontal, Vertical, Basic};
      }
   }
}
