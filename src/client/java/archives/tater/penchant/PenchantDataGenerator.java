package archives.tater.penchant;

import archives.tater.penchant.datagen.*;
import archives.tater.penchant.registry.PenchantFlag;
import archives.tater.penchant.registry.PenchantModules;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import net.minecraft.resources.Identifier;

public class PenchantDataGenerator implements DataGeneratorEntrypoint {

    private static FabricDataGenerator.Pack createPack(FabricDataGenerator fabricDataGenerator, Identifier id) {
        var pack = fabricDataGenerator.createBuiltinResourcePack(id);
        pack.addProvider(PackMetaGen.pack(id));
        return pack;
    }

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(FlagTagGenerator.generator());
        pack.addProvider(EnchantmentTagGenerator::new);
        pack.addProvider(BlockTagGenerator::new);
        pack.addProvider(ItemTagGenerator::new);
//        pack.addProvider(PenchantmentDefinitionGenerator::new);

        var durabilityPack = createPack(fabricDataGenerator, PenchantModules.DURABILITY_REWORK);
        durabilityPack.addProvider(DurabilityEnchantmentGenerator::new);
        durabilityPack.addProvider(DurabilityEnchantmentTagGenerator::new);

        var bookshelfPack = createPack(fabricDataGenerator, PenchantModules.BOOKSHELF_PLACEMENT);
        bookshelfPack.addProvider(FlagTagGenerator.generator(PenchantFlag.LENIENT_BOOKSHELF_PLACEMENT));
        bookshelfPack.addProvider(BookshelfBlockTagGenerator::new);

        var anvilPack = createPack(fabricDataGenerator, PenchantModules.NO_ANVIL_BOOKS);
        anvilPack.addProvider(FlagTagGenerator.generator(PenchantFlag.NO_ANVIL_BOOKS));

        var tablePack = createPack(fabricDataGenerator, PenchantModules.TABLE_REWORK);
        tablePack.addProvider(FlagTagGenerator.generator(PenchantFlag.REWORKED_TABLE_MENU));
        tablePack.addProvider(TableAdvancementGenerator::new);

        var lootPack = createPack(fabricDataGenerator, PenchantModules.LOOT_REWORK);
        lootPack.addProvider(FlagTagGenerator.generator(PenchantFlag.ZOMBIE_SPAWN_PICKAXE, PenchantFlag.REPLACE_BOOK_LOOT_TAG));
        lootPack.addProvider(LootModificationGenerator::new);
        lootPack.addProvider(LootEnchantmentTagGenerator::new);
        lootPack.addProvider(LootAdvancementGenerator::new);
        lootPack.addProvider(LootEnchantmentProviderGenerator::new);

        var dropPack = createPack(fabricDataGenerator, PenchantModules.GUARANTEED_DROPS);
        dropPack.addProvider(FlagTagGenerator.generator(PenchantFlag.GUARANTEED_ENCHANTED_DROP, PenchantFlag.GUARANTEED_TRIDENT_DROP));
        dropPack.addProvider(DropEnchantmentTagGenerator::new);

        var librarianPack = createPack(fabricDataGenerator, PenchantModules.RANDOMIZED_LIBRARIANS);
        librarianPack.addProvider(VillagerTradeGenerator::new);
	}
}
