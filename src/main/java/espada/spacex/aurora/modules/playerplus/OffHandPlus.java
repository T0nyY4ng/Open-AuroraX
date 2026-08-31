package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.combatplus.autocrystal.AutoCrystal;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import java.util.Objects;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class OffHandPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgHealth;
   private final Setting<Boolean> onlyInInv;
   private final Setting<ItemMode> itemMode;
   private final Setting<GapMode> gapMode;
   private final Setting<SwordMode> swordMode;
   private final Setting<Boolean> safeSword;
   private final Setting<Double> delay;
   private final Setting<Boolean> pickswitch;
   private final Setting<Integer> hp;
   private final Setting<Boolean> safety;
   private final Setting<Integer> safetyHealth;
   private double timer;
   private Item item;
   private Suicide suicide;
   private AutoCrystal autoCrystalRewrite;
   private CrystalAura crystalAura;
   private AuroraMine autoMine;
   private long lastTime;

   public OffHandPlus() {
      super(Aurora.PlayerPlus, "SmartOffhand", "Better offhand.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgHealth = this.settings.createGroup("Health");
      this.onlyInInv = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only in inventory")).description("Will only switch if you are in your inventory.")).defaultValue(false)).build());
      this.itemMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Item Mode")).description("Which item should be held in offhand.")).defaultValue(OffHandPlus.ItemMode.Totem)).build());
      this.gapMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Gapple Mode")).description("When should we hold golden apples.")).defaultValue(OffHandPlus.GapMode.Combat)).build());
      SettingGroup var10001 = this.sgGeneral;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Sword Mode")).description("When should we hold sword.")).defaultValue(OffHandPlus.SwordMode.Pressed);
      GapMode var10003 = (GapMode)this.gapMode.get();
      Objects.requireNonNull(var10003);
      this.swordMode = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::isSword)).build());
      var10001 = this.sgGeneral;
      BoolSetting.Builder var3 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Safe Sword")).description("Only sword gaps if you have enough health.")).defaultValue(false);
      var10003 = (GapMode)this.gapMode.get();
      Objects.requireNonNull(var10003);
      this.safeSword = var10001.add(((BoolSetting.Builder)var3.visible(var10003::isSword)).build());
      this.delay = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Delay")).description("Delay between switches.")).defaultValue(0.1).range((double)0.0F, (double)1.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.pickswitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("pickswitch")).description("Uses pick silent and swap with offhand packets to bypass ncp inventory checks.")).defaultValue(false)).build());
      this.hp = this.sgHealth.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Health")).description("Switches to totem when health is under this value.")).defaultValue(14)).range(0, 36).sliderMax(36).build());
      this.safety = this.sgHealth.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Safety")).description("Tries to prevent offhand fails by switching while in danger.")).defaultValue(true)).build());
      var10001 = this.sgHealth;
      IntSetting.Builder var4 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Safety Health")).description("Holds totem if you would have under this health after possible damages.")).defaultValue(0)).range(0, 36).sliderMax(36);
      Setting<Boolean> var6 = this.safety;
      Objects.requireNonNull(var6);
      this.safetyHealth = var10001.add(((IntSetting.Builder)var4.visible(var6::get)).build());
      this.timer = (double)0.0F;
      this.item = null;
      this.suicide = null;
      this.autoCrystalRewrite = null;
      this.crystalAura = null;
      this.autoMine = null;
      this.lastTime = 0L;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         this.timer -= (double)(System.currentTimeMillis() - this.lastTime) / (double)1000.0F;
         this.lastTime = System.currentTimeMillis();
         if (this.suicide == null) {
            this.suicide = (Suicide)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Suicide.class);
         }

         if (this.autoCrystalRewrite == null) {
            this.autoCrystalRewrite = (AutoCrystal)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AutoCrystal.class);
         }

         if (this.crystalAura == null) {
            this.crystalAura = (CrystalAura)meteordevelopment.meteorclient.systems.modules.Modules.get().get(CrystalAura.class);
         }

         if (this.autoMine == null) {
            this.autoMine = (AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);
         }

         this.item = this.getItem();
         if (this.item != null) {
            this.update();
         }

      }
   }

   private void update() {
      if (!(this.timer > (double)0.0F)) {
         if (!this.getPredicate(this.item).test(this.mc.player.getOffHandStack().getItem())) {
            if (!(Boolean)this.onlyInInv.get() || this.mc.currentScreen instanceof InventoryScreen) {
               int slot = this.getSlot(this.getPredicate(this.item));
               this.move(slot);
               this.timer = (Double)this.delay.get();
            }
         }
      }
   }

   private void move(int slot) {
      if ((Boolean)this.pickswitch.get()) {
         BOInvUtils.pickSwitch(slot);
         this.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN, 0));
         BOInvUtils.pickSwapBack();
         InvUtils.swap(Managers.HOLDING.slot, false);
      } else {
         InvUtils.move().from(slot).toOffhand();
      }
   }

   private Predicate<Item> getPredicate(Item item) {
      if (item == Items.GOLDEN_APPLE) {
         return OLEPOSSUtils::isGapple;
      } else if (item == Items.RED_BED) {
         Objects.requireNonNull(BedItem.class);
         return BedItem.class::isInstance;
      } else {
         Objects.requireNonNull(item);
         return item::equals;
      }
   }

   private Item getItem() {
      if (this.mc.player.getMainHandStack().getItem() instanceof PickaxeItem && (!(Boolean)this.safeSword.get() || !this.inDanger()) && ((GapMode)this.gapMode.get()).sword) {
         switch (((SwordMode)this.swordMode.get()).ordinal()) {
            case 0:
               if (this.mc.options.useKey.isPressed()) {
                  return Items.GOLDEN_APPLE;
               }
               break;
            case 1:
               return Items.GOLDEN_APPLE;
         }
      }

      if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem && (!(Boolean)this.safeSword.get() || !this.inDanger()) && ((GapMode)this.gapMode.get()).sword) {
         switch (((SwordMode)this.swordMode.get()).ordinal()) {
            case 0:
               if (this.mc.options.useKey.isPressed()) {
                  return Items.GOLDEN_APPLE;
               }
               break;
            case 1:
               return Items.GOLDEN_APPLE;
         }
      }

      if (this.inDanger() && !this.suicide.isActive() && this.itemAvailable((itemStack) -> itemStack.getItem() == Items.TOTEM_OF_UNDYING)) {
         return Items.TOTEM_OF_UNDYING;
      } else {
         switch (((ItemMode)this.itemMode.get()).ordinal()) {
            case 0:
               if (!this.suicide.isActive() && this.itemAvailable((itemStack) -> itemStack.getItem() == Items.TOTEM_OF_UNDYING)) {
                  return Items.TOTEM_OF_UNDYING;
               }
               break;
            case 1:
               if (this.itemAvailable((itemStack) -> itemStack.getItem() == Items.END_CRYSTAL)) {
                  return Items.END_CRYSTAL;
               }
               break;
            case 2:
               if (this.itemAvailable(OLEPOSSUtils::isGapple)) {
                  return Items.GOLDEN_APPLE;
               }
               break;
            case 3:
               if (this.itemAvailable((itemStack) -> itemStack.getItem() instanceof BedItem)) {
                  return Items.RED_BED;
               }
         }

         return this.itemAvailable(OLEPOSSUtils::isGapple) && this.gapMode.get() == OffHandPlus.GapMode.LastOption ? Items.GOLDEN_APPLE : null;
      }
   }

   private boolean inDanger() {
      double health = (double)(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount());
      return health <= (double)(Integer)this.hp.get() || (Boolean)this.safety.get() && health - (double)PlayerUtils.possibleHealthReductions() <= (double)(Integer)this.safetyHealth.get();
   }

   private int getSlot(Predicate<Item> predicate) {
      double amount = (double)-1.0F;
      int slot = -1;

      for(int i = 9; i < this.mc.player.getInventory().size() + 1; ++i) {
         ItemStack s = this.mc.player.getInventory().getStack(i);
         if (predicate.test(s.getItem()) && (double)s.getCount() > amount) {
            slot = i;
            amount = (double)s.getCount();
         }
      }

      if (slot >= 0) {
         return slot;
      } else {
         for(int i = 0; i < 9; ++i) {
            ItemStack s = this.mc.player.getInventory().getStack(i);
            if (predicate.test(s.getItem()) && (double)s.getCount() > amount) {
               slot = i;
               amount = (double)s.getCount();
            }
         }

         return slot;
      }
   }

   private boolean itemAvailable(Predicate<ItemStack> predicate) {
      return InvUtils.find(predicate).found();
   }

   public static enum ItemMode {
      Totem,
      Crystal,
      Gapple,
      Bed;

      // $FF: synthetic method
      private static ItemMode[] $values() {
         return new ItemMode[]{Totem, Crystal, Gapple, Bed};
      }
   }

   public static enum GapMode {
      LastOption(false),
      Combat(true),
      Never(false);

      public final boolean sword;

      private GapMode(boolean sword) {
         this.sword = sword;
      }

      public boolean isSword() {
         return this.sword;
      }

      // $FF: synthetic method
      private static GapMode[] $values() {
         return new GapMode[]{LastOption, Combat, Never};
      }
   }

   public static enum SwordMode {
      Pressed,
      Always;

      // $FF: synthetic method
      private static SwordMode[] $values() {
         return new SwordMode[]{Pressed, Always};
      }
   }
}
