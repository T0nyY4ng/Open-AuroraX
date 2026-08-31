package espada.spacex.aurora.utils;

public class MathUtils {
   public static int randomNum(int min, int max) {
      return min + (int)(Math.random() * (double)(max - min + 1));
   }

   public static float random(float min, float max) {
      return (float)(Math.random() * (double)(max - min) + (double)min);
   }

   public static double clamp(double num, double min, double max) {
      return num < min ? min : Math.min(num, max);
   }

   public static float clamp(float num, float min, float max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }
}
