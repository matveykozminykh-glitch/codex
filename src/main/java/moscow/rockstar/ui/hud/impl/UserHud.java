package moscow.rockstar.ui.hud.impl;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.client.gui.screen.ChatScreen;

public class UserHud extends HudElement {
   public UserHud() {
      super("hud.user", "icons/hud/player.png");
   }

   @Override
   public void update(UIContext context) {
      this.width = 118.0F;
      this.height = 34.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      if (mc.player == null) {
         return;
      }

      Font titleFont = Fonts.MEDIUM.getFont(7.0F);
      Font valueFont = Fonts.REGULAR.getFont(6.5F);
      float alpha = this.animation.getValue();
      context.drawClientRect(this.x, this.y, this.width, this.height, alpha, this.dragAnim.getValue(), 6.0F);
      context.drawHead(mc.player, this.x + 7.0F, this.y + 7.0F, 20.0F, BorderRadius.all(4.0F), ColorRGBA.WHITE.withAlpha(255.0F * alpha));
      context.drawText(titleFont, mc.player.getName().getString(), this.x + 33.0F, this.y + 7.0F, Colors.getTextColor());
      context.drawText(valueFont, String.format("HP %.1f", EntityUtility.getHealth(mc.player)), this.x + 33.0F, this.y + 16.0F, Colors.getTextColor().mulAlpha(0.8F));
      context.drawText(
         valueFont,
         "Food " + mc.player.getHungerManager().getFoodLevel(),
         this.x + 72.0F,
         this.y + 16.0F,
         Colors.getTextColor().mulAlpha(0.8F)
      );
      float healthProgress = Math.min(1.0F, EntityUtility.getHealth(mc.player) / 20.0F);
      context.drawRoundedRect(this.x + 33.0F, this.y + 25.0F, this.width - 40.0F, 4.0F, BorderRadius.all(2.0F), Colors.getAdditionalColor().mulAlpha(0.9F));
      context.drawRoundedRect(
         this.x + 33.0F,
         this.y + 25.0F,
         (this.width - 40.0F) * healthProgress,
         4.0F,
         BorderRadius.all(2.0F),
         Colors.getAccentColor()
      );
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
