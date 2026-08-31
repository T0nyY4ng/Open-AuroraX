package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.CrystalUtil;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import org.joml.Vector3d;

public class MaoJunQingAura extends Modules {
   private double renderProgress = (double)0.0F;
   private long lastMillis = System.currentTimeMillis();
   double dmg;
   double self;
   Vector3d vec;
   private final List<Render> renderBlocks = new ArrayList();
   private final SettingGroup sgGeneral;
   private final SettingGroup sgDamage;
   private final SettingGroup sgRender;
   private final SettingGroup sgDev;
   private final Setting<Boolean> toggleModules;
   private final Setting<Boolean> toggleBack;
   private final Setting<List<Module>> modules;
   private final Setting<Boolean> pauseEat;
   private final Setting<SwitchMode> switchMode;
   private final Setting<LogicMode> logicMode;
   private final Setting<Double> speed;
   private final Setting<Double> minDmg;
   private final Setting<Double> maxDmg;
   private final Setting<Double> minRatio;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> interactSwing;
   private final Setting<SwingHand> interactHand;
   private final Setting<Boolean> damage;
   private final Setting<Double> damageScale;
   private final Setting<SettingColor> damageColor;
   private final Setting<FadeMode> fadeMode;
   private final Setting<Double> animationSpeed;
   private final Setting<Double> animationMoveExponent;
   private final Setting<Double> animationExponent;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   public final Setting<SettingColor> color;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private final Setting<Integer> Predict;
   private final Setting<Integer> Radius;
   private BlockPos[] blocks;
   private int lastIndex;
   private int length;
   private long tickTime;
   private double bestDmg;
   private long lastTime;
   private Vec3d renderTarget;
   private BlockPos placePos;
   private PlaceData placeData;
   private BlockPos calcPos;
   private PlaceData calcData;
   private Vec3d renderPos;
   private List<PlayerEntity> targets;
   private final Map<BlockPos, Anchor> anchors;
   private final ArrayList<Module> toActivate;
   double timer;

