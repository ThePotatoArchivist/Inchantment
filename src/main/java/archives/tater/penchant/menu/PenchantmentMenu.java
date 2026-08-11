package archives.tater.penchant.menu;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.registry.PenchantAdvancements;
import archives.tater.penchant.registry.PenchantBlockTags;
import archives.tater.penchant.registry.PenchantEnchantmentTags;
import archives.tater.penchant.registry.PenchantMenus;
import archives.tater.penchant.util.PenchantmentHelper;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static archives.tater.penchant.util.PenchantUtil.streamOrdered;
import static java.util.Comparator.comparingInt;

public class PenchantmentMenu extends AbstractContainerMenu {
    private final Container enchantSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    private final DataSlot bookCount = addDataSlot(DataSlot.standalone());
    private final DataSlot hasDisenchanter = addDataSlot(DataSlot.standalone());
    private final ContainerLevelAccess access;
    private final Player player;
    private final Registry<Enchantment> enchantments;
    private Set<Holder<Enchantment>> availableEnchantments = Set.of();
    private List<Holder<Enchantment>> displayedEnchantments = List.of();

    private Runnable onSlotsChange = () -> {};

    public PenchantmentMenu(int containerId, Inventory playerInventory, Set<Holder<Enchantment>> unlockedEnchantments) {
        this(containerId, playerInventory, unlockedEnchantments, ContainerLevelAccess.NULL);
    }

