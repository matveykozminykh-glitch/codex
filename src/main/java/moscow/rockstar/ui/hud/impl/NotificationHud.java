package moscow.rockstar.ui.hud.impl;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.notifications.Notification;
import moscow.rockstar.systems.notifications.NotificationOther;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.screen.ChatScreen;

public class NotificationHud extends HudList {
   public NotificationHud() {
      super("hud.notifications", "icons/info.png");
   }

   @Override
   public void update(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      this.width = 108.0F;
      this.height = 22.0F;

      for (String line : this.lines()) {
         this.width = Math.max(this.width, font.width(line) + 18.0F);
      }

      this.height += Math.max(1, this.lines().size()) * 13.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      super.renderComponent(context);
      List<String> lines = this.lines();
      float offset = 22.0F;
      if (lines.isEmpty()) {
         context.drawCenteredText(font, "No notifications", this.x + this.width / 2.0F, this.y + offset + 1.0F, Colors.getTextColor().mulAlpha(0.55F));
         return;
      }

      for (String line : lines) {
         context.drawText(font, line, this.x + 7.0F, this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 10.0F), Colors.getTextColor());
         offset += 13.0F;
      }
   }

   private List<String> lines() {
      List<String> other = Rockstar.getInstance().getNotificationManager().getNotificationsOther().stream().limit(4L).map(n -> n.getTitle() + " - " + n.getDesc()).toList();
      if (!other.isEmpty()) {
         return other;
      }

      return Rockstar.getInstance().getNotificationManager().getNotifications().stream().limit(4L).map(Notification::getText).toList();
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
