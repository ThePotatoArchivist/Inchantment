package archives.tater.penchant.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;
import java.util.Set;

public class OpenTableTrigger extends SimpleCriterionTrigger<OpenTableTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int bookCount, Set<Holder<Enchantment>> enchantments) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(bookCount, enchantments));
    }

    public record TriggerInstance(
            Optional<Holder<LootItemCondition>> player,
            MinMaxBounds.Ints bookCount,
            Optional<HolderSet<Enchantment>> containsEnchantment,
            boolean requireEnchantment
    ) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("book_count", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::bookCount),
                RegistryCodecs.holderSet(Registries.ENCHANTMENT, true).optionalFieldOf("contains_enchantment").forGetter(TriggerInstance::containsEnchantment),
                Codec.BOOL.optionalFieldOf("require_enchantment", false).forGetter(TriggerInstance::requireEnchantment)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(int bookCount, Set<Holder<Enchantment>> enchantments) {
            return this.bookCount.matches(bookCount)
                    && containsEnchantment.map(set -> set.stream().anyMatch(enchantments::contains)).orElse(true)
                    && (!requireEnchantment || !enchantments.isEmpty());
        }
    }
}
