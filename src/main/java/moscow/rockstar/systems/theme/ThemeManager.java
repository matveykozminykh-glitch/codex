package moscow.rockstar.systems.theme;

import lombok.Generated;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;

public class ThemeManager {
   private Theme currentTheme = Theme.DARK;
   private ColorRGBA accentColor = Colors.ACCENT;

   public void switchTheme() {
      this.currentTheme = this.currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK;
   }

   public Theme getCurrentTheme() {
      return Interface.glassSelected() ? Theme.DARK : this.currentTheme;
   }

   public ColorRGBA getAccentColor() {
      return this.accentColor;
   }

   @Generated
   public void setCurrentTheme(Theme currentTheme) {
      this.currentTheme = currentTheme;
   }

   @Generated
   public void setAccentColor(ColorRGBA accentColor) {
      this.accentColor = accentColor;
   }
}
