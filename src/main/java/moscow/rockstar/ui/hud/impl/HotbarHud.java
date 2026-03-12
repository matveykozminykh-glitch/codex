package moscow.rockstar.ui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;

public class HotbarHud extends HudElement {
   private static final float SLOT_SPACING = 19.0F;
   private static final float SLOT_WIDTH = 17.5F;
   private static final float SLOT_HEIGHT = 14.5F;
   private static final float SELECTED_SLOT_WIDTH = 18.5F;
   private static final float SELECTED_SLOT_HEIGHT = 15.5F;
   private static final float OFFHAND_SLOT_WIDTH = 17.5F;
   private static final float OFFHAND_GAP = 7.0F;
   private final Animation selectedSlotAnimation = new Animation(220L, Easing.FIGMA_EASE_IN_OUT);

   public HotbarHud() {
      super("hud.hotbar", "icons/hud/player.png");
   }

   @Override
   public void update(UIContext context) {
      this.width = 182.0F + OFFHAND_GAP + OFFHAND_SLOT_WIDTH;
      this.height = 24.0F;
      if (this.x == 0.0F && this.y == 0.0F) {
         this.x = context.getScaledWindowWidth() / 2.0F - this.width / 2.0F;
         this.y = context.getScaledWindowHeight() - 28.0F;
      }

      super.update(context);
      this.selectedSlotAnimation.update(this.x + 5.5F + mc.player.getInventory().selectedSlot * SLOT_SPACING);
   }

   @Override
   protected void renderComponent(UIContext context) {
      context.drawClientRect(this.x, this.y, this.width, this.height, this.animation.getValue(), this.dragAnim.getValue(), 7.0F);
      float slotX = this.x + 5.5F;
      float alpha = 255.0F * this.animation.getValue();
      float selectedX = this.selectedSlotAnimation.getValue();
      float selectedY = this.y + 3.5F;
      context.drawBlurredRect(
         selectedX - 2.0F,
         selectedY - 2.0F,
         SELECTED_SLOT_WIDTH + 4.0F,
         SELECTED_SLOT_HEIGHT + 4.0F,
         28.0F,
         BorderRadius.all(5.5F),
         Colors.getAccentColor().withAlpha(alpha * 0.34F)
      );
      context.drawRoundedRect(
         selectedX,
         selectedY,
         SELECTED_SLOT_WIDTH,
         SELECTED_SLOT_HEIGHT,
         BorderRadius.all(4.5F),
         Colors.getAccentColor().withAlpha(alpha * 0.42F)
      );

      for (int i = 0; i < 9; i++) {
         boolean selected = mc.player.getInventory().selectedSlot == i;
         float slotWidth = selected ? SELECTED_SLOT_WIDTH : SLOT_WIDTH;
         float slotHeight = selected ? SELECTED_SLOT_HEIGHT : SLOT_HEIGHT;
         float slotY = this.y + (selected ? 3.5F : 4.0F);
         if (!selected) {
            context.drawRoundedRect(
               slotX,
               slotY,
               slotWidth,
               slotHeight,
               BorderRadius.all(4.0F),
               Colors.getAdditionalColor().withAlpha(alpha * 0.82F)
            );
         }

         ItemStack stack = mc.player.getInventory().getStack(i);
         if (!stack.isEmpty()) {
            float itemScale = this.getItemScale(slotWidth, slotHeight, selected);
            float itemSize = 16.0F * itemScale;
            float itemX = slotX + (slotWidth - itemSize) / 2.0F;
            float itemY = slotY + (slotHeight - itemSize) / 2.0F;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.animation.getValue());
            context.drawItem(stack, itemX, itemY, itemScale);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (stack.getCount() > 1) {
               context.drawRightText(
                  Fonts.SEMIBOLD.getFont(5.5F), Integer.toString(stack.getCount()), slotX + slotWidth - 1.6F, slotY + slotHeight - 1.8F, Colors.getTextColor()
               );
            }
         }

         slotX += SLOT_SPACING;
      }

      this.renderOffhand(context, alpha, slotX + OFFHAND_GAP);
   }

   private float getItemScale(float slotWidth, float slotHeight, boolean selected) {
      float fitScale = Math.min(slotWidth, slotHeight) / 16.0F;
      return fitScale * (selected ? 0.98F : 0.95F);
   }

   private void renderOffhand(UIContext context, float alpha, float slotX) {
      ItemStack offhandStack = mc.player.getOffHandStack();
      float slotY = this.y + 4.0F;
      context.drawRoundedRect(
         slotX,
         slotY,
         OFFHAND_SLOT_WIDTH,
         SLOT_HEIGHT,
         BorderRadius.all(4.0F),
         Colors.getAdditionalColor().withAlpha(alpha * 0.82F)
      );
      if (offhandStack.isEmpty()) {
         return;
      }

      float itemScale = this.getItemScale(OFFHAND_SLOT_WIDTH, SLOT_HEIGHT, false);
      float itemSize = 16.0F * itemScale;
      float itemX = slotX + (OFFHAND_SLOT_WIDTH - itemSize) / 2.0F;
      float itemY = slotY + (SLOT_HEIGHT - itemSize) / 2.0F;
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.animation.getValue());
      context.drawItem(offhandStack, itemX, itemY, itemScale);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (offhandStack.getCount() > 1) {
         context.drawRightText(
            Fonts.SEMIBOLD.getFont(5.5F),
            Integer.toString(offhandStack.getCount()),
            slotX + OFFHAND_SLOT_WIDTH - 1.6F,
            slotY + SLOT_HEIGHT - 1.8F,
            Colors.getTextColor()
         );
      }
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
