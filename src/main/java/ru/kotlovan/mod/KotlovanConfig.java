package ru.kotlovan.mod;

import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class KotlovanConfig {

    private static Path configPath;

    private static Path path() {
        if (configPath == null) {
            configPath = FabricLoader.getInstance().getConfigDir().resolve("kotlovan.cfg");
        }
        return configPath;
    }

    public static void save(KotlovanClient k) {
        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
                w.write("fly=" + k.isFly() + "\n");
                w.write("speed=" + k.isSpeed() + "\n");
                w.write("nuker=" + k.isNuker() + "\n");
                w.write("killaura=" + k.isKillAura() + "\n");
                w.write("sprint=" + k.isSprint() + "\n");
                w.write("nofall=" + k.isNoFall() + "\n");
                w.write("fullbright=" + k.isFullBright() + "\n");
                w.write("step=" + k.isStep() + "\n");
                w.write("airjump=" + k.isAirJump() + "\n");
                w.write("spider=" + k.isSpider() + "\n");
                w.write("autotool=" + k.isAutoTool() + "\n");
                w.write("instantattack=" + k.isInstantAttack() + "\n");
                w.write("glide=" + k.isGlide() + "\n");
                w.write("longjump=" + k.isLongJump() + "\n");
                w.write("criticals=" + k.isCriticals() + "\n");
                w.write("autosword=" + k.isAutoSword() + "\n");
                w.write("cheststealer=" + k.isChestStealer() + "\n");
                w.write("hidehud=" + k.isHideHud() + "\n");
                w.write("nukerRadius=" + k.getNukerRadius() + "\n");
                w.write("speedMul=" + k.getSpeedMul() + "\n");
                w.write("killAuraRange=" + k.getKillAuraRange() + "\n");
                w.write("name=" + k.getName() + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    public static void load(KotlovanClient k) {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    int eq = line.indexOf('=');
                    if (eq < 0) continue;
                    String key = line.substring(0, eq).trim();
                    String val = line.substring(eq + 1).trim();
                    apply(k, key, val);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void apply(KotlovanClient k, String key, String val) {
        boolean b = "true".equalsIgnoreCase(val);
        try {
            switch (key) {
                case "fly": k.setFlyOn(b); break;
                case "speed": k.setSpeedOn(b); break;
                case "nuker": k.setNukerOn(b); break;
                case "killaura": k.setKillAuraOn(b); break;
                case "sprint": k.setSprintOn(b); break;
                case "nofall": k.setNoFallOn(b); break;
                case "fullbright": k.setFullBrightOn(b); break;
                case "step": k.setStepOn(b); break;
                case "airjump": k.setAirJumpOn(b); break;
                case "spider": k.setSpiderOn(b); break;
                case "autotool": k.setAutoToolOn(b); break;
                case "instantattack": k.setInstantAttackOn(b); break;
                case "glide": k.setGlideOn(b); break;
                case "longjump": k.setLongJumpOn(b); break;
                case "criticals": k.setCriticalsOn(b); break;
                case "autosword": k.setAutoSwordOn(b); break;
                case "cheststealer": k.setChestStealerOn(b); break;
                case "hidehud": k.setHideHudOn(b); break;
                case "nukerRadius": k.setNukerRadius(Integer.parseInt(val)); break;
                case "speedMul": k.setSpeedMul(Double.parseDouble(val)); break;
                case "killAuraRange": k.setKillAuraRange(Double.parseDouble(val)); break;
                case "name": k.setName(val); break;
                default: break;
            }
        } catch (Exception ignored) {
        }
    }
}
