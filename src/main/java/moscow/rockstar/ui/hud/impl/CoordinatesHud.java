package moscow.rockstar.ui.hud.impl;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.ui.hud.inline.InlineElement;
import moscow.rockstar.ui.hud.inline.InlineValue;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.world.World;

public class CoordinatesHud extends InlineElement {
   private final InlineValue xyz = new InlineValue(this.elements, "XYZ");
   private final InlineValue biome = new InlineValue(this.elements, "Biome");
   private final InlineValue nether = new InlineValue(this.elements, "Nether");

   public CoordinatesHud() {
      super("hud.coordinates", "icons/hud/world.png");
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
      if (mc.player == null || mc.world == null) {
         return;
      }

      this.xyz.update(
         String.format("%d %d %d", Math.round(mc.player.getX()), Math.round(mc.player.getY()), Math.round(mc.player.getZ())),
         String.format("%.2f %.2f %.2f", mc.player.getX(), mc.player.getY(), mc.player.getZ())
      );
      this.biome.update(mc.world.getBiome(mc.player.getBlockPos()).getKey().map(key -> key.getValue().getPath()).orElse("unknown"));
      double modifier = mc.world.getRegistryKey() == World.NETHER ? 8.0 : 0.125;
      this.nether.update(String.format("%.1f %.1f", mc.player.getX() * modifier, mc.player.getZ() * modifier));
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
