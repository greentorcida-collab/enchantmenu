package com.enchantmenu;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EnchantMenu implements ModInitializer {
    public static final Identifier APPLY_PACKET = new Identifier("enchantmenu", "apply");

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(APPLY_PACKET,
            (server, player, handler, buf, responseSender) -> {
                int count = Math.min(buf.readInt(), 100);
                Map<Enchantment, Integer> map = new LinkedHashMap<>();
                for (int i = 0; i < count; i++) {
                    String id = buf.readString();
                    int level = buf.readInt();
                    Enchantment enchantment = Registry.ENCHANTMENT.get(new Identifier(id));
                    if (enchantment != null && level > 0) {
                        map.put(enchantment, level);
                    }
                }
                server.execute(() -> {
                    ItemStack stack = player.getMainHandStack();
                    if (!stack.isEmpty()) {
                        EnchantmentHelper.set(map, stack);
                    }
                });
            });
    }
}
