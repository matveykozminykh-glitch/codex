package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

@ModuleInfo(name = "Chams", category = ModuleCategory.VISUALS, desc = "Цветные модели игроков и мобов")
public class Chams extends BaseModule {
   private final BooleanSetting applyPlayers = new BooleanSetting(this, "modules.settings.chams.players").enable();
   private final BooleanSetting applyMobs = new BooleanSetting(this, "modules.settings.chams.mobs");
   private final BooleanSetting applySelf = new BooleanSetting(this, "modules.settings.chams.self");
   private final ColorSetting chamsColor = new ColorSetting(this, "modules.settings.chams.color").color(new ColorRGBA(255.0F, 255.0F, 255.0F, 120.0F));
   private final BooleanSetting glow = new BooleanSetting(this, "modules.settings.chams.glow");
   private final SliderSetting glowIntensity = new SliderSetting(this, "modules.settings.chams.glow_intensity").min(1.0F).max(30.0F).step(0.1F).currentValue(15.0F);
   private final SliderSetting glowAlpha = new SliderSetting(this, "modules.settings.chams.glow_alpha").min(0.0F).max(1.0F).step(0.01F).currentValue(0.26F);

   public static boolean shouldApply(Entity entity) {
      Chams module = Rockstar.getInstance().getModuleManager().getModule(Chams.class);
      if (!module.isEnabled() || !(entity instanceof LivingEntity living)) {
         return false;
      }

      if (living == module.mc.player) {
         return module.applySelf.isEnabled();
      } else if (living instanceof PlayerEntity) {
         return module.applyPlayers.isEnabled();
      } else {
         return living instanceof MobEntity && module.applyMobs.isEnabled();
      }
   }

   public static ColorRGBA getColor() {
      return Rockstar.getInstance().getModuleManager().getModule(Chams.class).chamsColor.getColor();
   }

   public static boolean glowEnabled() {
      Chams module = Rockstar.getInstance().getModuleManager().getModule(Chams.class);
      return module.isEnabled() && module.glow.isEnabled();
   }

   public static float glowScale() {
      Chams module = Rockstar.getInstance().getModuleManager().getModule(Chams.class);
      return 1.0F + module.glowIntensity.getCurrentValue() * 0.01F;
   }

   public static ColorRGBA getGlowColor() {
      Chams module = Rockstar.getInstance().getModuleManager().getModule(Chams.class);
      ColorRGBA base = module.chamsColor.getColor();
      return base.withAlpha(255.0F * module.glowAlpha.getCurrentValue());
   }

   public static int applyColor(Entity entity, int originalColor) {
      if (!shouldApply(entity)) {
         return originalColor;
      }

      ColorRGBA overlay = getColor();
      ColorRGBA original = ColorRGBA.fromInt(originalColor);
      float blend = overlay.getAlpha() / 255.0F;
      return original.mix(overlay.withAlpha(255.0F), blend).withAlpha(Math.max(original.getAlpha(), overlay.getAlpha())).getRGB();
   }
}
