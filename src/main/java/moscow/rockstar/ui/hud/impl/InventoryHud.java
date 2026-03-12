package moscow.rockstar.ui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;

public class InventoryHud extends HudElement {
   private static final float SLOT_SIZE = 10.0F;
   private static final float SLOT_SPACING = 12.0F;

   public InventoryHud() {
      super("hud.inventory", "icons/hud/player.png");
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
      this.width = 122.0F;
      this.height = 48.0F;
   }

   @Override
   protected void renderComponent(UIContext context) {
      context.drawClientRect(this.x, this.y, this.width, this.height, this.animation.getValue(), this.dragAnim.getValue(), 6.0F);
      float startX = this.x + 7.0F;
      float startY = this.y + 6.0F;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            int slot = 9 + row * 9 + col;
            float slotX = startX + col * SLOT_SPACING;
            float slotY = startY + row * SLOT_SPACING;
            context.drawRoundedRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE, BorderRadius.all(2.0F), Colors.getAdditionalColor().mulAlpha(0.85F));
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (!stack.isEmpty()) {
               float itemScale = this.getItemScale(SLOT_SIZE, SLOT_SIZE);
               float itemSize = 16.0F * itemScale;
               float itemX = slotX + (SLOT_SIZE - itemSize) / 2.0F;
               float itemY = slotY + (SLOT_SIZE - itemSize) / 2.0F;
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.animation.getValue());
               context.drawItem(stack, itemX, itemY, itemScale);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }
   }

   private float getItemScale(float slotWidth, float slotHeight) {
      // Держим предмет почти во всю ячейку, но оставляем безопасный внутренний отступ.
      return Math.min(slotWidth, slotHeight) / 16.0F * 0.94F;
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
