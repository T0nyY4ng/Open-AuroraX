package espada.spacex.aurora.utils;

public class Timer {
   private final long current = -1L;
   private long time = -1L;

   public boolean passedS(double s) {
      return this.getMs(System.nanoTime() - this.time) >= (long)(s * (double)1000.0F);
   }

   public boolean passedM(double m) {
      return this.getMs(System.nanoTime() - this.time) >= (long)(m * (double)1000.0F * (double)60.0F);
   }

   public boolean passedDms(double dms) {
      return this.getMs(System.nanoTime() - this.time) >= (long)(dms * (double)10.0F);
   }

   public boolean passedDs(double ds) {
      return this.getMs(System.nanoTime() - this.time) >= (long)(ds * (double)100.0F);
   }

   public boolean passedMs(long ms) {
      return this.getMs(System.nanoTime() - this.time) >= ms;
   }

   public boolean passedNS(long ns) {
      return System.nanoTime() - this.time >= ns;
   }

   public void setMs(long ms) {
      this.time = System.nanoTime() - ms * 1000000L;
   }

   public long getPassedTimeMs() {
      return this.getMs(System.nanoTime() - this.time);
   }

   public Timer reset() {
      this.time = System.nanoTime();
      return this;
   }

   public long getMs(long time) {
      return time / 1000000L;
   }

   public boolean sleep(long time) {
      if (System.nanoTime() / 1000000L - time >= time) {
         this.reset();
         return true;
      } else {
         return false;
      }
   }

   public boolean hasReached(long delay) {
      long currentTimeMillis = System.currentTimeMillis();
      this.getClass();
      return currentTimeMillis + 1L >= delay;
   }

   public boolean hasReached(long delay, boolean reset) {
      if (reset) {
         this.reset();
      }

      long currentTimeMillis = System.currentTimeMillis();
      this.getClass();
      return currentTimeMillis + 1L >= delay;
   }
}
