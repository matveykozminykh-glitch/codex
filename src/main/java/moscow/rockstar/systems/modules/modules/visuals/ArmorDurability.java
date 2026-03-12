package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Armor Durability", category = ModuleCategory.VISUALS, desc = "Цветовая подсветка прочности брони")
public class ArmorDurability extends BaseModule {
   private final ColorSetting fullDurability = new ColorSetting(this, "modules.settings.armor_durability.full").color(Colors.GREEN);
   private final ColorSetting lowDurability = new ColorSetting(this, "modules.settings.armor_durability.low").color(Colors.RED);
   private final BooleanSetting smoothTransition = new BooleanSetting(this, "modules.settings.armor_durability.smooth_transition").enable();
   private final SliderSetting intensity = new SliderSetting(this, "modules.settings.armor_durability.intensity").min(0.3F).max(2.0F).step(0.05F).currentValue(1.0F);
   private final BooleanSetting onlyDamaged = new BooleanSetting(this, "modules.settings.armor_durability.only_damaged");

   public static ColorRGBA resolveColor(ItemStack stack, ColorRGBA fallback) {
      ArmorDurability module = Rockstar.getInstance().getModuleManager().getModules().stream().filter(m -> m instanceof ArmorDurability).map(m -> (ArmorDurability)m).findFirst().orElse(null);
      if (module == null || !module.isEnabled() || !stack.isDamageable()) {
         return fallback;
      }

      if (module.onlyDamaged.isEnabled() && stack.getDamage() <= 0) {
         return fallback;
      }

      float durability = 1.0F - (float)stack.getDamage() / stack.getMaxDamage();
      if (!module.smoothTransition.isEnabled()) {
         return durability > 0.5F ? module.fullDurability.getColor() : module.lowDurability.getColor();
      }

      return module.lowDurability.getColor().mix(module.fullDurability.getColor(), durability).mulAlpha(module.intensity.getCurrentValue() / 2.0F);
   }
}