   public MaoJunQingAura() {
      super(Aurora.CombatPlus, "MaoZedong Aura", "Automatically destroys people using anchors.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgDamage = this.settings.createGroup("Damage");
      this.sgRender = this.settings.createGroup("Render");
      this.sgDev = this.settings.createGroup("Dev");
      this.toggleModules = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Modules")).description("Turn off other modules when Cev Breaker is activated.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Back On")).description("Turn the modules back on when Cev Breaker is deactivated.")).defaultValue(false);
      Setting<Boolean> var10003 = this.toggleModules;
      Objects.requireNonNull(var10003);
      this.toggleBack = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgGeneral;
      ModuleListSetting.Builder var6 = (ModuleListSetting.Builder)((ModuleListSetting.Builder)(new ModuleListSetting.Builder()).name("modules")).description("Which modules to toggle.");
      var10003 = this.toggleModules;
      Objects.requireNonNull(var10003);
      this.modules = var10001.add(((ModuleListSetting.Builder)var6.visible(var10003::get)).build());
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when you are eating.")).defaultValue(true)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Switching method. Silent is the most reliable but doesn't work everywhere.")).defaultValue(MaoJunQingAura.SwitchMode.Silent)).build());
      this.logicMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Logic Mode")).description("Logic for bullying kids.")).defaultValue(MaoJunQingAura.LogicMode.BreakPlace)).build());
      this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("How many anchors should be blown every second.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.minDmg = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Min Damage")).description("Minimum damage required to place.")).defaultValue((double)8.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.maxDmg = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Max Damage")).description("Maximum damage to self.")).defaultValue((double)6.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.minRatio = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Min Damage Ratio")).description("Damage ratio between enemy damage and self damage (enemy / self).")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var7 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var7.visible(var10003::get)).build());
      this.interactSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Interact Swing")).description("Renders swing animation when interacting with a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var7 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Interact Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.interactSwing;
      Objects.requireNonNull(var10003);
      this.interactHand = var10001.add(((EnumSetting.Builder)var7.visible(var10003::get)).build());
      this.damage = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Damage")).description("Renders Damage.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      DoubleSetting.Builder var9 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("damage-scale")).description("How big the damage text should be.")).defaultValue((double)1.25F).min((double)1.0F).sliderMax((double)4.0F);
      var10003 = this.damage;
      Objects.requireNonNull(var10003);
      this.damageScale = var10001.add(((DoubleSetting.Builder)var9.visible(var10003::get)).build());
      var10001 = this.sgRender;
      ColorSetting.Builder var10 = ((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Render Damage")).description("Renders Damage.")).defaultValue(new SettingColor(255, 255, 255, 255));
      var10003 = this.damage;
      Objects.requireNonNull(var10003);
      this.damageColor = var10001.add(((ColorSetting.Builder)var10.visible(var10003::get)).build());
      this.fadeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Fade Mode")).description("How long the fading should take.")).defaultValue(MaoJunQingAura.FadeMode.Normal)).build());
      this.animationSpeed = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Move Speed")).description("How fast should aurora mode box move.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.animationMoveExponent = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Move Exponent")).description("Moves faster when longer away from the target.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.animationExponent = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Exponent")).description("How fast should aurora mode box grow.")).defaultValue((double)3.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of render should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Line color of rendered boxes")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.color = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Side color of rendered boxes")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.Predict = this.sgDev.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("PredictTicks")).description("PredictTicks.")).defaultValue(2)).min(0).sliderRange(0, 10).build());
      this.Radius = this.sgDev.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Radius")).description("Radius.")).defaultValue(2)).min(0).sliderRange(0, 10).build());
      this.blocks = new BlockPos[0];
      this.lastIndex = 0;
      this.length = 0;
      this.tickTime = -1L;
      this.bestDmg = (double)-1.0F;
      this.lastTime = 0L;
      this.renderTarget = null;
      this.placePos = null;
      this.placeData = null;
      this.calcPos = null;
      this.calcData = null;
      this.renderPos = null;
      this.targets = new ArrayList();
      this.anchors = new HashMap();
      this.toActivate = new ArrayList();
      this.timer = (double)0.0F;
   }

   @EventHandler(
      priority = 200
   )
   private void onTickPre(TickEvent.Post event) {
      this.calculate(this.length - 1);
      this.placePos = this.calcPos;
      this.placeData = this.calcData;
      this.blocks = this.getBlocks(this.mc.player.getEyePos(), Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));
      this.tickTime = System.currentTimeMillis();
      this.length = this.blocks.length;
      this.lastIndex = 0;
      this.bestDmg = (double)-1.0F;
      this.calcPos = null;
      this.calcData = null;
      this.updateTargets();
   }

   public void onActivate() {
      if ((Boolean)this.toggleModules.get() && !((List<Module>)(List<?>)this.modules.get()).isEmpty() && this.mc.world != null && this.mc.player != null) {
         for(Module module : (List<Module>)(List<?>)this.modules.get()) {
            if (module.isActive()) {
               module.toggle();
               this.toActivate.add(module);
            }
         }
      }

      super.onActivate();
      this.renderPos = null;
      this.renderProgress = (double)0.0F;
      this.lastMillis = System.currentTimeMillis();
   }

   public void onDeactivate() {
      if ((Boolean)this.toggleBack.get() && !this.toActivate.isEmpty() && this.mc.world != null && this.mc.player != null) {
         for(Module module : this.toActivate) {
            if (!module.isActive()) {
               module.toggle();
            }
         }
      }

   }

   @EventHandler(
      priority = 201
   )
   private void onRender(Render3DEvent event) {
      this.renderBlocks.removeIf((rx) -> System.currentTimeMillis() - rx.time > 1000L);
      this.renderBlocks.forEach((rx) -> {
         double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - rx.time) + (Double)this.renderTime.get() * (double)1000.0F, (Double)this.fadeTime.get() * (double)1000.0F) / ((Double)this.fadeTime.get() * (double)1000.0F);
         event.renderer.box(rx.blockPos, RenderUtils.injectAlpha((Color)this.color.get(), (int)Math.round((double)((SettingColor)this.color.get()).a * progress)), RenderUtils.injectAlpha((Color)this.lineColor.get(), (int)Math.round((double)((SettingColor)this.lineColor.get()).a * progress)), (ShapeMode)this.shapeMode.get(), 0);
      });
      double delta = (double)((float)(System.currentTimeMillis() - this.lastMillis) / 1000.0F);
      this.timer += delta;
      this.lastMillis = System.currentTimeMillis();
      if (this.tickTime >= 0L && this.mc.player != null && this.mc.world != null) {
         if (this.pauseCheck()) {
            this.update();
         }

         if (this.placePos != null && this.pauseCheck()) {
            this.renderProgress = Math.min((double)1.0F, this.renderProgress + delta);
            this.renderTarget = (new Vec3d((double)this.placePos.getX(), (double)this.placePos.getY(), (double)this.placePos.getZ())).add((double)0.0F, (double)1.0F, (double)0.0F);
         } else {
            this.renderProgress = Math.max((double)0.0F, this.renderProgress - delta);
         }

         if (this.renderTarget != null) {
            this.renderPos = this.smoothMove(this.renderPos, this.renderTarget, delta * (Double)this.animationSpeed.get() * (double)5.0F);
         }

         if (this.renderPos != null) {
            double r = (double)0.5F - Math.pow((double)1.0F - this.renderProgress, (Double)this.animationExponent.get()) / (double)2.0F;
            if (r >= 0.001 && this.fadeMode.get() != MaoJunQingAura.FadeMode.Test2) {
               double down = (double)-0.5F;
               double up = (double)-0.5F;
               double width = (double)0.5F;
               int a = 0;
               switch (((FadeMode)this.fadeMode.get()).ordinal()) {
                  case 0:
                     up = (double)0.0F;
                     down = -(r * (double)2.0F);
                     break;
                  case 1:
                     up = (double)-1.0F + r * (double)2.0F;
                     down = (double)-1.0F;
                     break;
                  case 2:
                     up = (double)-0.5F + r;
                     down = (double)-0.5F - r;
                     width = r;
                     break;
                  case 3:
                     up = (double)0.0F;
                     down = (double)-1.0F;
                     a = (int)(-r * (double)100.0F);
               }

               Box box = new Box(this.renderPos.getX() + (double)0.5F - width, this.renderPos.getY() + down, this.renderPos.getZ() + (double)0.5F - width, this.renderPos.getX() + (double)0.5F + width, this.renderPos.getY() + up, this.renderPos.getZ() + (double)0.5F + width);
               event.renderer.box(box, new Color(((SettingColor)this.color.get()).r, ((SettingColor)this.color.get()).g, ((SettingColor)this.color.get()).b, ((SettingColor)this.color.get()).a - a), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
            }
         }

      }
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (this.tickTime >= 0L && this.mc.player != null && this.mc.world != null) {
         if (this.placePos != null && this.pauseCheck()) {
            this.vec = new Vector3d(this.renderPos.getX() + (double)0.5F, this.renderPos.getY() - (double)0.5F, this.renderPos.getZ() + (double)0.5F);
         }

         if (this.vec != null && NametagUtils.to2D(this.vec, (Double)this.damageScale.get())) {
            NametagUtils.begin(this.vec);
            TextRenderer.get().begin((double)1.0F, false, true);
            String var10000 = String.format("%.1f", this.dmg);
            String text = var10000 + "/" + String.format("%.1f", this.self);
            double w = TextRenderer.get().getWidth(text) * (double)0.5F;
            TextRenderer.get().render(text, -w, (double)0.0F, (Color)this.damageColor.get(), true);
            TextRenderer.get().end();
            NametagUtils.end();
         }

      }
   }

   private boolean pauseCheck() {
      return !(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem();
   }

   private void calculate(int index) {
      for(int i = this.lastIndex; i < index; ++i) {
         BlockPos pos = this.blocks[i];
         this.dmg = this.getDmg(pos);
         this.self = BODamageUtils.anchorDamage(this.mc.player, this.mc.player.getBoundingBox().offset(CrystalUtil.getMotionVec(this.mc.player, (Integer)this.Predict.get(), true)), pos, pos.toCenterPos());
         if (this.dmgCheck(this.dmg, this.self)) {
            PlaceData data = SettingUtils.getPlaceData(pos);
            if (data.valid() && !pos.equals(this.getMinePos()) && !EntityUtils.intersectsWithEntity(new Box(pos), (entity) -> !(entity instanceof ItemEntity))) {
               this.calcData = data;
               this.calcPos = pos;
               this.bestDmg = this.dmg;
            }
         }
      }

      this.lastIndex = index;
   }

   private BlockPos getMinePos() {
      return ((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).targetPos();
   }

   private void updateTargets() {
      List<PlayerEntity> players = new ArrayList();
      double closestDist = (double)1000.0F;

      for(int i = 3; i > 0; --i) {
         PlayerEntity closest = null;

         for(PlayerEntity player : this.mc.world.getPlayers()) {
            if (!players.contains(player) && !Friends.get().isFriend(player) && player != this.mc.player) {
               double dist = (double)player.distanceTo(this.mc.player);
               if (!(dist > (double)15.0F) && (closest == null || dist < closestDist)) {
                  closestDist = dist;
                  closest = player;
               }
            }
         }

         if (closest != null) {
            players.add(closest);
         }
      }

      this.targets = players;
   }

   private BlockPos[] getBlocks(Vec3d middle, double radius) {
      ArrayList<BlockPos> result = new ArrayList();
      int i = (int)Math.ceil(radius);

      for(int x = -i; x <= i; ++x) {
         for(int y = -i; y <= i; ++y) {
            for(int z = -i; z <= i; ++z) {
               BlockPos pos = new BlockPos((int)(Math.floor(middle.x) + (double)x), (int)(Math.floor(middle.y) + (double)y), (int)(Math.floor(middle.z) + (double)z));
               if ((OLEPOSSUtils.replaceable(pos) || this.mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) && this.inRangeToTargets(pos) && SettingUtils.inPlaceRange(pos)) {
                  result.add(pos);
               }
            }
         }
      }

      return (BlockPos[])result.toArray(new BlockPos[0]);
   }

   private boolean inRangeToTargets(BlockPos pos) {
      for(PlayerEntity target : this.targets) {
         if (target.getPos().add((double)0.0F, (double)1.0F, (double)0.0F).distanceTo(Vec3d.ofCenter(pos)) < (double)3.5F) {
            return true;
         }
      }

      return false;
   }

   private void update() {
      if (this.placePos != null && this.placeData != null && this.placeData.valid()) {
         Anchor anchor = this.getAnchor(this.placePos);
         if (this.logicMode.get() == MaoJunQingAura.LogicMode.PlaceBreak) {
            switch (anchor.state.ordinal()) {
               case 0:
                  if (this.timer <= (double)1.0F / (Double)this.speed.get()) {
                     return;
                  }

                  if (this.placeUpdate()) {
                     this.anchors.remove(this.placePos);
                     this.anchors.put(this.placePos, new Anchor(MaoJunQingAura.AnchorState.Anchor, 0, System.currentTimeMillis()));
                     this.timer = (double)0.0F;
                  }
                  break;
               case 1:
                  if (this.chargeUpdate(this.placePos)) {
                     Anchor a = new Anchor(MaoJunQingAura.AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());
                     this.anchors.remove(this.placePos);
                     this.anchors.put(this.placePos, a);
                  }
                  break;
               case 2:
                  if (this.explodeUpdate(this.placePos)) {
                     this.anchors.remove(this.placePos);
                     this.anchors.put(this.placePos, new Anchor(MaoJunQingAura.AnchorState.Air, 0, System.currentTimeMillis()));
                  }
            }
         } else {
            switch (anchor.state.ordinal()) {
               case 0:
                  if (this.placeUpdate()) {
                     this.anchors.remove(this.placePos);
                     this.anchors.put(this.placePos, new Anchor(MaoJunQingAura.AnchorState.Anchor, 0, System.currentTimeMillis()));
                  }
                  break;
               case 1:
                  if (this.chargeUpdate(this.placePos)) {
                     Anchor a = new Anchor(MaoJunQingAura.AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());
                     this.anchors.remove(this.placePos);
                     this.anchors.put(this.placePos, a);
                  }
                  break;
               case 2:
                  if (this.timer <= (double)1.0F / (Double)this.speed.get()) {
                     return;
                  }

                  if (this.explodeUpdate(this.placePos)) {
                     this.anchors.remove(this.placePos);
                     this.anchors.put(this.placePos, new Anchor(MaoJunQingAura.AnchorState.Air, 0, System.currentTimeMillis()));
                     this.timer = (double)0.0F;
                  }
            }
         }

      }
   }

   public boolean Exploding() {
      return this.isActive() && this.targets.stream().allMatch((target) -> target != null && this.placePos != null);
   }

   private void place(Hand hand) {
      this.placeBlock(hand, this.placeData.pos().toCenterPos(), this.placeData.dir(), this.placeData.pos());
      if ((Boolean)this.placeSwing.get()) {
         this.clientSwing((SwingHand)this.placeHand.get(), hand);
      }

   }

   private Anchor getAnchor(BlockPos pos) {
      if (this.anchors.containsKey(pos)) {
         return (Anchor)this.anchors.get(pos);
      } else {
         BlockState state = this.mc.world.getBlockState(pos);
         return new Anchor(state.getBlock() == Blocks.RESPAWN_ANCHOR ? ((Integer)state.get(Properties.CHARGES) < 1 ? MaoJunQingAura.AnchorState.Anchor : MaoJunQingAura.AnchorState.Loaded) : MaoJunQingAura.AnchorState.Air, state.getBlock() == Blocks.RESPAWN_ANCHOR ? (Integer)state.get(Properties.CHARGES) : 0, System.currentTimeMillis());
      }
   }

   private boolean placeUpdate() {
      Hand hand = Managers.HOLDING.isHolding(Items.RESPAWN_ANCHOR) ? Hand.MAIN_HAND : (this.mc.player.getOffHandStack().getItem() == Items.RESPAWN_ANCHOR ? Hand.OFF_HAND : null);
      boolean switched = hand != null;
      if (!switched) {
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 0:
            case 1:
               FindItemResult result1 = InvUtils.findInHotbar(new Item[]{Items.RESPAWN_ANCHOR});
               switched = result1.found();
               break;
            case 2:
            case 3:
               FindItemResult result2 = InvUtils.find(new Item[]{Items.RESPAWN_ANCHOR});
               switched = result2.found();
         }
      }

      if (!switched) {
         return false;
      } else if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(this.placeData.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
         return false;
      } else {
         if (hand == null) {
            switch (((SwitchMode)this.switchMode.get()).ordinal()) {
               case 0:
               case 1:
                  FindItemResult result3 = InvUtils.findInHotbar(new Item[]{Items.RESPAWN_ANCHOR});
                  InvUtils.swap(result3.slot(), true);
                  break;
               case 2:
                  FindItemResult result4 = InvUtils.find(new Item[]{Items.RESPAWN_ANCHOR});
                  switched = BOInvUtils.pickSwitch(result4.slot());
                  break;
               case 3:
                  FindItemResult result5 = InvUtils.find(new Item[]{Items.RESPAWN_ANCHOR});
                  switched = BOInvUtils.invSwitch(result5.slot());
            }
         }

         if (!switched) {
            return false;
         } else {
            this.place(hand == null ? Hand.MAIN_HAND : hand);
            if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
               Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
            }

            if (hand == null) {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0:
                     InvUtils.swapBack();
                  case 1:
                  default:
                     break;
                  case 2:
                     BOInvUtils.pickSwapBack();
                     break;
                  case 3:
                     BOInvUtils.swapBack();
               }
            }

            return true;
         }
      }
   }

   private boolean chargeUpdate(BlockPos pos) {
      Hand hand = Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : (this.mc.player.getOffHandStack().getItem() == Items.GLOWSTONE ? Hand.OFF_HAND : null);
      Direction dir = SettingUtils.getPlaceOnDirection(pos);
      if (dir == null) {
         return false;
      } else {
         boolean switched = hand != null;
         if (!switched) {
            switch (((SwitchMode)this.switchMode.get()).ordinal()) {
               case 0:
               case 1:
                  FindItemResult result6 = InvUtils.findInHotbar(new Item[]{Items.GLOWSTONE});
                  switched = result6.found();
                  break;
               case 2:
               case 3:
                  FindItemResult result7 = InvUtils.find(new Item[]{Items.GLOWSTONE});
                  switched = result7.found();
            }
         }

         if (!switched) {
            return false;
         } else if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(pos, (double)this.priority, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "interact"}))) {
            return false;
         } else {
            if (hand == null) {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0:
                  case 1:
                     FindItemResult result8 = InvUtils.findInHotbar(new Item[]{Items.GLOWSTONE});
                     InvUtils.swap(result8.slot(), true);
                     break;
                  case 2:
                     FindItemResult result9 = InvUtils.find(new Item[]{Items.GLOWSTONE});
                     switched = BOInvUtils.pickSwitch(result9.slot());
                     break;
                  case 3:
                     FindItemResult result10 = InvUtils.find(new Item[]{Items.GLOWSTONE});
                     switched = BOInvUtils.invSwitch(result10.slot());
               }
            }

            if (!switched) {
               return false;
            } else {
               this.interact(pos, dir, hand == null ? Hand.MAIN_HAND : hand);
               if (SettingUtils.shouldRotate(RotationType.Interact)) {
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "interact"}));
               }

               if (hand == null) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 0:
                        InvUtils.swapBack();
                     case 1:
                     default:
                        break;
                     case 2:
                        BOInvUtils.pickSwapBack();
                        break;
                     case 3:
                        BOInvUtils.swapBack();
                  }
               }

               return true;
            }
         }
      }
   }

   private boolean explodeUpdate(BlockPos pos) {
      Hand hand = !Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : (this.mc.player.getOffHandStack().getItem() != Items.GLOWSTONE ? Hand.OFF_HAND : null);
      Direction dir = SettingUtils.getPlaceOnDirection(pos);
      if (dir == null) {
         return false;
      } else {
         boolean switched = hand != null;
         if (!switched) {
            switch (((SwitchMode)this.switchMode.get()).ordinal()) {
               case 0:
               case 1:
                  FindItemResult result11 = InvUtils.findInHotbar((stack) -> stack.getItem() != Items.GLOWSTONE);
                  switched = result11.found();
                  break;
               case 2:
               case 3:
                  FindItemResult result12 = InvUtils.find((stack) -> stack.getItem() != Items.GLOWSTONE);
                  switched = result12.found();
            }
         }

         if (!switched) {
            return false;
         } else if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(pos, (double)this.priority, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "explode"}))) {
            return false;
         } else {
            if (hand == null) {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0:
                  case 1:
                     FindItemResult result13 = InvUtils.findInHotbar((item) -> item.getItem() != Items.GLOWSTONE);
                     InvUtils.swap(result13.slot(), true);
                     break;
                  case 2:
                     FindItemResult result14 = InvUtils.find((item) -> item.getItem() != Items.GLOWSTONE);
                     switched = BOInvUtils.pickSwitch(result14.slot());
                     break;
                  case 3:
                     FindItemResult result15 = InvUtils.find((item) -> item.getItem() != Items.GLOWSTONE);
                     switched = BOInvUtils.invSwitch(result15.slot());
               }
            }

            if (!switched) {
               return false;
            } else {
               this.interact(pos, dir, hand == null ? Hand.MAIN_HAND : hand);
               if (SettingUtils.shouldRotate(RotationType.Interact)) {
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "explode"}));
               }

               if (hand == null) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 0:
                        InvUtils.swapBack();
                     case 1:
                     default:
                        break;
                     case 2:
                        BOInvUtils.pickSwapBack();
                        break;
                     case 3:
                        BOInvUtils.swapBack();
                  }
               }

               return true;
            }
         }
      }
   }

   private void interact(BlockPos pos, Direction dir, Hand hand) {
      this.interactBlock(hand, pos.toCenterPos(), dir, pos);
      if (this.fadeMode.get() == MaoJunQingAura.FadeMode.Test2) {
         this.renderBlocks.add(new Render(pos, System.currentTimeMillis()));
      }

      if ((Boolean)this.interactSwing.get()) {
         this.clientSwing((SwingHand)this.interactHand.get(), hand);
      }

   }

   private boolean dmgCheck(double dmg, double self) {
      if (dmg < this.bestDmg) {
         return false;
      } else if (dmg < (Double)this.minDmg.get()) {
         return false;
      } else if (self > (Double)this.maxDmg.get()) {
         return false;
      } else {
         return dmg / self >= (Double)this.minRatio.get();
      }
   }

   private double getDmg(BlockPos pos) {
      double highest = (double)-1.0F;

      for(PlayerEntity target : this.targets) {
         highest = Math.max(highest, BODamageUtils.anchorDamage(target, target.getBoundingBox().offset(CrystalUtil.getMotionVec(target, (Integer)this.Predict.get(), true)), pos, pos.toCenterPos()));
      }

      return highest;
   }

   private Vec3d calcPredict(Entity e, int ticks) {
      return ticks == 0 ? e.getPos() : new Vec3d(e.getX() + (e.getX() - e.lastRenderX) * (double)ticks, e.getY() + (e.getY() - e.lastRenderY) * (double)ticks, e.getZ() + (e.getZ() - e.lastRenderZ) * (double)ticks);
   }

   private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
      if (current == null) {
         return target;
      } else {
         double absX = Math.abs(current.x - target.x);
         double absY = Math.abs(current.y - target.y);
         double absZ = Math.abs(current.z - target.z);
         double x = (absX + Math.pow(absX, (Double)this.animationMoveExponent.get() - (double)1.0F)) * delta;
         double y = (absY + Math.pow(absY, (Double)this.animationMoveExponent.get() - (double)1.0F)) * delta;
         double z = (absZ + Math.pow(absZ, (Double)this.animationMoveExponent.get() - (double)1.0F)) * delta;
         return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x), current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y), current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
      }
   }

   public static enum LogicMode {
      PlaceBreak,
      BreakPlace;

      // $FF: synthetic method
      private static LogicMode[] $values() {
         return new LogicMode[]{PlaceBreak, BreakPlace};
      }
   }

   public static enum SwitchMode {
      Silent,
      Normal,
      PickSilent,
      InvSwitch,
      Disabled;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Silent, Normal, PickSilent, InvSwitch, Disabled};
      }
   }

   public static enum AnchorState {
      Air,
      Anchor,
      Loaded;

      // $FF: synthetic method
      private static AnchorState[] $values() {
         return new AnchorState[]{Air, Anchor, Loaded};
      }
   }

   public static enum FadeMode {
      Up,
      Down,
      Normal,
      Test,
      Test2;

      // $FF: synthetic method
      private static FadeMode[] $values() {
         return new FadeMode[]{Up, Down, Normal, Test, Test2};
      }
   }

   private static record Anchor(AnchorState state, int charges, long time) {
   }

   public static record Render(BlockPos blockPos, long time) {
   }
}
