package espada.spacex.aurora;

import com.mojang.logging.LogUtils;
import espada.spacex.aurora.commands.BlackoutGit;
import espada.spacex.aurora.commands.Coords;
import espada.spacex.aurora.hud.ArmorHudPlus;
import espada.spacex.aurora.hud.AuroraArray;
import espada.spacex.aurora.hud.CatGirl;
import espada.spacex.aurora.hud.GearHud;
import espada.spacex.aurora.hud.HudWaterMark;
import espada.spacex.aurora.hud.Keys;
import espada.spacex.aurora.hud.MineHud;
import espada.spacex.aurora.hud.OnTope;
import espada.spacex.aurora.hud.PacketHud;
import espada.spacex.aurora.hud.Rsr;
import espada.spacex.aurora.hud.TargetHud;
import espada.spacex.aurora.hud.TickShiftHud;
import espada.spacex.aurora.hud.TimerPlusHud;
import espada.spacex.aurora.hud.Welcomer;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.combatplus.AntiPiston;
import espada.spacex.aurora.modules.combatplus.AntiWeak;
import espada.spacex.aurora.modules.combatplus.Aura;
import espada.spacex.aurora.modules.combatplus.AutoAnvil;
import espada.spacex.aurora.modules.combatplus.AutoMoan;
import espada.spacex.aurora.modules.combatplus.AutoTrapPlus;
import espada.spacex.aurora.modules.combatplus.BedAuraPlus;
import espada.spacex.aurora.modules.combatplus.Blocker;
import espada.spacex.aurora.modules.combatplus.CevBreaker;
import espada.spacex.aurora.modules.combatplus.FemboyPush;
import espada.spacex.aurora.modules.combatplus.HoleFillPlus;
import espada.spacex.aurora.modules.combatplus.HoleFillRewrite;
import espada.spacex.aurora.modules.combatplus.KeyCity;
import espada.spacex.aurora.modules.combatplus.MaoJunQingAura;
import espada.spacex.aurora.modules.combatplus.PistonCrystal;
import espada.spacex.aurora.modules.combatplus.PistonPush;
import espada.spacex.aurora.modules.combatplus.SelfTrapPlus;
import espada.spacex.aurora.modules.combatplus.SurroundPlus;
import espada.spacex.aurora.modules.combatplus.autocrystal.AutoCrystal;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.modules.combatplus.autoweb.AutoWeb;
import espada.spacex.aurora.modules.combatplus.autoweb.FaceWebHelper;
import espada.spacex.aurora.modules.globalsettings.ColorSetting;
import espada.spacex.aurora.modules.globalsettings.FacingSettings;
import espada.spacex.aurora.modules.globalsettings.RSRClientPlusTitle;
import espada.spacex.aurora.modules.globalsettings.RangeSettings;
import espada.spacex.aurora.modules.globalsettings.RaytraceSettings;
import espada.spacex.aurora.modules.globalsettings.RotationPrioritySettings;
import espada.spacex.aurora.modules.globalsettings.RotationSettings;
import espada.spacex.aurora.modules.globalsettings.ServerSettings;
import espada.spacex.aurora.modules.globalsettings.SwingSettings;
import espada.spacex.aurora.modules.miscplus.AutoEz;
import espada.spacex.aurora.modules.miscplus.AutoLoadKit;
import espada.spacex.aurora.modules.miscplus.Automation;
import espada.spacex.aurora.modules.miscplus.CapesModule;
import espada.spacex.aurora.modules.miscplus.RPC;
import espada.spacex.aurora.modules.miscplus.SoundModifier;
import espada.spacex.aurora.modules.miscplus.Suffix;
import espada.spacex.aurora.modules.miscplus.WeakAlert;
import espada.spacex.aurora.modules.movementplus.BRotateBypass;
import espada.spacex.aurora.modules.movementplus.BurrowMove;
import espada.spacex.aurora.modules.movementplus.ElytraFlyPlus;
import espada.spacex.aurora.modules.movementplus.FastWeb;
import espada.spacex.aurora.modules.movementplus.FlightPlus;
import espada.spacex.aurora.modules.movementplus.HoleSnap;
import espada.spacex.aurora.modules.movementplus.JesusPlus;
import espada.spacex.aurora.modules.movementplus.MoveFix;
import espada.spacex.aurora.modules.movementplus.MoveUp;
import espada.spacex.aurora.modules.movementplus.PacketFly;
import espada.spacex.aurora.modules.movementplus.PacketFlyPlus;
import espada.spacex.aurora.modules.movementplus.SpeedPlus;
import espada.spacex.aurora.modules.movementplus.SprintPlus;
import espada.spacex.aurora.modules.movementplus.Step;
import espada.spacex.aurora.modules.movementplus.StepPlus;
import espada.spacex.aurora.modules.movementplus.Strafe;
import espada.spacex.aurora.modules.movementplus.StrictNoSlow;
import espada.spacex.aurora.modules.movementplus.TickShift;
import espada.spacex.aurora.modules.movementplus.timer.TimerPlus;
import espada.spacex.aurora.modules.playerplus.AntiAim;
import espada.spacex.aurora.modules.playerplus.AutoCraftingTable;
import espada.spacex.aurora.modules.playerplus.AutoItem;
import espada.spacex.aurora.modules.playerplus.AutoMend;
import espada.spacex.aurora.modules.playerplus.AutoPearl;
import espada.spacex.aurora.modules.playerplus.AutoPot;
import espada.spacex.aurora.modules.playerplus.BreakCrystal;
import espada.spacex.aurora.modules.playerplus.BurrowPlus;
import espada.spacex.aurora.modules.playerplus.BurrowPlus2;
import espada.spacex.aurora.modules.playerplus.Daroo;
import espada.spacex.aurora.modules.playerplus.FemboyItem;
import espada.spacex.aurora.modules.playerplus.HnadSync;
import espada.spacex.aurora.modules.playerplus.MCP;
import espada.spacex.aurora.modules.playerplus.MultiTasks;
import espada.spacex.aurora.modules.playerplus.NewFakePlayer;
import espada.spacex.aurora.modules.playerplus.OffHandPlus;
import espada.spacex.aurora.modules.playerplus.PacketEat;
import espada.spacex.aurora.modules.playerplus.PortalGodMode;
import espada.spacex.aurora.modules.playerplus.ScaffoldPlus;
import espada.spacex.aurora.modules.playerplus.SkinBlinker;
import espada.spacex.aurora.modules.playerplus.Suicide;
import espada.spacex.aurora.modules.renderplus.AntiCrawl;
import espada.spacex.aurora.modules.renderplus.AspectRatio;
import espada.spacex.aurora.modules.renderplus.AttackIndicator;
import espada.spacex.aurora.modules.renderplus.BlockSelectionPlus;
import espada.spacex.aurora.modules.renderplus.CrystalESP;
import espada.spacex.aurora.modules.renderplus.CustomFOV;
import espada.spacex.aurora.modules.renderplus.CustomWeather;
import espada.spacex.aurora.modules.renderplus.FeetESP;
import espada.spacex.aurora.modules.renderplus.Fog;
import espada.spacex.aurora.modules.renderplus.ForceSneak;
import espada.spacex.aurora.modules.renderplus.KillEffects;
import espada.spacex.aurora.modules.renderplus.LightsOut;
import espada.spacex.aurora.modules.renderplus.MineESP;
import espada.spacex.aurora.modules.renderplus.NewNameTags;
import espada.spacex.aurora.modules.renderplus.PlaceRender;
import espada.spacex.aurora.modules.renderplus.SwingModifier;
import espada.spacex.aurora.modules.renderplus.HoleEsp.HoleEsp;
import espada.spacex.aurora.utils.RenderUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class Aurora extends MeteorAddon {
   public static final Logger LOG = LogUtils.getLogger();
   public static final Category CombatPlus;
   public static final Category Settings;
   public static final Category RenderPlus;
   public static final Category MovementPlus;
   public static final Category MiscPlus;
   public static final Category PlayerPlus;
   public static final HudGroup HUD_EDIT;
   public static final String NAME = "Aurora";
   public static final String VERSION = "X";
   public static final String COLOR = "Color is the visual perception of different wavelengths of light as hue, saturation, and brightness";

   public void onInitialize() {
      LOG.info("Initializing Aurora");
      RenderUtils.initShaders();
      this.initializeModules(meteordevelopment.meteorclient.systems.modules.Modules.get());
      this.initializeSettings(meteordevelopment.meteorclient.systems.modules.Modules.get());
      this.initializeCommands();
      this.initializeHud(Hud.get());
      Managers.PLAYER.init();
   }

   private void initializeModules(meteordevelopment.meteorclient.systems.modules.Modules modules) {
      modules.add(new MoveFix());
      modules.add(new MaoJunQingAura());
      modules.add(new AntiAim());
      modules.add(new AntiCrawl());
      modules.add(new AutoCraftingTable());
      modules.add(new AutoEz());
      modules.add(new Automation());
      modules.add(new AutoMend());
      modules.add(new AutoPot());
      // Autofix disabled: it hard-depends on Baritone types (Goal/GoalNear/BaritoneAPI)
      // at class-load time, but Baritone is only modCompileOnly — never shipped at runtime,
      // so new Autofix() throws NoClassDefFoundError during initializeModules.
      // Re-enable once a full Baritone runtime is present in run/mods.
      // modules.add(new Autofix());
      modules.add(new AuroraMine());
      modules.add(new AutoItem());
      modules.add(new AutoMoan());
      modules.add(new AutoPearl());
      modules.add(new AutoTrapPlus());
      modules.add(new BedAuraPlus());
      modules.add(new Blocker());
      modules.add(new BurrowPlus());
      modules.add(new CustomFOV());
      modules.add(new CapesModule());
      modules.add(new Daroo());
      modules.add(new ElytraFlyPlus());
      modules.add(new FlightPlus());
      modules.add(new FemboyPush());
      modules.add(new FemboyItem());
      modules.add(new HoleFillPlus());
      modules.add(new HoleFillRewrite());
      modules.add(new HoleSnap());
      modules.add(new JesusPlus());
      modules.add(new Aura());
      modules.add(new LightsOut());
      modules.add(new OffHandPlus());
      modules.add(new PacketFly());
      modules.add(new PacketFlyPlus());
      modules.add(new PistonCrystal());
      modules.add(new PistonPush());
      modules.add(new PortalGodMode());
      modules.add(new RPC());
      modules.add(new ScaffoldPlus());
      modules.add(new SelfTrapPlus());
      modules.add(new SoundModifier());
      modules.add(new SpeedPlus());
      modules.add(new SprintPlus());
      modules.add(new StepPlus());
      modules.add(new StrictNoSlow());
      modules.add(new Suicide());
      modules.add(new SurroundPlus());
      modules.add(new SwingModifier());
      modules.add(new TickShift());
      modules.add(new WeakAlert());
      modules.add(new BurrowMove());
      modules.add(new PacketEat());
      modules.add(new SkinBlinker());
      modules.add(new BurrowPlus2());
      modules.add(new AntiPiston());
      modules.add(new FaceWebHelper());
      modules.add(new KeyCity());
      modules.add(new HnadSync());
      modules.add(new NewNameTags());
      modules.add(new AntiWeak());
      modules.add(new BlockSelectionPlus());
      modules.add(new TimerPlus());
      modules.add(new FastWeb());
      modules.add(new NewFakePlayer());
      modules.add(new Step());
      modules.add(new MultiTasks());
      modules.add(new Suffix());
      modules.add(new AttackIndicator());
      modules.add(new AutoLoadKit());
      modules.add(new BreakCrystal());
      modules.add(new CevBreaker());
      modules.add(new AutoAnvil());
      modules.add(new MoveUp());
      modules.add(new AutoCrystal());
      modules.add(new AutoWeb());
      modules.add(new Strafe());
      modules.add(new BRotateBypass());
      modules.add(new MCP());
      modules.add(new HoleEsp());
      modules.add(new FeetESP());
      modules.add(new Fog());
      modules.add(new ForceSneak());
      modules.add(new MineESP());
      modules.add(new CustomWeather());
      modules.add(new KillEffects());
   }

   private void initializeSettings(meteordevelopment.meteorclient.systems.modules.Modules modules) {
      modules.add(new AspectRatio());
      modules.add(new FacingSettings());
      modules.add(new RangeSettings());
      modules.add(new RaytraceSettings());
      modules.add(new RotationSettings());
      modules.add(new ServerSettings());
      modules.add(new SwingSettings());
      modules.add(new ColorSetting());
      modules.add(new CrystalESP());
      modules.add(new PlaceRender());
      modules.add(new RotationPrioritySettings());
      modules.add(new RSRClientPlusTitle());
   }

   private void initializeCommands() {
      Commands.add(new BlackoutGit());
      Commands.add(new Coords());
   }

   private void initializeHud(Hud hud) {
      hud.register(ArmorHudPlus.INFO);
      hud.register(AuroraArray.INFO);
      hud.register(Rsr.INFO);
      hud.register(GearHud.INFO);
      hud.register(HudWaterMark.INFO);
      hud.register(Keys.INFO);
      hud.register(TargetHud.INFO);
      hud.register(Welcomer.INFO);
      hud.register(OnTope.INFO);
      hud.register(CatGirl.INFO);
      hud.register(TickShiftHud.INFO);
      hud.register(MineHud.INFO);
      hud.register(PacketHud.INFO);
      hud.register(TimerPlusHud.INFO);
   }

   public void onRegisterCategories() {
      meteordevelopment.meteorclient.systems.modules.Modules.registerCategory(CombatPlus);
      meteordevelopment.meteorclient.systems.modules.Modules.registerCategory(MiscPlus);
      meteordevelopment.meteorclient.systems.modules.Modules.registerCategory(RenderPlus);
      meteordevelopment.meteorclient.systems.modules.Modules.registerCategory(MovementPlus);
      meteordevelopment.meteorclient.systems.modules.Modules.registerCategory(PlayerPlus);
      meteordevelopment.meteorclient.systems.modules.Modules.registerCategory(Settings);
   }

   public String getPackage() {
      return "espada.spacex.aurora";
   }

   static {
      CombatPlus = new Category("Combat+", Items.END_CRYSTAL.getDefaultStack());
      Settings = new Category("Settings", Items.OBSIDIAN.getDefaultStack());
      RenderPlus = new Category("Render+", Items.GLASS.getDefaultStack());
      MovementPlus = new Category("Movement+", Items.SADDLE.getDefaultStack());
      MiscPlus = new Category("Misc+", Items.DIRT.getDefaultStack());
      PlayerPlus = new Category("Player+", Items.PLAYER_HEAD.getDefaultStack());
      HUD_EDIT = new HudGroup("Aurora");
   }
}
