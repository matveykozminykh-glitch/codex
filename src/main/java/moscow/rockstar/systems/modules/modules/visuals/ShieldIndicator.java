package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@ModuleInfo(name = "Shield Indicator", category = ModuleCategory.VISUALS, desc = "Индикатор кулдауна щита")
public class ShieldIndicator extends BaseModule {
   private final ColorSetting ready = new ColorSetting(this, "modules.settings.shield_indicator.ready").color(Colors.GREEN);
   private final ColorSetting cooldown = new ColorSetting(this, "modules.settings.shield_indicator.cooldown").color(Colors.RED);
   private final BooleanSetting smoothTransition = new BooleanSetting(this, "modules.settings.shield_indicator.smooth_transition").enable();
   private final SliderSetting intensity = new SliderSetting(this, "modules.settings.shield_indicator.intensity").min(0.3F).max(2.0F).step(0.05F).currentValue(1.0F);
   private final EventListener<HudRenderEvent> onHud = event -> {
      if (!EntityUtility.isInGame()) {
         return;
      }

      boolean hasShield = mc.player.getOffHandStack().isOf(Items.SHIELD) || mc.player.getMainHandStack().isOf(Items.SHIELD);
      if (!hasShield) {
         return;
      }

      ItemStack shieldStack = mc.player.getOffHandStack().isOf(Items.SHIELD) ? mc.player.getOffHandStack() : mc.player.getMainHandStack();
      float cooldownProgress = 1.0F - mc.player.getItemCooldownManager().getCooldownProgress(shieldStack, event.getTickDelta());
      if (!this.smoothTransition.isEnabled()) {
         cooldownProgress = cooldownProgress >= 1.0F ? 1.0F : 0.0F;
      }

      ColorRGBA color = this.cooldown.getColor().mix(this.ready.getColor(), cooldownProgress);
      float width = 38.0F;
      float height = 5.0F;
      float x = event.getContext().getScaledWindowWidth() / 2.0F - width / 2.0F;
      float y = event.getContext().getScaledWindowHeight() - 39.0F;
      event.getContext().drawRoundedRect(x, y, width, height, moscow.rockstar.framework.objects.BorderRadius.all(2.5F), Colors.getAdditionalColor().mulAlpha(0.9F));
      event.getContext().drawRoundedRect(
         x,
         y,
         width * cooldownProgress,
         height,
         moscow.rockstar.framework.objects.BorderRadius.all(2.5F),
         color.withAlpha(255.0F * this.intensity.getCurrentValue() / 2.0F)
      );
   };
}
