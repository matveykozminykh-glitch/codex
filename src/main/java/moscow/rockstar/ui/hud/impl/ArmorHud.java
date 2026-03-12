package moscow.rockstar.ui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.visuals.ArmorDurability;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;

public class ArmorHud extends HudElement {
   public ArmorHud() {
      super("hud.armor", "icons/hud/player.png");
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
      this.width = 96.0F;
      this.height = 22.0F;
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font smallFont = Fonts.SEMIBOLD.getFont(6.0F);
      context.drawClientRect(this.x, this.y, this.width, this.height, this.animation.getValue(), this.dragAnim.getValue(), 6.0F);
      List<ItemStack> armor = new ArrayList<>();
      mc.player.getArmorItems().forEach(armor::add);
      Collections.reverse(armor);
      float xOffset = this.x + 7.0F;

      for (ItemStack stack : armor) {
         float alpha = 255.0F * this.animation.getValue();
         float slotX = xOffset;
         float slotY = this.y + 4.0F;
         float slotWidth = 18.0F;
         float slotHeight = 14.0F;
         context.drawRoundedRect(slotX, slotY, slotWidth, slotHeight, BorderRadius.all(3.0F), Colors.getAdditionalColor().withAlpha(alpha * 0.8F));
         if (!stack.isEmpty()) {
            float itemScale = this.getItemScale(slotWidth, slotHeight);
            float itemSize = 16.0F * itemScale;
            float itemX = slotX + (slotWidth - itemSize) / 2.0F;
            float itemY = slotY + (slotHeight - itemSize) / 2.0F;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.animation.getValue());
            context.drawItem(stack, itemX, itemY, itemScale);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.drawCenteredText(
               smallFont,
               this.getDurabilityPercent(stack),
               slotX + slotWidth / 2.0F,
               slotY + 8.0F,
               ArmorDurability.resolveColor(stack, this.getDurabilityColor(stack)).withAlpha(alpha)
            );
         } else {
            context.drawCenteredText(smallFont, "-", slotX + slotWidth / 2.0F, slotY + 8.0F, Colors.getTextColor().withAlpha(alpha * 0.45F));
         }

         xOffset += 20.0F;
      }
   }

   private float getItemScale(float slotWidth, float slotHeight) {
      return Math.min(slotWidth, slotHeight) / 16.0F * 0.95F;
   }

   private String getDurabilityPercent(ItemStack stack) {
      if (!stack.isDamageable()) {
         return "100";
      }

      float value = 100.0F - (float)stack.getDamage() / stack.getMaxDamage() * 100.0F;
      return Integer.toString(Math.round(Math.max(0.0F, value)));
   }

   private ColorRGBA getDurabilityColor(ItemStack stack) {
      if (!stack.isDamageable()) {
         return Colors.GREEN;
      }

      float value = 1.0F - (float)stack.getDamage() / stack.getMaxDamage();
      return new ColorRGBA(255.0F * (1.0F - value), 255.0F * value, 90.0F);
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
