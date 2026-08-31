package espada.spacex.aurora.modules.combatplus.automine;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.globalsettings.SwingSettings;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.RSCombatInfo;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
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
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import org.joml.Vector3d;

public class AuroraMine extends Modules {
   private static AuroraMine INSTANCE = new AuroraMine();
   private final SettingGroup sgGeneral;
   private final SettingGroup sgSpeed;
   private final SettingGroup sgExplode;
   private final SettingGroup sgCev;
   private final SettingGroup sgAntiSurround;
   private final SettingGroup sgAntiBurrow;
   private final SettingGroup sgRender;
   private final SettingGroup sgText;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> pauseanchor;
   private final Setting<Boolean> pauseweb;
   private final Setting<Boolean> pauseSword;
   private final Setting<SwitchMode> pickAxeSwitchMode;
   private final Setting<SwitchMode> crystalSwitchMode;
   private final Setting<Boolean> autoMine;
   private final Setting<ListMode> listMode;
   private final Setting<List<Block>> blacklist;
   private final Setting<List<Block>> whitelist;
   private final Setting<Boolean> manualMine;
   private final Setting<Boolean> manualInsta;
   private final Setting<Boolean> doubleBreak;
   private final Setting<Boolean> silentDouble;
   private final Setting<silenttype> doublesilenttype;
   private final Setting<Boolean> manualRemine;
   private final Setting<Boolean> fastRemine;
   private final Setting<Boolean> manualRangeReset;
   private final Setting<Boolean> resetOnSwitch;
   private final Setting<Boolean> debug;
   private final Setting<Double> speed;
   private final Setting<Double> instaDelay;
   private final Setting<Boolean> onGroundCheck;
   private final Setting<Boolean> effectCheck;
   private final Setting<Boolean> waterCheck;
   private final Setting<Double> explodeSpeed;
   private final Setting<Double> explodeTime;
   private final Setting<Priority> cevPriority;
   private final Setting<Boolean> instaCev;
   private final Setting<Priority> trapCevPriority;
   private final Setting<Boolean> instaTrapCev;
   private final Setting<Priority> surroundCevPriority;
   private final Setting<Boolean> instaSurroundCev;
   private final Setting<Priority> surroundMinerPriority;
   private final Setting<Boolean> instaSurroundMiner;
   private final Setting<Priority> autoCityPriority;
   private final Setting<Boolean> instaAutoCity;
   private final Setting<Boolean> explodeCrystal;
   private final Setting<Priority> antiBurrowPriority;
   private final Setting<Boolean> mineStartSwing;
   private final Setting<Boolean> mineEndSwing;
   private final Setting<SwingHand> mineHand;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> attackSwing;
   private final Setting<SwingHand> attackHand;
   private final Setting<Double> animationExp;
   private final Setting<RenderMode> renderMode;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineStartColor;
   private final Setting<SettingColor> lineEndColor;
   private final Setting<SettingColor> startColor;
   private final Setting<SettingColor> endColor;
   private final Setting<ShapeMode> shapeModeDouble;
   private final Setting<SettingColor> lineStartColorDouble;
   private final Setting<SettingColor> lineEndColorDouble;
   private final Setting<SettingColor> startColorDouble;
   private final Setting<SettingColor> endColorDouble;
   private final Setting<Boolean> text;
   private final Setting<Boolean> shadow;
   private final Setting<Double> textScale;
   private final Setting<SettingColor> startTextColor;
   private final Setting<SettingColor> endTextColor;
   private final Setting<SettingColor> waitColor;
   private final Setting<SettingColor> startTextColorDouble;
   private final Setting<SettingColor> endTextColorDouble;
   private final Setting<SettingColor> waitColorDouble;
   private double minedFor;
   public Target target;
   private boolean started;
   private BlockPos civPos;
   private List<AbstractClientPlayerEntity> enemies;
   private long lastTime;
   private long lastPlace;
   private long lastExplode;
   private long lastCiv;
   private boolean canUpdate;
   private double render;
   private double delta;
   private final Map<BlockPos, Long> explodeAt;
   private boolean reset;
   private boolean mined;
   private int OldSlot;
   private BlockState lastState;
   private BlockPos lastPos;
   public BlockPos breakPos;

