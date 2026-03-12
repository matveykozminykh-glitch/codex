package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@ModuleInfo(name = "Jump Circles", category = ModuleCategory.VISUALS, desc = "Круги на земле при прыжке")
public class JumpCircles extends BaseModule {
   private final List<CircleEffect> circles = new ArrayList<>();
   private final ModeSetting style = new ModeSetting(this, "modules.settings.jump_circles.style");
   private final ModeSetting.Value circle = new ModeSetting.Value(this.style, "CIRCLE").select();
   private final ModeSetting.Value square = new ModeSetting.Value(this.style, "SQUARE");
   private final ModeSetting.Value star = new ModeSetting.Value(this.style, "STAR");
   private final ModeSetting animation = new ModeSetting(this, "modules.settings.jump_circles.animation");
   private final ModeSetting.Value bounce = new ModeSetting.Value(this.animation, "Bounce").select();
   private final ModeSetting.Value fade = new ModeSetting.Value(this.animation, "Fade");
   private final ModeSetting.Value expand = new ModeSetting.Value(this.animation, "Expand");
   private final SliderSetting liveTime = new SliderSetting(this, "modules.settings.jump_circles.live_time").min(0.1F).max(3.0F).step(0.1F).currentValue(1.0F);
   private final BooleanSetting onlyFriends = new BooleanSetting(this, "modules.settings.jump_circles.only_friends");
   private final SliderSetting scale = new SliderSetting(this, "modules.settings.jump_circles.scale").min(1.0F).max(100.0F).step(1.0F).currentValue(60.0F);
   private final SliderSetting spinSpeed = new SliderSetting(this, "modules.settings.jump_circles.spin_speed").min(0.0F).max(10.0F).step(0.1F).currentValue(3.0F);
   private final BooleanSetting brighterGlow = new BooleanSetting(this, "modules.settings.jump_circles.brighter_glow");
   private final EventListener<EntityJumpEvent> onJump = event -> {
      if (!EntityUtility.isInGame()) {
         return;
      }

      LivingEntity entity = event.getEntity();
      if (entity instanceof PlayerEntity player && this.onlyFriends.isEnabled() && player != mc.player
         && !Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) {
         return;
      }

      Vec3d position = new Vec3d(entity.getX(), entity.getY() + 0.02, entity.getZ());
      this.circles.add(new CircleEffect(position, (long)(this.liveTime.getCurrentValue() * 1000.0F)));
   };
   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (this.circles.isEmpty()) {
         return;
      }

      Camera camera = event.getCamera();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.lineWidth(this.brighterGlow.isEnabled() ? 2.5F : 1.5F);
      BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      Matrix4f matrix = event.getMatrices().peek().getPositionMatrix();
      Iterator<CircleEffect> iterator = this.circles.iterator();

      while (iterator.hasNext()) {
         CircleEffect effect = iterator.next();
         if (effect.isDead()) {
            iterator.remove();
         } else {
            this.renderCircle(matrix, builder, camera, effect);
         }
      }

      RenderSystem.lineWidth(1.0F);
      moscow.rockstar.utility.render.RenderUtility.buildBuffer(builder);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   };

   private void renderCircle(Matrix4f matrix, BufferBuilder builder, Camera camera, CircleEffect effect) {
      float progress = effect.progress();
      float baseRadius = this.scale.getCurrentValue() * 0.01F;
      float radius = this.expand.isSelected() ? baseRadius * progress : baseRadius * (0.55F + progress * 0.45F);
      if (this.bounce.isSelected()) {
         radius *= 0.85F + (float)Math.sin(progress * Math.PI) * 0.25F;
      }

      float alpha = this.fade.isSelected() ? 1.0F - progress : 1.0F - progress * 0.75F;
      double spin = progress * this.spinSpeed.getCurrentValue() * Math.PI * 2.0;
      List<Vec3d> points = new ArrayList<>();
      if (this.circle.isSelected()) {
         for (int i = 0; i < 32; i++) {
            double angle = spin + Math.PI * 2.0 * i / 32.0;
            points.add(VisualRenderHelper.relative(effect.position.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius), camera));
         }
      } else if (this.square.isSelected()) {
         points.add(VisualRenderHelper.relative(effect.position.add(-radius, 0.0, -radius), camera));
         points.add(VisualRenderHelper.relative(effect.position.add(radius, 0.0, -radius), camera));
         points.add(VisualRenderHelper.relative(effect.position.add(radius, 0.0, radius), camera));
         points.add(VisualRenderHelper.relative(effect.position.add(-radius, 0.0, radius), camera));
      } else {
         for (int i = 0; i < 10; i++) {
            double angle = spin + Math.PI / 5.0 * i;
            double pointRadius = i % 2 == 0 ? radius : radius * 0.45F;
            points.add(VisualRenderHelper.relative(effect.position.add(Math.cos(angle) * pointRadius, 0.0, Math.sin(angle) * pointRadius), camera));
         }
      }

      ColorRGBA color = Colors.ACCENT.withAlpha(255.0F * alpha);
      VisualRenderHelper.lineLoop(matrix, builder, points, color);
      if (this.brighterGlow.isEnabled()) {
         ColorRGBA glow = color.withAlpha(color.getAlpha() * 0.35F);
         List<Vec3d> enlarged = new ArrayList<>();
         for (Vec3d point : points) {
            enlarged.add(new Vec3d(point.x * 1.03, point.y + 0.003, point.z * 1.03));
         }

         VisualRenderHelper.lineLoop(matrix, builder, enlarged, glow);
      }
   }

   @Override
   public void onDisable() {
      this.circles.clear();
      super.onDisable();
   }

   private static final class CircleEffect {
      private final Vec3d position;
      private final long lifeTime;
      private final long spawnTime = System.currentTimeMillis();

      private CircleEffect(Vec3d position, long lifeTime) {
         this.position = position;
         this.lifeTime = lifeTime;
      }

      private float progress() {
         return Math.min(1.0F, (float)(System.currentTimeMillis() - this.spawnTime) / (float)this.lifeTime);
      }

      private boolean isDead() {
         return System.currentTimeMillis() - this.spawnTime > this.lifeTime;
      }
   }
}
