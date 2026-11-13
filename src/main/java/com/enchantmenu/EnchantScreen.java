package com.enchantmenu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;

public class EnchantScreen extends Screen {
    private static final int PER_PAGE = 8;

    private final List<Enchantment> all = new ArrayList<>();
    private final Map<Enchantment, Integer> levels = new LinkedHashMap<>();
    private int page = 0;

    public EnchantScreen() {
        super(new LiteralText("Enchant"));
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            all.add(enchantment);
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            ItemStack stack = mc.player.getMainHandStack();
            if (!stack.isEmpty()) {
                levels.putAll(EnchantmentHelper.get(stack));
            }
        }
    }

    private int pageCount() {
        return (all.size() + PER_PAGE - 1) / PER_PAGE;
    }

    private Text label(Enchantment enchantment) {
        int level = levels.getOrDefault(enchantment, 0);
        return new TranslatableText(enchantment.getTranslationKey())
            .append(new LiteralText(": " + level + "/" + enchantment.getMaxLevel()));
    }

    private void changePage(int delta) {
        page = Math.floorMod(page + delta, pageCount());
        this.init(this.client, this.width, this.height);
    }

    private void apply() {
        MinecraftClient mc = this.client;
        if (mc == null || mc.player == null || mc.player.getMainHandStack().isEmpty()) {
            return;
        }
        List<Map.Entry<Enchantment, Integer>> chosen = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : levels.entrySet()) {
            if (entry.getValue() > 0) {
                chosen.add(entry);
            }
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(chosen.size());
        for (Map.Entry<Enchantment, Integer> entry : chosen) {
            buf.writeString(Registry.ENCHANTMENT.getId(entry.getKey()).toString());
            buf.writeInt(entry.getValue());
        }
        ClientPlayNetworking.send(EnchantMenu.APPLY_PACKET, buf);
        mc.setScreen(null);
    }

    @Override
    protected void init() {
        int colWidth = 150;
        int center = this.width / 2;
        int left = center - colWidth - 5;
        int right = center + 5;
        int start = page * PER_PAGE;

        for (int i = 0; i < PER_PAGE; i++) {
            int index = start + i;
            if (index >= all.size()) {
                break;
            }
            Enchantment enchantment = all.get(index);
            int x = (i % 2 == 0) ? left : right;
            int y = 36 + (i / 2) * 24;
            this.addDrawableChild(new ButtonWidget(x, y, colWidth, 20, label(enchantment), button -> {
                int max = enchantment.getMaxLevel();
                int next = (levels.getOrDefault(enchantment, 0) + 1) % (max + 1);
                levels.put(enchantment, next);
                button.setMessage(label(enchantment));
            }));
        }

        int bottom = this.height - 28;
        this.addDrawableChild(new ButtonWidget(center - 155, bottom, 30, 20,
            new LiteralText("<"), button -> changePage(-1)));
        this.addDrawableChild(new ButtonWidget(center - 120, bottom, 30, 20,
            new LiteralText(">"), button -> changePage(1)));
        this.addDrawableChild(new ButtonWidget(center - 80, bottom, 120, 20,
            new LiteralText("Terapkan"), button -> apply()));
        this.addDrawableChild(new ButtonWidget(center + 45, bottom, 110, 20,
            new LiteralText("Tutup"), button -> this.client.setScreen(null)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        ItemStack held = this.client != null && this.client.player != null
            ? this.client.player.getMainHandStack() : ItemStack.EMPTY;
        Text header = held.isEmpty() ? new LiteralText("Pegang alat di tangan dulu") : held.getName();
        drawCenteredText(matrices, this.textRenderer, header, this.width / 2, 10, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer,
            new LiteralText("Halaman " + (page + 1) + "/" + pageCount()), this.width / 2, 22, 0xAAAAAA);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
