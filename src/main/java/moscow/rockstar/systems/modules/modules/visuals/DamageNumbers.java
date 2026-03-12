package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.GameTickEvent;
import moscow.rockstar.systems.event.impl.game.PostAttackEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Damage Numbers", category = ModuleCategory.VISUALS, desc = "Цифры урона над сущностью")
public class DamageNumbers extends BaseModule {
   private final Map<Integer, PendingDamage> pendingDamages = new HashMap<>();
   private final Map<Integer, Integer> hurtTimeTracker = new HashMap<>();
   private final List<FloatingText> floatingTexts = new ArrayList<>();
   private final SliderSetting renderTime = new SliderSetting(this, "modules.settings.damage_numbers.render_time").min(0.5F).max(5.0F).step(0.1F).currentValue(3.0F);
   private final SliderSetting scale = new SliderSetting(this, "modules.settings.damage_numbers.scale").min(0.5F).max(3.0F).step(0.1F).currentValue(1.0F);
   private final SliderSetting speed = new SliderSetting(this, "modules.settings.damage_numbers.speed").min(0.5F).max(5.0F).step(0.01F).currentValue(1.18F);
   private final ModeSetting spawnPosition = new ModeSetting(this, "modules.settings.damage_numbers.spawn_position");
   private final ModeSetting.Value body = new ModeSetting.Value(this.spawnPosition, "Body").select();
   private final ModeSetting.Value head = new ModeSetting.Value(this.spawnPosition, "Head");
   private final ModeSetting.Value random = new ModeSetting.Value(this.spawnPosition, "Random");
   private final ModeSetting colorMode = new ModeSetting(this, "modules.settings.damage_numbers.color_mode");
   private final ModeSetting.Value customColorMode = new ModeSetting.Value(this.colorMode, "Custom Color").select();
   private final ModeSetting.Value healthBased = new ModeSetting.Value(this.colorMode, "Health Based");
   private final ModeSetting.Value clientColor = new ModeSetting.Value(this.colorMode, "Client Color");
   private final ColorSetting customColor = new ColorSetting(this, "modules.settings.damage_numbers.custom_color").color(Colors.RED);
   private final BooleanSetting critOnly = new BooleanSetting(this, "modules.settings.damage_numbers.crit_only");
   private final EventListener<PostAttackEvent> onAttack = event -> {
      if (!EntityUtility.isInGame() || !(event.getEntity() instanceof LivingEntity living) || !living.isAlive()) {
         return;
      }

      boolean critical = CombatUtility.canPerformCriticalHit(living, true);
      if (this.critOnly.isEnabled() && !critical) {
         return;
      }

      this.pendingDamages.put(living.getId(), new PendingDamage(living, this.currentHealth(living), critical));
   };
   private final EventListener<GameTickEvent> onTick = event -> {
      if (EntityUtility.isInGame()) {
         for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity living && living.isAlive()) {
               int currentHurt = living.hurtTime;
               int previousHurt = this.hurtTimeTracker.getOrDefault(living.getId(), 0);
               if (currentHurt > previousHurt) {
                  boolean critical = CombatUtility.canPerformCriticalHit(living, true);
                  if (!this.critOnly.isEnabled() || critical) {
                     this.pendingDamages.putIfAbsent(living.getId(), new PendingDamage(living, this.currentHealth(living), critical));
                  }
               }

               this.hurtTimeTracker.put(living.getId(), currentHurt);
            }
         }
      } else {
         this.hurtTimeTracker.clear();
      }

      Iterator<PendingDamage> iterator = this.pendingDamages.values().iterator();
      while (iterator.hasNext()) {
         PendingDamage pending = iterator.next();
         if (!pending.entity.isAlive()) {
            iterator.remove();
            continue;
         }

         float currentHealth = this.currentHealth(pending.entity);
         float damage = pending.previousHealth - currentHealth;
         if (damage > 0.0F) {
            this.floatingTexts.add(new FloatingText(this.spawn(pending.entity), damage, this.colorFor(pending.entity), pending.critical, (long)(this.renderTime.getCurrentValue() * 1000.0F)));
            iterator.remove();
         } else if (System.currentTimeMillis() - pending.time > 450L) {
            iterator.remove();
         }
      }
   };
   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (this.floatingTexts.isEmpty()) {
         return;
      }

      Camera camera = event.getCamera();
      MatrixStack matrices = event.getMatrices();
      Iterator<FloatingText> iterator = this.floatingTexts.iterator();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();

      while (iterator.hasNext()) {
         FloatingText text = iterator.next();
         if (text.isDead()) {
            iterator.remove();
         } else {
            this.renderText(matrices, camera, text);
         }
      }

      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   };

   private void renderText(MatrixStack matrices, Camera camera, FloatingText text) {
      float progress = text.progress();
      float textScale = 0.025F * this.scale.getCurrentValue();
      Vec3d pos = text.position.add(0.0, progress * this.speed.getCurrentValue() * 0.45F, 0.0);
      matrices.push();
      matrices.translate(pos.x - camera.getPos().x, pos.y - camera.getPos().y, pos.z - camera.getPos().z);
      matrices.multiply(camera.getRotation());
      matrices.scale(-textScale, -textScale, textScale);
      String value = (text.critical ? "*" : "") + String.format("%.1f", text.damage);
      float width = Fonts.SEMIBOLD.getWidth(value, 9.0F);
      ColorRGBA color = text.color.withAlpha(255.0F * (1.0F - progress));
      MsdfRenderer.renderText(Fonts.SEMIBOLD, value, 9.0F, color.getRGB(), matrices.peek().getPositionMatrix(), -width / 2.0F, 0.0F, 0.0F);
      matrices.pop();
   }

   private Vec3d spawn(LivingEntity entity) {
      Vec3d interpolated = VisualRenderHelper.interpolated(entity, mc.getRenderTickCounter().getTickDelta(false));
      double y = this.body.isSelected() ? entity.getHeight() * 0.55F : entity.getHeight() + 0.2F;
      if (this.random.isSelected()) {
         return interpolated.add((Math.random() - 0.5) * entity.getWidth(), entity.getHeight() * (0.35 + Math.random() * 0.65), (Math.random() - 0.5) * entity.getWidth());
      }

      return interpolated.add(0.0, y, 0.0);
   }

   private ColorRGBA colorFor(LivingEntity entity) {
      if (this.clientColor.isSelected()) {
         return Colors.ACCENT;
      } else if (this.healthBased.isSelected()) {
         float percent = this.currentHealth(entity) / Math.max(1.0F, entity.getMaxHealth());
         return percent > 0.65F ? Colors.GREEN : (percent > 0.35F ? new ColorRGBA(255.0F, 215.0F, 0.0F) : Colors.RED);
      } else {
         return this.customColor.getColor();
      }
   }

   private float currentHealth(LivingEntity entity) {
      return entity instanceof PlayerEntity player ? EntityUtility.getHealth(player) : entity.getHealth() + entity.getAbsorptionAmount();
   }

   @Override
   public void onDisable() {
      this.pendingDamages.clear();
      this.hurtTimeTracker.clear();
      this.floatingTexts.clear();
      super.onDisable();
   }

   private static final class PendingDamage {
      private final LivingEntity entity;
      private final float previousHealth;
      private final boolean critical;
      private final long time = System.currentTimeMillis();

      private PendingDamage(LivingEntity entity, float previousHealth, boolean critical) {
         this.entity = entity;
         this.previousHealth = previousHealth;
         this.critical = critical;
      }
   }

   private static final class FloatingText {
      private final Vec3d position;
      private final float damage;
      private final ColorRGBA color;
      private final boolean critical;
      private final long lifeTime;
      private final long spawnTime = System.currentTimeMillis();

      private FloatingText(Vec3d position, float damage, ColorRGBA color, boolean critical, long lifeTime) {
         this.position = position;
         this.damage = damage;
         this.color = color;
         this.critical = critical;
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
