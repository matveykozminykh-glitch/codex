package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.GameTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@ModuleInfo(name = "Trails", category = ModuleCategory.VISUALS, desc = "Шлейф за движущимися игроками")
public class Trails extends BaseModule {
   private final Map<UUID, Deque<Vec3d>> trails = new HashMap<>();
   private final ModeSetting style = new ModeSetting(this, "modules.settings.trails.style");
   private final ModeSetting.Value centered = new ModeSetting.Value(this.style, "CENTERED").select();
   private final ModeSetting.Value feet = new ModeSetting.Value(this.style, "FEET");
   private final ModeSetting.Value wings = new ModeSetting.Value(this.style, "WINGS");
   private final SliderSetting length = new SliderSetting(this, "modules.settings.trails.length").min(0.1F).max(2.0F).step(0.01F).currentValue(0.56F);
   private final SliderSetting opacity = new SliderSetting(this, "modules.settings.trails.opacity").min(0.0F).max(1.0F).step(0.01F).currentValue(1.0F);
   private final SliderSetting fadeStrength = new SliderSetting(this, "modules.settings.trails.fade_strength").min(0.0F).max(1.0F).step(0.01F).currentValue(0.93F);
   private final BooleanSetting lines = new BooleanSetting(this, "modules.settings.trails.lines").enable();
   private final SliderSetting lineWidth = new SliderSetting(this, "modules.settings.trails.line_width").min(1.0F).max(10.0F).step(0.1F).currentValue(3.0F);
   private final BooleanSetting onlyFriends = new BooleanSetting(this, "modules.settings.trails.only_friends");
   private final EventListener<GameTickEvent> onTick = event -> {
      if (mc.world == null || mc.player == null) {
         this.trails.clear();
         return;
      }

      int maxPoints = Math.max(4, (int)(this.length.getCurrentValue() * 40.0F));
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player == mc.player || !player.isAlive()) {
            continue;
         }

         if (this.onlyFriends.isEnabled() && !Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) {
            continue;
         }

         Deque<Vec3d> points = this.trails.computeIfAbsent(player.getUuid(), uuid -> new ArrayDeque<>());
         points.addFirst(this.anchor(player));
         while (points.size() > maxPoints) {
            points.removeLast();
         }
      }

      Iterator<Map.Entry<UUID, Deque<Vec3d>>> iterator = this.trails.entrySet().iterator();
      while (iterator.hasNext()) {
         Map.Entry<UUID, Deque<Vec3d>> entry = iterator.next();
         if (mc.world.getPlayerByUuid(entry.getKey()) == null) {
            iterator.remove();
         }
      }
   };
   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (mc.world == null || mc.player == null || this.trails.isEmpty()) {
         return;
      }

      Camera camera = event.getCamera();
      MatrixStack matrices = event.getMatrices();
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      if (this.lines.isEnabled()) {
         RenderSystem.lineWidth(this.lineWidth.getCurrentValue());
         BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         for (Deque<Vec3d> trail : this.trails.values()) {
            this.renderLineTrail(matrix, builder, camera, trail);
         }

         moscow.rockstar.utility.render.RenderUtility.buildBuffer(builder);
         RenderSystem.lineWidth(1.0F);
      } else {
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, moscow.rockstar.Rockstar.id("textures/bloom.png"));
         BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         for (Deque<Vec3d> trail : this.trails.values()) {
            this.renderSpriteTrail(matrices, builder, camera, trail);
         }

         moscow.rockstar.utility.render.RenderUtility.buildBuffer(builder);
         RenderSystem.setShaderTexture(0, 0);
      }

      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   };

   private void renderLineTrail(Matrix4f matrix, BufferBuilder builder, Camera camera, Deque<Vec3d> trail) {
      if (trail.size() < 2) {
         return;
      }

      List<Vec3d> points = new ArrayList<>();
      List<ColorRGBA> colors = new ArrayList<>();
      int index = 0;
      for (Vec3d pos : trail) {
         float alpha = this.opacity.getCurrentValue() * (float)Math.pow(this.fadeStrength.getCurrentValue(), index) * 255.0F;
         points.add(VisualRenderHelper.relative(pos, camera));
         colors.add(Colors.ACCENT.withAlpha(alpha));
         index++;
      }

      VisualRenderHelper.lineStrip(matrix, builder, points, colors);
   }

   private void renderSpriteTrail(MatrixStack matrices, BufferBuilder builder, Camera camera, Deque<Vec3d> trail) {
      int index = 0;
      for (Vec3d pos : trail) {
         float alpha = this.opacity.getCurrentValue() * (float)Math.pow(this.fadeStrength.getCurrentValue(), index) * 255.0F;
         float size = 0.12F + index * 0.005F;
         matrices.push();
         matrices.translate(pos.x - camera.getPos().x, pos.y - camera.getPos().y, pos.z - camera.getPos().z);
         matrices.multiply(camera.getRotation());
         moscow.rockstar.utility.render.DrawUtility.drawImage(
            matrices,
            builder,
            -size / 2.0,
            -size / 2.0,
            0.0,
            size,
            size,
            Colors.ACCENT.withAlpha(alpha)
         );
         matrices.pop();
         index++;
      }
   }

   private Vec3d anchor(PlayerEntity player) {
      if (this.feet.isSelected()) {
         return player.getPos().add(0.0, 0.05, 0.0);
      } else if (this.wings.isSelected()) {
         return player.getPos().add(0.0, player.getHeight() * 0.75F, 0.0);
      } else {
         return player.getPos().add(0.0, player.getHeight() * 0.5F, 0.0);
      }
   }

   @Override
   public void onDisable() {
      this.trails.clear();
      super.onDisable();
   }
}
