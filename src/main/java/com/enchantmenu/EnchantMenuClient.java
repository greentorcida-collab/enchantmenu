package com.enchantmenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.LiteralText;

public class EnchantMenuClient implements ClientModInitializer {
    private static final int SIZE = 18;

    private static int buttonX(HandledScreen<?> screen) {
        int x = screen.x + screen.backgroundWidth + 4;
        if (x + SIZE > screen.width) {
            x = screen.x - SIZE - 4;
        }
        return x;
    }

    private static int buttonY(HandledScreen<?> screen) {
        return screen.y + 4;
    }

    private static boolean inside(HandledScreen<?> screen, double mx, double my) {
        int x = buttonX(screen);
        int y = buttonY(screen);
        return mx >= x && mx < x + SIZE && my >= y && my < y + SIZE;
    }

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen handled)) {
                return;
            }

            ScreenEvents.afterRender(screen).register((s, matrices, mouseX, mouseY, tickDelta) -> {
                int x = buttonX(handled);
                int y = buttonY(handled);
                DrawableHelper.fill(matrices, x, y, x + SIZE, y + SIZE, 0xFF373737);
                DrawableHelper.fill(matrices, x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, 0xFF7A3FBF);
                client.textRenderer.drawWithShadow(matrices, "E", x + 6, y + 5, 0xFFFFFFFF);
                if (inside(handled, mouseX, mouseY)) {
                    s.renderTooltip(matrices, new LiteralText("Enchant"), mouseX, mouseY);
                }
            });

            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> {
                if (inside(handled, mouseX, mouseY)) {
                    MinecraftClient.getInstance().setScreen(new EnchantScreen());
                    return false;
                }
                return true;
            });
        });
    }
}
