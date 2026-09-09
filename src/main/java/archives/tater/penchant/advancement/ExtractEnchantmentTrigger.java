package archives.tater.penchant.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class ExtractEnchantmentTrigger extends SimpleCriterionTrigger<ExtractEnchantmentTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack stack, Holder<Enchantment> enchantment) {
        trigger(player, instance -> instance.matches(stack, enchantment));
    }

    public record TriggerInstance(
            Optional<Holder<LootItemCondition>> player,
            ItemPredicate item,
            Optional<HolderSet<Enchantment>> enchantment
    ) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ItemPredicate.CODEC.optionalFieldOf("item", ItemPredicate.Builder.item().build()).forGetter(TriggerInstance::item),
                RegistryCodecs.holderSet(Registries.ENCHANTMENT, false).optionalFieldOf("enchantment").forGetter(TriggerInstance::enchantment)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(ItemStack stack, Holder<Enchantment> enchantment) {
            return item.test(stack) && this.enchantment.map(set -> set.contains(enchantment)).orElse(true);
        }
    }
}
