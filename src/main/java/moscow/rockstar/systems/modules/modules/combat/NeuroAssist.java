package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

@ModuleInfo(name = "Neuro Assist", category = ModuleCategory.COMBAT, desc = "Ассист атаки без автоповорота камеры")
public class NeuroAssist extends BaseModule {
   private final SliderSetting attackRange = new SliderSetting(this, "attack_range").min(1.0F).max(6.0F).step(0.1F).currentValue(3.1F);
   private final SliderSetting aimRange = new SliderSetting(this, "aim_range").min(1.0F).max(8.0F).step(0.1F).currentValue(4.2F);
   private final SliderSetting fovLimit = new SliderSetting(this, "fov_limit").min(10.0F).max(180.0F).step(1.0F).currentValue(85.0F);
   private final BooleanSetting onlyCriticals = new BooleanSetting(this, "only_crits");
   private final BooleanSetting onlyWeapon = new BooleanSetting(this, "only_weapon").enable();
   private final BooleanSetting targetFriends = new BooleanSetting(this, "target_friends");
   private final SelectSetting targets = new SelectSetting(this, "targets");
   private final SelectSetting.Value players = new SelectSetting.Value(this.targets, "players").select();
   private final SelectSetting.Value mobs = new SelectSetting.Value(this.targets, "mobs").select();
   private final SelectSetting.Value animals = new SelectSetting.Value(this.targets, "animals");
   private final SelectSetting.Value invisibles = new SelectSetting.Value(this.targets, "invisibles");
   private final Timer attackTimer = new Timer();
   private LivingEntity currentTarget;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (!EntityUtility.isInGame() || mc.player == null || mc.world == null) {
         return;
      }

      TargetSettings settings = new TargetSettings.Builder()
         .targetPlayers(this.players.isSelected())
         .targetMobs(this.mobs.isSelected())
         .targetAnimals(this.animals.isSelected())
         .targetInvisibles(this.invisibles.isSelected())
         .targetFriends(this.targetFriends.isEnabled())
         .requiredRange(this.aimRange.getCurrentValue())
         .sortBy(TargetComparators.FOV)
         .build();
      Rockstar.getInstance().getTargetManager().update(settings);
      this.currentTarget = Rockstar.getInstance().getTargetManager().getCurrentTarget() instanceof LivingEntity living ? living : null;
      if (this.currentTarget == null || !this.currentTarget.isAlive()) {
         return;
      }

      if (this.canAttack(this.currentTarget)) {
         mc.interactionManager.attackEntity(mc.player, this.currentTarget);
         mc.player.swingHand(Hand.MAIN_HAND);
         this.attackTimer.reset();
      }
   };

   private boolean canAttack(LivingEntity target) {
      if (mc.interactionManager == null || mc.player == null) {
         return false;
      }

      if (this.onlyWeapon.isEnabled() && !this.hasCombatWeapon()) {
         return false;
      }

      if (mc.player.distanceTo(target) > this.attackRange.getCurrentValue()) {
         return false;
      }

      if (this.onlyCriticals.isEnabled() && !CombatUtility.canPerformCriticalHit(target, true)) {
         return false;
      }

      if (!this.isTargetInSight(target)) {
         return false;
      }

      return mc.player.getAttackCooldownProgress(0.5F) >= 0.92F && this.attackTimer.finished(160L);
   }

   private boolean isTargetInSight(LivingEntity target) {
      if (!(mc.crosshairTarget instanceof EntityHitResult hitResult) || hitResult.getEntity() != target) {
         return false;
      }

      float yawDiff = Math.abs(RotationMath.getAngleDifference(mc.player.getYaw(), RotationMath.getRotationTo(RotationMath.getNearestPoint(target)).getYaw()));
      return yawDiff <= this.fovLimit.getCurrentValue();
   }

   private boolean hasCombatWeapon() {
      return mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof MaceItem;
   }

   @Override
   public void onDisable() {
      this.currentTarget = null;
      Rockstar.getInstance().getTargetManager().reset();
      super.onDisable();
   }
}