    public PenchantmentMenu(int containerId, Inventory playerInventory, Set<Holder<Enchantment>> unlockedEnchantments, ContainerLevelAccess access) {
        super(PenchantMenus.PENCHANTMENT_MENU, containerId);
        player = playerInventory.player;
        enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        this.access = access;

        availableEnchantments = Stream.concat(
                unlockedEnchantments.stream(),
                enchantments
                        .get(EnchantmentTags.IN_ENCHANTING_TABLE).stream()
                        .flatMap(HolderSet::stream)
        ).collect(Collectors.toSet());

        addSlot(new Slot(enchantSlots, 0, 15, 58) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new Slot(enchantSlots, 1, 35, 58) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isEnchantingIngredient(stack) || canDisenchant() && isDisenchantingIngredient(stack);
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return isDisenchantingIngredient(stack) ? 1 : super.getMaxStackSize(stack);
            }
        });
        addStandardInventorySlots(playerInventory, 23, 90);

        access.execute((level, pos) -> {
            var bookCount = getBookCount(level, pos);
            this.bookCount.set(bookCount);
            hasDisenchanter.set(player.hasInfiniteMaterials() || hasDisenchanter(level, pos) ? 1 : 0);

            if (player instanceof ServerPlayer serverPlayer)
                PenchantAdvancements.OPEN_TABLE.trigger(serverPlayer, bookCount, unlockedEnchantments);
        });
    }

    public boolean isAvailable(Holder<Enchantment> enchantment) {
        return availableEnchantments.contains(enchantment);
    }

    public List<Holder<Enchantment>> getDisplayedEnchantments() {
        return displayedEnchantments;
    }

    public int getBookCount() {
       return bookCount.get();
    }

    public boolean hasDisenchanter() {
        return hasDisenchanter.get() != 0;
    }

    public ItemStack getEnchantingStack() {
        return enchantSlots.getItem(0);
    }

    public ItemStack getIngredientStack() {
        return enchantSlots.getItem(1);
    }

    public int getPlayerXp() {
        return player.experienceLevel;
    }

    public boolean canDisenchant() {
        if (!hasDisenchanter()) return false;
        var stack = getEnchantingStack();
        return stack.isEmpty() || !PenchantmentHelper.getEnchantments(stack).isEmpty();
    }

    public boolean isEnchanting() {
        var ingredientStack = getIngredientStack();
        return isEnchantingIngredient(ingredientStack) || ingredientStack.isEmpty();
    }

    public boolean isDisenchanting() {
        return canDisenchant() && isDisenchantingIngredient(getIngredientStack());
    }

    public static boolean isEnchantingIngredient(ItemStack stack) {
        return stack.is(Items.LAPIS_LAZULI);
    }

    public static boolean isDisenchantingIngredient(ItemStack stack) {
        return stack.is(Items.BOOK) || EnchantmentHelper.getComponentType(stack) == DataComponents.STORED_ENCHANTMENTS;
    }

    public static Set<Holder<Enchantment>> getUnlockedEnchantments(Level level, BlockPos pos) {
        return PenchantmentHelper.getBookshelfOffsets().stream()
                .filter(offset -> EnchantingTableBlock.isValidBookShelf(level, pos, offset))
                .map(pos::offset)
                .map(level::getBlockEntity)
                .flatMap(entity -> entity instanceof ChiseledBookShelfBlockEntity bookshelf ? bookshelf.getItems().stream() : Stream.empty())
                .flatMap(stack -> PenchantmentHelper.getEnchantments(stack).keySet().stream())
                .distinct()
                .filter(enchantment -> !enchantment.is(EnchantmentTags.IN_ENCHANTING_TABLE))
                .collect(Collectors.toSet());
    }

    public static int getBookCount(Level level, BlockPos pos) {
        return PenchantmentHelper.getBookshelfOffsets().stream()
                .filter(offset -> EnchantingTableBlock.isValidBookShelf(level, pos, offset))
                .map(pos::offset)
                .map(level::getBlockState)
                .filter(state -> state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER))
                .mapToInt(PenchantmentHelper::getBookCount)
                .sum();
    }

    public static boolean hasDisenchanter(Level level, BlockPos pos) {
        return PenchantmentHelper.getBookshelfOffsets().stream()
                .anyMatch(offset -> level.getBlockState(pos.offset(offset)).is(PenchantBlockTags.DISENCHANTER));
    }

    public void handleEnchant(Holder<Enchantment> enchantment) {
        var stack = getEnchantingStack();
        if (isEnchanting()) {
            var levelCost = PenchantmentHelper.getXpLevelCost(enchantment);
            if (!player.hasInfiniteMaterials() && (
                    !PenchantmentHelper.canEnchant(stack, enchantment)
                            || !availableEnchantments.contains(enchantment)
                            || getIngredientStack().isEmpty()
                            || getBookCount() < PenchantmentHelper.getBookRequirement(enchantment)
                            || getPlayerXp() < levelCost
            )) {
                Penchant.LOGGER.warn("Cannot enchant!");
                return;
            }
            access.execute((level, pos) -> {
                player.onEnchantmentPerformed(stack, levelCost);
                var result = PenchantmentHelper.enchant(stack, enchantment);
                enchantSlots.setItem(0, result);

                if (!player.hasInfiniteMaterials())
                    getIngredientStack().shrink(1);

                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer)
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, result, levelCost);

                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                enchantSlots.setChanged();
            });
        } else if (isDisenchanting()) {
            var ingredientStack = getIngredientStack();
            if (!player.hasInfiniteMaterials() &&
                    (!PenchantmentHelper.hasEnchantment(stack, enchantment)
                    || getBookCount() < PenchantmentHelper.getBookRequirement(enchantment)
                    || !EnchantmentHelper.isEnchantmentCompatible(PenchantmentHelper.getEnchantments(ingredientStack).keySet(), enchantment))) {
                Penchant.LOGGER.warn("Cannot disenchant!");
                return;
            }
            access.execute((level, pos) -> {
                var newStack = PenchantmentHelper.updateEnchantments(stack, enchantments -> {
                    enchantments.set(enchantment, 0);
                });
                enchantSlots.setItem(0, newStack);

                enchantSlots.setItem(1, PenchantmentHelper.enchant(ingredientStack, enchantment));

                if (player instanceof ServerPlayer serverPlayer)
                    PenchantAdvancements.EXTRACT_ENCHANTMENT.trigger(serverPlayer, newStack, enchantment);

                level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                enchantSlots.setChanged();
            });
        } else
            Penchant.LOGGER.warn("Cannot enchant or disenchant, no ingredient is present");
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container != this.enchantSlots) return;
        var stack = getEnchantingStack();
        if (stack.isEmpty()) {
            displayedEnchantments = List.of();
            onSlotsChange.run();
            return;
        }
        if (isEnchanting()) {
            var creative = player.hasInfiniteMaterials();
            displayedEnchantments = streamOrdered(enchantments, EnchantmentTags.TOOLTIP_ORDER)
                    .filter(enchantment ->
                            !enchantment.is(PenchantEnchantmentTags.DISABLED) &&
                            PenchantmentHelper.canEnchantItem(stack, enchantment) &&
                            (!enchantment.is(EnchantmentTags.CURSE) || availableEnchantments.contains(enchantment) || PenchantmentHelper.hasEnchantment(stack, enchantment))
                    )
                    .sorted(comparingInt(enchantment ->
                            creative || !availableEnchantments.contains(enchantment) && !PenchantmentHelper.hasEnchantment(stack, enchantment) ? 2
                            : enchantment.is(EnchantmentTags.CURSE) ? 1
                            : 0
                    ))
                    .toList();
        } else if (isDisenchanting()) {
            displayedEnchantments = streamOrdered(enchantments, EnchantmentTags.TOOLTIP_ORDER)
                    .filter(enchantment -> PenchantmentHelper.hasEnchantment(stack, enchantment))
                    .toList();
        } else {
            displayedEnchantments = List.of();
        }
        onSlotsChange.run();
    }

    public void setSlotChangeListener(Runnable onSlotsChange) {
        this.onSlotsChange = onSlotsChange;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack itemStack2 = slot.getItem();
        var itemStack = itemStack2.copy();
        if (index == 0) {
            if (!moveItemStackTo(itemStack2, 2, Inventory.INVENTORY_SIZE + 2, true))
                return ItemStack.EMPTY;
        } else if (index == 1) {
            if (!moveItemStackTo(itemStack2, 2, Inventory.INVENTORY_SIZE + 2, true))
                return ItemStack.EMPTY;
        } else if (isEnchantingIngredient(itemStack2) || !getEnchantingStack().isEmpty() && canDisenchant() && isDisenchantingIngredient(itemStack2)) {
            if (!moveItemStackTo(itemStack2, 1, 2, true))
                return ItemStack.EMPTY;
        } else {
            if (slots.getFirst().hasItem() || !slots.getFirst().mayPlace(itemStack2))
                return ItemStack.EMPTY;

            ItemStack itemStack3 = itemStack2.copyWithCount(1);
            itemStack2.shrink(1);
            slots.getFirst().setByPlayer(itemStack3);
        }

        if (itemStack2.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();

        if (itemStack2.getCount() == itemStack.getCount())
            return ItemStack.EMPTY;

        slot.onTake(player, itemStack2);

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, blockPos) -> clearContainer(player, enchantSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Blocks.ENCHANTING_TABLE);
    }
}
