package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;

import java.util.Set;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PotionBrew {

    private static final Set<Holder<MobEffect>> BLOCKED_EFFECTS = Set.of(
            MobEffects.INVISIBILITY,
            MobEffects.INSTANT_HEALTH,
            MobEffects.INSTANT_DAMAGE,
            MobEffects.RESISTANCE,
            MobEffects.SLOWNESS,
            MobEffects.LEVITATION,
            MobEffects.HASTE
    );

    @SubscribeEvent
    public static void onPotionBrew(PotionBrewEvent.Pre event) {

        for (int i = 0; i < event.getLength(); i++) {
            ItemStack stack = event.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof PotionItem)) {
                continue;
            }

            if (containsBlockedEffect(stack)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean containsBlockedEffect(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        if (contents == null) {
            return false;
        }

        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (BLOCKED_EFFECTS.contains(effect.getEffect())) {
                return true;
            }
        }

        return false;
    }
}