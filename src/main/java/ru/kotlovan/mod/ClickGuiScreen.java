package ru.kotlovan.mod;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private static final String[] CATS = {"Движение", "Комбат", "Игрок", "Рендер"};

    private final KotlovanClient k = KotlovanMod.client();
    private int panelX;
    private int panelY;
    private int selectedCat = 0;
    private String selectedModule;

    public ClickGuiScreen() {
        super(new LiteralText("Котлован v2"));
    }

    private static class Module {
        final String name;
        final int cat;
        final java.util.function.BooleanSupplier isOn;
        final Runnable toggle;
        Module(String name, int cat, java.util.function.BooleanSupplier isOn, Runnable toggle) {
            this.name = name;
            this.cat = cat;
            this.isOn = isOn;
            this.toggle = toggle;
        }
    }

    private List<Module> modules;

    private void buildModules() {
        modules = new ArrayList<>();
        // Движение (0)
        modules.add(new Module("Fly", 0, k::isFly, k::toggleFly));
        modules.add(new Module("Speed", 0, k::isSpeed, k::toggleSpeed));
        modules.add(new Module("Sprint", 0, k::isSprint, k::toggleSprint));
        modules.add(new Module("Step", 0, k::isStep, k::toggleStep));
        modules.add(new Module("AirJump", 0, k::isAirJump, k::toggleAirJump));
        modules.add(new Module("Spider", 0, k::isSpider, k::toggleSpider));
        // Комбат (1)
        modules.add(new Module("KillAura", 1, k::isKillAura, k::toggleKillAura));
        modules.add(new Module("InstantAttack", 1, k::isInstantAttack, k::toggleInstantAttack));
        modules.add(new Module("AutoTool", 1, k::isAutoTool, k::toggleAutoTool));
        // Игрок (2)
        modules.add(new Module("NoFall", 2, k::isNoFall, k::toggleNoFall));
        modules.add(new Module("Nuker", 2, k::isNuker, k::toggleNuker));
        // Рендер (3)
        modules.add(new Module("FullBright", 3, k::isFullBright, k::toggleFullBright));
    }

    @Override
    protected void init() {
        buildModules();
        panelX = 40;
        panelY = 40;
        this.addButtons();
    }

    private static final int ROW_W = 120;
    private static final int ROW_H = 17;

    private void addButtons() {
        // Категории
        int tx = panelX;
        for (int c = 0; c < CATS.length; c++) {
            final int cat = c;
            int tw = textRenderer.getWidth(CATS[c]) + 16;
            this.addButton(new ButtonWidget(tx, panelY, tw, 18,
                    new LiteralText((selectedCat == c ? "§b" : "§7") + CATS[c]),
                    b -> { selectedCat = cat; selectedModule = null; this.rebuild(); }));
            tx += tw;
        }
        // Модули категории
        int y = panelY + 24;
        for (Module m : modules) {
            if (m.cat != selectedCat) continue;
            boolean on = m.isOn.getAsBoolean();
            String sel = selectedModule != null && selectedModule.equals(m.name) ? " »" : "";
            this.addButton(new ButtonWidget(panelX, y, ROW_W, ROW_H,
                    new LiteralText((on ? "§a" : "§7") + "▌ " + (on ? "§f" : "§8") + m.name + sel),
                    b -> {
                        m.toggle.run();
                        this.rebuild();
                    }));
            y += ROW_H + 2;
        }
        // Панель настроек выбранного модуля
        if (selectedModule != null) {
            addSettingsPanel();
        }
    }

    private void addSettingsPanel() {
        int sx = panelX + ROW_W + 22;
        int sy = panelY + 24;
        int sw = 150;

        addButton(new ButtonWidget(sx, sy, sw, 18, new LiteralText("§l" + selectedModule), b -> {}));

        if ("Nuker".equals(selectedModule)) {
            String[] vals = {"1", "2", "3", "4", "5", "6", "7", "8"};
            String cur = String.valueOf(k.getNukerRadius());
            addButton(new ButtonWidget(sx, sy + 24, sw, 18,
                    new LiteralText("§7Радиус: §f" + cur), b -> {
                int next = (k.getNukerRadius() % 8) + 1;
                k.setNukerRadius(next);
                this.rebuild();
            }));
        } else if ("Speed".equals(selectedModule)) {
            double[] vals = {1.5, 2.0, 2.5, 3.0, 4.0, 5.0};
            addButton(new ButtonWidget(sx, sy + 24, sw, 18,
                    new LiteralText("§7Множитель: §f" + fmtCur(vals, k.getSpeedMul())), b -> {
                double next = cycle(vals, k.getSpeedMul());
                k.setSpeedMul(next);
                this.rebuild();
            }));
        } else if ("KillAura".equals(selectedModule)) {
            double[] vals = {3.0, 3.5, 4.0, 4.5, 5.0, 6.0, 7.0};
            addButton(new ButtonWidget(sx, sy + 24, sw, 18,
                    new LiteralText("§7Дальность: §f" + fmtCur(vals, k.getKillAuraRange())), b -> {
                double next = cycle(vals, k.getKillAuraRange());
                k.setKillAuraRange(next);
                this.rebuild();
            }));
        } else {
            addButton(new ButtonWidget(sx, sy + 24, sw, 18, new LiteralText("§7Нет опций"), b -> {}));
        }

        addButton(new ButtonWidget(sx, sy + 50, sw, 18, new LiteralText("§c← Назад"),
                b -> { selectedModule = null; this.rebuild(); }));
    }

    private void rebuild() {
        this.init(this.client, this.width, this.height);
    }

    private double cycle(double[] vals, double cur) {
        for (int i = 0; i < vals.length; i++) {
            if (Math.abs(vals[i] - cur) < 0.01) return vals[(i + 1) % vals.length];
        }
        return vals[0];
    }

    private String fmtCur(double[] vals, double cur) {
        String s = "";
        for (double v : vals) if (Math.abs(v - cur) < 0.01) s = format(v);
        if (s.isEmpty()) s = format(cur);
        return s;
    }

    private String format(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.valueOf(d);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // Сверху слева название
        this.textRenderer.drawWithShadow(matrices, "§l§bКотлован §7v2.0", 6, 6, 0xFFFFFF);

        // Панель под табами (фон)
        int modCount = 0;
        for (Module m : modules) if (m.cat == selectedCat) modCount++;
        int moduleH = modCount * (ROW_H + 2) + 4;
        net.minecraft.client.gui.DrawableHelper.fill(matrices, panelX - 2, panelY + 20, panelX + ROW_W + 2, panelY + 22 + moduleH, 0xC011111A);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
