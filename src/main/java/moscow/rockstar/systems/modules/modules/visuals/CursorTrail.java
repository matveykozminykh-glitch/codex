package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.Identifier;

@ModuleInfo(name = "Cursor Trail", category = ModuleCategory.VISUALS, desc = "Частицы за курсором в GUI")
public class CursorTrail extends BaseModule {
   private final ModeSetting style = new ModeSetting(this, "modules.settings.cursor_trail.style");
   private final ModeSetting.Value particles = new ModeSetting.Value(this.style, "Particles").select();
   private final SliderSetting lifetime = new SliderSetting(this, "modules.settings.cursor_trail.lifetime").min(0.1F).max(2.0F).step(0.05F).currentValue(0.3F);
   private final SliderSetting size = new SliderSetting(this, "modules.settings.cursor_trail.size").min(1.0F).max(12.0F).step(0.1F).currentValue(4.0F);
   private final SliderSetting spread = new SliderSetting(this, "modules.settings.cursor_trail.spread").min(0.0F).max(10.0F).step(0.1F).currentValue(5.0F);
   private final List<Particle> particlesList = new CopyOnWriteArrayList<>();
   private Vector2f lastMouse;
   private final EventListener<ScreenRenderEvent> onScreenRender = event -> {
      if (mc.currentScreen == null || this.style.getValue() == null) {
         this.particlesList.clear();
         this.lastMouse = null;
         return;
      }

      Vector2f mouse = GuiUtility.getMouse();
      if (this.lastMouse == null) {
         this.lastMouse = mouse;
      }

      float dx = mouse.getX() - this.lastMouse.getX();
      float dy = mouse.getY() - this.lastMouse.getY();
      float distance = (float)Math.hypot(dx, dy);
      int spawnCount = Math.max(1, Math.min(4, (int)(distance / 6.0F) + 1));
      for (int i = 0; i < spawnCount; i++) {
         float progress = spawnCount == 1 ? 1.0F : i / (float)(spawnCount - 1);
         float px = this.lastMouse.getX() + dx * progress;
         float py = this.lastMouse.getY() + dy * progress;
         this.particlesList.add(new Particle(px, py, (float)(Math.random() - 0.5) * this.spread.getCurrentValue() * 0.2F, (float)(Math.random() - 0.5) * this.spread.getCurrentValue() * 0.2F));
      }

      this.lastMouse = mouse;
      Identifier texture = Rockstar.id("textures/bloom.png");
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      for (Particle particle : this.particlesList) {
         float life = particle.life.update(1.0F);
         float progress = Math.min(1.0F, (System.currentTimeMillis() - particle.createdAt) / (this.lifetime.getCurrentValue() * 1000.0F));
         particle.x += particle.motionX;
         particle.y += particle.motionY;
         float alpha = 1.0F - progress;
         float drawSize = this.size.getCurrentValue() * (0.6F + 0.4F * life);
         event.getContext().drawTexture(texture, particle.x - drawSize / 2.0F, particle.y - drawSize / 2.0F, drawSize, drawSize, Colors.getAccentColor().withAlpha(255.0F * alpha));
      }

      RenderSystem.disableBlend();
      this.particlesList.removeIf(particle -> System.currentTimeMillis() - particle.createdAt > this.lifetime.getCurrentValue() * 1000.0F);
   };

   private static class Particle {
      private float x;
      private float y;
      private final float motionX;
      private final float motionY;
      private final long createdAt = System.currentTimeMillis();
      private final Animation life = new Animation(220L, 0.0F, Easing.FIGMA_EASE_IN_OUT);

      private Particle(float x, float y, float motionX, float motionY) {
         this.x = x;
         this.y = y;
         this.motionX = motionX;
         this.motionY = motionY;
      }
   }
}
