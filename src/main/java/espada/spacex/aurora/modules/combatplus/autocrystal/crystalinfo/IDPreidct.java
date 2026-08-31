package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class IDPreidct {
   public static Setting<Boolean> idPredict(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("ID Predict")).description("Hits the crystal before it spawns.")).defaultValue(false)).build());
   }

   public static Setting<Integer> idStartOffset(SettingGroup group) {
      return group.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Id Start Offset")).description("How many id's ahead should we attack.")).defaultValue(1)).min(0).sliderMax(10).build());
   }

   public static Setting<Integer> idOffset(SettingGroup group) {
      return group.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Id Packet Offset")).description("How many id's ahead should we attack between id packets.")).defaultValue(1)).min(1).sliderMax(10).build());
   }

   public static Setting<Integer> idPackets(SettingGroup group) {
      return group.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Id Packets")).description("How many packets to send.")).defaultValue(1)).min(1).sliderMax(10).build());
   }

   public static Setting<Double> idDelay(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("ID Start Delay")).description("Starts sending id predict packets after this many seconds.")).defaultValue(0.05).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
   }

   public static Setting<Double> idPacketDelay(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("ID Packet Delay")).description("Waits this many seconds between sending ID packets.")).defaultValue(0.05).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
   }
}
