package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class CooldownsHud extends HudList {
   public CooldownsHud() {
      super("hud.cooldowns", "icons/hud/player.png");
   }

   @Override
   public void update(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      List<ItemStack> stacks = this.getCooldownStacks();
      this.width = 94.0F;
      this.height = 22.0F;

      for (ItemStack stack : stacks) {
         this.width = Math.max(this.width, font.width(stack.getName().getString()) + 48.0F);
      }

      this.height += Math.max(1, stacks.size()) * 13.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      super.renderComponent(context);
      List<ItemStack> stacks = this.getCooldownStacks();
      float offset = 22.0F;
      if (stacks.isEmpty()) {
         context.drawCenteredText(font, "No cooldowns", this.x + this.width / 2.0F, this.y + offset + 1.0F, Colors.getTextColor().mulAlpha(0.55F));
         return;
      }

      for (ItemStack stack : stacks) {
         float progress = MathHelper.clamp(
            mc.player.getItemCooldownManager().getCooldownProgress(stack, mc.getRenderTickCounter().getTickDelta(false)), 0.0F, 1.0F
         );
         context.drawItem(stack, this.x + 7.0F, this.y + offset - 1.0F, 0.65F);
         context.drawText(font, stack.getName().getString(), this.x + 20.0F, this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 10.0F), Colors.getTextColor());
         context.drawRoundedRect(this.x + this.width - 43.0F, this.y + offset + 3.0F, 36.0F, 4.0F, BorderRadius.all(2.0F), Colors.getAdditionalColor());
            context.drawRoundedRect(
               this.x + this.width - 43.0F,
               this.y + offset + 3.0F,
               36.0F * (1.0F - progress),
               4.0F,
               BorderRadius.all(2.0F),
               Colors.getAccentColor()
            );
         offset += 13.0F;
      }
   }

   private List<ItemStack> getCooldownStacks() {
      List<ItemStack> result = new ArrayList<>();

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (!stack.isEmpty() && mc.player.getItemCooldownManager().getCooldownProgress(stack, mc.getRenderTickCounter().getTickDelta(false)) > 0.0F) {
            result.add(stack);
         }
      }

      ItemStack offhand = mc.player.getOffHandStack();
      if (!offhand.isEmpty() && mc.player.getItemCooldownManager().getCooldownProgress(offhand, mc.getRenderTickCounter().getTickDelta(false)) > 0.0F) {
         result.add(offhand);
      }

      return result;
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
