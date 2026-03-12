package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityDeathEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;

@ModuleInfo(name = "Kill Sound", category = ModuleCategory.OTHER, desc = "Проигрывает звук при убийстве цели")
public class KillSound extends BaseModule {
   private final ModeSetting sound = new ModeSetting(this, "modules.settings.kill_sound.sound");
   private final ModeSetting.Value kill1 = new ModeSetting.Value(this.sound, "KILL_1").select();
   private final ModeSetting.Value kill2 = new ModeSetting.Value(this.sound, "KILL_2");
   private final ModeSetting.Value orb = new ModeSetting.Value(this.sound, "ORB");
   private final SliderSetting volume = new SliderSetting(this, "modules.settings.kill_sound.volume").min(0.0F).max(1.0F).step(0.01F).currentValue(1.0F);
   private final SliderSetting pitch = new SliderSetting(this, "modules.settings.kill_sound.pitch").min(0.5F).max(2.0F).step(0.01F).currentValue(1.0F);
   private final EventListener<EntityDeathEvent> onEntityDeath = event -> {
      if (mc.player == null) {
         return;
      }

      LivingEntity killer = event.getKillerEntity();
      if (killer == mc.player && event.getEntity() != mc.player) {
         mc.getSoundManager().play(PositionedSoundInstance.master(this.resolveSound(), this.pitch.getCurrentValue(), this.volume.getCurrentValue()));
      }
   };

   private net.minecraft.sound.SoundEvent resolveSound() {
      if (this.kill2.isSelected()) {
         return SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value();
      } else if (this.orb.isSelected()) {
         return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
      } else {
         return SoundEvents.ENTITY_PLAYER_LEVELUP;
      }
   }
}
