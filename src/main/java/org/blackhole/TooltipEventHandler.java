package org.blackhole;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

@Mod.EventBusSubscriber(modid = AutoCycling.MODID, value = Dist.CLIENT)
public class TooltipEventHandler {

    @SubscribeEvent
    public static void addEnchantmentBooksId(ItemTooltipEvent event){
        ItemStack stack = event.getItemStack();
        if (stack.is(Items.ENCHANTED_BOOK)) {
            // Получаем список чар на книге
            Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);

            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                String namespace = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey()).getNamespace();
                String path = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey()).getPath();

                event.getToolTip().add(Component.literal("§7[§aAutoCycling§7] §bID: §f" + path));
                event.getToolTip().add(Component.literal("§7[§aAutoCycling§7] §bLvl: §f" + entry.getValue()));
            }
        }
    }
}
