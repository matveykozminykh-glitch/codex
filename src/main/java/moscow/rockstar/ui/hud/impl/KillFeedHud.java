package moscow.rockstar.ui.hud.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityDeathEvent;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;

public class KillFeedHud extends HudList {
   private final List<Entry> entries = new CopyOnWriteArrayList<>();
   private final EventListener<EntityDeathEvent> onEntityDeath = event -> {
      if (mc.player == null) {
         return;
      }

      LivingEntity killer = event.getKillerEntity();
      if (killer == mc.player && event.getEntity() != mc.player) {
         this.entries.add(0, new Entry("Killed " + event.getEntity().getName().getString()));
      }
   };

   public KillFeedHud() {
      super("hud.killfeed", "icons/hud/target.png");
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   @Override
   public void update(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      this.entries.removeIf(Entry::expired);
      this.width = 96.0F;
      this.height = 22.0F;

      for (Entry entry : this.entries) {
         this.width = Math.max(this.width, font.width(entry.text) + 18.0F);
      }

      this.height += Math.max(1, this.entries.size()) * 13.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      super.renderComponent(context);
      Font font = Fonts.REGULAR.getFont(7.0F);
      float offset = 22.0F;
      if (this.entries.isEmpty()) {
         context.drawCenteredText(font, "No kills", this.x + this.width / 2.0F, this.y + offset + 1.0F, Colors.getTextColor().mulAlpha(0.55F));
         return;
      }

      for (Entry entry : this.entries) {
         float age = Math.min(1.0F, (System.currentTimeMillis() - entry.createdAt) / 300.0F);
         float fade = Math.min(1.0F, Math.max(0.0F, (entry.expiresAt - System.currentTimeMillis()) / 500.0F));
         context.drawText(
            font,
            entry.text,
            this.x + 7.0F,
            this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 10.0F),
            Colors.getTextColor().withAlpha(255.0F * Math.min(age, fade))
         );
         offset += 13.0F;
      }
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || !this.entries.isEmpty();
   }

   private static class Entry {
      private final String text;
      private final long createdAt = System.currentTimeMillis();
      private final long expiresAt = this.createdAt + 4500L;

      private Entry(String text) {
         this.text = text;
      }

      private boolean expired() {
         return System.currentTimeMillis() > this.expiresAt;
      }
   }
}
