package archives.tater.penchant.component;

import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.registry.PenchantEnchantmentTags;
import archives.tater.penchant.util.PenchantmentHelper;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.hash;

@SuppressWarnings("ClassCanBeRecord")
public class EnchantmentProgress {

    private final Object2IntOpenHashMap<Holder<Enchantment>> progress;

    public EnchantmentProgress(Object2IntOpenHashMap<Holder<Enchantment>> progress) {
        this.progress = progress;
    }

    public int getProgress(Holder<Enchantment> enchantment) {
        return progress.getInt(enchantment);
    }

    public Mutable toMutable() {
        return new Mutable(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EnchantmentProgress) obj;
        return Objects.equals(this.progress, that.progress);
    }

    @Override
    public int hashCode() {
        return hash(progress);
    }

    public static class Mutable {

        private final Object2IntOpenHashMap<Holder<Enchantment>> progress;

        public Mutable(EnchantmentProgress progress) {
            this.progress = progress.progress.clone();
        }

        public int getProgress(Holder<Enchantment> enchantment) {
            return progress.getInt(enchantment);
        }

        public void setProgress(Holder<Enchantment> enchantment, int progress) {
            this.progress.put(enchantment, progress);
        }

        public void removeProgress(Holder<Enchantment> enchantment) {
            progress.removeInt(enchantment);
        }

        public void addProgress(Holder<Enchantment> enchantment, int progress) {
            setProgress(enchantment, getProgress(enchantment) + progress);
        }

        public EnchantmentProgress toImmutable() {
            return new EnchantmentProgress(progress);
        }

    }

    public static boolean shouldShowTooltip(Holder<Enchantment> enchantment) {
        return enchantment.value().getMaxLevel() != 1 && !enchantment.is(PenchantEnchantmentTags.NO_LEVELING);
    }

    public static final Codec<EnchantmentProgress> CODEC =
            Codec.unboundedMap(Enchantment.CODEC, Codec.intRange(0, Integer.MAX_VALUE)).xmap(
                    map -> new EnchantmentProgress(new Object2IntOpenHashMap<>(map)),
                    component -> component.progress
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentProgress> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(Object2IntOpenHashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT),
                    progress -> progress.progress,
                    EnchantmentProgress::new
            );

    public static final EnchantmentProgress EMPTY = new EnchantmentProgress(new Object2IntOpenHashMap<>());

    public static int getMaxProgress(Holder<Enchantment> enchantment, int currentLevel, DataComponentGetter stack) {
        return stack.getOrDefault(PenchantComponents.ENCHANTMENT_PROGRESS_COST_FACTOR, 1) * PenchantmentHelper.getProgressCostFactor(enchantment, currentLevel + 1);
    }

    public static EnchantmentProgress getProgress(ItemStack stack) {
        return stack.getOrDefault(PenchantComponents.ENCHANTMENT_PROGRESS, EMPTY);
    }

    public static void onDurabilityDamage(ItemStack stack, @Nullable LivingEntity user) {
        addToProgress(stack, 1, user);
    }

    public static void addToProgress(ItemStack stack, int increase, @Nullable LivingEntity user) {
        var enchantments = stack.getEnchantments();
        if (enchantments.isEmpty()) return;

        var newProgress = getProgress(stack).toMutable();

        for (var enchantment : enchantments.keySet())
            if (!enchantment.is(PenchantEnchantmentTags.NO_LEVELING))
                newProgress.addProgress(enchantment, increase);

        updateEnchantmentsForStack(newProgress, enchantments, stack, user);

        stack.set(PenchantComponents.ENCHANTMENT_PROGRESS, newProgress.toImmutable());
    }

    public static void addRandomProgress(ItemStack stack, RandomSource random) {
        var enchantments = stack.getEnchantments();
        if (enchantments.isEmpty()) return;

        var newProgress = new Mutable(EnchantmentProgress.EMPTY);

        for (var enchantment : enchantments.keySet()) {
            var level = enchantments.getLevel(enchantment);
            if (!enchantment.is(PenchantEnchantmentTags.NO_LEVELING) && level < enchantment.value().getMaxLevel())
                newProgress.setProgress(enchantment,
                        (int) (random.nextFloat() * getMaxProgress(enchantment, level, stack)));
        }

        if (newProgress.progress.isEmpty()) return;

        stack.set(PenchantComponents.ENCHANTMENT_PROGRESS, newProgress.toImmutable());
    }

    /**
     * Levels up any necessary enchantments
     */
    public static boolean updateEnchantments(EnchantmentProgress.Mutable progress, ItemEnchantments.Mutable enchantments, DataComponentGetter components) {
        boolean changed = false;

        for (var enchantment : enchantments.keySet()) {
            if (enchantment.is(PenchantEnchantmentTags.NO_LEVELING)) continue;

            var level = enchantments.getLevel(enchantment);

            while (true) {
                if (level >= enchantment.value().getMaxLevel()) {
                    progress.removeProgress(enchantment);
                    break;
                }

                var maxProgress = getMaxProgress(enchantment, level, components);

                var progressValue = progress.getProgress(enchantment);

                if (progressValue < maxProgress) break;

                level++;
                progress.setProgress(enchantment, progressValue - maxProgress);
            }

            if (level == enchantments.getLevel(enchantment)) continue;

            enchantments.set(enchantment, level);

            changed = true;
        }

        return changed;
    }

    /**
     * Levels up any necessary enchantments and if so plays effects
     *
     * @param stack is mutated
     */
    public static void updateEnchantmentsForStack(EnchantmentProgress.Mutable progress, ItemEnchantments enchantments, ItemStack stack, @Nullable LivingEntity user) {
        var newEnchantments = new ItemEnchantments.Mutable(enchantments);

        if (!updateEnchantments(progress, newEnchantments, stack)) return;
        stack.set(DataComponents.ENCHANTMENTS, newEnchantments.toImmutable());

        if (user == null) return;
        user.level().playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, user.getSoundSource(), 1, user.getRandom().nextFloat() * 0.1F + 0.9F);

        if (!(user.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.ENCHANT, user.getX(), user.getEyeY(), user.getZ(), 16, 0, 0, 0, 1.0);
    }
}
