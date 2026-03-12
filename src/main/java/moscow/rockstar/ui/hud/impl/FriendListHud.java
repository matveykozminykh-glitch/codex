package moscow.rockstar.ui.hud.impl;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.PlayerEntity;

public class FriendListHud extends HudList {
   public FriendListHud() {
      super("hud.friends", "icons/hud/player.png");
   }

   @Override
   public void update(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      List<String> friends = Rockstar.getInstance().getFriendManager().listFriends();
      this.width = 92.0F;
      this.height = 22.0F;

      for (String friend : friends) {
         this.width = Math.max(this.width, font.width(friend) + 34.0F);
      }

      this.height += Math.max(1, friends.size()) * 13.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      super.renderComponent(context);
      List<String> friends = Rockstar.getInstance().getFriendManager().listFriends();
      float offset = 22.0F;
      if (friends.isEmpty()) {
         context.drawCenteredText(font, "No friends", this.x + this.width / 2.0F, this.y + offset + 1.0F, Colors.getTextColor().mulAlpha(0.55F));
         return;
      }

      for (String friend : friends) {
         boolean online = mc.world != null && mc.world.getPlayers().stream().map(PlayerEntity::getNameForScoreboard).anyMatch(name -> name.equalsIgnoreCase(friend));
         context.drawRoundedRect(this.x + 7.0F, this.y + offset + 4.0F, 4.0F, 4.0F, moscow.rockstar.framework.objects.BorderRadius.all(2.0F), (online ? Colors.GREEN : Colors.RED));
         context.drawText(font, friend, this.x + 16.0F, this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 10.0F), Colors.getTextColor());
         context.drawRightText(font, online ? "online" : "offline", this.x + this.width - 7.0F, this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 10.0F), Colors.getTextColor().mulAlpha(0.55F));
         offset += 13.0F;
      }
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
