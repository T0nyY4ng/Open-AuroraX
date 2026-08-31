package espada.spacex.aurora.modules.combatplus.autocrystal;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.mixins.IInteractEntityC2SPacket;
import espada.spacex.aurora.modules.combatplus.MaoJunQingAura;
import espada.spacex.aurora.modules.combatplus.PistonCrystal;
import espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit.AirCheck;
import espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit.AliveCheck;
import espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit.CrystalPlaceBlockCheck;
import espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit.HandCheck;
import espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit.RangeCheck;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.AutoMine;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Extrap;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.HyperCalc;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.IDPreidct;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Misc;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.MultiTask;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Place;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Render;
import espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.friend;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.modules.playerplus.Suicide;
import espada.spacex.aurora.timers.TimerList;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.MixinPlugin;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;
import org.joml.Vector3d;

public class AutoCrystal extends Modules {
   private final SettingGroup sgExtrapolation;
   private final SettingGroup sgHyperid;
   private final SettingGroup sgPlace;
   private final SettingGroup sgExplode;
   private final SettingGroup sgSetDead;
   private final SettingGroup sgobi;
   private final SettingGroup sgFriend;
   private final SettingGroup sgMulti;
   private final SettingGroup sgMisc;
   private final SettingGroup sgAutoMine;
   private final SettingGroup sgID;
   private final SettingGroup sgRender;
   private final Setting<Integer> maxtarget;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> smartRot;
   private final Setting<Boolean> ignoreTerrain;
   private final Setting<Boolean> OnAnchorPlacePause;
   private final Setting<Boolean> performance;
   private final Setting<Boolean> preplacecalc;
   private final Setting<Boolean> preplacepos;
   private final Setting<Boolean> preplacedir;
   private final Setting<Boolean> place;
   private final Setting<Boolean> instantPlace;
   private final Setting<Double> speedLimit;
   private final Setting<Double> placeSpeed;
   private final Setting<AutoCrystalType.DelayMode> placeDelayMode;
   private final Setting<Double> placeDelay;
   private final Setting<Double> placeDelayTicks;
   private final Setting<Double> MinDmg;
   private final Setting<Double> maxPlace;
   private final Setting<Double> minPlaceRatio;
   private final Setting<Boolean> Break;
   private final Setting<Boolean> onlyOwn;
   private final Setting<AutoCrystalType.DelayMode> existedMode;
   private final Setting<Integer> existed;
   private final Setting<Double> existedTicks;
   private final Setting<AutoCrystalType.SequentialMode> sequential;
   public final Setting<Boolean> instantAttack;
   private final Setting<Double> expSpeedLimit;
   private final Setting<Double> expSpeed;
   private final Setting<Double> minExplode;
   private final Setting<Double> maxExp;
   private final Setting<Double> minExpRatio;
   private final Setting<Boolean> FastDead;
   private final Setting<Boolean> PauseDead;
   private final Setting<ObsidianHelper.Mode> obsidian;
   private final Setting<Double> speed;
   private final Setting<Double> minFriendPlaceRatio;
   private final Setting<Double> maxFriendPlace;
   private final Setting<Double> maxFriendExp;
   private final Setting<Double> minFriendExpRatio;
   private final Setting<AutoCrystalType.SwitchMode> switchMode;
   private final Setting<Double> CoolDown;
   private final Setting<Double> slowDamage;
   private final Setting<Double> slowSpeed;
   private final Setting<AutoCrystalType.ExplodeMode> expMode;
   private final Setting<AutoCrystalType.calcMode> calcMode;
   private final Setting<Double> Desyncforce;
   private final Setting<Double> selfCheck;
   private final Setting<Boolean> idPredict;
   private final Setting<Integer> idStartOffset;
   private final Setting<Integer> idOffset;
   private final Setting<Integer> idPackets;
   private final Setting<Double> idDelay;
   private final Setting<Double> idPacketDelay;
   private final Setting<Integer> placeExtrap;
   private final Setting<Integer> BreakExtrap;
   private final Setting<Integer> rangePre;
   private final Setting<Integer> blockextrap;
   private final Setting<Integer> Self;
   private final Setting<Integer> PlaceExtrapTick;
   private final Setting<Boolean> renderExt;
   private final Setting<Boolean> renderSelfExt;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> attackSwing;
   private final Setting<SwingHand> attackHand;
   private final Setting<Boolean> render;
   private final Setting<AutoCrystalType.RenderMode> renderMode;
   private final Setting<Double> renderTime;
   private final Setting<AutoCrystalType.FadeMode> fadeMode;
   private final Setting<AutoCrystalType.MotionOutMode> MotionOutFadeMode;
   private final Setting<AutoCrystalType.EarthFadeMode> earthFadeMode;
   private final Setting<Double> fadeTime;
   private final Setting<Double> animationSpeed;
   private final Setting<Double> animationMoveExponent;
   private final Setting<Double> HyperionExponent;
   private final Setting<Double> animationExponent;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   public final Setting<SettingColor> color;
   private final Setting<Boolean> renderTargetEsp;
   private final Setting<SettingColor> color2;
   private final Setting<Boolean> renderDmg;
   private final Setting<Double> scale;
   private final Setting<Integer> decimal;
   private final Setting<SettingColor> damageColor;
   public final Setting<Double> autoMineDamage;
   public final Setting<Boolean> amPlace;
   public final Setting<Double> amProgress;
   public final Setting<Boolean> amSpam;
   private final Setting<AutoCrystalType.AutoMineBrokenMode> amBroken;
   private final Setting<Boolean> paAttack;
   private final Setting<Boolean> paPlace;
   private long ticksEnabled;
   private double placeTimer;
   private double placeLimitTimer;
   private double delayTimer;
   private int delayTicks;
   public BlockPos placePos;
   private Direction placeDir;
   private Entity expEntity;
   private Box expEntityBB;
   private final TimerList<Integer> attackedList;
   private final Map<BlockPos, Long> existedList;
   private final Map<BlockPos, Long> existedTicksList;
   private final Map<BlockPos, Long> own;
   private final Map<AbstractClientPlayerEntity, Box> extPos;
   private final Map<AbstractClientPlayerEntity, Box> extHitbox;
   private Vec3d rangePos;
   private final List<Box> blocked;
   private final Map<BlockPos, Double[]> earthMap;
   private double attackTimer;
   private double switchTimer;
   private int confirmed;
   private long lastMillis;
   private boolean suicide;
   public static boolean placing = false;
   public long lastAttack;
   private Vec3d renderTarget;
   private Vec3d renderPos;
   private double renderProgress;
   public static AuroraMine autoMine = null;
   public int placed;
   private double cps;
   private final List<Long> explosions;
   private final List<Predict> predicts;
   private final List<SetDead> setDeads;
   private MaoJunQingAura autoAnchor;
   public PlayerEntity bestTarget;
   public final List<PlayerEntity> targets;
   private AutoCrystalType.SwitchMode SwitchMode;
   private AutoCrystalType.calcMode calc;
   private AutoCrystalType.DelayMode DelayMode;
   private AutoCrystalType.RenderMode RenderMode;
   private AutoCrystalType.ExplodeMode ExplodeMode;

