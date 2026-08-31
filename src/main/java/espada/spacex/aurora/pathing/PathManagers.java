package espada.spacex.aurora.pathing;

import java.lang.reflect.InvocationTargetException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.PreInit;

public class PathManagers {
   private static IPathManager INSTANCE = new NopPathManager();

   public static IPathManager get() {
      return INSTANCE;
   }

   @PreInit
   public static void init() {
      if (exists("meteordevelopment.voyager.PathManager")) {
         try {
            INSTANCE = (IPathManager)Class.forName("meteordevelopment.voyager.PathManager").getConstructor().newInstance();
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException e) {
            throw new RuntimeException(e);
         }
      }

      MeteorClient.LOG.info("Path Manager: {}", INSTANCE.getName());
   }

   private static boolean exists(String name) {
      try {
         Class.forName(name);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }
}
