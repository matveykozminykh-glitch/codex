package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.GameTickEvent;
import moscow.rockstar.systems.event.impl.game.PostAttackEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Hit Particles", category = ModuleCategory.VISUALS, desc = "modules.descriptions.hit_particles")
public class HitParticles extends BaseModule {
   private static final float SPEED_MULTIPLIER = 0.045F;
   private static final float GRAVITY = 0.035F;
   private final List<HitParticle> particles = new ArrayList<>();
   private final Map<Integer, Integer> hurtTimeTracker = new HashMap<>();
   private final SliderSetting count = new SliderSetting(this, "modules.settings.hit_particles.count").min(1.0F).max(30.0F).step(1.0F).currentValue(10.0F);
   private final SliderSetting speed = new SliderSetting(this, "modules.settings.hit_particles.speed").min(1.0F).max(20.0F).step(0.1F).currentValue(6.0F);
   private final SliderSetting renderTime = new SliderSetting(this, "modules.settings.hit_particles.render_time")
      .min(0.1F)
      .max(5.0F)
      .step(0.1F)
      .currentValue(2.0F);
   private final SliderSetting scale = new SliderSetting(this, "modules.settings.hit_particles.scale").min(1.0F).max(20.0F).step(0.1F).currentValue(6.0F);
   private final ModeSetting texture = new ModeSetting(this, "modules.settings.hit_particles.texture");
   private final ModeSetting.Value bloom = new ModeSetting.Value(this.texture, "Bloom").select();
   private final ModeSetting.Value star = new ModeSetting.Value(this.texture, "Star");
   private final ModeSetting.Value heart = new ModeSetting.Value(this.texture, "Heart");
   private final ModeSetting.Value point = new ModeSetting.Value(this.texture, "Point");
   private final ModeSetting.Value snowflake = new ModeSetting.Value(this.texture, "Snowflake");
   private final BooleanSetting critOnly = new BooleanSetting(this, "modules.settings.hit_particles.crit_only");
   private final ModeSetting physic = new ModeSetting(this, "modules.settings.hit_particles.physic");
   private final ModeSetting.Value fly = new ModeSetting.Value(this.physic, "FLY").select();
   private final ModeSetting.Value bounce = new ModeSetting.Value(this.physic, "BOUNCE");
   private final ModeSetting.Value floatMode = new ModeSetting.Value(this.physic, "FLOAT");
   private final ModeSetting disappear = new ModeSetting(this, "modules.settings.hit_particles.disappear");
   private final ModeSetting.Value fadeOut = new ModeSetting.Value(this.disappear, "FADE_OUT").select();
   private final ModeSetting.Value scaleDisappear = new ModeSetting.Value(this.disappear, "SCALE");
   private final ModeSetting.Value alphaDisappear = new ModeSetting.Value(this.disappear, "ALPHA");
   private final EventListener<PostAttackEvent> onPostAttack = event -> {
      if (!EntityUtility.isInGame() || !(event.getEntity() instanceof LivingEntity target) || !target.isAlive()) {
         return;
      }

      if (this.critOnly.isEnabled() && !CombatUtility.canPerformCriticalHit(target, true)) {
         return;
      }

      this.spawnParticles(target);
   };
   private final EventListener<GameTickEvent> onTick = event -> {
      if (!EntityUtility.isInGame()) {
         this.hurtTimeTracker.clear();
         return;
      }

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof LivingEntity living && living.isAlive()) {
            int currentHurt = living.hurtTime;
            int previousHurt = this.hurtTimeTracker.getOrDefault(living.getId(), 0);
            if (currentHurt > previousHurt && (!this.critOnly.isEnabled() || CombatUtility.canPerformCriticalHit(living, true))) {
               this.spawnParticles(living);
            }

            this.hurtTimeTracker.put(living.getId(), currentHurt);
         }
      }
   };
   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (this.particles.isEmpty() || !EntityUtility.isInGame()) {
         return;
      }

      MatrixStack matrices = event.getMatrices();
      Camera camera = event.getCamera();
      Identifier textureId = this.resolveTexture();
      BufferBuilder builder = null;
      RenderUtility.setupRender3D(true);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      RenderSystem.setShaderTexture(0, textureId);
      builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      try {
         Iterator<HitParticle> iterator = this.particles.iterator();

         while (iterator.hasNext()) {
            HitParticle particle = iterator.next();
            particle.update();
            if (particle.isDead()) {
               iterator.remove();
            } else {
               this.renderParticle(matrices, builder, camera, particle);
            }
         }
      } finally {
         if (builder != null) {
            RenderUtility.buildBuffer(builder);
         }

         RenderSystem.setShaderTexture(0, 0);
         RenderUtility.endRender3D();
      }
   };

   private void spawnParticles(LivingEntity target) {
      Vec3d center = Utils.getInterpolatedPos(target, mc.getRenderTickCounter().getTickDelta(false))
         .add(0.0, target.getHeight() * 0.55F, 0.0);
      int particleCount = (int)this.count.getCurrentValue();
      long lifeTime = (long)(this.renderTime.getCurrentValue() * 1000.0F);
      float configuredSpeed = this.speed.getCurrentValue() * SPEED_MULTIPLIER;
      PhysicsType physicsType = this.getPhysicsType();
      DisappearType disappearType = this.getDisappearType();
      ColorRGBA color = Colors.ACCENT;

      for (int i = 0; i < particleCount; i++) {
         Vec3d spawnPos = center.add(
            MathUtility.random(-0.35, 0.35),
            MathUtility.random(-0.25, 0.35),
            MathUtility.random(-0.35, 0.35)
         );
         Vec3d direction = new Vec3d(MathUtility.random(-1.0, 1.0), MathUtility.random(0.15, 1.0), MathUtility.random(-1.0, 1.0));
         if (direction.lengthSquared() < 1.0E-4) {
            direction = new Vec3d(0.0, 1.0, 0.0);
         }

         direction = direction.normalize().multiply(configuredSpeed * MathUtility.random(0.75, 1.2));
         this.particles.add(new HitParticle(spawnPos, direction, color, lifeTime, physicsType, disappearType));
      }
   }

   private void renderParticle(MatrixStack matrices, BufferBuilder builder, Camera camera, HitParticle particle) {
      float progress = particle.getProgress();
      float alphaFactor = switch (particle.disappearType) {
         case FADE_OUT -> 1.0F - progress;
         case SCALE -> 1.0F - progress * 0.65F;
         case ALPHA -> 1.0F - progress * progress;
      };
      float sizeFactor = switch (particle.disappearType) {
         case FADE_OUT -> 1.0F;
         case SCALE -> 1.0F - progress;
         case ALPHA -> 0.8F + (1.0F - progress) * 0.2F;
      };
      float size = this.scale.getCurrentValue() * 0.035F * Math.max(0.05F, sizeFactor);
      ColorRGBA color = particle.color.withAlpha(particle.color.getAlpha() * Math.max(0.0F, alphaFactor));
      matrices.push();
      RenderUtility.prepareMatrices(matrices, particle.position);
      matrices.multiply(camera.getRotation());
      DrawUtility.drawImage(
         matrices,
         builder,
         (double)(-size / 2.0F),
         (double)(-size / 2.0F),
         0.0,
         (double)size,
         (double)size,
         color
      );
      matrices.pop();
   }

   private PhysicsType getPhysicsType() {
      if (this.bounce.isSelected()) {
         return PhysicsType.BOUNCE;
      } else {
         return this.floatMode.isSelected() ? PhysicsType.FLOAT : PhysicsType.FLY;
      }
   }

   private DisappearType getDisappearType() {
      if (this.scaleDisappear.isSelected()) {
         return DisappearType.SCALE;
      } else {
         return this.alphaDisappear.isSelected() ? DisappearType.ALPHA : DisappearType.FADE_OUT;
      }
   }

   private Identifier resolveTexture() {
      if (this.star.isSelected()) {
         return Rockstar.id("textures/star.png");
      } else if (this.heart.isSelected()) {
         return Rockstar.id("textures/heart.png");
      } else if (this.point.isSelected()) {
         return Rockstar.id("textures/point.png");
      } else if (this.snowflake.isSelected()) {
         return Rockstar.id("textures/snowflake.png");
      } else if (this.bloom.isSelected()) {
         return Rockstar.id("textures/bloom.png");
      } else {
         return Rockstar.id("textures/bloom.png");
      }
   }

   @Override
   public void onDisable() {
      this.particles.clear();
      this.hurtTimeTracker.clear();
      super.onDisable();
   }

   private enum DisappearType {
      FADE_OUT,
      SCALE,
      ALPHA
   }

   private enum PhysicsType {
      FLY,
      BOUNCE,
      FLOAT
   }

   private static final class HitParticle {
      private Vec3d position;
      private Vec3d velocity;
      private final Vec3d floorPos;
      private final ColorRGBA color;
      private final long spawnTime;
      private final long lifeTime;
      private final PhysicsType physicsType;
      private final DisappearType disappearType;
      private long lastUpdateTime;

      private HitParticle(Vec3d position, Vec3d velocity, ColorRGBA color, long lifeTime, PhysicsType physicsType, DisappearType disappearType) {
         this.position = position;
         this.velocity = velocity;
         this.floorPos = position.subtract(0.0, MathUtility.random(0.25, 0.6), 0.0);
         this.color = color;
         this.lifeTime = lifeTime;
         this.physicsType = physicsType;
         this.disappearType = disappearType;
         this.spawnTime = System.currentTimeMillis();
         this.lastUpdateTime = this.spawnTime;
      }

      private void update() {
         long currentTime = System.currentTimeMillis();
         float delta = Math.max(0.001F, (currentTime - this.lastUpdateTime) / 50.0F);
         this.lastUpdateTime = currentTime;
         switch (this.physicsType) {
            case FLOAT -> {
               this.position = this.position.add(this.velocity.x * delta, this.velocity.y * delta * 0.45F, this.velocity.z * delta);
               this.velocity = new Vec3d(this.velocity.x * 0.96F, this.velocity.y * 0.92F + 0.0025F * delta, this.velocity.z * 0.96F);
            }
            case BOUNCE -> {
               this.position = this.position.add(this.velocity.x * delta, this.velocity.y * delta, this.velocity.z * delta);
               this.velocity = this.velocity.add(0.0, -GRAVITY * delta, 0.0);
               if (this.position.y <= this.floorPos.y) {
                  this.position = new Vec3d(this.position.x, this.floorPos.y, this.position.z);
                  this.velocity = new Vec3d(this.velocity.x * 0.72F, Math.abs(this.velocity.y) * 0.58F, this.velocity.z * 0.72F);
               }
            }
            case FLY -> {
               this.position = this.position.add(this.velocity.x * delta, this.velocity.y * delta, this.velocity.z * delta);
               this.velocity = this.velocity.add(0.0, -GRAVITY * 0.45F * delta, 0.0).multiply(0.985F);
            }
         }
      }

      private float getProgress() {
         return MathHelper.clamp((float)(System.currentTimeMillis() - this.spawnTime) / (float)this.lifeTime, 0.0F, 1.0F);
      }

      private boolean isDead() {
         return System.currentTimeMillis() - this.spawnTime >= this.lifeTime;
      }
   }
}
