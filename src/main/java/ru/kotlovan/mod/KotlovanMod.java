package ru.kotlovan.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class KotlovanMod implements ClientModInitializer {

    private static final String CAT = "Котлован";

    public static KeyBinding NUKER_KEY;
    public static KeyBinding FLY_KEY;
    public static KeyBinding SPEED_KEY;
    public static KeyBinding SPRINT_KEY;
    public static KeyBinding NOFALL_KEY;
    public static KeyBinding KILLAURA_KEY;
    public static KeyBinding FULLBRIGHT_KEY;
    public static KeyBinding STEP_KEY;
    public static KeyBinding AIRJUMP_KEY;
    public static KeyBinding SPIDER_KEY;
    public static KeyBinding AUTOTOOL_KEY;
    public static KeyBinding INSTANT_KEY;
    public static KeyBinding GLIDE_KEY;
    public static KeyBinding LONGJUMP_KEY;
    public static KeyBinding CRITICALS_KEY;
    public static KeyBinding AUTOSWORD_KEY;
    public static KeyBinding FREECAM_KEY;
    public static KeyBinding CHESTSTEAL_KEY;
    public static KeyBinding HIDEHUD_KEY;
    public static KeyBinding PANIC_KEY;
    public static KeyBinding GUI_KEY;

    private static final KotlovanClient CLIENT = new KotlovanClient();
    private static boolean configLoaded = false;

    public static KotlovanClient client() {
        return CLIENT;
    }

    @Override
    public void onInitializeClient() {
        NUKER_KEY = reg("Nuker", GLFW.GLFW_KEY_R);
        FLY_KEY = reg("Fly", GLFW.GLFW_KEY_F);
        SPEED_KEY = reg("Speed", GLFW.GLFW_KEY_C);
        SPRINT_KEY = reg("AutoSprint", GLFW.GLFW_KEY_P);
        NOFALL_KEY = reg("NoFall", GLFW.GLFW_KEY_N);
        KILLAURA_KEY = reg("KillAura", GLFW.GLFW_KEY_H);
        FULLBRIGHT_KEY = reg("FullBright", GLFW.GLFW_KEY_V);
        STEP_KEY = reg("Step", GLFW.GLFW_KEY_U);
        AIRJUMP_KEY = reg("AirJump", GLFW.GLFW_KEY_J);
        SPIDER_KEY = reg("Spider", GLFW.GLFW_KEY_K);
        AUTOTOOL_KEY = reg("AutoTool", GLFW.GLFW_KEY_T);
        INSTANT_KEY = reg("InstantAttack", GLFW.GLFW_KEY_I);
        GLIDE_KEY = reg("Glide", GLFW.GLFW_KEY_G);
        LONGJUMP_KEY = reg("LongJump", GLFW.GLFW_KEY_B);
        CRITICALS_KEY = reg("Criticals", GLFW.GLFW_KEY_Y);
        AUTOSWORD_KEY = reg("AutoSword", GLFW.GLFW_KEY_LEFT_ALT);
        FREECAM_KEY = reg("Freecam", GLFW.GLFW_KEY_Z);
        CHESTSTEAL_KEY = reg("ChestStealer", GLFW.GLFW_KEY_X);
        HIDEHUD_KEY = reg("Скрыть HUD", GLFW.GLFW_KEY_INSERT);
        PANIC_KEY = reg("ТРЕВОГА", GLFW.GLFW_KEY_DELETE);
        GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Открыть ClickGUI", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CAT));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!configLoaded) {
                configLoaded = true;
                CLIENT.loadConfig();
            }
            if (mc.world == null || mc.player == null) return;
            CLIENT.tick();
            while (GUI_KEY.wasPressed()) {
                if (mc.currentScreen == null) {
                    mc.openScreen(new ClickGuiScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register(HudRenderer::render);
    }

    private static KeyBinding reg(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, CAT));
    }

    public static void chat(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(new LiteralText("§8[§6Котлован§8] §7" + msg), false);
        }
    }
}
