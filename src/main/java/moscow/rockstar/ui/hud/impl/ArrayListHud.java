package moscow.rockstar.ui.hud.impl;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.ScissorUtility;
import net.minecraft.client.gui.screen.ChatScreen;

public class ArrayListHud extends HudElement {
   public ArrayListHud() {
      super("hud.arraylist", "icons/hud/keybinds.png");
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
      Font font = Fonts.MEDIUM.getFont(8.0F);
      this.width = 84.0F;
      this.height = 20.0F;

      for (Module module : this.getVisibleModules()) {
         this.width = Math.max(this.width, font.width(module.getName()) + 16.0F);
         this.height += 12.0F;
      }
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font titleFont = Fonts.MEDIUM.getFont(7.0F);
      Font lineFont = Fonts.MEDIUM.getFont(8.0F);
      List<Module> modules = this.getVisibleModules();
      context.drawClientRect(this.x, this.y, this.width, this.height, this.animation.getValue(), this.dragAnim.getValue(), 6.0F);
      context.drawText(titleFont, "ArrayList", this.x + 7.0F, this.y + 6.0F, Colors.getTextColor());
      context.drawRoundedRect(this.x + 6.0F, this.y + 17.0F, this.width - 12.0F, 1.0F, BorderRadius.all(0.5F), Colors.getSeparatorColor());
      ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
      float offset = 22.0F;
      int index = 0;

      for (Module module : modules) {
         float alpha = 255.0F * this.animation.getValue();
         float lineWidth = lineFont.width(module.getName()) + 8.0F;
         context.drawRoundedRect(
            this.x + this.width - lineWidth - 7.0F,
            this.y + offset - 1.0F,
            lineWidth,
            10.0F,
            BorderRadius.all(3.0F),
            Colors.getAdditionalColor().withAlpha(alpha * 0.72F)
         );
         context.drawRoundedRect(
            this.x + this.width - lineWidth - 7.0F,
            this.y + offset - 1.0F,
            1.5F,
            10.0F,
            BorderRadius.all(3.0F),
                  Colors.getAccentColor().withAlpha(alpha)
         );
         context.drawRightText(
            lineFont,
            module.getName(),
            this.x + this.width - 11.0F,
            this.y + offset + 1.0F,
            this.getLineColor(index).withAlpha(alpha)
         );
         offset += 12.0F;
         index++;
      }

      ScissorUtility.pop();
   }

   private List<Module> getVisibleModules() {
      return Rockstar.getInstance()
         .getModuleManager()
         .getModules()
         .stream()
         .filter(module -> module.isEnabled() && !module.isHidden() && module.getCategory().name() != null && !"Interface".equalsIgnoreCase(module.getName()))
         .sorted((left, right) -> Float.compare(Fonts.MEDIUM.getFont(8.0F).width(right.getName()), Fonts.MEDIUM.getFont(8.0F).width(left.getName())))
         .toList();
   }

   private ColorRGBA getLineColor(int index) {
      float pulse = 0.72F + 0.28F * (float)Math.sin(System.currentTimeMillis() * 0.003 + index * 0.45);
      return new ColorRGBA(
         Colors.getAccentColor().getRed() * pulse,
         Colors.getAccentColor().getGreen() * pulse,
         Colors.getAccentColor().getBlue() * pulse,
         Colors.getAccentColor().getAlpha()
      );
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
