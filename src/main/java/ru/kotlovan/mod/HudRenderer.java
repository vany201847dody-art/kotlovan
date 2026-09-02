package ru.kotlovan.mod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {

    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final List<String[]> modules = new ArrayList<>();

    public static void render(MatrixStack matrices, float tickDelta) {
        if (MC.options == null || MC.options.debugEnabled) return;
        if (MC.player == null || MC.world == null) return;

        modules.clear();
        KotlovanClient k = KotlovanMod.client();
        if (k.isNuker()) modules.add(new String[]{"Nuker", k.getNukerRadius() + "r"});
        if (k.isFly()) modules.add(new String[]{"Fly", ""});
        if (k.isSpeed()) modules.add(new String[]{"Speed", "x" + fmt(k.getSpeedMul())});
        if (k.isSprint()) modules.add(new String[]{"AutoSprint", ""});
        if (k.isNoFall()) modules.add(new String[]{"NoFall", ""});
        if (k.isKillAura()) modules.add(new String[]{"KillAura", fmt(k.getKillAuraRange())});
        if (k.isFullBright()) modules.add(new String[]{"FullBright", ""});
        if (k.isStep()) modules.add(new String[]{"Step", ""});
        if (k.isAirJump()) modules.add(new String[]{"AirJump", ""});
        if (k.isSpider()) modules.add(new String[]{"Spider", ""});
        if (k.isAutoTool()) modules.add(new String[]{"AutoTool", ""});
        if (k.isInstantAttack()) modules.add(new String[]{"InstantAttack", ""});

        if (modules.isEmpty()) return;

        int x = 4;
        int y = 4;
        int panelW = 0;
        // Рассчитываем ширину под самый длинный модуль
        for (String[] m : modules) {
            int w = MC.textRenderer.getWidth("  " + m[0] + (m[1].isEmpty() ? "" : " " + m[1]));
            if (w > panelW) panelW = w;
        }
        panelW += 8;

        int h = modules.size() * 12 + 8;
        net.minecraft.client.gui.DrawableHelper.fill(matrices, x - 3, y - 3, x + panelW - 4, y + h, 0x90101018);
        net.minecraft.client.gui.DrawableHelper.fill(matrices, x - 3, y - 3, x + panelW - 4, y + 4, 0xFF00e5a0);

        int yy = y;
        for (String[] m : modules) {
            String color = moduleColor(m[0]);
            String txt = "§7> " + color + m[0] + (m[1].isEmpty() ? "" : " §8[§f" + m[1] + "§8]");
            MC.textRenderer.drawWithShadow(matrices, txt, x, yy, 0xFFFFFF);
            yy += 12;
        }
    }

    private static String moduleColor(String name) {
        switch (name) {
            case "Nuker": return "\u00a7c";
            case "KillAura": return "\u00a74";
            case "Fly": return "\u00a7b";
            case "Speed": return "\u00a7e";
            default: return "\u00a7a";
        }
    }

    private static String fmt(double d) {
        if (d == Math.floor(d)) return String.valueOf((int) d);
        return String.valueOf(d);
    }
}
