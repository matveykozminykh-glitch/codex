package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;

public class ScoreBoardHud extends HudList {
   public ScoreBoardHud() {
      super("hud.scoreboard", "icons/hud/world.png");
   }

   @Override
   public void update(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      this.width = 102.0F;
      this.height = 22.0F;

      for (String line : this.getScoreLines()) {
         this.width = Math.max(this.width, font.width(line) + 18.0F);
      }

      this.height += Math.max(1, this.getScoreLines().size()) * 13.0F;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      super.renderComponent(context);
      ScoreboardObjective objective = this.getObjective();
      float offset = 22.0F;
      if (objective == null) {
         context.drawCenteredText(font, "No scoreboard", this.x + this.width / 2.0F, this.y + offset + 1.0F, Colors.getTextColor().mulAlpha(0.55F));
         return;
      }

         context.drawText(Fonts.MEDIUM.getFont(7.0F), objective.getDisplayName().getString(), this.x + 7.0F, this.y + offset + 1.0F, Colors.getAccentColor());
      offset += 13.0F;
      for (String line : this.getScoreLines()) {
         context.drawText(font, line, this.x + 7.0F, this.y + offset + GuiUtility.getMiddleOfBox(font.height(), 10.0F), Colors.getTextColor());
         offset += 13.0F;
      }
   }

   private ScoreboardObjective getObjective() {
      return mc.player == null ? null : mc.player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
   }

   private List<String> getScoreLines() {
      List<String> lines = new ArrayList<>();
      ScoreboardObjective objective = this.getObjective();
      if (objective == null) {
         return lines;
      }

      Scoreboard scoreboard = objective.getScoreboard();
      for (ScoreHolder holder : scoreboard.getKnownScoreHolders()) {
         ReadableScoreboardScore score = scoreboard.getScore(holder, objective);
         if (score != null) {
            String name = holder.getDisplayName().getString();
            String value = ReadableScoreboardScore.getFormattedScore(score, objective.getNumberFormatOr(StyledNumberFormat.EMPTY)).getString();
            if (!name.isEmpty()) {
               lines.add(name + " " + value);
            }
         }
      }

      if (lines.size() > 8) {
         return lines.subList(0, 8);
      }

      return lines;
   }

   @Override
   public boolean show() {
      return mc.currentScreen instanceof ChatScreen || mc.player != null;
   }
}
