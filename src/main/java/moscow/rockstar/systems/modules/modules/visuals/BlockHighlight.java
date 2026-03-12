package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

@ModuleInfo(name = "Block Highlight", category = ModuleCategory.VISUALS, desc = "Подсветка блока под прицелом")
public class BlockHighlight extends BaseModule {
   private final BooleanSetting fullBlock = new BooleanSetting(this, "modules.settings.block_highlight.full_block").enable();
   private final BooleanSetting dashedOutline = new BooleanSetting(this, "modules.settings.block_highlight.dashed_outline");
   private final SliderSetting dashDensity = new SliderSetting(this, "modules.settings.block_highlight.dash_density").min(0.1F).max(5.0F).step(0.1F).currentValue(1.0F);
   private final BooleanSetting smoothTransition = new BooleanSetting(this, "modules.settings.block_highlight.smooth_transition").enable();
   private final SliderSetting animationSpeed = new SliderSetting(this, "modules.settings.block_highlight.animation_speed").min(1.0F).max(20.0F).step(0.1F).currentValue(8.0F);
   private final BooleanSetting stretchEffect = new BooleanSetting(this, "modules.settings.block_highlight.stretch_effect").enable();
   private final Animation appearAnimation = new Animation(220L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private Box currentBox;
   private BlockPos lastPos;
   private final EventListener<Render3DEvent> onRender3D = event -> {
      HitResult target = mc.crosshairTarget;
      boolean blockSelected = target instanceof BlockHitResult;
      this.appearAnimation.update(blockSelected);
      if (!blockSelected || !(target instanceof BlockHitResult blockHit)) {
         return;
      }

      Box targetBox = new Box(blockHit.getBlockPos());
      if (this.currentBox == null || this.lastPos == null || !this.lastPos.equals(blockHit.getBlockPos())) {
         this.lastPos = blockHit.getBlockPos();
         this.currentBox = targetBox;
         this.appearAnimation.reset(0.0F);
         this.appearAnimation.update(true);
      }

      if (this.smoothTransition.isEnabled()) {
         float speed = Math.min(1.0F, event.getTickDelta() * this.animationSpeed.getCurrentValue() * 0.15F);
         this.currentBox = this.lerpBox(this.currentBox, targetBox, speed);
      } else {
         this.currentBox = targetBox;
      }

      float scale = this.stretchEffect.isEnabled() ? 0.75F + this.appearAnimation.getValue() * 0.25F : 1.0F;
      Box box = VisualRenderHelper.scale(this.currentBox, scale);
      this.renderBox(event.getCamera(), event.getMatrices().peek().getPositionMatrix(), box);
   };

   private void renderBox(Camera camera, Matrix4f matrix, Box box) {
      ColorRGBA accent = Colors.ACCENT;
      Box relative = VisualRenderHelper.relative(box, camera);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      if (this.fullBlock.isEnabled()) {
         BufferBuilder fill = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         VisualRenderHelper.fillBox(matrix, fill, relative, accent.withAlpha(55.0F * this.appearAnimation.getValue()));
         moscow.rockstar.utility.render.RenderUtility.buildBuffer(fill);
      }

      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.lineWidth(2.0F);
      BufferBuilder outline = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      VisualRenderHelper.outlineBox(matrix, outline, relative, accent.withAlpha(255.0F * this.appearAnimation.getValue()), this.dashedOutline.isEnabled(), this.dashDensity.getCurrentValue());
      moscow.rockstar.utility.render.RenderUtility.buildBuffer(outline);
      RenderSystem.lineWidth(1.0F);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   private Box lerpBox(Box from, Box to, float delta) {
      return new Box(
         from.minX + (to.minX - from.minX) * delta,
         from.minY + (to.minY - from.minY) * delta,
         from.minZ + (to.minZ - from.minZ) * delta,
         from.maxX + (to.maxX - from.maxX) * delta,
         from.maxY + (to.maxY - from.maxY) * delta,
         from.maxZ + (to.maxZ - from.maxZ) * delta
      );
   }
}
