package com.example.shiftchallenge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ShiftChallengeClient implements ClientModInitializer {
    private static final String CATEGORY = "key.category.shiftchallenge";

    private static KeyBinding menuKey;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        ChallengeConfig.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shiftchallenge.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shiftchallenge.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new ChallengeScreen());
                }
            }
            while (toggleKey.wasPressed()) {
                if (client.player != null) ChallengeManager.toggle();
            }
            ChallengeManager.tick(client);
        });

        HudRenderCallback.EVENT.register(ChallengeHud::render);

        // Command opsional: /shiftchallenge start [target] [detik] | stop
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("shiftchallenge")
                .then(ClientCommandManager.literal("start")
                    .executes(ctx -> { ChallengeManager.start(); return 1; })
                    .then(ClientCommandManager.argument("target", IntegerArgumentType.integer(1, 100000))
                        .then(ClientCommandManager.argument("seconds", IntegerArgumentType.integer(1, 3600))
                            .executes(ctx -> {
                                ChallengeConfig.target = IntegerArgumentType.getInteger(ctx, "target");
                                ChallengeConfig.seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                ChallengeManager.start();
                                return 1;
                            }))))
                .then(ClientCommandManager.literal("stop")
                    .executes(ctx -> { ChallengeManager.stop(); return 1; }))
            ));
    }
}
