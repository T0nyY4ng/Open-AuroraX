package espada.spacex.aurora.modules.renderplus.HoleEsp;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class HoleEsp extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgRender;
   private final Setting<Double> horizontalDistance;
   private final Setting<Double> verticalDistance;
   private final Setting<UpdateHoles> updateHoles;
   private final Setting<Boolean> allowHalf;
   private final Setting<Boolean> webs;
   private final Setting<Boolean> ignoreOwn;
   private final Setting<Boolean> ignoreBurrow;
   private final Setting<Boolean> render;
   private final Setting<Boolean> fadeIn;
   private final Setting<Boolean> fadeOut;
   private final Setting<Integer> renderTicks;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Double> renderHeight;
   private final Setting<Double> shrinkSpeed;
   private final Setting<Boolean> topQuad;
   private final Setting<Boolean> bottomQuad;
   private final Setting<SettingColor> bedrockSidesTop;
   private final Setting<SettingColor> bedrockSidesBottom;
   private final Setting<SettingColor> bedrockLinesTop;
   private final Setting<SettingColor> bedrockLinesBottom;
   private final Setting<SettingColor> obsidianSidesTop;
   private final Setting<SettingColor> obsidianSidesBottom;
   private final Setting<SettingColor> obsidianLinesTop;
   private final Setting<SettingColor> obsidianLinesBottom;
   private final Setting<SettingColor> mixedSidesTop;
   private final Setting<SettingColor> mixedSidesBottom;
   private final Setting<SettingColor> mixedLinesTop;
   private final Setting<SettingColor> mixedLinesBottom;
   private final Pool<RenderBlock> renderBlockPool;
   private final List<RenderBlock> renderBlocks;

   public HoleEsp() {
      super(Aurora.RenderPlus, "HoleEsp", "Displays holes that you will take less damage in.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgRender = this.settings.createGroup("Render");
      this.horizontalDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("horizontal-distance")).description("The horizontal radius around you in which holes are rendered.")).defaultValue((double)6.0F).sliderMin((double)0.0F).sliderMax((double)12.0F).min((double)0.0F).build());
      this.verticalDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("vertical-distance")).description("The vertical radius around you in which holes are rendered.")).defaultValue((double)7.0F).sliderMin((double)0.0F).sliderMax((double)12.0F).min((double)0.0F).build());
      this.updateHoles = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("update-holes")).description("When to update the holes to check if they are still valid.")).defaultValue(HoleEsp.UpdateHoles.Render)).build());
      this.allowHalf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("allow-half")).description("Renders holes which are hard to get inside.")).defaultValue(false)).build());
      this.webs = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("webs")).description("Whether to show holes that have webs inside of them.")).defaultValue(false)).build());
      this.ignoreOwn = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("ignore-own")).description("Ignores the hole you are sitting in.")).defaultValue(false)).build());
      this.ignoreBurrow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("ignore-burrow")).description("Ignores your burrow block.")).defaultValue(true)).build());
      this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("render")).description("Renders the blocks being placed.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("fade-in")).description("Fades in the hole when rendering a new one.")).defaultValue(true);
      Setting<Boolean> var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.fadeIn = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgRender;
      var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("fade-out")).description("Fades out the hole when removing it.")).defaultValue(true);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.fadeOut = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgRender;
      IntSetting.Builder var9 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("ticks")).description("How many ticks it should take for a block to disappear.")).defaultValue(10)).min(1).sliderMin(1).sliderMax(15);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.renderTicks = var10001.add(((IntSetting.Builder)var9.visible(var10003::get)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var10 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.shapeMode = var10001.add(((EnumSetting.Builder)var10.visible(var10003::get)).build());
      var10001 = this.sgRender;
      DoubleSetting.Builder var11 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("height")).description("The height of rendering.")).defaultValue((double)0.0F).min((double)0.0F).sliderMin((double)0.0F).sliderMax((double)1.0F);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.renderHeight = var10001.add(((DoubleSetting.Builder)var11.visible(var10003::get)).build());
      var10001 = this.sgRender;
      var11 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("shrink-speed")).description("How fast the hole shrinks per tick.")).defaultValue(0.1).min((double)0.0F).sliderMin((double)0.0F).sliderMax((double)0.25F);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.shrinkSpeed = var10001.add(((DoubleSetting.Builder)var11.visible(var10003::get)).build());
      var10001 = this.sgRender;
      BoolSetting.Builder var13 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("top-quad")).description("Whether to render a quad at the top of the hole.")).defaultValue(false);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.topQuad = var10001.add(((BoolSetting.Builder)var13.visible(var10003::get)).build());
      var10001 = this.sgRender;
      var13 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("bottom-quad")).description("Whether to render a quad at the bottom of the hole.")).defaultValue(true);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.bottomQuad = var10001.add(((BoolSetting.Builder)var13.visible(var10003::get)).build());
      this.bedrockSidesTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("bedrock-sides-top")).description("The top side color for holes that are completely bedrock.")).defaultValue(new SettingColor(100, 255, 0, 0)).onChanged((changed) -> this.updateAll())).build());
      this.bedrockSidesBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("bedrock-sides-bottom")).description("The bottom side color for holes that are completely bedrock.")).defaultValue(new SettingColor(100, 255, 0, 25)).onChanged((changed) -> this.updateAll())).build());
      this.bedrockLinesTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("bedrock-lines-top")).description("The top line color for holes that are completely bedrock.")).defaultValue(new SettingColor(100, 255, 0, 0)).onChanged((changed) -> this.updateAll())).build());
      this.bedrockLinesBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("bedrock-lines-bottom")).description("The bottom line color for holes that are completely bedrock.")).defaultValue(new SettingColor(100, 255, 0, 200)).onChanged((changed) -> this.updateAll())).build());
      this.obsidianSidesTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("obsidian-sides-top")).description("The top side color for holes that are completely obsidian.")).defaultValue(new SettingColor(255, 0, 0, 0)).onChanged((changed) -> this.updateAll())).build());
      this.obsidianSidesBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("obsidian-sides-bottom")).description("The bottom side color for holes that are completely obsidian.")).defaultValue(new SettingColor(255, 0, 0, 25)).onChanged((changed) -> this.updateAll())).build());
      this.obsidianLinesTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("obsidian-lines-top")).description("The top line color for holes that are completely obsidian.")).defaultValue(new SettingColor(255, 0, 0, 0)).onChanged((changed) -> this.updateAll())).build());
      this.obsidianLinesBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("obsidian-lines-bottom")).description("The bottom line color for holes that are completely obsidian.")).defaultValue(new SettingColor(255, 0, 0, 200)).onChanged((changed) -> this.updateAll())).build());
      this.mixedSidesTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("mixed-sides-top")).description("The top side color for holes that have mixed bedrock and obsidian.")).defaultValue(new SettingColor(255, 127, 0, 0)).onChanged((changed) -> this.updateAll())).build());
      this.mixedSidesBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("mixed-sides-bottom")).description("The bottom side color for holes that have mixed bedrock and obsidian.")).defaultValue(new SettingColor(255, 127, 0, 25)).onChanged((changed) -> this.updateAll())).build());
      this.mixedLinesTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("mixed-lines-top")).description("The top line color for holes that have mixed bedrock and obsidian.")).defaultValue(new SettingColor(255, 127, 0, 0)).onChanged((changed) -> this.updateAll())).build());
      this.mixedLinesBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("mixed-lines-bottom")).description("The bottom line color for holes that have mixed bedrock and obsidian.")).defaultValue(new SettingColor(255, 127, 0, 200)).onChanged((changed) -> this.updateAll())).build());
      this.renderBlockPool = new Pool(() -> new RenderBlock());
      this.renderBlocks = new ArrayList();
   }

   public void onActivate() {
      if (!this.renderBlocks.isEmpty()) {
         for(RenderBlock block : this.renderBlocks) {
            this.renderBlockPool.free(block);
         }

         this.renderBlocks.clear();
      }

   }

   public void onDeactivate() {
      if (!this.renderBlocks.isEmpty()) {
         for(RenderBlock block : this.renderBlocks) {
            this.renderBlockPool.free(block);
         }

         this.renderBlocks.clear();
      }

   }

   @EventHandler
   private void onPreTick(TickEvent.Pre event) {
      List<Hole> holes = new ArrayList();
      int pX = this.mc.player.getBlockX();
      int pY = this.mc.player.getBlockY();
      int pZ = this.mc.player.getBlockZ();
      int horizontal = (int)Math.floor((Double)this.horizontalDistance.get());
      int vertical = (int)Math.floor((Double)this.verticalDistance.get());

      for(int x = pX - horizontal; x <= pX + horizontal; ++x) {
         for(int z = pZ - horizontal; z <= pZ + horizontal; ++z) {
            for(int y = Math.max(pY - vertical, this.mc.world.getBottomY() - 1); y <= Math.min(pY + vertical, this.mc.world.getTopY()); ++y) {
               int dX = Math.abs(x - pX);
               int dY = Math.abs(y - pY);
               int dZ = Math.abs(z - pZ);
               if ((double)dX <= (Double)this.horizontalDistance.get() && (double)dY <= (Double)this.verticalDistance.get() && (double)dZ <= (Double)this.horizontalDistance.get()) {
                  BlockPos pos = new BlockPos(x, y, z);
                  if (this.isValidHole(pos, true) && this.isValidHole(pos.up(), false)) {
                     int air = 0;
                     int surr = 0;
                     int bedrock = 0;
                     int obsidian = 0;
                     BlockPos second = null;
                     Direction excludeDir = null;
                     if (this.mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                        ++bedrock;
                     } else {
                        ++obsidian;
                     }

                     for(CardinalDirection cardinal : CardinalDirection.values()) {
                        Direction direction = cardinal.toDirection();
                        if (this.isValidHole(pos.offset(direction), true) && this.isValidHole(pos.offset(direction).up(), false)) {
                           int surrounded = 0;
                           if (this.mc.world.getBlockState(pos.offset(direction).down()).getBlock() == Blocks.BEDROCK) {
                              ++bedrock;
                           } else {
                              ++obsidian;
                           }

                           for(CardinalDirection dir : CardinalDirection.values()) {
                              if (this.mc.world.getBlockState(pos.offset(direction).offset(dir.toDirection())).getBlock().getBlastResistance() >= 600.0F) {
                                 ++surrounded;
                                 if (this.mc.world.getBlockState(pos.offset(direction).offset(dir.toDirection())).getBlock() == Blocks.BEDROCK) {
                                    ++bedrock;
                                 } else {
                                    ++obsidian;
                                 }
                              }
                           }

                           if (surrounded == 3) {
                              excludeDir = direction;
                              second = pos.offset(direction);
                              ++air;
                           } else {
                              air = 0;
                           }
                        } else if (this.mc.world.getBlockState(pos.offset(direction)).getBlock().getBlastResistance() >= 600.0F) {
                           ++surr;
                           if (this.mc.world.getBlockState(pos.offset(direction)).getBlock() == Blocks.BEDROCK) {
                              ++bedrock;
                           } else {
                              ++obsidian;
                           }
                        }
                     }

                     if (air == 1 && surr >= 3 && (!(Boolean)this.allowHalf.get() || this.isValidHole(pos.up(2), false) || second != null && this.isValidHole(second.up(2), false)) || air == 0 && surr >= 4 && this.isValidHole(pos.up(2), false)) {
                        HoleType type = bedrock == 0 ? HoleEsp.HoleType.Obsidian : (bedrock > 0 && obsidian > 0 ? HoleEsp.HoleType.Mixed : HoleEsp.HoleType.Bedrock);
                        holes.add(new Hole(type, pos, excludeDir != null ? second : null, second != null ? excludeDir : null));
                     }
                  }
               }
            }
         }
      }

      if (!holes.isEmpty()) {
         holes.sort(Comparator.comparingDouble((hole) -> this.distance(this.mc.player.getPos(), Vec3d.ofCenter(hole.pos1))));

         for(Hole hole : holes) {
            if (hole.isDouble()) {
               if ((Boolean)this.ignoreOwn.get() && (this.mc.player.getBlockPos().equals(hole.pos1) || this.mc.player.getBlockPos().equals(hole.pos2)) || (Boolean)this.ignoreBurrow.get() && (this.mc.player.getBlockPos().up().equals(hole.pos1) || this.mc.player.getBlockPos().up().equals(hole.pos2))) {
                  for(RenderBlock block : this.renderBlocks) {
                     if (hole.pos1.equals(block.pos) || hole.pos2.equals(block.pos)) {
                        block.invalidate();
                     }
                  }
               } else {
                  if (this.canRender(hole.pos1) && this.distanceXZ(this.mc.player.getPos(), Vec3d.ofCenter(hole.pos1)) <= (Double)this.horizontalDistance.get() && this.distanceY(this.mc.player.getY(), (double)hole.pos1.getY() + (double)0.5F) <= (Double)this.verticalDistance.get()) {
                     this.renderBlocks.add(((RenderBlock)this.renderBlockPool.get()).set(hole.type, hole.pos1, Dir.get(hole.direction)));
                  } else {
                     for(RenderBlock block : this.renderBlocks) {
                        if (block.pos.equals(hole.pos1)) {
                           block.update(hole.type, Dir.get(hole.direction));
                        }
                     }
                  }

                  if (this.canRender(hole.pos2) && this.distanceXZ(this.mc.player.getPos(), Vec3d.ofCenter(hole.pos2)) <= (Double)this.horizontalDistance.get() && this.distanceY(this.mc.player.getY(), (double)hole.pos2.getY() + (double)0.5F) <= (Double)this.verticalDistance.get()) {
                     this.renderBlocks.add(((RenderBlock)this.renderBlockPool.get()).set(hole.type, hole.pos2, Dir.get(hole.direction.getOpposite())));
                  } else {
                     for(RenderBlock block : this.renderBlocks) {
                        if (block.pos.equals(hole.pos2)) {
                           block.update(hole.type, Dir.get(hole.direction.getOpposite()));
                        }
                     }
                  }
               }
            } else if ((!(Boolean)this.ignoreOwn.get() || !this.mc.player.getBlockPos().equals(hole.pos1)) && (!(Boolean)this.ignoreBurrow.get() || !this.mc.player.getBlockPos().up().equals(hole.pos1))) {
               if (this.canRender(hole.pos1) && this.distanceXZ(this.mc.player.getPos(), Vec3d.ofCenter(hole.pos1)) <= (Double)this.horizontalDistance.get() && this.distanceY(this.mc.player.getY(), (double)hole.pos1.getY() + (double)0.5F) <= (Double)this.verticalDistance.get()) {
                  this.renderBlocks.add(((RenderBlock)this.renderBlockPool.get()).set(hole.type, hole.pos1, 0));
               } else {
                  for(RenderBlock block : this.renderBlocks) {
                     if (block.pos.equals(hole.pos1)) {
                        block.update(hole.type, 0);
                     }
                  }
               }
            } else {
               for(RenderBlock block : this.renderBlocks) {
                  if (hole.pos1.equals(block.pos)) {
                     block.invalidate();
                  }
               }
            }
         }
      }

   }

   private double distance(Vec3d vec1, Vec3d vec2) {
      double dX = vec2.x - vec1.x;
      double dY = vec2.y - vec1.y;
      double dZ = vec2.z - vec1.z;
      return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
   }

   private double distanceXZ(Vec3d pos1, Vec3d pos2) {
      double dX = pos1.getX() - pos2.getX();
      double dZ = pos1.getZ() - pos2.getZ();
      return (double)MathHelper.sqrt((float)(dX * dX + dZ * dZ));
   }

   private double distanceY(double y1, double y2) {
      return Math.abs(y1 - y2);
   }

   @EventHandler
   private void onPostTick(TickEvent.Post event) {
      this.renderBlocks.forEach(RenderBlock::tick);
      this.renderBlocks.removeIf((block) -> block.ticks <= 0);
      if (this.updateHoles.get() == HoleEsp.UpdateHoles.Tick) {
         this.renderBlocks.removeIf(RenderBlock::isInvalid);
      }

   }

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      if (!this.renderBlocks.isEmpty()) {
         if (this.updateHoles.get() == HoleEsp.UpdateHoles.Render) {
            this.renderBlocks.removeIf(RenderBlock::isInvalid);
         }

         this.renderBlocks.sort(Comparator.comparingInt((block) -> -block.ticks));
         this.renderBlocks.forEach((block) -> block.render(event, (ShapeMode)this.shapeMode.get()));
      }

   }

   private boolean isValidHole(BlockPos pos, boolean checkDown) {
      return this.mc.world.getBlockState(pos).isReplaceable() && (this.mc.world.getBlockState(pos).getBlock() != Blocks.COBWEB || (Boolean)this.webs.get()) && (!checkDown || this.mc.world.getBlockState(pos.down()).getBlock().getBlastResistance() >= 600.0F && this.mc.world.getBlockState(pos.down()).getCollisionShape(this.mc.world, pos.down()) != null && !this.mc.world.getBlockState(pos.down()).getCollisionShape(this.mc.world, pos.down()).isEmpty()) && (this.mc.world.getBlockState(pos).getCollisionShape(this.mc.world, pos) == null || this.mc.world.getBlockState(pos).getCollisionShape(this.mc.world, pos).isEmpty());
   }

   private void updateAll() {
      for(RenderBlock block : this.renderBlocks) {
         block.update(block.type, block.exclude);
      }

   }

   private boolean canRender(BlockPos pos) {
      for(RenderBlock block : this.renderBlocks) {
         if (block.pos.equals(pos)) {
            return false;
         }
      }

      return true;
   }

   private boolean isHole(BlockPos pos) {
      if (this.isValidHole(pos, true) && this.isValidHole(pos.up(), false)) {
         int air = 0;
         int surr = 0;
         BlockPos second = null;

         for(CardinalDirection cardinal : CardinalDirection.values()) {
            Direction direction = cardinal.toDirection();
            if (this.isValidHole(pos.offset(direction), true) && this.isValidHole(pos.offset(direction).up(), false)) {
               int surrounded = 0;

               for(CardinalDirection dir : CardinalDirection.values()) {
                  if (this.mc.world.getBlockState(pos.offset(direction).offset(dir.toDirection())).getBlock().getBlastResistance() >= 600.0F) {
                     ++surrounded;
                  }
               }

               if (surrounded == 3) {
                  second = pos.offset(direction);
                  ++air;
               } else {
                  air = 0;
               }
            } else if (this.mc.world.getBlockState(pos.offset(direction)).getBlock().getBlastResistance() >= 600.0F) {
               ++surr;
            }
         }

         return air == 1 && surr >= 3 && (!(Boolean)this.allowHalf.get() || this.isValidHole(pos.up(2), false) || second != null && this.isValidHole(second.up(2), false)) || air == 0 && surr >= 4 && this.isValidHole(pos.up(2), false);
      } else {
         return false;
      }
   }

   private static enum HoleType {
      Bedrock,
      Mixed,
      Obsidian;

      // $FF: synthetic method
      private static HoleType[] $values() {
         return new HoleType[]{Bedrock, Mixed, Obsidian};
      }
   }

   public static enum UpdateHoles {
      Ignore,
      Render,
      Tick;

      // $FF: synthetic method
      private static UpdateHoles[] $values() {
         return new UpdateHoles[]{Ignore, Render, Tick};
      }
   }

   private static class Hole {
      public final HoleType type;
      public final BlockPos pos1;
      public final BlockPos pos2;
      public final Direction direction;

      public Hole(HoleType type, BlockPos pos1, BlockPos pos2, Direction direction) {
         this.type = type;
         this.pos1 = pos1;
         this.pos2 = pos2;
         this.direction = direction;
      }

      public boolean isDouble() {
         return this.pos1 != null && this.pos2 != null && this.direction != null;
      }
   }

   public class RenderBlock {
      public final BlockPos.Mutable pos = new BlockPos.Mutable();
      public HoleType type;
      public int exclude;
      public int ticks;
      private double height;
      private boolean valid;
      private Color constSidesTop;
      private Color constSidesBottom;
      private Color constLinesTop;
      private Color constLinesBottom;
      private Color sidesTop;
      private Color sidesBottom;
      private Color linesTop;
      private Color linesBottom;

      public RenderBlock set(HoleType type, BlockPos pos, int exclude) {
         this.pos.set(pos);
         this.ticks = (Boolean)HoleEsp.this.fadeIn.get() ? 1 : (Integer)HoleEsp.this.renderTicks.get();
         this.valid = true;
         this.update(type, exclude);
         this.height = (Double)HoleEsp.this.renderHeight.get();
         return this;
      }

      public void tick() {
         if (!(HoleEsp.this.distanceXZ(HoleEsp.this.mc.player.getPos(), Vec3d.ofCenter(this.pos)) > (Double)HoleEsp.this.horizontalDistance.get()) && !(HoleEsp.this.distanceY(HoleEsp.this.mc.player.getY(), (double)this.pos.getY() + (double)0.5F) > (Double)HoleEsp.this.verticalDistance.get()) && HoleEsp.this.isHole(this.pos.mutableCopy()) && this.valid) {
            if ((Boolean)HoleEsp.this.fadeIn.get() && this.ticks < (Integer)HoleEsp.this.renderTicks.get()) {
               ++this.ticks;
               this.height = this.height + (Double)HoleEsp.this.shrinkSpeed.get() > (Double)HoleEsp.this.renderHeight.get() ? (Double)HoleEsp.this.renderHeight.get() : this.height + (Double)HoleEsp.this.shrinkSpeed.get();
            }
         } else if ((Boolean)HoleEsp.this.fadeOut.get()) {
            --this.ticks;
            this.height = this.height - (Double)HoleEsp.this.shrinkSpeed.get() <= (double)0.0F ? (double)0.0F : this.height - (Double)HoleEsp.this.shrinkSpeed.get();
         } else {
            this.ticks = 0;
            this.height = (double)0.0F;
         }

      }

      public void update(HoleType type, int exclude) {
         this.exclude = exclude;
         this.type = type;
         this.constSidesTop = new Color(type == HoleEsp.HoleType.Bedrock ? (Color)HoleEsp.this.bedrockSidesTop.get() : (type == HoleEsp.HoleType.Obsidian ? (Color)HoleEsp.this.obsidianSidesTop.get() : (Color)HoleEsp.this.mixedSidesTop.get()));
         this.constSidesBottom = new Color(type == HoleEsp.HoleType.Bedrock ? (Color)HoleEsp.this.bedrockSidesBottom.get() : (type == HoleEsp.HoleType.Obsidian ? (Color)HoleEsp.this.obsidianSidesBottom.get() : (Color)HoleEsp.this.mixedSidesBottom.get()));
         this.constLinesTop = new Color(type == HoleEsp.HoleType.Bedrock ? (Color)HoleEsp.this.bedrockLinesTop.get() : (type == HoleEsp.HoleType.Obsidian ? (Color)HoleEsp.this.obsidianLinesTop.get() : (Color)HoleEsp.this.mixedLinesTop.get()));
         this.constLinesBottom = new Color(type == HoleEsp.HoleType.Bedrock ? (Color)HoleEsp.this.bedrockLinesBottom.get() : (type == HoleEsp.HoleType.Obsidian ? (Color)HoleEsp.this.obsidianLinesBottom.get() : (Color)HoleEsp.this.mixedLinesBottom.get()));
         this.sidesTop = this.constSidesTop;
         this.sidesBottom = this.constSidesBottom;
         this.linesTop = this.constLinesTop;
         this.linesBottom = this.constLinesBottom;
      }

      public boolean isInvalid() {
         return !(Boolean)HoleEsp.this.fadeOut.get() && (HoleEsp.this.distanceXZ(HoleEsp.this.mc.player.getPos(), Vec3d.ofCenter(this.pos)) > (Double)HoleEsp.this.horizontalDistance.get() || HoleEsp.this.distanceY(HoleEsp.this.mc.player.getY(), (double)this.pos.getY() + (double)0.5F) > (Double)HoleEsp.this.verticalDistance.get()) || !(Boolean)HoleEsp.this.fadeOut.get() && (!HoleEsp.this.isHole(this.pos.mutableCopy()) || !this.valid);
      }

      public void invalidate() {
         this.valid = false;
      }

      public void render(Render3DEvent event, ShapeMode shapeMode) {
         Color prevSidesTop = this.sidesTop.copy();
         Color prevSidesBottom = this.sidesBottom.copy();
         Color prevLinesTop = this.linesTop.copy();
         Color prevLinesBottom = this.linesBottom.copy();
         Color var10000 = this.sidesTop;
         var10000.a = (int)((double)var10000.a * ((double)this.ticks / (double)8.0F));
         var10000 = this.sidesBottom;
         var10000.a = (int)((double)var10000.a * ((double)this.ticks / (double)8.0F));
         var10000 = this.linesTop;
         var10000.a = (int)((double)var10000.a * ((double)this.ticks / (double)8.0F));
         var10000 = this.linesBottom;
         var10000.a = (int)((double)var10000.a * ((double)this.ticks / (double)8.0F));
         this.sidesTop = this.sidesTop.a > this.constSidesTop.a ? this.constSidesTop : this.sidesTop;
         this.sidesBottom = this.sidesBottom.a > this.constSidesBottom.a ? this.constSidesBottom : this.sidesBottom;
         this.linesTop = this.linesTop.a > this.constLinesTop.a ? this.constLinesTop : this.linesTop;
         this.linesBottom = this.linesBottom.a > this.constLinesBottom.a ? this.constLinesBottom : this.linesBottom;
         int x = this.pos.getX();
         int y = this.pos.getY();
         int z = this.pos.getZ();
         if (shapeMode.lines()) {
            if (Dir.isNot(this.exclude, (byte)32) && Dir.isNot(this.exclude, (byte)8)) {
               event.renderer.line((double)x, (double)y, (double)z, (double)x, (double)y + this.height, (double)z, this.linesBottom, this.linesTop);
            }

            if (Dir.isNot(this.exclude, (byte)32) && Dir.isNot(this.exclude, (byte)16)) {
               event.renderer.line((double)x, (double)y, (double)(z + 1), (double)x, (double)y + this.height, (double)(z + 1), this.linesBottom, this.linesTop);
            }

            if (Dir.isNot(this.exclude, (byte)64) && Dir.isNot(this.exclude, (byte)8)) {
               event.renderer.line((double)(x + 1), (double)y, (double)z, (double)(x + 1), (double)y + this.height, (double)z, this.linesBottom, this.linesTop);
            }

            if (Dir.isNot(this.exclude, (byte)64) && Dir.isNot(this.exclude, (byte)16)) {
               event.renderer.line((double)(x + 1), (double)y, (double)(z + 1), (double)(x + 1), (double)y + this.height, (double)(z + 1), this.linesBottom, this.linesTop);
            }

            if (Dir.isNot(this.exclude, (byte)8)) {
               event.renderer.line((double)x, (double)y, (double)z, (double)(x + 1), (double)y, (double)z, this.linesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)8)) {
               event.renderer.line((double)x, (double)y + this.height, (double)z, (double)(x + 1), (double)y + this.height, (double)z, this.linesTop);
            }

            if (Dir.isNot(this.exclude, (byte)16)) {
               event.renderer.line((double)x, (double)y, (double)(z + 1), (double)(x + 1), (double)y, (double)(z + 1), this.linesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)16)) {
               event.renderer.line((double)x, (double)y + this.height, (double)(z + 1), (double)(x + 1), (double)y + this.height, (double)(z + 1), this.linesTop);
            }

            if (Dir.isNot(this.exclude, (byte)32)) {
               event.renderer.line((double)x, (double)y, (double)z, (double)x, (double)y, (double)(z + 1), this.linesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)32)) {
               event.renderer.line((double)x, (double)y + this.height, (double)z, (double)x, (double)y + this.height, (double)(z + 1), this.linesTop);
            }

            if (Dir.isNot(this.exclude, (byte)64)) {
               event.renderer.line((double)(x + 1), (double)y, (double)z, (double)(x + 1), (double)y, (double)(z + 1), this.linesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)64)) {
               event.renderer.line((double)(x + 1), (double)y + this.height, (double)z, (double)(x + 1), (double)y + this.height, (double)(z + 1), this.linesTop);
            }
         }

         if (shapeMode.sides()) {
            if (Dir.isNot(this.exclude, (byte)2) && (Boolean)HoleEsp.this.topQuad.get()) {
               event.renderer.quad((double)x, (double)y + this.height, (double)z, (double)x, (double)y + this.height, (double)(z + 1), (double)(x + 1), (double)y + this.height, (double)(z + 1), (double)(x + 1), (double)y + this.height, (double)z, this.sidesTop);
            }

            if (Dir.isNot(this.exclude, (byte)4) && (Boolean)HoleEsp.this.bottomQuad.get()) {
               event.renderer.quad((double)x, (double)y, (double)z, (double)x, (double)y, (double)(z + 1), (double)(x + 1), (double)y, (double)(z + 1), (double)(x + 1), (double)y, (double)z, this.sidesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)8)) {
               event.renderer.gradientQuadVertical((double)x, (double)y, (double)z, (double)(x + 1), (double)y + this.height, (double)z, this.sidesTop, this.sidesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)16)) {
               event.renderer.gradientQuadVertical((double)x, (double)y, (double)(z + 1), (double)(x + 1), (double)y + this.height, (double)(z + 1), this.sidesTop, this.sidesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)32)) {
               event.renderer.gradientQuadVertical((double)x, (double)y, (double)z, (double)x, (double)y + this.height, (double)(z + 1), this.sidesTop, this.sidesBottom);
            }

            if (Dir.isNot(this.exclude, (byte)64)) {
               event.renderer.gradientQuadVertical((double)(x + 1), (double)y, (double)z, (double)(x + 1), (double)y + this.height, (double)(z + 1), this.sidesTop, this.sidesBottom);
            }
         }

         this.sidesTop = prevSidesTop.copy();
         this.sidesBottom = prevSidesBottom.copy();
         this.linesTop = prevLinesTop.copy();
         this.linesBottom = prevLinesBottom.copy();
      }
   }
}
