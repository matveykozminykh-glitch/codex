package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexFormats;

public class InfoHud extends HudList {
   private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.info.always_display");

   public InfoHud() {
      super("hud.info", "icons/info.png");
   }

   @Override
   public void update(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      List<String> lines = this.buildLines();
      this.width = 108.0F;
      this.height = 22.0F;

      for (String line : lines) {
         this.width = Math.max(this.width, font.width(line) + 18.0F);
      }

      this.height += lines.size() * 14.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      super.renderComponent(context);
      FontBatching batching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, font.getFont());
      List<String> lines = this.buildLines();
      float offset = 22.0F;

      for (String line : lines) {
         context.drawText(font, line, this.x + 7.0F, this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 12.0F), Colors.getTextColor());
         offset += 14.0F;
      }

      batching.draw();
   }

   private List<String> buildLines() {
      List<String> lines = new ArrayList<>();
      if (mc.player == null) {
         return lines;
      }

      int ping = 0;
      PlayerListEntry entry = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
      if (entry != null) {
         ping = entry.getLatency();
      }

      double bps = EntityUtility.getVelocity() * 20.0;
      lines.add(String.format("XYZ: %d %d %d", (int)mc.player.getX(), (int)mc.player.getY(), (int)mc.player.getZ()));
      lines.add(String.format("BPS: %.2f", bps));
      lines.add(String.format("TPS: %.1f", Rockstar.getInstance().getTpsHandler().getTPS()));
      lines.add(String.format("PING: %dms", ping));
      lines.add("SERVER: " + ServerUtility.getServerName(true));
      return lines;
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || this.alwaysDisplay.isEnabled() || EntityUtility.isInGame();
   }
}
