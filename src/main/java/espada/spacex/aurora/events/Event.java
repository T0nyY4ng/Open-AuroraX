package espada.spacex.aurora.events;

public abstract class Event {
   private boolean cancelled = false;
   private final Stage stage;

   public Event(Stage stage) {
      this.stage = stage;
   }

   public Event() {
      this.stage = Event.Stage.None;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void cancel() {
      this.cancelled = true;
   }

   public Stage getStage() {
      return this.stage;
   }

   public boolean isPost() {
      return this.stage == Event.Stage.Post;
   }

   public boolean isPre() {
      return this.stage == Event.Stage.Pre;
   }

   public static enum Stage {
      Pre,
      Post,
      None;

      // $FF: synthetic method
      private static Stage[] $values() {
         return new Stage[]{Pre, Post, None};
      }
   }
}
