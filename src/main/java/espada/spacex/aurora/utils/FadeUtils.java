package espada.spacex.aurora.utils;

public class FadeUtils {
   protected long start;
   protected long length;

   public FadeUtils(long ms) {
      this.length = ms;
      this.reset();
   }

   public void reset() {
      this.start = System.currentTimeMillis();
   }

   public boolean isEnd() {
      return this.getTime() >= this.length;
   }

   protected long getTime() {
      return System.currentTimeMillis() - this.start;
   }

   public void setLength(long length) {
      this.length = length;
   }

   private double getFadeOne() {
      return this.isEnd() ? (double)1.0F : (double)this.getTime() / (double)this.length;
   }

   public double getFadeInDefault() {
      return Math.tanh((double)this.getTime() / (double)this.length * (double)3.0F);
   }

   public double getFadeOutDefault() {
      return (double)1.0F - Math.tanh((double)this.getTime() / (double)this.length * (double)3.0F);
   }

   public double getEpsEzFadeIn() {
      return (double)1.0F - Math.sin((Math.PI / 2D) * this.getFadeOne()) * Math.sin(2.5132741228718345 * this.getFadeOne());
   }

   public double getEpsEzFadeOut() {
      return Math.sin((Math.PI / 2D) * this.getFadeOne()) * Math.sin(2.5132741228718345 * this.getFadeOne());
   }

   public double easeOutQuad() {
      return this.length == 0L ? (double)1.0F : (double)1.0F - ((double)1.0F - this.getFadeOne()) * ((double)1.0F - this.getFadeOne());
   }

   public double easeInQuad() {
      return this.getFadeOne() * this.getFadeOne();
   }

   public double def() {
      return this.isEnd() ? (double)1.0F : this.getFadeOne();
   }
}