   public AutoCrystal() {
      super(Aurora.CombatPlus, "AutoCrystal", "dobetterbyalexjonny");
      this.sgExtrapolation = this.settings.createGroup("Predict");
      this.sgHyperid = this.settings.createGroup("HyperidCalc");
      this.sgPlace = this.settings.createGroup("Place");
      this.sgExplode = this.settings.createGroup("Break");
      this.sgSetDead = this.settings.createGroup("SetDead");
      this.sgobi = this.settings.createGroup("Obsidian");
      this.sgFriend = this.settings.createGroup("FriendCheck");
      this.sgMulti = this.settings.createGroup("MultiCalc");
      this.sgMisc = this.settings.createGroup("Misc");
      this.sgAutoMine = this.settings.createGroup("AutoMine");
      this.sgID = this.settings.createGroup("ID Predict");
      this.sgRender = this.settings.createGroup("Render");
      this.maxtarget = Misc.maxtarget(this.sgMisc);
      this.pauseEat = Misc.Pause(this.sgMisc);
      this.smartRot = Misc.smartRot(this.sgMisc);
      this.ignoreTerrain = Misc.ignoreTerrain(this.sgMisc);
      this.OnAnchorPlacePause = Misc.OnAnchorPlacePause(this.sgMisc);
      this.performance = MultiTask.performance(this.sgMulti);
      this.preplacecalc = MultiTask.preplacecalc(this.sgMulti);
      this.preplacepos = MultiTask.preplacepos(this.sgMulti);
      this.preplacedir = MultiTask.preplacedir(this.sgMulti);
      this.place = Place.Place(this.sgPlace);
      this.instantPlace = Place.instantPlace(this.sgPlace);
      this.speedLimit = Place.speedLimit(this.sgPlace);
      this.placeSpeed = Place.placeSpeed(this.sgPlace);
      this.placeDelayMode = this.sgPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Delay Mode")).description("Should we count the delay in seconds or ticks.")).defaultValue(AutoCrystalType.DelayMode.Seconds)).build());
      this.placeDelay = Place.placeDelay(this.sgPlace);
      this.placeDelayTicks = Place.placeDelayTicks(this.sgPlace);
      this.MinDmg = Place.MinDmg(this.sgPlace);
      this.maxPlace = Place.maxPlace(this.sgPlace);
      this.minPlaceRatio = Place.minPlaceRatio(this.sgPlace);
      this.Break = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.Break(this.sgExplode);
      this.onlyOwn = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.onlyOwn(this.sgExplode);
      this.existedMode = this.sgExplode.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Existed Mode")).description("Should crystal existed times be counted in seconds or ticks.")).defaultValue(AutoCrystalType.DelayMode.Ticks)).build());
      this.existed = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.existed(this.sgExplode);
      this.existedTicks = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.existedTicks(this.sgExplode);
      this.sequential = this.sgExplode.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Sequential")).description("Doesn't place and attack during the same tick.")).defaultValue(AutoCrystalType.SequentialMode.Disabled)).build());
      this.instantAttack = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.instantAttack(this.sgExplode);
      this.expSpeedLimit = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.expSpeedLimit(this.sgExplode);
      this.expSpeed = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.expSpeed(this.sgExplode);
      this.minExplode = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.minExplode(this.sgExplode);
      this.maxExp = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.maxExp(this.sgExplode);
      this.minExpRatio = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.Break.minExpRatio(this.sgExplode);
      this.FastDead = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.SetDead.FastDead(this.sgSetDead);
      this.PauseDead = espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo.SetDead.PauseDead(this.sgSetDead);
      this.obsidian = this.sgobi.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Obsidian")).description("It's not done yet. will add in next time")).defaultValue(ObsidianHelper.Mode.none)).build());
      this.speed = this.sgobi.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place_Speed")).description("w")).defaultValue((double)4.0F).range((double)0.0F, (double)20.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.minFriendPlaceRatio = friend.minFriendPlaceRatio(this.sgFriend);
      this.maxFriendPlace = friend.maxFriendPlace(this.sgFriend);
      this.maxFriendExp = friend.maxFriendExp(this.sgFriend);
      this.minFriendExpRatio = friend.minFriendExpRatio(this.sgFriend);
      this.switchMode = this.sgHyperid.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Mode for switching to crystal in main hand.")).defaultValue(AutoCrystalType.SwitchMode.Disabled)).build());
      this.CoolDown = HyperCalc.CoolDown(this.sgHyperid);
      this.slowDamage = HyperCalc.slowDamage(this.sgHyperid);
      this.slowSpeed = HyperCalc.slowSpeed(this.sgHyperid);
      this.expMode = this.sgHyperid.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Explode Damage Mode")).description("Which things should be checked for exploding.")).defaultValue(AutoCrystalType.ExplodeMode.Crystal)).build());
      this.calcMode = this.sgHyperid.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("calcMode ")).description("3arthh = always safe, meteor = need selfcheck set to selfsafe")).defaultValue(AutoCrystalType.calcMode.Normal)).build());
      this.Desyncforce = HyperCalc.Desyncforce(this.sgHyperid);
      this.selfCheck = HyperCalc.selfCheck(this.sgHyperid);
      this.idPredict = IDPreidct.idPredict(this.sgID);
      this.idStartOffset = IDPreidct.idStartOffset(this.sgID);
      this.idOffset = IDPreidct.idOffset(this.sgID);
      this.idPackets = IDPreidct.idPackets(this.sgID);
      this.idDelay = IDPreidct.idDelay(this.sgID);
      this.idPacketDelay = IDPreidct.idPacketDelay(this.sgID);
      this.placeExtrap = Extrap.placeExtrap(this.sgExtrapolation);
      this.BreakExtrap = Extrap.breakExtrap(this.sgExtrapolation);
      this.rangePre = Extrap.rangePre(this.sgExtrapolation);
      this.blockextrap = Extrap.block(this.sgExtrapolation);
      this.Self = Extrap.Self(this.sgExtrapolation);
      this.PlaceExtrapTick = Extrap.PlaceExtrapTick(this.sgExtrapolation);
      this.renderExt = Extrap.renderExt(this.sgExtrapolation);
      this.renderSelfExt = Extrap.renderSelfExt(this.sgExtrapolation);
      this.placeSwing = Render.placeSwing(this.sgRender);
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place-Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.attackSwing = Render.attackSwing(this.sgRender);
      var10001 = this.sgRender;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack-Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.attackSwing;
      Objects.requireNonNull(var10003);
      this.attackHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.render = Render.Render(this.sgRender);
      this.renderMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("RenderMode")).description("What should the render look like.")).defaultValue(AutoCrystalType.RenderMode.Smooth)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Tick-Time")).description("ticktime2 by alexjonny testing")).defaultValue((double)0.5F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> this.renderMode.get() != AutoCrystalType.RenderMode.Smooth)).build());
      this.fadeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Fade_Mode")).description("How long the fading should take.")).defaultValue(AutoCrystalType.FadeMode.Normal)).visible(() -> ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.Smooth))).build());
      this.MotionOutFadeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("MotionOut_FadeMode")).description("How long the fading should take.")).defaultValue(AutoCrystalType.MotionOutMode.None)).visible(() -> ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.MotionOut))).build());
      this.earthFadeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Earthhack_FadeMode")).description(".")).defaultValue(AutoCrystalType.EarthFadeMode.Normal)).visible(() -> this.renderMode.get() == AutoCrystalType.RenderMode.Earthhack)).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade_Time")).description("fade out.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> this.renderMode.get() != AutoCrystalType.RenderMode.Smooth)).build());
      this.animationSpeed = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Move Speed")).description("How fast should aurora mode box move.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.Smooth) || ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.MotionOut))).build());
      this.animationMoveExponent = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Move Exponent")).description("Moves faster when longer away from the target.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.Smooth) || ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.MotionOut))).build());
      this.HyperionExponent = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("HyperionExponent")).description("Greater than 3 = bug")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)2.0F).visible(() -> ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.MotionOut))).build());
      this.animationExponent = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Exponent")).description("How fast should aurora mode box grow.")).defaultValue((double)3.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> ((AutoCrystalType.RenderMode)this.renderMode.get()).equals(AutoCrystalType.RenderMode.Smooth))).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of render should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = Render.lineColor(this.sgRender);
      this.color = Render.color(this.sgRender);
      this.renderTargetEsp = Render.renderTargetEsp(this.sgRender);
      this.color2 = Render.color2(this.sgRender);
      this.renderDmg = Render.renderDmg(this.sgRender);
      this.scale = Render.scale(this.sgRender);
      this.decimal = Render.decimal(this.sgRender);
      this.damageColor = Render.damageColor(this.sgRender);
      this.autoMineDamage = AutoMine.autoMineDamage(this.sgAutoMine);
      this.amPlace = AutoMine.amPlace(this.sgAutoMine);
      this.amProgress = AutoMine.amProgress(this.sgAutoMine);
      this.amSpam = AutoMine.amSpam(this.sgAutoMine);
      this.amBroken = AutoMine.amBroken(this.sgAutoMine);
      this.paAttack = AutoMine.paAttack(this.sgAutoMine);
      this.paPlace = AutoMine.paPlace(this.sgAutoMine);
      this.ticksEnabled = 0L;
      this.placeTimer = (double)0.0F;
      this.placeLimitTimer = (double)0.0F;
      this.delayTimer = (double)0.0F;
      this.delayTicks = 0;
      this.placePos = null;
      this.placeDir = null;
      this.expEntity = null;
      this.expEntityBB = null;
      this.attackedList = new TimerList<Integer>();
      this.existedList = new HashMap();
      this.existedTicksList = new HashMap();
      this.own = new HashMap();
      this.extPos = new HashMap();
      this.extHitbox = new HashMap();
      this.rangePos = null;
      this.blocked = new ArrayList();
      this.earthMap = new HashMap();
      this.attackTimer = (double)0.0F;
      this.switchTimer = (double)0.0F;
      this.confirmed = Integer.MIN_VALUE;
      this.lastMillis = System.currentTimeMillis();
      this.suicide = false;
      this.lastAttack = 0L;
      this.renderTarget = null;
      this.renderPos = null;
      this.renderProgress = (double)0.0F;
      this.placed = 0;
      this.cps = (double)0.0F;
      this.explosions = Collections.synchronizedList(new ArrayList());
      this.predicts = new ArrayList();
      this.setDeads = new ArrayList();
      this.autoAnchor = null;
      this.bestTarget = null;
      this.targets = new ArrayList();
   }

   public void onActivate() {
      super.onActivate();
      this.ticksEnabled = 0L;
      this.targets.clear();
      this.earthMap.clear();
      this.existedTicksList.clear();
      this.existedList.clear();
      this.blocked.clear();
      this.extPos.clear();
      this.own.clear();
      this.renderPos = null;
      this.renderProgress = (double)0.0F;
      this.lastMillis = System.currentTimeMillis();
      this.attackedList.clear();
      this.lastAttack = 0L;
      this.predicts.clear();
      this.setDeads.clear();
      this.autoAnchor = (MaoJunQingAura)meteordevelopment.meteorclient.systems.modules.Modules.get().get(MaoJunQingAura.class);
   }

   public String getInfoString() {
      super.getInfoString();
      return this.bestTarget != null ? EntityUtils.getName(this.bestTarget) : null;
   }

   @EventHandler(
      priority = 200
   )
   private void onTickPost(TickEvent.Post event) {
      ++this.delayTicks;
      ++this.ticksEnabled;
      ++this.placed;
      if (this.mc.player != null && this.mc.world != null) {
         if (autoMine == null) {
            autoMine = (AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);
         }

         ListenerExtrapolation.extrapolateMap(this.extPos, (player) -> player == this.mc.player ? (Integer)this.Self.get() : (Integer)this.placeExtrap.get(), (player) -> (Integer)this.PlaceExtrapTick.get());
         ListenerExtrapolation.extrapolateMap(this.extHitbox, (player) -> (Integer)this.BreakExtrap.get(), (player) -> (Integer)this.PlaceExtrapTick.get());
         ListenerExtrapolation.extrapolateblock(this.mc.player);
         this.blockextrap.get();
         Box rangeBox = ListenerExtrapolation.extrapolate(this.mc.player, (Integer)this.rangePre.get(), (Integer)this.PlaceExtrapTick.get());
         if (rangeBox == null) {
            this.rangePos = this.mc.player.getEyePos();
         } else {
            this.rangePos = new Vec3d((rangeBox.minX + rangeBox.maxX) / (double)2.0F, rangeBox.minY + (double)this.mc.player.getEyeHeight(this.mc.player.getPose()), (rangeBox.minZ + rangeBox.maxZ) / (double)2.0F);
         }

         List<BlockPos> toRemove = new ArrayList();
         this.existedList.forEach((key, val) -> {
            if (System.currentTimeMillis() - val >= (long)(5000 + (Integer)this.existed.get() * 1000)) {
               toRemove.add(key);
            }

         });
         Map var10001 = this.existedList;
         Objects.requireNonNull(var10001);
         toRemove.forEach(var10001::remove);
         toRemove.clear();
         this.existedTicksList.forEach((key, val) -> {
            if ((double)(this.ticksEnabled - val) >= (double)100.0F + (Double)this.existedTicks.get()) {
               toRemove.add(key);
            }

         });
         var10001 = this.existedTicksList;
         Objects.requireNonNull(var10001);
         toRemove.forEach(var10001::remove);
         toRemove.clear();
         this.own.forEach((key, val) -> {
            if (System.currentTimeMillis() - val >= 5000L) {
               toRemove.add(key);
            }

         });
         var10001 = this.own;
         Objects.requireNonNull(var10001);
         toRemove.forEach(var10001::remove);
         if ((Boolean)this.performance.get()) {
            this.updatePlacement();
         }

      }
   }

   @EventHandler(
      priority = 201
   )
   private void onRender3D(Render3DEvent event) {
      this.attackedList.update();
      if (autoMine == null) {
         autoMine = (AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);
      }

      this.suicide = meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Suicide.class);
      double delta = (double)((float)(System.currentTimeMillis() - this.lastMillis) / 1000.0F);
      this.lastMillis = System.currentTimeMillis();
      this.cps = (double)0.0F;
      synchronized(this.explosions) {
         this.explosions.removeIf((time) -> {
            double p = (double)(System.currentTimeMillis() - time) / (double)1000.0F;
            if (p >= (double)5.0F) {
               return true;
            } else {
               double d = p <= (double)4.0F ? (double)1.0F : (double)1.0F - (p - (double)4.0F);
               this.cps += d;
               return false;
            }
         });
      }

      this.cps /= (double)4.5F;
      this.attackedList.update();
      this.attackTimer = Math.max(this.attackTimer - delta, (double)0.0F);
      this.placeTimer = Math.max(this.placeTimer - delta * this.getSpeed(), (double)0.0F);
      this.placeLimitTimer += delta;
      this.delayTimer += delta;
      this.switchTimer = Math.max((double)0.0F, this.switchTimer - delta);
      this.update();
      this.checkDelayed();
      if ((Boolean)this.renderTargetEsp.get() && this.bestTarget != null && this.placePos != null) {
         RenderUtils.drawJello(event.matrices, this.bestTarget, (Color)this.color2.get());
      }

      if ((Boolean)this.render.get()) {
         switch ((AutoCrystalType.RenderMode)this.renderMode.get()) {
            case Smooth:
               if (this.placePos != null && !this.isPaused() && this.holdingCheck() && !this.isAnchor()) {
                  this.renderProgress = Math.min((double)1.0F, this.renderProgress + delta);
                  this.renderTarget = new Vec3d((double)this.placePos.getX(), (double)this.placePos.getY(), (double)this.placePos.getZ());
               } else {
                  this.renderProgress = Math.max((double)0.0F, this.renderProgress - delta);
               }

               if (this.renderTarget != null) {
                  this.renderPos = this.smoothMove(this.renderPos, this.renderTarget, delta * (Double)this.animationSpeed.get() * (double)5.0F);
               }

               if (this.renderPos != null) {
                  double r = (double)0.5F - Math.pow((double)1.0F - this.renderProgress, (Double)this.animationExponent.get()) / (double)2.0F;
                  if (r >= 0.001) {
                     double down = (double)-0.5F;
                     double up = (double)-0.5F;
                     double width = (double)0.5F;
                     switch ((AutoCrystalType.FadeMode)this.fadeMode.get()) {
                        case Up:
                           up = (double)0.0F;
                           down = -(r * (double)2.0F);
                           break;
                        case Down:
                           up = (double)-1.0F + r * (double)2.0F;
                           down = (double)-1.0F;
                           break;
                        case Normal:
                           up = (double)-0.5F + r;
                           down = (double)-0.5F - r;
                           width = r;
                     }

                     Box box = new Box(this.renderPos.getX() + (double)0.5F - width, this.renderPos.getY() + down, this.renderPos.getZ() + (double)0.5F - width, this.renderPos.getX() + (double)0.5F + width, this.renderPos.getY() + up, this.renderPos.getZ() + (double)0.5F + width);
                     event.renderer.box(box, new Color(((SettingColor)this.color.get()).r, ((SettingColor)this.color.get()).g, ((SettingColor)this.color.get()).b, ((SettingColor)this.color.get()).a), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
                  }
               }
               break;
            case MotionOut:
               if (this.placePos != null && !this.isPaused() && this.holdingCheck() && !this.isAnchor()) {
                  this.renderProgress = Math.min((double)1.0F, this.renderProgress + delta);
                  this.renderTarget = new Vec3d((double)this.placePos.getX(), (double)this.placePos.getY(), (double)this.placePos.getZ());
                  this.renderProgress = (Double)this.fadeTime.get() + (Double)this.renderTime.get();
               } else {
                  this.renderProgress = Math.max((double)0.0F, this.renderProgress - delta);
               }

               if (this.renderTarget != null) {
                  this.renderPos = this.smoothMove(this.renderPos, this.renderTarget, delta * (Double)this.animationSpeed.get() * (double)5.0F);
               }

               if (this.renderPos != null) {
                  double r = (double)0.5F - Math.pow((double)1.0F - this.renderProgress, (Double)this.HyperionExponent.get()) / (double)2.0F;
                  if (this.renderProgress > (double)0.0F && this.renderPos != null) {
                     event.renderer.box(new Box(this.renderPos.getX(), this.renderPos.getY() - (double)1.0F, this.renderPos.getZ(), this.renderPos.getX() + (double)1.0F, this.renderPos.getY(), this.renderPos.getZ() + (double)1.0F), new Color(((SettingColor)this.color.get()).r, ((SettingColor)this.color.get()).g, ((SettingColor)this.color.get()).b, (int)Math.round((double)((SettingColor)this.color.get()).a * Math.min((double)1.0F, this.renderProgress / (Double)this.fadeTime.get()))), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * Math.min((double)1.0F, this.renderProgress / (Double)this.fadeTime.get()))), (ShapeMode)this.shapeMode.get(), 0);
                  }

                  if (r >= 0.001) {
                     double down = (double)-0.5F;
                     double up = (double)-0.5F;
                     double width = (double)0.5F;
                     switch ((AutoCrystalType.MotionOutMode)this.MotionOutFadeMode.get()) {
                        case None:
                           return;
                        case blockbox:
                           up = -0.8 + r;
                           down = -0.2 - r;
                           width = r;
                        default:
                           Box box = new Box(this.renderPos.getX() + (double)0.5F - width, this.renderPos.getY() + down, this.renderPos.getZ() + (double)0.5F - width, this.renderPos.getX() + (double)0.5F + width, this.renderPos.getY() + up, this.renderPos.getZ() + (double)0.5F + width);
                           event.renderer.box(box, new Color(((SettingColor)this.color.get()).r, ((SettingColor)this.color.get()).g, ((SettingColor)this.color.get()).b, ((SettingColor)this.color.get()).a), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
                     }
                  }
               }
               break;
            case Future:
               if (this.placePos != null && !this.isPaused() && this.holdingCheck() && !this.isAnchor()) {
                  this.renderPos = new Vec3d((double)this.placePos.getX(), (double)this.placePos.getY(), (double)this.placePos.getZ());
                  this.renderProgress = (Double)this.fadeTime.get() + (Double)this.renderTime.get();
               } else {
                  this.renderProgress = Math.max((double)0.0F, this.renderProgress - delta);
               }

               if (this.renderProgress > (double)0.0F && this.renderPos != null) {
                  event.renderer.box(new Box(this.renderPos.getX(), this.renderPos.getY() - (double)1.0F, this.renderPos.getZ(), this.renderPos.getX() + (double)1.0F, this.renderPos.getY(), this.renderPos.getZ() + (double)1.0F), new Color(((SettingColor)this.color.get()).r, ((SettingColor)this.color.get()).g, ((SettingColor)this.color.get()).b, (int)Math.round((double)((SettingColor)this.color.get()).a * Math.min((double)1.0F, this.renderProgress / (Double)this.fadeTime.get()))), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * Math.min((double)1.0F, this.renderProgress / (Double)this.fadeTime.get()))), (ShapeMode)this.shapeMode.get(), 0);
               }
               break;
            case Earthhack:
               List<BlockPos> toRemove = new ArrayList();

               for(Map.Entry<BlockPos, Double[]> entry : this.earthMap.entrySet()) {
                  BlockPos pos = (BlockPos)entry.getKey();
                  Double[] alpha = (Double[])entry.getValue();
                  if (alpha[0] <= delta) {
                     toRemove.add(pos);
                  } else {
                     double r = Math.min((double)1.0F, alpha[0] / alpha[1]) / (double)2.0F;
                     double down = (double)-0.5F;
                     double up = (double)-0.5F;
                     double width = (double)0.5F;
                     switch ((AutoCrystalType.EarthFadeMode)this.earthFadeMode.get()) {
                        case Normal:
                           up = (double)1.0F;
                           down = (double)0.0F;
                           break;
                        case Up:
                           up = (double)1.0F;
                           down = (double)1.0F - r * (double)2.0F;
                           break;
                        case Down:
                           up = r * (double)2.0F;
                           down = (double)0.0F;
                           break;
                        case Shrink:
                           up = (double)0.5F + r;
                           down = (double)0.5F - r;
                           width = r;
                     }

                     Box box = new Box((double)pos.getX() + (double)0.5F - width, (double)pos.getY() + down, (double)pos.getZ() + (double)0.5F - width, (double)pos.getX() + (double)0.5F + width, (double)pos.getY() + up, (double)pos.getZ() + (double)0.5F + width);
                     event.renderer.box(box, new Color(((SettingColor)this.color.get()).r, ((SettingColor)this.color.get()).g, ((SettingColor)this.color.get()).b, (int)Math.round((double)((SettingColor)this.color.get()).a * Math.min((double)1.0F, alpha[0] / alpha[1]))), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * Math.min((double)1.0F, alpha[0] / alpha[1]))), (ShapeMode)this.shapeMode.get(), 0);
                     entry.setValue(new Double[]{alpha[0] - delta, alpha[1]});
                  }
               }

               Map var10001 = this.earthMap;
               Objects.requireNonNull(var10001);
               toRemove.forEach(var10001::remove);
         }
      }

      if (this.mc.player != null && (Boolean)this.renderExt.get()) {
         this.extPos.forEach((name, bb) -> {
            if ((Boolean)this.renderSelfExt.get() || !name.equals(this.mc.player)) {
               event.renderer.box(bb, (Color)this.color.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
            }

         });
      }

   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if ((Boolean)this.renderDmg.get() && !this.isPaused() && !this.isAnchor() && this.holdingCheck() && this.placePos != null && this.renderPos != null) {
         this.renderMode.get();
         Vector3d vec3d = new Vector3d(this.renderPos.x + (double)0.5F, this.renderPos.add((double)0.0F, -1.2, (double)0.0F).y + (double)0.5F, this.renderPos.z + (double)0.5F);
         if (NametagUtils.to2D(vec3d, (Double)this.scale.get(), true)) {
            TextRenderer font = TextRenderer.get();
            NametagUtils.begin(vec3d);
            font.begin((Double)this.scale.get());
            NumberFormat why = NumberFormat.getNumberInstance();
            why.setMaximumFractionDigits((Integer)this.decimal.get());
            String enemy = why.format(this.getDmg(this.placePos.toCenterPos(), false)[0][0]);
            font.render(enemy, -(font.getWidth(enemy) / (double)2.0F), -font.getHeight(), (Color)this.damageColor.get(), false);
            font.end();
            NametagUtils.end();
         }
      }

   }

   private boolean isAnchor() {
      return (Boolean)this.OnAnchorPlacePause.get() && this.autoAnchor.Exploding();
   }

   public boolean isPaused() {
      return (Boolean)this.pauseEat.get() && this.mc.player.isUsingItem();
   }

   @EventHandler(
      priority = 200
   )
   private void onEntity(EntityAddedEvent event) {
      this.confirmed = event.entity.getId();
      if (event.entity.getBlockPos().equals(this.placePos)) {
         this.explosions.add(System.currentTimeMillis());
      }

   }

   @EventHandler(
      priority = 200
   )
   private void onSend(PacketEvent.Send event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            this.switchTimer = (Double)this.CoolDown.get();
         }

         Packet var3 = event.packet;
         if (var3 instanceof PlayerInteractBlockC2SPacket) {
            PlayerInteractBlockC2SPacket packet = (PlayerInteractBlockC2SPacket)var3;
            if (packet.getHand() == Hand.MAIN_HAND) {
               if (!Managers.HOLDING.isHolding(Items.END_CRYSTAL)) {
                  return;
               }
            } else if (this.mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
               return;
            }

            if (this.isOwn(packet.getBlockHitResult().getBlockPos().up())) {
               this.own.remove(packet.getBlockHitResult().getBlockPos().up());
            }

            this.own.put(packet.getBlockHitResult().getBlockPos().up(), System.currentTimeMillis());
            this.blocked.add(OLEPOSSUtils.getCrystalBox(packet.getBlockHitResult().getBlockPos().up()));
            this.addExisted(packet.getBlockHitResult().getBlockPos().up());
         }
      }

   }

   private void update() {
      placing = false;
      this.expEntity = null;
      Hand hand = HandCheck.getHand((stack) -> stack.getItem() == Items.END_CRYSTAL);
      Hand handToUse = hand;
      if (!(Boolean)this.performance.get()) {
         this.updatePlacement();
      }

      switch ((AutoCrystalType.SwitchMode)this.switchMode.get()) {
         case Simple:
            int slot = InvUtils.findInHotbar(new Item[]{Items.END_CRYSTAL}).slot();
            if (this.placePos != null && hand == null && slot >= 0) {
               InvUtils.swap(slot, false);
               handToUse = Hand.MAIN_HAND;
            }
            break;
         case Gapple:
            int gapSlot = InvUtils.findInHotbar(OLEPOSSUtils::isGapple).slot();
            if (this.mc.options.useKey.isPressed() && Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE) && gapSlot >= 0) {
               if (HandCheck.getHand(OLEPOSSUtils::isGapple) == null) {
                  InvUtils.swap(gapSlot, false);
               }

               handToUse = HandCheck.getHand((itemStack) -> itemStack.getItem() == Items.END_CRYSTAL);
            } else if (Managers.HOLDING.isHolding(Items.END_CRYSTAL, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)) {
               int crystalSlot = InvUtils.findInHotbar(new Item[]{Items.END_CRYSTAL}).slot();
               if (this.placePos != null && hand == null && crystalSlot >= 0) {
                  InvUtils.swap(crystalSlot, false);
                  handToUse = Hand.MAIN_HAND;
               }
            }
      }

      if (this.placePos != null && this.placeDir != null && !this.isPaused() && !this.isAnchor() && (!(Boolean)this.paPlace.get() || !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(PistonCrystal.class))) {
         label194: {
            int silentSlot = InvUtils.find((itemStack) -> itemStack.getItem() == Items.END_CRYSTAL).slot();
            int hotbar = InvUtils.findInHotbar(new Item[]{Items.END_CRYSTAL}).slot();
            if (handToUse == null) {
               Object var10000 = this.switchMode.get();
               AutoCrystalType.SwitchMode var10001 = this.SwitchMode;
               if (var10000 != AutoCrystalType.SwitchMode.Silent || hotbar < 0) {
                  var10000 = this.switchMode.get();
                  var10001 = this.SwitchMode;
                  if (var10000 != AutoCrystalType.SwitchMode.PickSilent) {
                     var10000 = this.switchMode.get();
                     var10001 = this.SwitchMode;
                     if (var10000 != AutoCrystalType.SwitchMode.InvSilent) {
                        break label194;
                     }
                  }

                  if (silentSlot < 0) {
                     break label194;
                  }
               }
            }

            placing = true;
            if ((!SettingUtils.shouldRotate(RotationType.Interact) || Managers.ROTATION.start(this.placePos.down(), (Boolean)this.smartRot.get() ? new Vec3d((double)this.placePos.getX() + (double)0.5F, (double)this.placePos.getY(), (double)this.placePos.getZ() + (double)0.5F) : null, (double)this.priority, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "placing"}))) && this.speedCheck() && this.delayCheck()) {
               this.placeCrystal(this.placePos.down(), this.placeDir, handToUse, silentSlot, hotbar);
            }
         }
      }

      PistonCrystal pa = (PistonCrystal)meteordevelopment.meteorclient.systems.modules.Modules.get().get(PistonCrystal.class);
      double[] value = null;
      if (!this.isPaused() && !this.isAnchor()) {
         label144: {
            if (hand == null) {
               Object var15 = this.switchMode.get();
               AutoCrystalType.SwitchMode var22 = this.SwitchMode;
               if (var15 != AutoCrystalType.SwitchMode.Silent) {
                  var15 = this.switchMode.get();
                  var22 = this.SwitchMode;
                  if (var15 != AutoCrystalType.SwitchMode.PickSilent) {
                     var15 = this.switchMode.get();
                     var22 = this.SwitchMode;
                     if (var15 != AutoCrystalType.SwitchMode.InvSilent) {
                        break label144;
                     }
                  }
               }
            }

            if ((Boolean)this.Break.get()) {
               for(Entity en : this.mc.world.getEntities()) {
                  if ((!(Boolean)this.paAttack.get() || !pa.isActive() || !en.getBlockPos().equals(pa.crystalPos)) && en instanceof EndCrystalEntity && !(this.switchTimer > (double)0.0F)) {
                     double[] dmg = this.getDmg(en.getPos(), true)[0];
                     if (this.canExplode(en.getPos())) {
                        if (this.expEntity != null && value != null) {
                           AutoCrystalType.calcMode var18 = (AutoCrystalType.calcMode)this.calcMode.get();
                           AutoCrystalType.calcMode var25 = this.calc;
                           if (!var18.equals(AutoCrystalType.calcMode.HyperCard) || !(dmg[0] > value[0])) {
                              var18 = (AutoCrystalType.calcMode)this.calcMode.get();
                              var25 = this.calc;
                              if (!var18.equals(AutoCrystalType.calcMode.earthhack) || !(dmg[2] / dmg[0] < value[2] / dmg[0])) {
                                 continue;
                              }
                           }
                        }

                        this.expEntity = en;
                        value = dmg;
                     }
                  }
               }
            }
         }
      }

      if (this.expEntity != null && this.multiTaskCheck() && !this.isAttacked(this.expEntity.getId()) && this.attackDelayCheck() && this.existedCheck(this.expEntity.getBlockPos()) && (!SettingUtils.shouldRotate(RotationType.Attacking) || this.startAttackRot())) {
         if (SettingUtils.shouldRotate(RotationType.Attacking)) {
            this.expEntityBB = this.expEntity.getBoundingBox();
         }

         this.explode(this.expEntity.getId(), this.expEntity.getPos());
      }

      if (!AliveCheck.isAlive(this.expEntityBB) && SettingUtils.shouldRotate(RotationType.Attacking)) {
         Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "attacking"}));
      }

   }

   private boolean attackDelayCheck() {
      if (!(Boolean)this.instantAttack.get()) {
         return (double)System.currentTimeMillis() > (double)this.lastAttack + (double)1000.0F / (Double)this.expSpeed.get();
      } else {
         return (Double)this.expSpeedLimit.get() <= (double)0.0F || (double)System.currentTimeMillis() > (double)this.lastAttack + (double)1000.0F / (Double)this.expSpeedLimit.get();
      }
   }

   private boolean startAttackRot() {
      this.expEntityBB = this.expEntity.getBoundingBox();
      return Managers.ROTATION.start(this.expEntity.getBoundingBox(), (Boolean)this.smartRot.get() ? this.expEntity.getPos() : null, (double)this.priority + (!this.isAttacked(this.expEntity.getId()) && this.blocksPlacePos(this.expEntity) ? -0.1 : 0.1), RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"}));
   }

   private boolean blocksPlacePos(Entity entity) {
      return this.placePos != null && entity.getBoundingBox().intersects(new Box((double)this.placePos.getX(), (double)this.placePos.getY(), (double)this.placePos.getZ(), (double)(this.placePos.getX() + 1), (double)(this.placePos.getY() + (SettingUtils.cc() ? 1 : 2)), (double)(this.placePos.getZ() + 1)));
   }

   private boolean speedCheck() {
      if ((Double)this.speedLimit.get() > (double)0.0F && this.placeLimitTimer < (double)1.0F / (Double)this.speedLimit.get()) {
         return false;
      } else if ((Boolean)this.instantPlace.get() && !this.shouldSlow() && !this.isBlocked(this.placePos)) {
         return true;
      } else {
         return this.placeTimer <= (double)0.0F;
      }
   }

   private boolean holdingCheck() {
      boolean var10000;
      switch ((AutoCrystalType.SwitchMode)this.switchMode.get()) {
         case Silent:
            var10000 = InvUtils.findInHotbar(new Item[]{Items.END_CRYSTAL}).slot() >= 0;
            break;
         case PickSilent:
         case InvSilent:
            var10000 = InvUtils.find(new Item[]{Items.END_CRYSTAL}).slot() >= 0;
            break;
         default:
            var10000 = HandCheck.getHand((itemStack) -> itemStack.getItem() == Items.END_CRYSTAL) != null;
      }

      return var10000;
   }

   private void updatePlacement() {
      if (!(Boolean)this.place.get()) {
         if (!(Boolean)this.preplacecalc.get()) {
            this.placed = Integer.parseInt((String)null);
         }

         if (!(Boolean)this.preplacepos.get()) {
            this.placePos = null;
         }

         if (!(Boolean)this.preplacedir.get()) {
            this.placeDir = null;
         }

         this.rangePos = null;
      } else {
         this.placePos = this.getPlacePos();
      }
   }

   private void placeCrystal(BlockPos pos, Direction dir, Hand handToUse, int sl, int hsl) {
      if (pos != null && this.mc.player != null) {
         AutoCrystalType.RenderMode var10000 = (AutoCrystalType.RenderMode)this.renderMode.get();
         AutoCrystalType.RenderMode var10001 = this.RenderMode;
         if (var10000.equals(AutoCrystalType.RenderMode.Earthhack)) {
            if (!this.earthMap.containsKey(pos)) {
               this.earthMap.put(pos, new Double[]{(Double)this.fadeTime.get() + (Double)this.renderTime.get(), (Double)this.fadeTime.get()});
            } else {
               this.earthMap.replace(pos, new Double[]{(Double)this.fadeTime.get() + (Double)this.renderTime.get(), (Double)this.fadeTime.get()});
            }
         }

         this.blocked.add(new Box((double)pos.getX() - (double)0.5F, (double)(pos.getY() + 1), (double)pos.getZ() - (double)0.5F, (double)pos.getX() + (double)1.5F, (double)(pos.getY() + 2), (double)pos.getZ() + (double)1.5F));
         boolean switched = handToUse == null;
         if (switched) {
            switch ((AutoCrystalType.SwitchMode)this.switchMode.get()) {
               case Silent -> InvUtils.swap(hsl, true);
               case PickSilent -> BOInvUtils.pickSwitch(sl);
               case InvSilent -> BOInvUtils.invSwitch(sl);
            }
         }

         this.addExisted(pos.up());
         if (!this.isOwn(pos.up())) {
            this.own.put(pos.up(), System.currentTimeMillis());
         } else {
            this.own.remove(pos.up());
            this.own.put(pos.up(), System.currentTimeMillis());
         }

         this.placeLimitTimer = (double)0.0F;
         this.placeTimer = (double)1.0F;
         this.placed = 0;
         this.interactBlock(switched ? Hand.MAIN_HAND : handToUse, pos.toCenterPos(), dir, pos);
         if ((Boolean)this.placeSwing.get()) {
            this.clientSwing((SwingHand)this.placeHand.get(), switched ? Hand.MAIN_HAND : handToUse);
         }

         if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
         }

         if (switched) {
            switch ((AutoCrystalType.SwitchMode)this.switchMode.get()) {
               case Silent -> InvUtils.swapBack();
               case PickSilent -> BOInvUtils.pickSwapBack();
               case InvSilent -> BOInvUtils.swapBack();
            }
         }

         if ((Boolean)this.idPredict.get()) {
            int highest = this.getHighest();
            int id = highest + (Integer)this.idStartOffset.get();

            for(int i = 0; i < (Integer)this.idPackets.get() * (Integer)this.idOffset.get(); i += (Integer)this.idOffset.get()) {
               this.addPredict(id + i, new Vec3d((double)pos.getX() + (double)0.5F, (double)(pos.getY() + 1), (double)pos.getZ() + (double)0.5F), (Double)this.idDelay.get() + (Double)this.idPacketDelay.get() * (double)i);
            }
         }
      }

   }

   private boolean delayCheck() {
      Object var10000 = this.placeDelayMode.get();
      AutoCrystalType.DelayMode var10001 = this.DelayMode;
      if (var10000 == AutoCrystalType.DelayMode.Seconds) {
         return this.delayTimer >= (Double)this.placeDelay.get();
      } else {
         return (double)this.delayTicks >= (Double)this.placeDelayTicks.get();
      }
   }

   private boolean multiTaskCheck() {
      return this.placed >= ((AutoCrystalType.SequentialMode)this.sequential.get()).ticks;
   }

   private int getHighest() {
      int highest = this.confirmed;

      for(Entity entity : this.mc.world.getEntities()) {
         if (entity.getId() > highest) {
            highest = entity.getId();
         }
      }

      if (highest > this.confirmed) {
         this.confirmed = highest;
      }

      return highest;
   }

   private boolean isBlocked(BlockPos pos) {
      Box box = new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1));

      for(Box bb : this.blocked) {
         if (bb.intersects(box)) {
            return true;
         }
      }

      return false;
   }

   private boolean isAttacked(int id) {
      return this.attackedList.contains(id);
   }

   private void explode(int id, Vec3d vec) {
      this.attackEntity(id, OLEPOSSUtils.getCrystalBox(vec), vec);
   }

   private void attackEntity(int id, Box bb, Vec3d vec) {
      if (this.mc.player != null) {
         this.lastAttack = System.currentTimeMillis();
         this.attackedList.add(id, (double)1.0F / (Double)this.expSpeed.get());
         this.delayTimer = (double)0.0F;
         this.delayTicks = 0;
         this.removeExisted(BlockPos.ofFloored(vec));
         SettingUtils.registerAttack(bb);
         PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(this.mc.player, this.mc.player.isSneaking());
         ((IInteractEntityC2SPacket)packet).setId(id);
         SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
         this.sendPacket(packet);
         SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
         if ((Boolean)this.attackSwing.get()) {
            this.clientSwing((SwingHand)this.attackHand.get(), Hand.MAIN_HAND);
         }

         this.blocked.clear();
         if ((Boolean)this.FastDead.get()) {
            Entity entity = this.mc.world.getEntityById(id);
            if (entity == null) {
               return;
            }

            this.addSetDead(entity);
         }
      }

   }

   private boolean existedCheck(BlockPos pos) {
      Object var10000 = this.existedMode.get();
      AutoCrystalType.DelayMode var10001 = this.DelayMode;
      if (var10000 == AutoCrystalType.DelayMode.Seconds) {
         return !this.existedList.containsKey(pos) || System.currentTimeMillis() > (Long)this.existedList.get(pos) + (long)((Integer)this.existed.get() * 1000);
      } else {
         return !this.existedTicksList.containsKey(pos) || (double)this.ticksEnabled >= (double)(Long)this.existedTicksList.get(pos) + (Double)this.existedTicks.get();
      }
   }

   private void addExisted(BlockPos pos) {
      Object var10000 = this.existedMode.get();
      AutoCrystalType.DelayMode var10001 = this.DelayMode;
      if (var10000 == AutoCrystalType.DelayMode.Seconds) {
         if (!this.existedList.containsKey(pos)) {
            this.existedList.put(pos, System.currentTimeMillis());
         }
      } else if (!this.existedTicksList.containsKey(pos)) {
         this.existedTicksList.put(pos, this.ticksEnabled);
      }

   }

   private void removeExisted(BlockPos pos) {
      Object var10000 = this.existedMode.get();
      AutoCrystalType.DelayMode var10001 = this.DelayMode;
      if (var10000 == AutoCrystalType.DelayMode.Seconds) {
         this.existedList.remove(pos);
      } else {
         this.existedTicksList.remove(pos);
      }

   }

   private boolean canExplode(Vec3d vec) {
      if ((Boolean)this.onlyOwn.get() && !this.isOwn(vec)) {
         return false;
      } else if (!this.inExplodeRange(vec)) {
         return false;
      } else {
         double[][] result = this.getDmg(vec, true);
         return this.explodeDamageCheck(result[0], result[1], this.isOwn(vec));
      }
   }

   private boolean canExplodePlacing(Vec3d vec) {
      if ((Boolean)this.onlyOwn.get() && !this.isOwn(vec)) {
         return false;
      } else if (!this.inExplodeRangePlacing(vec)) {
         return false;
      } else {
         double[][] result = this.getDmg(vec, false);
         return this.explodeDamageCheck(result[0], result[1], this.isOwn(vec));
      }
   }

   private void setEntityDead(Entity en) {
      this.mc.world.removeEntity(en.getId(), RemovalReason.KILLED);
   }

   private BlockPos getPlacePos() {
      int r = (int)Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));
      BlockPos bestPos = null;
      Direction bestDir = null;
      double[] highest = null;
      BlockPos pPos = BlockPos.ofFloored(this.mc.player.getEyePos());

      for(int x = -r; x <= r; ++x) {
         for(int y = -r; y <= r; ++y) {
            for(int z = -r; z <= r; ++z) {
               BlockPos pos = pPos.add(x, y, z);
               if (AirCheck.air(pos) && (!SettingUtils.oldCrystals() || AirCheck.air(pos.up())) && CrystalPlaceBlockCheck.crystalBlock(pos.down()) && !this.blockBroken(pos.down())) {
                  Direction dir = SettingUtils.getPlaceOnDirection(pos.down());
                  if (dir != null && RangeCheck.inPlaceRange(pos.down()) && this.inExplodeRangePlacing(new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F))) {
                     double[][] result = this.getDmg(new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F), false);
                     if (this.placeDamageCheck(result[0], result[1], highest)) {
                        Box box = new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + (SettingUtils.cc() ? 1 : 2)), (double)(pos.getZ() + 1));
                        if (!BOEntityUtils.intersectsWithEntity(box, this::validForIntersect, this.extHitbox)) {
                           bestDir = dir;
                           bestPos = pos;
                           highest = result[0];
                        }
                     }
                  }
               }
            }
         }
      }

      this.placeDir = bestDir;
      return bestPos;
   }

   private boolean placeDamageCheck(double[] dmg, double[] health, double[] highest) {
      if (highest != null) {
         AutoCrystalType.calcMode var10000 = (AutoCrystalType.calcMode)this.calcMode.get();
         AutoCrystalType.calcMode var10001 = this.calc;
         if (var10000.equals(AutoCrystalType.calcMode.Normal) && dmg[2] / dmg[0] > highest[0]) {
            return false;
         }

         var10000 = (AutoCrystalType.calcMode)this.calcMode.get();
         var10001 = this.calc;
         if (var10000.equals(AutoCrystalType.calcMode.HyperCard) && dmg[0] < highest[0]) {
            return false;
         }

         var10000 = (AutoCrystalType.calcMode)this.calcMode.get();
         var10001 = this.calc;
         if (var10000.equals(AutoCrystalType.calcMode.earthhack) && dmg[2] / dmg[0] > highest[2] / highest[0]) {
            return false;
         }
      }

      double playerHP = (double)(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount());
      if (playerHP >= (double)0.0F && dmg[2] * (Double)this.selfCheck.get() >= playerHP) {
         return false;
      } else if (health[0] >= (double)0.0F && dmg[0] * (Double)this.Desyncforce.get() >= health[0]) {
         return true;
      } else if (dmg[0] < (Double)this.MinDmg.get()) {
         return false;
      } else if (dmg[1] > (Double)this.maxFriendPlace.get()) {
         return false;
      } else if (dmg[1] >= (double)0.0F && dmg[0] / dmg[1] < (Double)this.minFriendPlaceRatio.get()) {
         return false;
      } else if (dmg[2] > (Double)this.maxPlace.get()) {
         return false;
      } else {
         return dmg[2] < (double)0.0F || dmg[0] / dmg[2] >= (Double)this.minPlaceRatio.get();
      }
   }

   private boolean explodeDamageCheck(double[] dmg, double[] health, boolean own) {
      boolean var9;
      label71: {
         Object var10000 = this.expMode.get();
         AutoCrystalType.ExplodeMode var10001 = this.ExplodeMode;
         if (var10000 != AutoCrystalType.ExplodeMode.Crystal) {
            var10000 = this.expMode.get();
            var10001 = this.ExplodeMode;
            if (var10000 != AutoCrystalType.ExplodeMode.Calc) {
               var9 = false;
               break label71;
            }
         }

         var9 = true;
      }

      boolean checkOwn;
      label81: {
         checkOwn = var9;
         Object var10 = this.expMode.get();
         AutoCrystalType.ExplodeMode var14 = this.ExplodeMode;
         if (var10 != AutoCrystalType.ExplodeMode.Crystal) {
            var10 = this.expMode.get();
            var14 = this.ExplodeMode;
            if (var10 != AutoCrystalType.ExplodeMode.Calc || own) {
               var9 = false;
               break label81;
            }
         }

         var9 = true;
      }

      boolean checkDmg = var9;
      double playerHP = (double)(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount());
      if (checkOwn && playerHP >= (double)0.0F && dmg[0] * (Double)this.Desyncforce.get() >= playerHP) {
         return true;
      } else {
         if (checkDmg) {
            if (health[0] >= (double)0.0F && dmg[0] * (Double)this.Desyncforce.get() >= health[0]) {
               return true;
            }

            if (dmg[0] < (Double)this.minExplode.get()) {
               return false;
            }

            if (dmg[1] >= (double)0.0F && dmg[0] / dmg[1] < (Double)this.minFriendExpRatio.get()) {
               return false;
            }

            if (dmg[2] >= (double)0.0F && dmg[0] / dmg[2] < (Double)this.minExpRatio.get()) {
               return false;
            }
         }

         if (checkOwn) {
            if (dmg[1] > (Double)this.maxFriendExp.get()) {
               return false;
            } else {
               return dmg[2] <= (Double)this.maxExp.get();
            }
         } else {
            return true;
         }
      }
   }

   private boolean isOwn(Vec3d vec) {
      return this.isOwn(BlockPos.ofFloored(vec));
   }

   private boolean isOwn(BlockPos pos) {
      for(Map.Entry<BlockPos, Long> entry : this.own.entrySet()) {
         if (((BlockPos)entry.getKey()).equals(pos)) {
            return true;
         }
      }

      return false;
   }

   private double[][] getDmg(Vec3d vec, boolean attack) {
      double self = BODamageUtils.crystal(this.mc.player, this.extPos.containsKey(this.mc.player) ? (Box)this.extPos.get(this.mc.player) : this.mc.player.getBoundingBox(), vec, this.ignorePos(attack), (Boolean)this.ignoreTerrain.get());
      if (this.suicide) {
         return new double[][]{{self, (double)-1.0F, (double)-1.0F}, {(double)20.0F, (double)20.0F}};
      } else {
         double highestEnemy = (double)-1.0F;
         double highestFriend = (double)-1.0F;
         double enemyHP = (double)-1.0F;
         double friendHP = (double)-1.0F;

         for(Map.Entry<AbstractClientPlayerEntity, Box> entry : this.extPos.entrySet()) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entry.getKey();
            Box box = (Box)entry.getValue();
            if (!(player.getHealth() <= 0.0F) && player != this.mc.player) {
               double dmg = BODamageUtils.crystal(player, box, vec, this.ignorePos(attack), (Boolean)this.ignoreTerrain.get());
               if (BlockPos.ofFloored(vec).down().equals(autoMine.targetPos())) {
                  dmg *= (Double)this.autoMineDamage.get();
               }

               double hp = (double)(player.getHealth() + player.getAbsorptionAmount());
               if (Friends.get().isFriend(player)) {
                  if (dmg > highestFriend) {
                     highestFriend = dmg;
                     friendHP = hp;
                  }
               } else if (dmg > highestEnemy) {
                  highestEnemy = dmg;
                  enemyHP = hp;
                  this.bestTarget = player;
               }
            }
         }

         return new double[][]{{highestEnemy, highestFriend, self}, {enemyHP, friendHP}};
      }
   }

   private boolean inExplodeRangePlacing(Vec3d vec) {
      return SettingUtils.inAttackRange(new Box(vec.getX() - (double)1.0F, vec.getY(), vec.getZ() - (double)1.0F, vec.getX() + (double)1.0F, vec.getY() + (double)2.0F, vec.getZ() + (double)1.0F), this.rangePos != null ? this.rangePos : null);
   }

   private boolean inExplodeRange(Vec3d vec) {
      return SettingUtils.inAttackRange(new Box(vec.getX() - (double)1.0F, vec.getY(), vec.getZ() - (double)1.0F, vec.getX() + (double)1.0F, vec.getY() + (double)2.0F, vec.getZ() + (double)1.0F));
   }

   private double getSpeed() {
      return this.shouldSlow() ? (Double)this.slowSpeed.get() : (Double)this.placeSpeed.get();
   }

   private boolean shouldSlow() {
      return this.placePos != null && this.getDmg(new Vec3d((double)this.placePos.getX() + (double)0.5F, (double)this.placePos.getY(), (double)this.placePos.getZ() + (double)0.5F), false)[0][0] <= (Double)this.slowDamage.get();
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

   private boolean validForIntersect(Entity entity) {
      if (entity instanceof EndCrystalEntity && this.canExplodePlacing(entity.getPos())) {
         return false;
      } else {
         return !(entity instanceof PlayerEntity) || !entity.isSpectator();
      }
   }

   private BlockPos ignorePos(boolean attack) {
      if (!(Boolean)this.amPlace.get()) {
         return null;
      } else if (!(Boolean)this.amSpam.get() && attack) {
         return null;
      } else if (autoMine != null && autoMine.isActive()) {
         if (autoMine.targetPos() == null) {
            return null;
         } else {
            return autoMine.getMineProgress() > (Double)this.amProgress.get() ? autoMine.targetPos() : null;
         }
      } else {
         return null;
      }
   }

   private boolean blockBroken(BlockPos pos) {
      if (!(Boolean)this.amPlace.get()) {
         return false;
      } else if (autoMine != null && autoMine.isActive()) {
         if (autoMine.targetPos() == null) {
            return false;
         } else if (!autoMine.targetPos().equals(pos)) {
            return false;
         } else {
            double progress = autoMine.getMineProgress();
            if (progress >= (double)1.0F && !((AutoCrystalType.AutoMineBrokenMode)this.amBroken.get()).broken) {
               return true;
            } else if (progress >= (Double)this.amProgress.get() && !((AutoCrystalType.AutoMineBrokenMode)this.amBroken.get()).near) {
               return true;
            } else {
               return progress < (Double)this.amProgress.get() && !((AutoCrystalType.AutoMineBrokenMode)this.amBroken.get()).normal;
            }
         }
      } else {
         return false;
      }
   }

   private void addPredict(int id, Vec3d pos, double delay) {
      this.predicts.add(new Predict(id, pos, Math.round((double)System.currentTimeMillis() + delay * (double)1000.0F)));
   }

   private void addSetDead(Entity entity) {
      this.setDeads.add(new SetDead(entity, (long)Math.round((float)System.currentTimeMillis())));
   }

   private void checkDelayed() {
      List<Predict> toRemove = new ArrayList();

      for(Predict p : this.predicts) {
         if (System.currentTimeMillis() >= p.time) {
            this.explode(p.id, p.pos);
            toRemove.add(p);
         }
      }

      List var10001 = this.predicts;
      Objects.requireNonNull(var10001);
      toRemove.forEach(var10001::remove);
      List<SetDead> toRemove2 = new ArrayList();

      for(SetDead p : this.setDeads) {
         if (!(Boolean)this.PauseDead.get() && System.currentTimeMillis() >= p.time) {
            this.setEntityDead(p.entity);
            toRemove2.add(p);
         }
      }

      var10001 = this.setDeads;
      Objects.requireNonNull(var10001);
      toRemove2.forEach(var10001::remove);
   }

   public WWidget getWidget(GuiTheme theme) {
      return MixinPlugin.isSodiumPresent ? theme.label("WARING:maxselfdmg fails when deyncforce is set higher than 1. ") : null;
   }

   private static record Predict(int id, Vec3d pos, long time) {
   }

   private static record SetDead(Entity entity, long time) {
   }
}