   public AuroraMine() {
      super(Aurora.CombatPlus, "AuroraMine", "Automatically mines blocks to destroy your enemies.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgSpeed = this.settings.createGroup("Speed");
      this.sgExplode = this.settings.createGroup("Explode");
      this.sgCev = this.settings.createGroup("Cev");
      this.sgAntiSurround = this.settings.createGroup("Anti Surround");
      this.sgAntiBurrow = this.settings.createGroup("Anti Burrow");
      this.sgRender = this.settings.createGroup("Render");
      this.sgText = this.settings.createGroup("Text");
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause On Eat")).description("Pause while eating.")).defaultValue(false)).build());
      this.pauseanchor = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause On Anchor")).description("Pause while Achor.")).defaultValue(false)).build());
      this.pauseweb = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause On Web")).description("Pause while web.")).defaultValue(false)).build());
      this.pauseSword = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause On Sword")).description("Doesn't mine while holding sword.")).defaultValue(false)).build());
      this.pickAxeSwitchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Pickaxe Switch Mode")).description("Method of switching. InvSwitch is used in most clients.")).defaultValue(AuroraMine.SwitchMode.Silent)).build());
      this.crystalSwitchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Crystal Switch Mode")).description("Method of switching. InvSwitch is used in most clients.")).defaultValue(AuroraMine.SwitchMode.Silent)).build());
      this.autoMine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Mine")).description("Sets target block to the block you clicked.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgGeneral;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("List Mode")).description("Selection mode.")).defaultValue(AuroraMine.ListMode.Blacklist);
      Setting<Boolean> var10003 = this.autoMine;
      Objects.requireNonNull(var10003);
      this.listMode = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.blacklist = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Blacklist")).description("The blocks you don't want to mine.")).defaultValue(new Block[]{Blocks.RESPAWN_ANCHOR, Blocks.BLUE_BED, Blocks.COBWEB}).visible(() -> (Boolean)this.autoMine.get() && this.listMode.get() == AuroraMine.ListMode.Blacklist)).build());
      this.whitelist = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Whitelist")).description("The blocks you want to mine.")).visible(() -> (Boolean)this.autoMine.get() && this.listMode.get() == AuroraMine.ListMode.Whitelist)).build());
      this.manualMine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Manual Mine")).description("Sets target block to the block you clicked.")).defaultValue(true)).build());
      this.manualInsta = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Manual Instant")).description("Uses civ mine when mining manually.")).defaultValue(false)).build());
      this.doubleBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Double Break")).description("Double break.")).defaultValue(true)).build());
      var10001 = this.sgGeneral;
      BoolSetting.Builder var9 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Silent Double")).description("Silent break.")).defaultValue(false);
      var10003 = this.doubleBreak;
      Objects.requireNonNull(var10003);
      this.silentDouble = var10001.add(((BoolSetting.Builder)var9.visible(var10003::get)).build());
      var10001 = this.sgGeneral;
      EnumSetting.Builder var10 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("SilentDoubleMode")).description("Silent break.")).defaultValue(AuroraMine.silenttype.UpdateSelectedSlotC2SPacket);
      var10003 = this.doubleBreak;
      Objects.requireNonNull(var10003);
      this.doublesilenttype = var10001.add(((EnumSetting.Builder)var10.visible(var10003::get)).build());
      this.manualRemine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Manual Remine")).description("Mines the manually mined block again.")).defaultValue(false)).build());
      this.fastRemine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fast Remine")).description("Calculates mining progress from last block broken.")).defaultValue(false)).build());
      this.manualRangeReset = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Manual Range Reset")).description("Resets manual mining if out of range.")).defaultValue(true)).build());
      this.resetOnSwitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Reset On Switch")).description("Resets mining when switched held item.")).defaultValue(false)).build());
      this.debug = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("de Bug")).description("an.")).defaultValue(false)).build());
      this.speed = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("Vanilla speed multiplier.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)2.0F).build());
      this.instaDelay = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Instant Delay")).description("Delay between civ mines.")).defaultValue((double)0.5F).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.onGroundCheck = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("On Ground Check")).description("Mines 5x slower when not on ground.")).defaultValue(true)).build());
      this.effectCheck = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Effect Check")).description("Modifies mining speed depending on haste and mining fatigue.")).defaultValue(true)).build());
      this.waterCheck = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Water Check")).description("Mines 5x slower while submerged in water.")).defaultValue(true)).build());
      this.explodeSpeed = this.sgExplode.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Explode Speed")).description("How many times to attack a crystal every second.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)2.0F).build());
      this.explodeTime = this.sgExplode.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Explode Time")).description("Tries to attack a crystal for this many seconds.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.cevPriority = this.sgCev.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Cev Priority")).description("Priority of cev.")).defaultValue(AuroraMine.Priority.Normal)).build());
      this.instaCev = this.sgCev.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Instant Cev")).description("Only sends 1 mine start packet for each block.")).defaultValue(false)).build());
      this.trapCevPriority = this.sgCev.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Trap Cev Priority")).description("Priority of trap cev.")).defaultValue(AuroraMine.Priority.Normal)).build());
      this.instaTrapCev = this.sgCev.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Instant Trap Cev")).description("Only sends 1 mine start packet for each block.")).defaultValue(false)).build());
      this.surroundCevPriority = this.sgCev.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Surround Cev Priority")).description("Priority of trap cev.")).defaultValue(AuroraMine.Priority.Normal)).build());
      this.instaSurroundCev = this.sgCev.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Instant Surround Cev")).description("Only sends 1 mine start packet for each block.")).defaultValue(false)).build());
      this.surroundMinerPriority = this.sgAntiSurround.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Surround Miner Priority")).description("Priority of surround miner.")).defaultValue(AuroraMine.Priority.Normal)).build());
      this.instaSurroundMiner = this.sgAntiSurround.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Instant Surround Miner")).description("Only sends 1 mine start packet for each block.")).defaultValue(false)).build());
      this.autoCityPriority = this.sgAntiSurround.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Auto City Priority")).description("Priority of anti surround. Places crystal next to enemy's surround block.")).defaultValue(AuroraMine.Priority.Normal)).build());
      this.instaAutoCity = this.sgAntiSurround.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Instant Auto City")).description("Only sends 1 mine start packet for each block.")).defaultValue(false)).build());
      this.explodeCrystal = this.sgAntiSurround.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Explode Crystal")).description("Attacks the crystal we placed.")).defaultValue(false)).build());
      this.antiBurrowPriority = this.sgAntiBurrow.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Anti Burrow Priority")).description("Priority of anti burrow.")).defaultValue(AuroraMine.Priority.Normal)).build());
      this.mineStartSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Mine Start Swing")).description("Renders swing animation when starting mining.")).defaultValue(true)).build());
      this.mineEndSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Mine End Swing")).description("Renders swing animation when ending mining.")).defaultValue(true)).build());
      this.mineHand = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mine Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand)).visible(() -> (Boolean)this.mineStartSwing.get() || (Boolean)this.mineEndSwing.get())).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing a crystal.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var10 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10.visible(var10003::get)).build());
      this.attackSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack Swing")).description("Renders swing animation when attacking a crystal.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var10 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.attackSwing;
      Objects.requireNonNull(var10003);
      this.attackHand = var10001.add(((EnumSetting.Builder)var10.visible(var10003::get)).build());
      this.animationExp = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Animation Exponent")).description("3 - 4 look cool.")).defaultValue((double)3.0F).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.renderMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Render Mode")).description(".")).defaultValue(AuroraMine.RenderMode.Normal)).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of render should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineStartColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Start Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 0)).build());
      this.lineEndColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line End Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.startColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Start Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 0)).build());
      this.endColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side End Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 50)).build());
      this.shapeModeDouble = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Double Shape Mode")).description("Which parts of render should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineStartColorDouble = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Double Line Start Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 0)).build());
      this.lineEndColorDouble = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Double Line End Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.startColorDouble = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Double Side Start Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 0)).build());
      this.endColorDouble = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Double Side End Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 50)).build());
      this.text = this.sgText.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Text")).description("Renders mine progress text in the block overlay.")).defaultValue(false)).build());
      var10001 = this.sgText;
      BoolSetting.Builder var13 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Shadow")).description("Do text shadow render.")).defaultValue(true);
      var10003 = this.text;
      Objects.requireNonNull(var10003);
      this.shadow = var10001.add(((BoolSetting.Builder)var13.visible(var10003::get)).build());
      var10001 = this.sgText;
      DoubleSetting.Builder var14 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Text Scale")).description("How big the progress text should be.")).defaultValue((double)1.0F).min((double)0.0F).sliderMax((double)4.0F);
      var10003 = this.text;
      Objects.requireNonNull(var10003);
      this.textScale = var10001.add(((DoubleSetting.Builder)var14.visible(var10003::get)).build());
      var10001 = this.sgText;
      ColorSetting.Builder var15 = ((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Start Text Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255));
      var10003 = this.text;
      Objects.requireNonNull(var10003);
      this.startTextColor = var10001.add(((ColorSetting.Builder)var15.visible(var10003::get)).build());
      var10001 = this.sgText;
      var15 = ((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("End Text Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255));
      var10003 = this.text;
      Objects.requireNonNull(var10003);
      this.endTextColor = var10001.add(((ColorSetting.Builder)var15.visible(var10003::get)).build());
      this.waitColor = this.sgText.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Waiting Text Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.text.get() && (Boolean)this.manualInsta.get())).build());
      this.startTextColorDouble = this.sgText.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Double Start Text Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.text.get() && (Boolean)this.doubleBreak.get())).build());
      this.endTextColorDouble = this.sgText.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Double End Text Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.text.get() && (Boolean)this.doubleBreak.get())).build());
      this.waitColorDouble = this.sgText.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Double Waiting Text Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.text.get() && (Boolean)this.doubleBreak.get() && (Boolean)this.manualInsta.get())).build());
      this.minedFor = (double)0.0F;
      this.target = null;
      this.started = false;
      this.civPos = null;
      this.enemies = new ArrayList();
      this.lastTime = 0L;
      this.lastPlace = 0L;
      this.lastExplode = 0L;
      this.lastCiv = 0L;
      this.render = (double)1.0F;
      this.delta = (double)0.0F;
      this.explodeAt = new HashMap();
      this.reset = false;
      this.mined = false;
      this.OldSlot = -1;
      this.lastState = null;
      this.lastPos = null;
      this.breakPos = null;
   }

   public static AuroraMine getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new AuroraMine();
      }

      return INSTANCE;
   }

   public void onActivate() {
      this.target = null;
      this.breakPos = null;
      this.minedFor = (double)0.0F;
      this.started = false;
      this.lastTime = System.currentTimeMillis();
      this.civPos = null;
      this.reset = false;
   }

   @EventHandler(
      priority = 200
   )
   private void onSend(PacketEvent.Send event) {
      if (event.packet instanceof UpdateSelectedSlotC2SPacket && (Boolean)this.resetOnSwitch.get()) {
         this.reset = true;
      }

   }

   public String getInfoString() {
      double var10000 = this.getMineProgress() * (double)100.0F < (double)0.0F ? (double)0.0F : Math.floor(this.getMineProgress() * (double)100.0F);
      return "Main " + var10000 + " Double " + (this.getMineProgressDouble() * (double)100.0F < (double)0.0F ? (double)0.0F : Math.floor(this.getMineProgressDouble() * (double)100.0F));
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.target != null && this.target.manual && (Boolean)this.manualRangeReset.get() && !SettingUtils.inMineRange(this.target.pos)) {
            this.minedFor = (double)0.0F;
            this.breakPos = null;
            this.started = false;
            this.lastTime = System.currentTimeMillis();
            this.civPos = null;
            this.reset = false;
         } else {
            if (this.target != null) {
               if (this.lastState != null && this.target.pos.equals(this.lastPos) && this.target.manual && (Boolean)this.manualRemine.get() && !(Boolean)this.fastRemine.get() && !this.lastState.isSolid() && BOBlockUtil.solid2(this.target.pos)) {
                  this.started = false;
               }

               this.lastPos = this.target.pos;
               this.lastState = this.mc.world.getBlockState(this.target.pos);
            } else {
               this.lastPos = null;
               this.lastState = null;
            }

            this.delta = (double)(System.currentTimeMillis() - this.lastTime) / (double)1000.0F;
            this.lastTime = System.currentTimeMillis();
            this.update();
            this.explodeUpdate();
            if (this.target != null) {
               switch (((RenderMode)this.renderMode.get()).ordinal()) {
                  case 0:
                     double progress = MathHelper.clamp(this.getMineProgress(), (double)0.0F, (double)1.0F);
                     Color color1 = progress >= 0.95 ? (Color)this.endColor.get() : (Color)this.startColor.get();
                     Color color2 = progress >= 0.95 ? (Color)this.lineEndColor.get() : (Color)this.lineStartColor.get();
                     event.renderer.box(this.target.pos, color1, color2, (ShapeMode)this.shapeMode.get(), 0);
                     if ((Boolean)this.doubleBreak.get() && this.breakPos != null) {
                        double progressD = MathHelper.clamp(this.getMineProgressDouble(), (double)0.0F, (double)1.0F);
                        Color colorD1 = progressD >= 0.95 ? (Color)this.endColorDouble.get() : (Color)this.startColorDouble.get();
                        Color colorD2 = progressD >= 0.95 ? (Color)this.lineEndColorDouble.get() : (Color)this.lineStartColorDouble.get();
                        event.renderer.box(this.breakPos, colorD1, colorD2, (ShapeMode)this.shapeModeDouble.get(), 0);
                     }
                     break;
                  case 1:
                     int slot = this.getFastestSlot(this.target.pos);
                     this.render = MathHelper.clamp(this.getMineTicks(slot, true) == this.getMineTicks(slot, false) ? this.render + this.delta * (double)2.0F : this.render - this.delta * (double)2.0F, (double)-2.0F, (double)2.0F);
                     double p = (double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicks(slot, false), (double)0.0F, (double)1.0F);
                     p = Math.pow(p, (Double)this.animationExp.get());
                     p = (double)1.0F - p;
                     event.renderer.box(this.getRenderBox(p / (double)2.0F), this.getColor((Color)this.startColor.get(), (Color)this.endColor.get(), p, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColor.get(), (Color)this.lineEndColor.get(), p, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     p = (double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicks(slot, true), (double)0.0F, (double)1.0F);
                     p = Math.pow(p, (Double)this.animationExp.get());
                     p = (double)1.0F - p;
                     event.renderer.box(this.getRenderBox(p / (double)2.0F), this.getColor((Color)this.startColor.get(), (Color)this.endColor.get(), p, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColor.get(), (Color)this.lineEndColor.get(), p, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     if ((Boolean)this.doubleBreak.get() && this.breakPos != null) {
                        int slot2 = this.getFastestSlot(this.breakPos);
                        this.render = MathHelper.clamp(this.getMineTicksDouble(slot2, true) == this.getMineTicksDouble(slot2, false) ? this.render + this.delta * (double)2.0F : this.render - this.delta * (double)2.0F, (double)-2.0F, (double)2.0F);
                        double p22 = (double)1.0F - Math.pow((double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicksDouble(slot2, false), (double)0.0F, (double)1.0F), (Double)this.animationExp.get());
                        event.renderer.box(this.getRenderBoxDouble(p22 / (double)2.0F), this.getColor((Color)this.startColorDouble.get(), (Color)this.endColorDouble.get(), p22, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColorDouble.get(), (Color)this.lineEndColorDouble.get(), p22, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                        double p23 = (double)1.0F - Math.pow((double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicksDouble(slot2, true), (double)0.0F, (double)1.0F), (Double)this.animationExp.get());
                        event.renderer.box(this.getRenderBoxDouble(p23 / (double)2.0F), this.getColor((Color)this.startColorDouble.get(), (Color)this.endColor.get(), p23, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColorDouble.get(), (Color)this.lineEndColorDouble.get(), p23, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     }
                     break;
                  case 2:
                     double shrinkProgress = MathHelper.clamp(this.getMineProgress(), (double)0.0F, (double)1.0F);
                     Color shrinkColor = shrinkProgress >= 0.95 ? (Color)this.endColor.get() : (Color)this.startColor.get();
                     Color shrinkLineColor = shrinkProgress >= 0.95 ? (Color)this.lineEndColor.get() : (Color)this.lineStartColor.get();
                     double shrinkMin = shrinkProgress / (double)2.0F;
                     Vec3d shrinkVec = this.target.pos.toCenterPos();
                     Box shrinkBox = new Box(shrinkVec.x - shrinkMin, shrinkVec.y - shrinkMin, shrinkVec.z - shrinkMin, shrinkVec.x + shrinkMin, shrinkVec.y + shrinkMin, shrinkVec.z + shrinkMin);
                     event.renderer.box(shrinkBox, shrinkColor, shrinkLineColor, (ShapeMode)this.shapeMode.get(), 0);
                     if ((Boolean)this.doubleBreak.get() && this.breakPos != null) {
                        double progressD = MathHelper.clamp(this.getMineProgressDouble(), (double)0.0F, (double)1.0F);
                        Color colorD1 = progressD >= 0.95 ? (Color)this.endColorDouble.get() : (Color)this.startColorDouble.get();
                        Color colorD2 = progressD >= 0.95 ? (Color)this.lineEndColorDouble.get() : (Color)this.lineStartColorDouble.get();
                        double minD = shrinkProgress / (double)2.0F;
                        Vec3d vec3dD = this.breakPos.toCenterPos();
                        Box boxD = new Box(vec3dD.x - minD, vec3dD.y - minD, vec3dD.z - minD, vec3dD.x + minD, vec3dD.y + minD, vec3dD.z + minD);
                        event.renderer.box(boxD, colorD1, colorD2, (ShapeMode)this.shapeModeDouble.get(), 0);
                     }
                     break;
                  case 3:
                     int growSlot = this.getFastestSlot(this.target.pos);
                     this.render = MathHelper.clamp(this.getMineTicks(growSlot, true) == this.getMineTicks(growSlot, false) ? this.render + this.delta * (double)2.0F : this.render - this.delta * (double)2.0F, (double)-2.0F, (double)2.0F);
                     double growMineProgress = MathHelper.clamp(this.getMineProgress(), (double)0.0F, (double)1.0F);
                     double growProgress = (double)1.0F - growMineProgress;
                     double growMax = (double)Math.round(growProgress * (double)100.0F) / (double)100.0F;
                     double growMin = (double)1.0F - growMax;
                     Vec3d growVec = new Vec3d((double)this.target.pos.getX(), (double)this.target.pos.getY(), (double)this.target.pos.getZ());
                     Box growBox = new Box(growVec.x + growMin, growVec.y + growMin, growVec.z + growMin, growVec.x + growMax, growVec.y + growMax, growVec.z + growMax);
                     double growP = (double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicks(growSlot, false), (double)0.0F, (double)1.0F);
                     growP = Math.pow(growP, (Double)this.animationExp.get());
                     growP = (double)1.0F - growP;
                     event.renderer.box(growBox, this.getColor((Color)this.startColor.get(), (Color)this.endColor.get(), growP, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColor.get(), (Color)this.lineEndColor.get(), growP, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     growP = (double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicks(growSlot, true), (double)0.0F, (double)1.0F);
                     growP = Math.pow(growP, (Double)this.animationExp.get());
                     growP = (double)1.0F - growP;
                     event.renderer.box(growBox, this.getColor((Color)this.startColor.get(), (Color)this.endColor.get(), growP, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColor.get(), (Color)this.lineEndColor.get(), growP, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     if ((Boolean)this.doubleBreak.get() && this.breakPos != null) {
                        int slot2 = this.getFastestSlot(this.breakPos);
                        this.render = MathHelper.clamp(this.getMineTicksDouble(slot2, true) == this.getMineTicksDouble(slot2, false) ? this.render + this.delta * (double)2.0F : this.render - this.delta * (double)2.0F, (double)-2.0F, (double)2.0F);
                        double mineProgressD = MathHelper.clamp(this.getMineProgressDouble(), (double)0.0F, (double)1.0F);
                        double progressD = (double)1.0F - mineProgressD;
                        double maxD = (double)Math.round(progressD * (double)100.0F) / (double)100.0F;
                        double minD = (double)1.0F - maxD;
                        Vec3d vec3dD = new Vec3d((double)this.breakPos.getX(), (double)this.breakPos.getY(), (double)this.breakPos.getZ());
                        Box renderBoxD = new Box(vec3dD.x + minD, vec3dD.y + minD, vec3dD.z + minD, vec3dD.x + maxD, vec3dD.y + maxD, vec3dD.z + maxD);
                        double p22 = (double)1.0F - Math.pow((double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicksDouble(slot2, false), (double)0.0F, (double)1.0F), (Double)this.animationExp.get());
                        event.renderer.box(renderBoxD, this.getColor((Color)this.startColorDouble.get(), (Color)this.endColorDouble.get(), p22, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColorDouble.get(), (Color)this.lineEndColorDouble.get(), p22, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                        double p23 = (double)1.0F - Math.pow((double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicksDouble(slot2, true), (double)0.0F, (double)1.0F), (Double)this.animationExp.get());
                        event.renderer.box(renderBoxD, this.getColor((Color)this.startColorDouble.get(), (Color)this.endColor.get(), p23, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColorDouble.get(), (Color)this.lineEndColorDouble.get(), p23, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     }
                     break;
                  case 4:
                     int fillSlot = this.getFastestSlot(this.target.pos);
                     this.render = MathHelper.clamp(this.getMineTicks(fillSlot, true) == this.getMineTicks(fillSlot, false) ? this.render + this.delta * (double)2.0F : this.render - this.delta * (double)2.0F, (double)-2.0F, (double)2.0F);
                     Box fillBox = new Box((double)this.target.pos.getX(), (double)this.target.pos.getY(), (double)this.target.pos.getZ(), (double)(this.target.pos.getX() + 1), (double)this.target.pos.getY() + MathHelper.clamp(this.getMineProgress(), (double)0.0F, (double)1.0F), (double)(this.target.pos.getZ() + 1));
                     double fillP = (double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicks(fillSlot, false), (double)0.0F, (double)1.0F);
                     fillP = Math.pow(fillP, (Double)this.animationExp.get());
                     fillP = (double)1.0F - fillP;
                     event.renderer.box(fillBox, this.getColor((Color)this.startColor.get(), (Color)this.endColor.get(), fillP, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColor.get(), (Color)this.lineEndColor.get(), fillP, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     fillP = (double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicks(fillSlot, true), (double)0.0F, (double)1.0F);
                     fillP = Math.pow(fillP, (Double)this.animationExp.get());
                     fillP = (double)1.0F - fillP;
                     event.renderer.box(fillBox, this.getColor((Color)this.startColor.get(), (Color)this.endColor.get(), fillP, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColor.get(), (Color)this.lineEndColor.get(), fillP, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     if ((Boolean)this.doubleBreak.get() && this.breakPos != null) {
                        int slot2 = this.getFastestSlot(this.breakPos);
                        this.render = MathHelper.clamp(this.getMineTicksDouble(slot2, true) == this.getMineTicksDouble(slot2, false) ? this.render + this.delta * (double)2.0F : this.render - this.delta * (double)2.0F, (double)-2.0F, (double)2.0F);
                        Box renderBoxDouble = new Box((double)this.breakPos.getX(), (double)this.breakPos.getY(), (double)this.breakPos.getZ(), (double)(this.breakPos.getX() + 1), (double)this.breakPos.getY() + MathHelper.clamp(this.getMineProgressDouble(), (double)0.0F, (double)1.0F), (double)(this.breakPos.getZ() + 1));
                        double p22 = (double)1.0F - Math.pow((double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicksDouble(slot2, false), (double)0.0F, (double)1.0F), (Double)this.animationExp.get());
                        event.renderer.box(renderBoxDouble, this.getColor((Color)this.startColorDouble.get(), (Color)this.endColorDouble.get(), p22, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColorDouble.get(), (Color)this.lineEndColorDouble.get(), p22, MathHelper.clamp(this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                        double p23 = (double)1.0F - Math.pow((double)1.0F - MathHelper.clamp(this.minedFor / (double)this.getMineTicksDouble(slot2, true), (double)0.0F, (double)1.0F), (Double)this.animationExp.get());
                        event.renderer.box(renderBoxDouble, this.getColor((Color)this.startColorDouble.get(), (Color)this.endColor.get(), p23, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), this.getColor((Color)this.lineStartColorDouble.get(), (Color)this.lineEndColorDouble.get(), p23, MathHelper.clamp(-this.render, (double)0.0F, (double)1.0F)), (ShapeMode)this.shapeMode.get(), 0);
                     }
               }

               if ((Boolean)this.debug.get()) {
                  BlockPos pos = this.target.pos.offset(SettingUtils.getPlaceOnDirection(this.target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(this.target.pos));
                  Box renderBox = new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1));
                  event.renderer.box(renderBox, new Color(255, 255, 255, 70), new Color(255, 255, 255, 0), (ShapeMode)this.shapeMode.get(), 0);
               }

            }
         }
      }
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if ((Boolean)this.text.get() && (this.target == null || !this.target.manual || !(Boolean)this.manualRangeReset.get() || SettingUtils.inMineRange(this.target.pos)) && this.target != null) {
         Vector3d vec3 = new Vector3d((double)this.target.pos.getX() + (double)0.5F, (double)this.target.pos.getY() + (double)0.5F, (double)this.target.pos.getZ() + (double)0.5F);
         if (NametagUtils.to2D(vec3, (Double)this.textScale.get())) {
            TextRenderer textRenderer = TextRenderer.get();
            NametagUtils.begin(vec3);
            textRenderer.begin((double)1.0F, false, true);
            String text = String.valueOf((int)Math.floor(this.getMineProgress() * (double)100.0F));
            textRenderer.render(this.isAir(this.target.pos) ? "Waiting" : text + "%", -textRenderer.getWidth(text) / (double)2.0F, (double)0.0F, this.get2DTextColor(), (Boolean)this.shadow.get());
            textRenderer.end();
            NametagUtils.end();
         }

         if ((Boolean)this.doubleBreak.get() && this.breakPos != null) {
            Vector3d vec3Double = new Vector3d((double)this.breakPos.getX() + (double)0.5F, (double)this.breakPos.getY() + (double)0.5F, (double)this.breakPos.getZ() + (double)0.5F);
            if (NametagUtils.to2D(vec3Double, (Double)this.textScale.get())) {
               TextRenderer textRenderer = TextRenderer.get();
               NametagUtils.begin(vec3Double);
               textRenderer.begin((double)1.0F, false, true);
               String text = String.valueOf((int)Math.floor(this.getMineProgressDouble() * (double)100.0F));
               textRenderer.render(this.isAir(this.breakPos) ? "Waiting" : text + "%", -textRenderer.getWidth(text) / (double)2.0F, (double)0.0F, this.get2DTextColorDouble(), (Boolean)this.shadow.get());
               textRenderer.end();
               NametagUtils.end();
            }
         }

      }
   }

   private Color get2DTextColor() {
      double progress = this.getMineProgress();
      String text = String.format("%.2f", progress);
      if ((Boolean)this.manualInsta.get() && text.equals("Infinity")) {
         return (Color)this.waitColor.get();
      } else {
         return progress >= 0.95 ? (Color)this.endTextColor.get() : (Color)this.startTextColor.get();
      }
   }

   private Color get2DTextColorDouble() {
      double progress = this.getMineProgressDouble();
      String text = String.format("%.2f", progress);
      if ((Boolean)this.manualInsta.get() && text.equals("Infinity")) {
         return (Color)this.waitColorDouble.get();
      } else {
         return progress >= 0.95 ? (Color)this.endTextColorDouble.get() : (Color)this.startTextColorDouble.get();
      }
   }

   private void explodeUpdate() {
      Entity targetCrystal = null;
      List<BlockPos> toRemove = new ArrayList();

      for(Map.Entry<BlockPos, Long> entry : this.explodeAt.entrySet()) {
         if ((double)(System.currentTimeMillis() - (Long)entry.getValue()) > (Double)this.explodeTime.get() * (double)1000.0F) {
            toRemove.add((BlockPos)entry.getKey());
         }

         EndCrystalEntity crystal = this.crystalAt((BlockPos)entry.getKey());
         if (crystal != null) {
            targetCrystal = crystal;
            break;
         }
      }

      Map var10001 = this.explodeAt;
      Objects.requireNonNull(var10001);
      toRemove.forEach(var10001::remove);
      if (targetCrystal != null && !this.isPaused() && this.mined && (double)(System.currentTimeMillis() - this.lastExplode) > (double)1000.0F / (Double)this.explodeSpeed.get() && (!SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(targetCrystal.getBoundingBox(), (double)this.priority, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"})))) {
         SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
         this.sendPacket(PlayerInteractEntityC2SPacket.attack(targetCrystal, this.mc.player.isSneaking()));
         SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
         if ((Boolean)this.attackSwing.get()) {
            this.clientSwing((SwingHand)this.attackHand.get(), Hand.MAIN_HAND);
         }

         this.lastExplode = System.currentTimeMillis();
         if (SettingUtils.shouldRotate(RotationType.Attacking)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "attacking"}));
         }
      }

   }

   public double getMineProgress() {
      return this.target == null ? (double)-1.0F : this.minedFor / (double)this.getMineTicks(this.fastestSlot(), true);
   }

   public double getMineProgressDouble() {
      return this.breakPos == null ? (double)-1.0F : this.minedFor / (double)this.getMineTicksDouble(this.fastestSlotDouble(), true);
   }

   private void update() {
      if (this.mc.world != null) {
         this.Back();
         if (this.reset) {
            if (this.target != null && !this.target.manual) {
               this.target = null;
            }

            this.started = false;
            this.reset = false;
         }

         this.enemies = this.mc.world.getPlayers().stream().filter((player) -> player != this.mc.player && !Friends.get().isFriend(player) && player.distanceTo(this.mc.player) < 10.0F).toList();
         BlockPos lastPos = this.target != null && this.target.pos != null ? this.target.pos : null;
         if (this.target != null && this.target.manual && (Boolean)this.manualRangeReset.get() && !SettingUtils.inMineRange(this.target.pos)) {
            this.minedFor = (double)0.0F;
            this.breakPos = null;
            this.started = false;
            this.lastTime = System.currentTimeMillis();
            this.civPos = null;
            this.reset = false;
         } else if (this.breakPos != null && this.target != null && (this.mc.world.isAir(this.breakPos) || this.breakPos.equals(this.target.pos) || (Boolean)this.manualRangeReset.get() && !SettingUtils.inMineRange(this.breakPos))) {
            this.breakPos = null;
         }

         if (this.target == null || !this.target.manual) {
            this.target = this.getTarget();
         }

         if (this.target != null) {
            if (this.target.pos != null && !this.target.pos.equals(lastPos)) {
               if (this.started) {
                  this.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.target.pos, Direction.DOWN, 0));
               }

               this.started = false;
            }

            if (!this.started) {
               boolean rotated = !SettingUtils.startMineRot() || Managers.ROTATION.start(this.target.pos, (double)this.priority, RotationType.Mining, (long)Objects.hash(new Object[]{this.name + "mining"}));
               if (BOBlockUtil.getBlock(((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).targetPos()) == Blocks.RESPAWN_ANCHOR && (Boolean)this.pauseanchor.get()) {
                  return;
               }

               if (BOBlockUtil.getBlock(((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).targetPos()) == Blocks.COBWEB && (Boolean)this.pauseweb.get()) {
                  return;
               }

               if (rotated) {
                  this.started = true;
                  this.minedFor = (double)0.0F;
                  this.civPos = null;
                  if (this.getMineTicks(this.fastestSlot(), true) == this.getMineTicks(this.fastestSlot(), false)) {
                     this.render = (double)2.0F;
                  } else {
                     this.render = (double)-2.0F;
                  }

                  this.sendSequenced((s) -> new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.target.pos, SettingUtils.getPlaceOnDirection(this.target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(this.target.pos), s));
                  if ((Boolean)this.doubleBreak.get()) {
                     this.sendSequenced((s) -> new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.target.pos, SettingUtils.getPlaceOnDirection(this.target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(this.target.pos), s));
                     this.sendSequenced((s) -> new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.target.pos, SettingUtils.getPlaceOnDirection(this.target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(this.target.pos), s));
                  }

                  if ((Boolean)this.debug.get()) {
                     String var10001 = String.valueOf(SettingUtils.getPlaceOnDirection(this.target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(this.target.pos));
                     this.error(var10001 + String.valueOf(this.target.pos) + "AutoMine 1", new Object[0]);
                     var10001 = String.valueOf(SettingUtils.getPlaceOnDirection(this.breakPos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(this.breakPos));
                     this.error(var10001 + String.valueOf(this.breakPos) + "AutoMine 2", new Object[0]);
                  }

                  SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);
                  this.mined = false;
                  if ((Boolean)this.mineStartSwing.get()) {
                     this.clientSwing((SwingHand)this.mineHand.get(), Hand.MAIN_HAND);
                  }

                  if (SettingUtils.startMineRot()) {
                     Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "mining"}));
                  }
               }
            }

            if (this.started) {
               this.minedFor += this.delta * (double)20.0F;
               if (!this.isPaused()) {
                  if (this.miningCheck(this.fastestSlot())) {
                     if (this.civCheck()) {
                        if (this.crystalCheck()) {
                           if (BOBlockUtil.solid2(this.target.pos)) {
                              this.endMine();
                              if (this.miningCheckDouble(this.fastestSlotDouble())) {
                                 if (BOBlockUtil.solid2(this.breakPos)) {
                                    this.Double();
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void Double() {
      int slot2 = this.fastestSlotDouble();
      if ((Boolean)this.doubleBreak.get() && (Boolean)this.silentDouble.get()) {
         if (this.breakPos == null || !BOBlockUtil.solid2(this.breakPos) || this.mc.player.isUsingItem() && (Boolean)this.pauseEat.get()) {
            return;
         }

         if (SettingUtils.startMineRot() && !Managers.ROTATION.start(this.breakPos, (double)this.priority, RotationType.Mining, (long)Objects.hash(new Object[]{this.name + "mining"}))) {
            boolean var4 = false;
         } else {
            boolean var10000 = true;
         }

         if (!(this.getBlock(this.breakPos) instanceof AirBlock) && this.getMineProgressDouble() * (double)100.0F > (double)75.0F) {
            if ((Boolean)this.debug.get()) {
               this.error("Silent", new Object[0]);
            }

            SettingUtils.mineSwing(SwingSettings.MiningSwingState.Double);
            if (this.doublesilenttype.get() == AuroraMine.silenttype.UpdateSelectedSlotC2SPacket) {
               this.sendPacket(new UpdateSelectedSlotC2SPacket(slot2));
            } else if (this.doublesilenttype.get() == AuroraMine.silenttype.selectedSlot) {
               if (this.OldSlot == -1) {
                  this.OldSlot = this.mc.player.getInventory().selectedSlot;
               }

               this.mc.player.getInventory().selectedSlot = slot2;
               this.mc.player.getInventory().updateItems();
            }

            if (this.doublesilenttype.get() == AuroraMine.silenttype.ClientselectedSwap) {
               int doubleSlot = this.getFastestSlot(this.breakPos);
               if (!(this.getBlock(this.breakPos) instanceof AirBlock)) {
                  this.OldSlot = this.mc.player.getInventory().selectedSlot;
                  this.sendPacket(new UpdateSelectedSlotC2SPacket(doubleSlot));
                  this.canUpdate = true;
               }
            }

            if (SettingUtils.startMineRot()) {
               Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "mining"}));
            }

            this.canUpdate = true;
         }
      }

   }

   private void Back() {
      if (this.canUpdate && this.breakPos != null && this.getBlock(this.breakPos) instanceof AirBlock) {
         if ((Boolean)this.debug.get()) {
            this.error("Back", new Object[0]);
         }

         SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
         if (this.doublesilenttype.get() == AuroraMine.silenttype.UpdateSelectedSlotC2SPacket) {
            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
         } else if (this.doublesilenttype.get() == AuroraMine.silenttype.selectedSlot && this.OldSlot != -1) {
            this.mc.player.getInventory().selectedSlot = this.OldSlot;
            this.mc.player.getInventory().updateItems();
            this.OldSlot = -1;
         }

         this.canUpdate = false;
      }

      if (Math.floor(this.getMineProgressDouble() * (double)100.0F) > (double)125.0F) {
         if ((Boolean)this.debug.get()) {
            this.error("Back", new Object[0]);
         }

         SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
         if (this.doublesilenttype.get() == AuroraMine.silenttype.UpdateSelectedSlotC2SPacket) {
            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
         } else if (this.doublesilenttype.get() == AuroraMine.silenttype.selectedSlot && this.OldSlot != -1) {
            this.mc.player.getInventory().selectedSlot = this.OldSlot;
            this.mc.player.getInventory().updateItems();
            this.OldSlot = -1;
         }

         if (this.breakPos != null) {
            this.breakPos = null;
         }

         this.canUpdate = false;
      }

   }

   private boolean isPaused() {
      if ((Boolean)this.pauseEat.get() && this.mc.player.isUsingItem()) {
         return true;
      } else {
         return (Boolean)this.pauseSword.get() && this.mc.player.getMainHandStack().getItem() instanceof SwordItem;
      }
   }

   private boolean civCheck() {
      if (this.civPos == null) {
         return true;
      } else {
         return !((double)(System.currentTimeMillis() - this.lastCiv) < (Double)this.instaDelay.get() * (double)1000.0F);
      }
   }

   private void endMine() {
      int slot = this.fastestSlot();
      boolean switched = this.miningCheck(Managers.HOLDING.slot);
      boolean swapBack = false;
      if (!SettingUtils.shouldRotate(RotationType.Mining) || Managers.ROTATION.start(this.target.pos, (double)this.priority, RotationType.Mining, (long)Objects.hash(new Object[]{this.name + "mining"}))) {
         if (!switched) {
            switch (((SwitchMode)this.pickAxeSwitchMode.get()).ordinal()) {
               case 0:
                  switched = true;
                  InvUtils.swap(slot, true);
                  break;
               case 1:
                  switched = true;
                  BOInvUtils.pickSwitch(slot);
                  break;
               case 2:
                  switched = BOInvUtils.invSwitch(slot);
            }

            swapBack = switched;
         }

         if (switched) {
            this.sendSequenced((s) -> new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.target.pos, SettingUtils.getPlaceOnDirection(this.target.pos) == null ? Direction.UP : SettingUtils.getPlaceOnDirection(this.target.pos), s));
            this.mined = true;
            SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
            if ((Boolean)this.mineEndSwing.get()) {
               this.clientSwing((SwingHand)this.mineHand.get(), Hand.MAIN_HAND);
            }

            if (this.target.civ) {
               this.civPos = this.target.pos;
            }

            if (SettingUtils.endMineRot()) {
               Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "mining"}));
            }

            if (swapBack) {
               switch (((SwitchMode)this.pickAxeSwitchMode.get()).ordinal()) {
                  case 0 -> InvUtils.swapBack();
                  case 1 -> BOInvUtils.pickSwapBack();
                  case 2 -> BOInvUtils.invSwapBack();
               }
            }

            if (this.target.civ) {
               this.civPos = this.target.pos;
               this.lastCiv = System.currentTimeMillis();
            } else if (this.target.manual && (Boolean)this.manualRemine.get()) {
               this.minedFor = (double)0.0F;
            } else {
               this.target = null;
               this.minedFor = (double)0.0F;
            }

         }
      }
   }

   private boolean crystalCheck() {
      switch (this.target.type.ordinal()) {
         case 0:
         case 1:
         case 2:
            if (this.crystalAt(this.target.crystalPos) != null) {
               return true;
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(this.target.crystalPos)).withMaxY((double)(this.target.crystalPos.getY() + (SettingUtils.cc() ? 1 : 2))), (entity) -> !entity.isSpectator())) {
               this.placeCrystal();
               return false;
            }
            break;
         case 3:
         default:
            return true;
         case 4:
            if (this.crystalAt(this.target.crystalPos) != null) {
               return true;
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(this.target.crystalPos)).withMaxY((double)(this.target.crystalPos.getY() + (SettingUtils.cc() ? 1 : 2))), (entity) -> !entity.isSpectator())) {
               return this.placeCrystal();
            }
      }

      return false;
   }

   private int getFastestSlot(BlockPos blockPos) {
      int slot = -1;
      if (this.mc.player != null && this.mc.world != null) {
         for(int i = 0; i < (this.pickAxeSwitchMode.get() == AuroraMine.SwitchMode.Silent ? 9 : 35); ++i) {
            if (slot == -1 || this.mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(this.mc.world.getBlockState(blockPos)) > this.mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(this.mc.world.getBlockState(blockPos))) {
               slot = i;
            }
         }

         return slot;
      } else {
         return -1;
      }
   }

   private EndCrystalEntity crystalAt(BlockPos pos) {
      for(Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof EndCrystalEntity crystal) {
            if (entity.getBlockPos().equals(pos)) {
               return crystal;
            }
         }
      }

      return null;
   }

   private boolean placeCrystal() {
      if (System.currentTimeMillis() - this.lastPlace < 250L) {
         return false;
      } else {
         Hand hand = this.getHand();
         int crystalSlot = InvUtils.find(new Item[]{Items.END_CRYSTAL}).slot();
         if (hand == null && crystalSlot < 0) {
            return false;
         } else {
            Direction dir = SettingUtils.getPlaceOnDirection(this.target.crystalPos.down());
            if (dir == null) {
               return false;
            } else {
               boolean rotated = !SettingUtils.shouldRotate(RotationType.Interact) || Managers.ROTATION.start(this.target.crystalPos.down(), (double)this.priority, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "placing"}));
               if (!rotated) {
                  return false;
               } else {
                  boolean switched = hand != null;
                  if (!switched) {
                     switch (((SwitchMode)this.crystalSwitchMode.get()).ordinal()) {
                        case 0:
                           switched = true;
                           InvUtils.swap(crystalSlot, true);
                           break;
                        case 1:
                           switched = BOInvUtils.pickSwitch(crystalSlot);
                           break;
                        case 2:
                           switched = BOInvUtils.invSwitch(crystalSlot);
                     }
                  }

                  if (!switched) {
                     return false;
                  } else {
                     this.interactBlock(hand == null ? Hand.MAIN_HAND : hand, this.target.crystalPos.down().toCenterPos(), dir, this.target.crystalPos.down());
                     if ((Boolean)this.placeSwing.get()) {
                        this.clientSwing((SwingHand)this.placeHand.get(), hand == null ? Hand.MAIN_HAND : hand);
                     }

                     this.lastPlace = System.currentTimeMillis();
                     if (this.shouldExplode()) {
                        this.addExplode();
                     }

                     if (SettingUtils.shouldRotate(RotationType.Interact)) {
                        Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
                     }

                     if (hand == null) {
                        switch (((SwitchMode)this.crystalSwitchMode.get()).ordinal()) {
                           case 0 -> InvUtils.swapBack();
                           case 1 -> BOInvUtils.pickSwapBack();
                           case 2 -> BOInvUtils.invSwapBack();
                        }
                     }

                     return true;
                  }
               }
            }
         }
      }
   }

   private void addExplode() {
      this.explodeAt.remove(this.target.crystalPos);
      this.explodeAt.put(this.target.crystalPos, System.currentTimeMillis());
   }

   private boolean shouldExplode() {
      boolean var10000;
      switch (this.target.type.ordinal()) {
         case 0:
         case 1:
         case 2:
            var10000 = true;
            break;
         case 3:
         case 5:
         case 6:
            var10000 = false;
            break;
         case 4:
            var10000 = (Boolean)this.explodeCrystal.get();
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private Target getTarget() {
      Target target = null;
      if (!(Boolean)this.autoMine.get()) {
         return target;
      } else {
         if (this.priorityCheck(target, (Priority)this.cevPriority.get())) {
            Target t = this.getCev();
            if (t != null) {
               if (this.listMode.get() == AuroraMine.ListMode.Whitelist && !((List)this.whitelist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               if (this.listMode.get() == AuroraMine.ListMode.Blacklist && ((List)this.blacklist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               target = t;
            }
         }

         if (this.priorityCheck(target, (Priority)this.trapCevPriority.get())) {
            Target t = this.getTrapCev();
            if (t != null) {
               if (this.listMode.get() == AuroraMine.ListMode.Whitelist && !((List)this.whitelist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               if (this.listMode.get() == AuroraMine.ListMode.Blacklist && ((List)this.blacklist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               target = t;
            }
         }

         if (this.priorityCheck(target, (Priority)this.surroundCevPriority.get())) {
            Target t = this.getSurroundCev();
            if (t != null) {
               if (this.listMode.get() == AuroraMine.ListMode.Whitelist && !((List)this.whitelist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               if (this.listMode.get() == AuroraMine.ListMode.Blacklist && ((List)this.blacklist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               target = t;
            }
         }

         if (this.priorityCheck(target, (Priority)this.surroundMinerPriority.get())) {
            Target t = this.getSurroundMiner();
            if (t != null) {
               if (this.listMode.get() == AuroraMine.ListMode.Whitelist && !((List)this.whitelist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               if (this.listMode.get() == AuroraMine.ListMode.Blacklist && ((List)this.blacklist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               target = t;
            }
         }

         if (this.priorityCheck(target, (Priority)this.autoCityPriority.get())) {
            Target t = this.getAutoCity();
            if (t != null) {
               if (this.listMode.get() == AuroraMine.ListMode.Whitelist && !((List)this.whitelist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               if (this.listMode.get() == AuroraMine.ListMode.Blacklist && ((List)this.blacklist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               target = t;
            }
         }

         if (this.priorityCheck(target, (Priority)this.antiBurrowPriority.get())) {
            Target t = this.getAntiBurrow();
            if (t != null) {
               if (this.listMode.get() == AuroraMine.ListMode.Whitelist && !((List)this.whitelist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               if (this.listMode.get() == AuroraMine.ListMode.Blacklist && ((List)this.blacklist.get()).contains(this.getBlock(t.pos))) {
                  return null;
               }

               target = t;
            }
         }

         return target;
      }
   }

   private Target getCev() {
      boolean civ = (Boolean)this.instaCev.get();
      Target best = null;
      double distance = (double)1000.0F;

      for(AbstractClientPlayerEntity player : this.enemies) {
         BlockPos pos = new BlockPos(player.getBlockX(), (int)Math.floor(player.getBoundingBox().maxY) + 1, player.getBlockZ());
         if ((civ && pos.equals(this.civPos) || this.getBlock(pos) == Blocks.OBSIDIAN) && (!civ || !pos.equals(this.civPos) || this.getBlock(pos) instanceof AirBlock || this.getBlock(pos) == Blocks.OBSIDIAN) && this.getBlock(pos.up()) == Blocks.AIR && (!SettingUtils.oldCrystals() || this.getBlock(pos.up(2)) == Blocks.AIR) && SettingUtils.inMineRange(pos) && SettingUtils.inPlaceRange(pos) && SettingUtils.inAttackRange(RSCombatInfo.getCrystalBox(pos.up())) && !this.blocked(pos.up())) {
            double d = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
            if (this.distanceCheck(civ, pos, distance, d)) {
               best = new Target(pos, pos.up(), AuroraMine.MineType.Cev, (double)((Priority)this.cevPriority.get()).priority + (civ && pos.equals(this.civPos) ? 0.1 : (double)0.0F), civ, false);
               distance = d;
            }
         }
      }

      return best;
   }

   private Target getTrapCev() {
      boolean civ = (Boolean)this.instaTrapCev.get();
      Target best = null;
      double distance = (double)1000.0F;

      for(AbstractClientPlayerEntity player : this.enemies) {
         for(Direction dir : Type.HORIZONTAL) {
            BlockPos pos = (new BlockPos(player.getBlockX(), (int)Math.floor(player.getBoundingBox().maxY), player.getBlockZ())).offset(dir);
            if ((civ && pos.equals(this.civPos) || this.getBlock(pos) == Blocks.OBSIDIAN) && (!civ || !pos.equals(this.civPos) || this.getBlock(pos) instanceof AirBlock || this.getBlock(pos) == Blocks.OBSIDIAN) && this.getBlock(pos.up()) == Blocks.AIR && (!SettingUtils.oldCrystals() || this.getBlock(pos.up(2)) == Blocks.AIR) && SettingUtils.inMineRange(pos) && SettingUtils.inPlaceRange(pos) && SettingUtils.inAttackRange(RSCombatInfo.getCrystalBox(pos.up())) && !this.blocked(pos.up())) {
               double d = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
               if (this.distanceCheck(civ, pos, distance, d)) {
                  best = new Target(pos, pos.up(), AuroraMine.MineType.TrapCev, (double)((Priority)this.trapCevPriority.get()).priority + (civ && pos.equals(this.civPos) ? 0.1 : (double)0.0F), civ, false);
                  distance = d;
               }
            }
         }
      }

      return best;
   }

   private Target getSurroundCev() {
      boolean civ = (Boolean)this.instaSurroundCev.get();
      Target best = null;
      double distance = (double)1000.0F;

      for(AbstractClientPlayerEntity player : this.enemies) {
         for(Direction dir : Type.HORIZONTAL) {
            BlockPos pos = this.getPos(player.getPos()).offset(dir);
            if ((civ && pos.equals(this.civPos) || this.getBlock(pos) == Blocks.OBSIDIAN) && (!civ || !pos.equals(this.civPos) || this.getBlock(pos) instanceof AirBlock || this.getBlock(pos) == Blocks.OBSIDIAN) && this.getBlock(pos.up()) == Blocks.AIR && (!SettingUtils.oldCrystals() || this.getBlock(pos.up(2)) == Blocks.AIR) && SettingUtils.inMineRange(pos) && SettingUtils.inPlaceRange(pos) && SettingUtils.inAttackRange(RSCombatInfo.getCrystalBox(pos.up())) && !this.blocked(pos.up())) {
               double d = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
               if (this.distanceCheck(civ, pos, distance, d)) {
                  best = new Target(pos, pos.up(), AuroraMine.MineType.SurroundCev, (double)((Priority)this.surroundCevPriority.get()).priority + (civ && pos.equals(this.civPos) ? 0.1 : (double)0.0F), civ, false);
                  distance = d;
               }
            }
         }
      }

      return best;
   }

   private Target getSurroundMiner() {
      boolean civ = (Boolean)this.instaSurroundMiner.get();
      Target best = null;
      double distance = (double)1000.0F;

      for(AbstractClientPlayerEntity player : this.enemies) {
         for(Direction dir : Type.HORIZONTAL) {
            BlockPos pos = this.getPos(player.getPos()).offset(dir);
            if ((civ && pos.equals(this.civPos) || BOBlockUtil.solid2(pos)) && this.getBlock(pos) != Blocks.BEDROCK && SettingUtils.inMineRange(pos)) {
               double d = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
               if (this.distanceCheck(civ, pos, distance, d)) {
                  best = new Target(pos, (BlockPos)null, AuroraMine.MineType.SurroundMiner, (double)((Priority)this.surroundMinerPriority.get()).priority + (civ && pos.equals(this.civPos) ? 0.1 : (double)0.0F), civ, false);
                  distance = d;
               }
            }
         }
      }

      return best;
   }

   private Target getAutoCity() {
      boolean civ = (Boolean)this.instaAutoCity.get();
      Target best = null;
      double distance = (double)1000.0F;

      for(AbstractClientPlayerEntity player : this.enemies) {
         for(Direction dir : Type.HORIZONTAL) {
            BlockPos pos = this.getPos(player.getPos()).offset(dir);
            if ((civ && pos.equals(this.civPos) || BOBlockUtil.solid2(pos)) && this.getBlock(pos) != Blocks.BEDROCK && this.getBlock(pos.offset(dir)) == Blocks.AIR && (!SettingUtils.oldCrystals() || this.getBlock(pos.offset(dir).up()) == Blocks.AIR) && this.crystalBlock(pos.offset(dir).down()) && SettingUtils.inMineRange(pos) && SettingUtils.inPlaceRange(pos.offset(dir).down()) && !this.blocked(pos.offset(dir))) {
               double d = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
               if (this.distanceCheck(civ, pos, distance, d)) {
                  best = new Target(pos, pos.offset(dir), AuroraMine.MineType.AutoCity, (double)((Priority)this.autoCityPriority.get()).priority + (civ && pos.equals(this.civPos) ? 0.1 : (double)0.0F), civ, false);
                  distance = d;
               }
            }
         }
      }

      return best;
   }

   private Target getAntiBurrow() {
      Target best = null;
      double distance = (double)1000.0F;

      for(AbstractClientPlayerEntity player : this.enemies) {
         BlockPos pos = this.getPos(player.getPos());
         if (BOBlockUtil.solid2(pos) && this.getBlock(pos) != Blocks.BEDROCK && this.getBlock(pos) != Blocks.COBWEB && SettingUtils.inMineRange(pos)) {
            double d = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
            if (d < distance) {
               best = new Target(pos, (BlockPos)null, AuroraMine.MineType.AntiBurrow, (double)((Priority)this.antiBurrowPriority.get()).priority, false, false);
               distance = d;
            }
         }
      }

      return best;
   }

   private boolean distanceCheck(boolean civ, BlockPos pos, double closest, double distance) {
      if (civ && pos.equals(this.civPos)) {
         return true;
      } else if (this.target != null && pos.equals(this.target.pos)) {
         return true;
      } else {
         return distance < closest;
      }
   }

   private boolean priorityCheck(Target current, Priority priority) {
      if (priority.priority < 0) {
         return false;
      } else if (current == null) {
         return true;
      } else {
         return (double)priority.priority >= current.priority;
      }
   }

   private void abort(BlockPos pos) {
      this.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, pos, Direction.UP));
      this.started = false;
   }

   private Block getBlock(BlockPos pos) {
      return this.mc.world.getBlockState(pos).getBlock();
   }

   private Hand getHand() {
      if (this.mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
         return Hand.OFF_HAND;
      } else {
         return Managers.HOLDING.isHolding(Items.END_CRYSTAL) ? Hand.MAIN_HAND : null;
      }
   }

   private boolean miningCheck(int slot) {
      if (this.target != null && this.target.pos != null) {
         return this.minedFor * (Double)this.speed.get() >= (double)this.getMineTicks(slot, true);
      } else {
         return false;
      }
   }

   private boolean miningCheckDouble(int slot) {
      if (this.breakPos == null) {
         return false;
      } else {
         return this.minedFor * (Double)this.speed.get() >= (double)this.getMineTicksDouble(slot, true);
      }
   }

   private float getTime(BlockPos pos, int slot, boolean speedMod) {
      BlockState state = this.mc.world.getBlockState(pos);
      float f = state.getHardness(this.mc.world, pos);
      if (f == -1.0F) {
         return 0.0F;
      } else {
         float i = state.isToolRequired() && !this.mc.player.getInventory().getStack(slot).isSuitableFor(state) ? 100.0F : 30.0F;
         return this.getSpeed(state, slot, speedMod) / f / i;
      }
   }

   private float getMineTicks(int slot, boolean speedMod) {
      return slot == -1 ? (float)slot : (float)((double)1.0F / ((double)this.getTime(this.target.pos, slot, speedMod) * (Double)this.speed.get()));
   }

   private float getMineTicksDouble(int slot, boolean speedMod) {
      return slot == -1 ? (float)slot : (float)((double)1.0F / ((double)this.getTime(this.breakPos, slot, speedMod) * (Double)this.speed.get()));
   }

   private float getSpeed(BlockState state, int slot, boolean speedMod) {
      ItemStack stack = this.mc.player.getInventory().getStack(slot);
      float f = this.mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
      if ((double)f > (double)1.0F) {
         int i = EnchantmentHelper.getLevel((RegistryEntry)this.mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY).get(), stack);
         if (i > 0 && !stack.isEmpty()) {
            f += (float)(i * i + 1);
         }
      }

      if (!speedMod) {
         return f;
      } else {
         if ((Boolean)this.effectCheck.get()) {
            if (StatusEffectUtil.hasHaste(this.mc.player)) {
               f = (float)((double)f * ((double)1.0F + (double)((float)(StatusEffectUtil.getHasteAmplifier(this.mc.player) + 1) * 0.2F)));
            }

            if (this.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
               f = (float)((double)f * Math.pow(0.3, (double)(this.mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier() + 1)));
            }
         }

         if ((Boolean)this.waterCheck.get() && this.mc.player.isSubmergedInWater() && EnchantmentHelper.getEquipmentLevel((RegistryEntry)this.mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.AQUA_AFFINITY).get(), this.mc.player) == 0) {
            f = (float)((double)f / (double)5.0F);
         }

         if ((Boolean)this.onGroundCheck.get() && !this.mc.player.isOnGround()) {
            f = (float)((double)f / (double)5.0F);
         }

         return f;
      }
   }

   public void onStart(BlockPos pos, Direction direction) {
      if (this.target != null && this.target.manual && pos.equals(this.target.pos)) {
         this.abort(this.target.pos);
         this.civPos = null;
         this.target = null;
      } else {
         if (this.target != null && this.breakPos == null && !this.mc.world.isAir(this.target.pos)) {
            this.breakPos = this.targetPos();
         }

         if ((Boolean)this.manualMine.get() && this.getBlock(pos) != Blocks.BEDROCK) {
            this.started = false;
            this.target = new Target(pos, (BlockPos)null, AuroraMine.MineType.Manual, (double)0.0F, (Boolean)this.manualInsta.get(), true);
         }

      }
   }

   public void onAbort(BlockPos pos) {
   }

   public void onStop() {
      this.target = null;
      this.started = false;
   }

   private int fastestSlot() {
      int slot = -1;
      if (this.mc.player != null && this.mc.world != null) {
         for(int i = 0; i < (this.pickAxeSwitchMode.get() == AuroraMine.SwitchMode.Silent ? 9 : 35); ++i) {
            if (slot == -1 || this.mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(this.mc.world.getBlockState(this.target.pos)) > this.mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(this.mc.world.getBlockState(this.target.pos))) {
               slot = i;
            }
         }

         return slot;
      } else {
         return -1;
      }
   }

   private int fastestSlotDouble() {
      int slot = -1;
      if (this.mc.player != null && this.mc.world != null) {
         if (this.breakPos == null) {
            return -1;
         } else {
            for(int i = 0; i < (this.pickAxeSwitchMode.get() == AuroraMine.SwitchMode.Silent ? 9 : 35); ++i) {
               if (slot == -1 || this.mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(this.mc.world.getBlockState(this.breakPos)) > this.mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(this.mc.world.getBlockState(this.breakPos))) {
                  slot = i;
               }
            }

            return slot;
         }
      } else {
         return -1;
      }
   }

   private Color getColor(Color start, Color end, double progress, double alphaMulti) {
      return new Color(this.lerp((double)start.r, (double)end.r, progress, (double)1.0F), this.lerp((double)start.g, (double)end.g, progress, (double)1.0F), this.lerp((double)start.b, (double)end.b, progress, (double)1.0F), this.lerp((double)start.a, (double)end.a, progress, alphaMulti));
   }

   private int lerp(double start, double end, double d, double multi) {
      return (int)Math.round((start + (end - start) * d) * multi);
   }

   private boolean crystalBlock(BlockPos pos) {
      return this.getBlock(pos) == Blocks.OBSIDIAN || this.getBlock(pos) == Blocks.BEDROCK;
   }

   private Box getRenderBox(double progress) {
      return new Box((double)this.target.pos.getX() + (double)0.5F - progress, (double)this.target.pos.getY() + (double)0.5F - progress, (double)this.target.pos.getZ() + (double)0.5F - progress, (double)this.target.pos.getX() + (double)0.5F + progress, (double)this.target.pos.getY() + (double)0.5F + progress, (double)this.target.pos.getZ() + (double)0.5F + progress);
   }

   private Box getRenderBoxDouble(double progress) {
      return new Box((double)this.breakPos.getX() + (double)0.5F - progress, (double)this.breakPos.getY() + (double)0.5F - progress, (double)this.breakPos.getZ() + (double)0.5F - progress, (double)this.breakPos.getX() + (double)0.5F + progress, (double)this.breakPos.getY() + (double)0.5F + progress, (double)this.breakPos.getZ() + (double)0.5F + progress);
   }

   private boolean blocked(BlockPos pos) {
      Box box = new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + (SettingUtils.cc() ? 1 : 2)), (double)(pos.getZ() + 1));
      return EntityUtils.intersectsWithEntity(box, (entity) -> entity instanceof PlayerEntity && !entity.isSpectator());
   }

   public BlockPos targetPos() {
      return this.target == null ? null : this.target.pos;
   }

   private BlockPos getPos(Vec3d vec) {
      return new BlockPos((int)Math.floor(vec.x), (int)Math.round(vec.y), (int)Math.floor(vec.z));
   }

   private boolean isAir(BlockPos blockPos) {
      return this.mc.world.isAir(blockPos) || this.getBlock(blockPos).equals(Blocks.FIRE);
   }

   public static enum ListMode {
      Whitelist,
      Blacklist;

      // $FF: synthetic method
      private static ListMode[] $values() {
         return new ListMode[]{Whitelist, Blacklist};
      }
   }

   public static enum SwitchMode {
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Silent, PickSilent, InvSwitch};
      }
   }

   public static enum RenderMode {
      Box,
      Normal,
      Future,
      Shrink,
      Grow;

      // $FF: synthetic method
      private static RenderMode[] $values() {
         return new RenderMode[]{Box, Normal, Future, Shrink, Grow};
      }
   }

   public static enum Priority {
      Highest(6),
      Higher(5),
      High(4),
      Normal(3),
      Low(2),
      Lower(1),
      Lowest(0),
      Disabled(-1);

      public final int priority;

      private Priority(int priority) {
         this.priority = priority;
      }

      // $FF: synthetic method
      private static Priority[] $values() {
         return new Priority[]{Highest, Higher, High, Normal, Low, Lower, Lowest, Disabled};
      }
   }

   public static enum MineType {
      Cev,
      TrapCev,
      SurroundCev,
      SurroundMiner,
      AutoCity,
      AntiBurrow,
      Manual;

      // $FF: synthetic method
      private static MineType[] $values() {
         return new MineType[]{Cev, TrapCev, SurroundCev, SurroundMiner, AutoCity, AntiBurrow, Manual};
      }
   }

   public static enum silenttype {
      UpdateSelectedSlotC2SPacket,
      selectedSlot,
      ClientselectedSwap;

      // $FF: synthetic method
      private static silenttype[] $values() {
         return new silenttype[]{UpdateSelectedSlotC2SPacket, selectedSlot, ClientselectedSwap};
      }
   }

   public static record Target(BlockPos pos, BlockPos crystalPos, MineType type, double priority, boolean civ, boolean manual) {
   }
}
