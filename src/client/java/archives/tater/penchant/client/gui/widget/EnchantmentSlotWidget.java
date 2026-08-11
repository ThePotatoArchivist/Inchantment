package archives.tater.penchant.client.gui.widget;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.network.EnchantPayload;
import archives.tater.penchant.util.PenchantmentHelper;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import cc.cassian.item_descriptions.client.DescriptionKey;
import cc.cassian.item_descriptions.client.ModClient;
import cc.cassian.item_descriptions.client.helpers.ModStyle;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentSlotWidget extends AbstractButton {
    public static final int WIDTH = 131;
    public static final int HEIGHT = 12;
    public static final WidgetSprites TEXTURES = new WidgetSprites(
            Penchant.id("container/enchanting_table/slot"),
            Penchant.id("container/enchanting_table/slot_disabled"),
            Penchant.id("container/enchanting_table/slot_highlighted")
    );
    public static final FontDescription.Resource ALT_FONT = new FontDescription.Resource(Identifier.withDefaultNamespace("alt"));

    public static final int DISABLED_COLOR = 0xFF685E4A;
    public static final int INSUFFICIENT_COLOR = 0xffff5555;
    public static final int XP_COLOR = 0xFF80FF20;
    public static final int BOOK_COLOR = 0XFFFFAA00;

    private final Holder<Enchantment> enchantment;
    private final Component text;
    private final @Nullable Component costText;
    private final boolean isCurse;

    private EnchantmentSlotWidget(int x, int y, Holder<Enchantment> enchantment, List<Holder<Enchantment>> incompatible, boolean remove, boolean showXpCost, boolean showBookCost, boolean canUse, boolean alreadyAdded, boolean hasIngredient, boolean hasEnoughBooks, boolean hasEnoughXp, boolean isUnlocked) {
        super(x, y, WIDTH, HEIGHT, enchantment.value().description());
        this.enchantment = enchantment;
        isCurse = enchantment.is(EnchantmentTags.CURSE);

        var text = enchantment.value().description().copy();
        if (!isUnlocked && !alreadyAdded) text.withStyle(style -> style.withFont(ALT_FONT));
        this.text = text;

        var xpCost = PenchantmentHelper.getXpLevelCost(enchantment);
        var bookRequirement = PenchantmentHelper.getBookRequirement(enchantment);

        {
            var costTexts = new ArrayList<Component>(3);
            if (!canUse && !incompatible.isEmpty()) costTexts.add(
                    Component.translatable("widget.penchant.enchantment_slot.incompatible")
                            .withColor(INSUFFICIENT_COLOR)
            );
            if (showBookCost) costTexts.add(Component.literal(Integer.toString(bookRequirement))
                    .withColor(alreadyAdded ? DISABLED_COLOR :
                            !hasEnoughBooks ? INSUFFICIENT_COLOR
                                    : BOOK_COLOR)
            );
            if (showXpCost) costTexts.add(
                    Component.literal(Integer.toString(xpCost))
                            .withColor(alreadyAdded ? DISABLED_COLOR :
                                    !hasEnoughXp ? INSUFFICIENT_COLOR
                                            : XP_COLOR)
            );
            this.costText = ComponentUtils.formatList(costTexts, Component.literal(" "));
        }

        if (alreadyAdded)
            setTooltip(Tooltip.create(Component.empty()
                    .append(PenchantmentHelper.getName(enchantment))
                    .append("\n")
                    .append(Component.translatable("widget.penchant.enchantment_slot.tooltip.added").withStyle(ChatFormatting.GRAY))));
        else if (!isUnlocked) {
            var name = enchantment.value().description().getString();
            setTooltip(Tooltip.create(Component.literal(name.substring(0, name.length() * 2 / 3).trim())
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("widget.penchant.enchantment_slot.tooltip.name_locked"))
                    .append("\n")
                    .append(Component.translatable("widget.penchant.enchantment_slot.tooltip.locked")
                            .withStyle(ChatFormatting.RED))
            ));
        } else {
            var tooltip = remove
                    ? Component.translatable("widget.penchant.enchantment_slot.tooltip.remove", enchantment.value().description())
                    : PenchantmentHelper.getName(enchantment).copy();

            if (!canUse && !incompatible.isEmpty()) tooltip
                    .append(Component.literal("\n"))
                    .append(Component.translatable("widget.penchant.enchantment_slot.tooltip.incompatible", ComponentUtils.formatList(incompatible, Component.literal(", "), holder -> holder.value().description()))
                            .withStyle(ChatFormatting.RED));

            if (showBookCost) tooltip
                    .append("\n")
                    .append(Component.translatable("widget.penchant.enchantment_slot.tooltip.book_requirement", bookRequirement)
                            .withColor(hasEnoughBooks ? BOOK_COLOR : INSUFFICIENT_COLOR));

            if (showXpCost) tooltip
                    .append("\n")
                    .append(Component.translatable("widget.penchant.enchantment_slot.tooltip.xp_cost", xpCost)
                            .withColor(hasEnoughXp ? XP_COLOR : INSUFFICIENT_COLOR));

            if (PenchantmentHelper.ITEM_DESCRIPTIONS_INSTALLED
                    && ModClient.CONFIG.enchantmentDescriptions.enable.value()
                    && ModClient.CONFIG.enchantmentDescriptions.enchantingTable.value()
                    && Enchantment.getFullname(enchantment, 1).getContents() instanceof TranslatableContents content)
                tooltip
                        .append("\n")
                        .append(new DescriptionKey(content.getKey()).toText().setStyle(ModStyle.ENCHANTMENT_DESCRIPTIONS));

            setTooltip(Tooltip.create(tooltip));
        }

        active = hasIngredient && hasEnoughBooks && hasEnoughXp && isUnlocked && canUse;
    }

    public EnchantmentSlotWidget(int x, int y, Holder<Enchantment> enchantment, List<Holder<Enchantment>> incompatible, boolean canAdd, boolean alreadyAdded, boolean hasIngredient, boolean hasEnoughBooks, boolean hasEnoughXp, boolean isUnlocked) {
        this(x, y, enchantment, isUnlocked ? incompatible : List.of(), false, isUnlocked, isUnlocked, canAdd, alreadyAdded, hasIngredient, hasEnoughBooks, hasEnoughXp, isUnlocked);
    }

    public EnchantmentSlotWidget(int x, int y, Holder<Enchantment> enchantment, List<Holder<Enchantment>> incompatible, boolean canRemove, boolean hasEnoughBooks) {
        this(x, y, enchantment, incompatible, true, false, true, canRemove, false, true, hasEnoughBooks, true, true);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURES.get(active, isHovered()), getX(), getY(), getWidth(), getHeight());

        var font = Minecraft.getInstance().font;

        graphics.text(font, text, getX() + 2, getY() + 2, active && isHovered ? 0xFFFCFC7E : isCurse ? 0xFF891d13 : 0xFF332E25, false);

        if (costText != null)
            graphics.text(font, costText, getX() + width - 2 - font.width(costText), getY() + 2, 0xFF404040, true);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        ClientPlayNetworking.send(new EnchantPayload(enchantment));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
