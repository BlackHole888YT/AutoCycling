package org.blackhole.autocycling.mixin;

import de.maxhenkel.easyvillagers.Main;
import de.maxhenkel.easyvillagers.net.MessageCycleTrades;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.registries.ForgeRegistries;
import org.blackhole.AutoCycling;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Unique public EditBox autoCycling$idInput;
    @Unique public EditBox autoCycling$lvlInput;
    @Unique private Button autoCycling$toggleButton;
    @Unique private boolean autoCycling$isSearching = false;
    @Unique private int autoCycling$tickDelay = 0;
    @Unique private boolean autoCycling$tradeLocked = false;
    @Unique private int autoCycling$baseTraderXp = 0;
    @Unique private int autoCycling$baseTraderLevel = 0;
    @Unique private boolean autoCycling$baselineReady = false;

    public MerchantScreenMixin(MerchantMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    protected void autoCycling$onInit(CallbackInfo ci) {
        int x = (this.width - 276) / 2;
        int y = (this.height - 166) / 2;

        autoCycling$idInput = new EditBox(font, x + 20, y - 20, 90, 12, Component.literal("Enchant ID"));
        autoCycling$idInput.setHint(Component.literal("mending...").withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(autoCycling$idInput);

        autoCycling$lvlInput = new EditBox(font, x + 115, y - 20, 20, 12, Component.literal("Lvl"));
        autoCycling$lvlInput.setValue("1");
        this.addRenderableWidget(autoCycling$lvlInput);

        autoCycling$baseTraderXp = 0;
        autoCycling$baseTraderLevel = 0;
        autoCycling$tradeLocked = false;
        autoCycling$baselineReady = false;

        autoCycling$toggleButton = this.addRenderableWidget(Button.builder(Component.literal("Auto"), (btn) -> {
            autoCycling$updateTradeLock();
            if (autoCycling$tradeLocked) {
                autoCycling$isSearching = false;
                btn.setMessage(Component.literal("§aAuto"));
                return;
            }
            autoCycling$isSearching = !autoCycling$isSearching;
            btn.setMessage(Component.literal(autoCycling$isSearching ? "§cSTOP" : "§aAuto"));
        }).bounds(x + 140, y - 22, 40, 16).build());
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void autoCycling$onRender(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        autoCycling$updateTradeLock();
        if (autoCycling$tradeLocked) {
            graphics.drawString(this.font, "§cLOCKED: traded before", this.leftPos + 20, this.topPos - 34, 0xFFFFFF, false);
        }
        if (autoCycling$isSearching) {
            if (autoCycling$tradeLocked) {
                autoCycling$isSearching = false;
                autoCycling$tickDelay = 0;
                if (autoCycling$toggleButton != null) {
                    autoCycling$toggleButton.setMessage(Component.literal("§aAuto"));
                }
                return;
            }
            autoCycling$tickDelay++;
            if (autoCycling$tickDelay >= AutoCycling.getAutoTraderSpeed()) {
                autoCycling$tickDelay = 0;
                if (autoCycling$checkTrade()) {
                    autoCycling$isSearching = false;
                    if (autoCycling$toggleButton != null) {
                        autoCycling$toggleButton.setMessage(Component.literal("§aAuto"));
                    }
                    Minecraft.getInstance().player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                } else {
                    if (Main.SIMPLE_CHANNEL != null) {
                        Main.SIMPLE_CHANNEL.sendToServer(new MessageCycleTrades());
                    }
                }
            }
        }
    }

    @Unique
    private boolean autoCycling$checkTrade() {
        String targetId = autoCycling$idInput.getValue().toLowerCase().trim();
        int targetLvl = 1;
        try { targetLvl = Integer.parseInt(autoCycling$lvlInput.getValue()); } catch (Exception ignored) {}

        if (targetId.isEmpty()) return false;

        for (MerchantOffer offer : this.menu.getOffers()) {
            ItemStack res = offer.getResult();
            if (res.is(Items.ENCHANTED_BOOK)) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(res);
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    String currentId = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey()).getPath();
                    if (currentId.equals(targetId) && entry.getValue() == targetLvl) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Unique
    private boolean autoCycling$hasUsedTrade() {
        for (MerchantOffer offer : this.menu.getOffers()) {
            if (offer.getUses() > 0) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private void autoCycling$updateTradeLock() {
        if (autoCycling$tradeLocked) {
            return;
        }

        if (!autoCycling$baselineReady && !this.menu.getOffers().isEmpty()) {
            autoCycling$baseTraderXp = this.menu.getTraderXp();
            autoCycling$baseTraderLevel = this.menu.getTraderLevel();
            autoCycling$baselineReady = true;

            // If villager already has progression, treat it as previously traded and block auto-cycling.
            if (autoCycling$baseTraderXp > 0 || autoCycling$baseTraderLevel > 1) {
                autoCycling$tradeLocked = true;
                return;
            }
        }

        if (autoCycling$hasUsedTrade()) {
            autoCycling$tradeLocked = true;
            return;
        }

        if (autoCycling$baselineReady
                && (this.menu.getTraderXp() > autoCycling$baseTraderXp
                || this.menu.getTraderLevel() > autoCycling$baseTraderLevel)) {
            autoCycling$tradeLocked = true;
        }
    }

    @Unique
    private boolean autoCycling$isInputFocused() {
        return (autoCycling$idInput != null && autoCycling$idInput.isFocused())
                || (autoCycling$lvlInput != null && autoCycling$lvlInput.isFocused());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!autoCycling$isInputFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        // Allow closing screen with Escape, but swallow all other hotkeys while typing.
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if ((autoCycling$idInput != null && autoCycling$idInput.keyPressed(keyCode, scanCode, modifiers))
                || (autoCycling$lvlInput != null && autoCycling$lvlInput.keyPressed(keyCode, scanCode, modifiers))) {
            return true;
        }

        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!autoCycling$isInputFocused()) {
            return super.charTyped(codePoint, modifiers);
        }

        if ((autoCycling$idInput != null && autoCycling$idInput.charTyped(codePoint, modifiers))
                || (autoCycling$lvlInput != null && autoCycling$lvlInput.charTyped(codePoint, modifiers))) {
            return true;
        }

        return true;
    }
}
