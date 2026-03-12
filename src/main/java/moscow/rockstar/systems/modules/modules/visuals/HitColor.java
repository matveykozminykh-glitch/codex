package moscow.rockstar.systems.modules.modules.visuals;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.PostAttackEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@ModuleInfo(name = "Hit Color", category = ModuleCategory.VISUALS, desc = "Подсвечивает модель после удара")
public class HitColor extends BaseModule {
   private static final long HIT_DURATION = 300L;
   private static final Map<Integer, Long> HIT_TIME = new ConcurrentHashMap<>();
   private final ModeSetting colorMode = new ModeSetting(this, "modules.settings.hit_color.mode");
   private final ModeSetting.Value clientColor = new ModeSetting.Value(this.colorMode, "Client Color").select();
   private final ModeSetting.Value customColorMode = new ModeSetting.Value(this.colorMode, "Custom Color");
   private final ColorSetting hitColor = new ColorSetting(this, "modules.settings.hit_color.color").color(Colors.RED);
   private final SliderSetting alpha = new SliderSetting(this, "modules.settings.hit_color.alpha").min(0.0F).max(255.0F).step(1.0F).currentValue(100.0F);
   private final EventListener<PostAttackEvent> onPostAttack = event -> {
      if (event.getEntity() instanceof LivingEntity living && living.isAlive()) {
         HIT_TIME.put(living.getId(), System.currentTimeMillis());
      }
   };

   public static ColorRGBA getOverlay(Entity entity) {
      HitColor module = Rockstar.getInstance().getModuleManager().getModule(HitColor.class);
      if (!module.isEnabled() || !(entity instanceof LivingEntity)) {
         return null;
      }

      Long hitTime = HIT_TIME.get(entity.getId());
      if (hitTime == null) {
         return null;
      }

      float progress = (float)(System.currentTimeMillis() - hitTime) / (float)HIT_DURATION;
      if (progress >= 1.0F) {
         HIT_TIME.remove(entity.getId());
         return null;
      }

      ColorRGBA baseColor = module.clientColor.isSelected() ? Colors.ACCENT : module.hitColor.getColor();
      return baseColor.withAlpha(module.alpha.getCurrentValue() * (1.0F - progress));
   }

   public static int applyOverlay(Entity entity, int originalColor) {
      ColorRGBA overlay = getOverlay(entity);
      if (overlay == null) {
         return originalColor;
      }

      ColorRGBA original = ColorRGBA.fromInt(originalColor);
      float blend = overlay.getAlpha() / 255.0F;
      return original.mix(overlay.withAlpha(255.0F), blend).withAlpha(original.getAlpha()).getRGB();
   }
}
