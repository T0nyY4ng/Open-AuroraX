package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;

public class TickShiftHud extends HudElement {
   private static final Color WHITE = new Color();
   private final SettingGroup sgGeneral;
   private final SettingGroup sgShown;
   private final SettingGroup sgScale;
   private final SettingGroup sgBackground;
   private double originalWidth;
   private double originalHeight;
   private boolean needsCompile;
   private boolean recalculateSize;
   private int timer;
   public final Setting<Integer> updateDelay;
   public final Setting<Boolean> shadow;
   public final Setting<Integer> border;
   public final Setting<Shown> shown;
   public final Setting<String> condition;
   public final Setting<Boolean> customScale;
   public final Setting<Double> scale;
   public final Setting<Boolean> background;
   public final Setting<SettingColor> backgroundColor;
   private Script script;
   private Script conditionScript;
   private Section section;
   private boolean firstTick;
   private boolean empty;
   private boolean visible;
   public static final HudElementInfo<TickShiftHud> INFO;

   public TickShiftHud() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgShown = this.settings.createGroup("Shown");
      this.sgScale = this.settings.createGroup("Scale");
      this.sgBackground = this.settings.createGroup("Background");
      this.updateDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("update-delay")).description("Update delay in ticks")).defaultValue(4)).onChanged((integer) -> {
         if (this.timer > integer) {
            this.timer = integer;
         }

      })).min(0).build());
      this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("shadow")).description("Renders shadow behind text.")).defaultValue(true)).onChanged((aBoolean) -> this.recalculateSize = true)).build());
      this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("border")).description("How much space to add around the text.")).defaultValue(0)).onChanged((integer) -> super.setSize(this.originalWidth + (double)(integer * 2), this.originalHeight + (double)(integer * 2)))).build());
      this.shown = this.sgShown.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("shown")).description("When this text element is shown.")).defaultValue(TickShiftHud.Shown.Always)).onChanged((s) -> this.recompile())).build());
      this.condition = this.sgShown.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)(new StringSetting.Builder()).name("condition")).description("Condition to check when shown is not Always.")).visible(() -> this.shown.get() != TickShiftHud.Shown.Always)).defaultValue("")).onChanged((s) -> this.recompile())).renderer(StarscriptTextBoxRenderer.class).build());
      this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("custom-scale")).description("Applies custom text scale rather than the global one.")).defaultValue(false)).onChanged((integer) -> this.recalculateSize = true)).build());
      SettingGroup var10001 = this.sgScale;
      DoubleSetting.Builder var10002 = (DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("scale")).description("Custom scale.");
      Setting<Boolean> var10003 = this.customScale;
      Objects.requireNonNull(var10003);
      this.scale = var10001.add(((DoubleSetting.Builder)((DoubleSetting.Builder)var10002.visible(var10003::get)).defaultValue((double)1.0F).onChanged((integer) -> this.recalculateSize = true)).min((double)0.5F).sliderRange((double)0.5F, (double)3.0F).build());
      this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("background")).description("Displays background.")).defaultValue(false)).build());
      var10001 = this.sgBackground;
      ColorSetting.Builder var2 = (ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("background-color")).description("Color used for the background.");
      var10003 = this.background;
      Objects.requireNonNull(var10003);
      this.backgroundColor = var10001.add(((ColorSetting.Builder)var2.visible(var10003::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
      this.firstTick = true;
      this.empty = false;
      this.needsCompile = true;
   }

   private void recompile() {
      this.firstTick = true;
      this.needsCompile = true;
   }

   public void setSize(double width, double height) {
      this.originalWidth = width;
      this.originalHeight = height;
      super.setSize(width + (double)((Integer)this.border.get() * 2), height + (double)((Integer)this.border.get() * 2));
   }

   private void calculateSize(HudRenderer renderer) {
      double width = (double)0.0F;
      if (this.section != null) {
         String str = this.section.toString();
         if (!str.isBlank()) {
            width = renderer.textWidth(str, (Boolean)this.shadow.get(), this.getScale());
         }
      }

      if (width != (double)0.0F) {
         this.setSize(width, renderer.textHeight((Boolean)this.shadow.get(), this.getScale()));
         this.empty = false;
      } else {
         this.setSize((double)100.0F, renderer.textHeight((Boolean)this.shadow.get(), this.getScale()));
         this.empty = true;
      }

   }

   public void tick(HudRenderer renderer) {
      if (this.recalculateSize) {
         this.calculateSize(renderer);
         this.recalculateSize = false;
      }

      if (this.timer <= 0) {
         this.runTick(renderer);
         this.timer = (Integer)this.updateDelay.get();
      } else {
         --this.timer;
      }

   }

   private void runTick(HudRenderer renderer) {
      if (this.needsCompile) {
         Parser.Result result = Parser.parse("{meteor.get_module_info(\"Tick Shift\")}");
         if (result.hasErrors()) {
            this.script = null;
            this.section = new Section(0, ((Error)result.errors.get(0)).toString());
            this.calculateSize(renderer);
         } else {
            this.script = Compiler.compile(result);
         }

         if (this.shown.get() != TickShiftHud.Shown.Always) {
            this.conditionScript = Compiler.compile(Parser.parse((String)this.condition.get()));
         }

         this.needsCompile = false;
      }

      try {
         if (this.script != null) {
            this.section = MeteorStarscript.ss.run(this.script);
            this.calculateSize(renderer);
         }
      } catch (StarscriptError error) {
         this.section = new Section(0, error.getMessage());
         this.calculateSize(renderer);
      }

      if (this.shown.get() != TickShiftHud.Shown.Always && this.conditionScript != null) {
         String text = MeteorStarscript.run(this.conditionScript);
         if (text == null) {
            this.visible = false;
         } else {
            this.visible = this.shown.get() == TickShiftHud.Shown.WhenTrue ? text.equalsIgnoreCase("true") : text.equalsIgnoreCase("false");
         }
      }

      this.firstTick = false;
   }

   public void render(HudRenderer renderer) {
      if (this.firstTick) {
         this.runTick(renderer);
      }

      boolean visible = this.shown.get() == TickShiftHud.Shown.Always || this.visible;
      if ((this.empty || !visible) && this.isInEditor()) {
         renderer.line((double)this.x, (double)this.y, (double)(this.x + this.getWidth()), (double)(this.y + this.getHeight()), Color.GRAY);
         renderer.line((double)this.x, (double)(this.y + this.getHeight()), (double)(this.x + this.getWidth()), (double)this.y, Color.GRAY);
      }

      if (this.section != null && visible) {
         double x = (double)(this.x + (Integer)this.border.get());

         for(Section s = this.section; s != null; s = s.next) {
            x = renderer.text(s.text, x, (double)(this.y + (Integer)this.border.get()), getSectionColor(s.index), (Boolean)this.shadow.get(), this.getScale());
         }

         if ((Boolean)this.background.get()) {
            renderer.quad((double)this.x, (double)this.y, (double)this.getWidth(), (double)this.getHeight(), (Color)this.backgroundColor.get());
         }

      }
   }

   public void onFontChanged() {
      this.recalculateSize = true;
   }

   private double getScale() {
      return (Boolean)this.customScale.get() ? (Double)this.scale.get() : (double)-1.0F;
   }

   public static Color getSectionColor(int i) {
      List<SettingColor> colors = (List)Hud.get().textColors.get();
      return i >= 0 && i < colors.size() ? (Color)colors.get(i) : WHITE;
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "TickShiftHud", "Welcomes you.", TickShiftHud::new);
   }

   public static enum Shown {
      Always,
      WhenTrue,
      WhenFalse;

      public String toString() {
         String var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = "Always";
            case 1 -> var10000 = "When True";
            case 2 -> var10000 = "When False";
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      // $FF: synthetic method
      private static Shown[] $values() {
         return new Shown[]{Always, WhenTrue, WhenFalse};
      }
   }
}
