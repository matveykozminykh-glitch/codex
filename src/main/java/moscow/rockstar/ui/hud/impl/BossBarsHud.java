package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.mixin.accessors.BossBarHudAccessor;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.screen.ChatScreen;

public class BossBarsHud extends HudList {
   public BossBarsHud() {
      super("hud.bossbars", "icons/target2.png");
   }

   @Override
   public void update(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      this.width = 108.0F;
      this.height = 22.0F;
      for (ClientBossBar bar : this.getBars()) {
         this.width = Math.max(this.width, font.width(bar.getName().getString()) + 18.0F);
      }

      this.height += Math.max(1, this.getBars().size()) * 15.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      super.renderComponent(context);
      List<ClientBossBar> bars = this.getBars();
      float offset = 22.0F;
      if (bars.isEmpty()) {
         context.drawCenteredText(font, "No boss bars", this.x + this.width / 2.0F, this.y + offset + 1.0F, Colors.getTextColor().mulAlpha(0.55F));
         return;
      }

      for (ClientBossBar bar : bars) {
         context.drawText(font, bar.getName().getString(), this.x + 7.0F, this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 10.0F), Colors.getTextColor());
         context.drawRoundedRect(this.x + 7.0F, this.y + offset + 9.0F, this.width - 14.0F, 4.0F, moscow.rockstar.framework.objects.BorderRadius.all(2.0F), Colors.getAdditionalColor());
            context.drawRoundedRect(
               this.x + 7.0F,
               this.y + offset + 9.0F,
               (this.width - 14.0F) * bar.getPercent(),
               4.0F,
               moscow.rockstar.framework.objects.BorderRadius.all(2.0F),
               Colors.getAccentColor()
            );
         offset += 15.0F;
      }
   }

   private List<ClientBossBar> getBars() {
      if (mc.inGameHud == null) {
         return List.of();
      }

      return new ArrayList<>(((BossBarHudAccessor)mc.inGameHud.getBossBarHud()).getBossBars().values());
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
