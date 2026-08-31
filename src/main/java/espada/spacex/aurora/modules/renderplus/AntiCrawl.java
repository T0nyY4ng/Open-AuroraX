package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;

public class AntiCrawl extends Modules {
   public AntiCrawl() {
      super(Aurora.RenderPlus, "Anti Crawl", "Doesn't crawl or sneak when in low space (should be used on 1.12.2).");
   }
}
