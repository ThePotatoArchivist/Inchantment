package archives.tater.penchant.datagen;

import archives.tater.penchant.component.RandomEnchantment;
import archives.tater.penchant.registry.PenchantComponents;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.item.trading.VillagerTrades;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VillagerTradeGenerator extends FabricDynamicRegistryProvider {

    public VillagerTradeGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    private static VillagerTrade randomEnnchantedBook(HolderLookup.Provider registries, int emeraldCost, int maxUses, int xp) {
        return VillagerTrade.builder(
                new TradeCost(Items.EMERALD, emeraldCost),
                new TradeCost(Items.BOOK, 1),
                new ItemStackTemplate(Items.ENCHANTED_BOOK, DataComponentPatch.builder()
                        .set(PenchantComponents.RANDOM_ENCHANTMENT, new RandomEnchantment(
                                Optional.of(registries.getOrThrow(EnchantmentTags.TRADEABLE)),
                                false
                        ))
                        .build()),
                maxUses,
                xp,
                0.2f
        ).build();
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.add(VillagerTrades.LIBRARIAN_1_EMERALD_AND_BOOK_ENCHANTED_BOOK, randomEnnchantedBook(registries, 15, 1, 6));
        entries.add(VillagerTrades.LIBRARIAN_2_EMERALD_AND_BOOK_ENCHANTED_BOOK, randomEnnchantedBook(registries, 18, 1, 15));
        entries.add(VillagerTrades.LIBRARIAN_3_EMERALD_AND_BOOK_ENCHANTED_BOOK, randomEnnchantedBook(registries, 20, 2, 25));
        entries.add(VillagerTrades.LIBRARIAN_4_EMERALD_AND_BOOK_ENCHANTED_BOOK, randomEnnchantedBook(registries, 24, 3, 45));
    }

    @Override
    public String getName() {
        return "Villager Trades";
    }
}
