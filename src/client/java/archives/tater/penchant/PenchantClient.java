package archives.tater.penchant;

import archives.tater.penchant.client.FontUtils;
import archives.tater.penchant.client.KeyMappingExt;
import archives.tater.penchant.client.PenchantClientConfig;
import archives.tater.penchant.client.gui.screen.PenchantmentScreen;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.network.UnlockedEnchantmentsPayload;
import archives.tater.penchant.registry.PenchantMenus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import static java.util.Objects.requireNonNull;
import static net.minecraft.util.Mth.clamp;
import static net.minecraft.util.Util.makeDescriptionId;


public class PenchantClient implements ClientModInitializer {
    private static KeyMappingExt keybind(Identifier id, int key, KeyMapping.Category category) {
        return (KeyMappingExt) KeyBindingHelper.registerKeyBinding(new KeyMappingExt(makeDescriptionId("key", id), Type.KEYSYM, key, category));
    }

    private static final KeyMapping.Category PENCHANT_CATEGORY = KeyMapping.Category.register(Penchant.id(Penchant.MOD_ID));
    public static final KeyMappingExt SHOW_PROGRESS_KEYBIND = keybind(
            Penchant.id("show_progress"),
            InputConstants.KEY_LCONTROL,
            PENCHANT_CATEGORY
    );

    public static final PenchantClientConfig CONFIG = PenchantClientConfig.createToml(FabricLoader.getInstance().getConfigDir(), Penchant.MOD_ID, "client", PenchantClientConfig.class);

    public static boolean shouldShowProgress() {
        return CONFIG.alwaysShowTooltipProgress || SHOW_PROGRESS_KEYBIND.isDownAnywhere();
    }

    public static boolean shouldShowKeyHint() {
        return !shouldShowProgress() && CONFIG.showTooltipKeyHint;
    }

    public static int getBarWidth() {
        return CONFIG.barWidth;
    }

    public static Component getProgressKeyHint() {
        return Component.translatable("penchant.tooltip.progress.key", Component.keybind(SHOW_PROGRESS_KEYBIND.getName()))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    public static Component getProgressTooltip(EnchantmentProgress progress, Holder<Enchantment> enchantment, int level, int maxDamage) {
        if (level >= enchantment.value().getMaxLevel())
            return Component.literal("  ")
                    .append(FontUtils.getBar(getBarWidth(), getBarWidth()))
                    .append(" ")
                    .append(Component.translatable("penchant.tooltip.progress.max"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE);

        var maxProgress = EnchantmentProgress.getMaxProgress(enchantment, level, maxDamage);
        var storedProgress = clamp(progress.getProgress(enchantment), 0, maxProgress);

        return Component.literal("  ")
                .append(FontUtils.getBar(getBarWidth(), getBarWidth() * storedProgress / maxProgress))
                .append(" ")
                .append(Component.translatable("penchant.tooltip.progress",
                        Component.literal(Integer.toString(storedProgress)).withStyle(ChatFormatting.LIGHT_PURPLE),
                        maxProgress
                ).withStyle(ChatFormatting.DARK_GRAY))
                ;
    }

    @Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        MenuScreens.register(PenchantMenus.PENCHANTMENT_MENU, PenchantmentScreen::new);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            PenchantmentDefinition.buildCache(requireNonNull(client.level).registryAccess());
        });

        ClientPlayNetworking.registerGlobalReceiver(UnlockedEnchantmentsPayload.TYPE, (payload, context) -> {
            if (!(context.player().containerMenu instanceof PenchantmentMenu menu)) {
                Penchant.LOGGER.warn("Recieved enchantments payload but enchantment menu was not open");
                return;
            }
            menu.setUnlockedEnchantments(payload.unlocked());
        });
	}
}
