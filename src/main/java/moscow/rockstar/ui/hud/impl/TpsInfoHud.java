package moscow.rockstar.ui.hud.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.ui.hud.inline.InlineElement;
import moscow.rockstar.ui.hud.inline.InlineValue;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;

public class TpsInfoHud extends InlineElement {
   private final InlineValue tps = new InlineValue(this.elements, "TPS", "TPS");
   private final InlineValue ping = new InlineValue(this.elements, "PING", "MS");
   private final InlineValue online = new InlineValue(this.elements, "ONLINE");

   public TpsInfoHud() {
      super("hud.tps", "icons/hud/world.png");
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
      int latency = 0;
      PlayerListEntry entry = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
      if (entry != null) {
         latency = entry.getLatency();
      }

      this.tps.update(String.format("%.1f", Rockstar.getInstance().getTpsHandler().getTPS()).replace(",", "."));
      this.ping.update(Integer.toString(latency));
      this.online.update(Integer.toString(mc.getNetworkHandler() == null ? 0 : mc.getNetworkHandler().getPlayerList().size()));
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
