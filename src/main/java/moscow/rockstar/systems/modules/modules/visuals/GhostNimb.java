package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Ghost Nimb", category = ModuleCategory.VISUALS, desc = "Ореол над игроками")
public class GhostNimb extends BaseModule {
   private final SliderSetting length = new SliderSetting(this, "modules.settings.ghost_nimb.length").min(1.0F).max(50.0F).step(1.0F).currentValue(32.0F);
   private final SliderSetting radius = new SliderSetting(this, "modules.settings.ghost_nimb.radius").min(10.0F).max(200.0F).step(1.0F).currentValue(88.0F);
   private final SliderSetting startSize = new SliderSetting(this, "modules.settings.ghost_nimb.start_size").min(0.1F).max(5.0F).step(0.1F).currentValue(1.0F);
   private final SliderSetting endSize = new SliderSetting(this, "modules.settings.ghost_nimb.end_size").min(1.0F).max(30.0F).step(0.1F).currentValue(15.0F);
   private final SliderSetting yOffset = new SliderSetting(this, "modules.settings.ghost_nimb.y_offset").min(0.0F).max(3.0F).step(0.01F).currentValue(1.15F);
   private final SliderSetting animationSpeed = new SliderSetting(this, "modules.settings.ghost_nimb.animation_speed").min(0.1F).max(3.0F).step(0.01F).currentValue(0.53F);
   private final SliderSetting brightness = new SliderSetting(this, "modules.settings.ghost_nimb.brightness").min(0.0F).max(100.0F).step(1.0F).currentValue(0.0F);
   private final BooleanSetting onlyFriends = new BooleanSetting(this, "modules.settings.ghost_nimb.only_friends");
   private final EventListener<Render3DEvent> onRender3D = event -> {
      Camera camera = event.getCamera();
      MatrixStack matrices = event.getMatrices();
      RenderUtility.setupRender3D(true);
      RenderSystem.setShaderTexture(0, Rockstar.id("textures/bloom.png"));
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player == mc.player || !player.isAlive()) {
            continue;
         }

         if (this.onlyFriends.isEnabled() && !Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) {
            continue;
         }

         this.renderPlayerNimb(matrices, builder, camera, player, event.getTickDelta());
      }

      RenderUtility.buildBuffer(builder);
      RenderSystem.setShaderTexture(0, 0);
      RenderUtility.endRender3D();
   };

   private void renderPlayerNimb(MatrixStack matrices, BufferBuilder builder, Camera camera, PlayerEntity player, float tickDelta) {
      Vec3d base = VisualRenderHelper.interpolated(player, tickDelta).add(0.0, player.getHeight() + this.yOffset.getCurrentValue(), 0.0);
      int count = (int)this.length.getCurrentValue();
      double radiusValue = this.radius.getCurrentValue() * 0.01F;
      double time = System.currentTimeMillis() / 1000.0 * this.animationSpeed.getCurrentValue();

      for (int i = 0; i < count; i++) {
         float progress = count <= 1 ? 1.0F : (float)i / (float)(count - 1);
         double angle = time + progress * Math.PI * 2.0;
         double yWave = Math.sin(angle * 1.5 + progress * Math.PI) * 0.12;
         Vec3d pos = base.add(Math.cos(angle) * radiusValue, yWave + progress * 0.12, Math.sin(angle) * radiusValue);
         float size = this.startSize.getCurrentValue() + (this.endSize.getCurrentValue() - this.startSize.getCurrentValue()) * progress;
         float finalSize = size * 0.03F;
         float alpha = 120.0F + this.brightness.getCurrentValue() * 1.35F;
         ColorRGBA color = Colors.ACCENT.withAlpha(alpha * (1.0F - progress * 0.55F));
         matrices.push();
         matrices.translate(pos.x - camera.getPos().x, pos.y - camera.getPos().y, pos.z - camera.getPos().z);
         matrices.multiply(camera.getRotation());
         DrawUtility.drawImage(matrices, builder, -finalSize / 2.0, -finalSize / 2.0, 0.0, finalSize, finalSize, color);
         matrices.pop();
      }
   }
}
