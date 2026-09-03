package ru.kotlovan.mod;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ClickGuiScreen extends Screen {

    private static final String[] CATS = {"Движение", "Комбат", "Игрок", "Система"};

    private final KotlovanClient k = KotlovanMod.client();
    private int panelX;
    private int panelY;
    private int selectedCat = 0;

    private List<Module> modules;
    private TextFieldWidget nameField;
    private TextFieldWidget fieldRadius;
    private TextFieldWidget fieldSpeed;
    private TextFieldWidget fieldRange;
    private int fieldsOffsetY;
    private int modulePanelH;

    private static class Module {
        final String name;
        final int cat;
        final BooleanSupplier isOn;
        final Runnable toggle;
        Module(String name, int cat, BooleanSupplier isOn, Runnable toggle) {
            this.name = name;
            this.cat = cat;
            this.isOn = isOn;
            this.toggle = toggle;
        }
    }

    public ClickGuiScreen() {
        super(new LiteralText("Котлован v2"));
    }

    private void buildModules() {
        modules = new ArrayList<>();
        // Движение (0)
        modules.add(new Module("Fly", 0, k::isFly, k::toggleFly));
        modules.add(new Module("Speed", 0, k::isSpeed, k::toggleSpeed));
        modules.add(new Module("Sprint", 0, k::isSprint, k::toggleSprint));
        modules.add(new Module("Step", 0, k::isStep, k::toggleStep));
        modules.add(new Module("Glide", 0, k::isGlide, k::toggleGlide));
        modules.add(new Module("AirJump", 0, k::isAirJump, k::toggleAirJump));
        modules.add(new Module("Spider", 0, k::isSpider, k::toggleSpider));
        modules.add(new Module("LongJump", 0, k::isLongJump, k::toggleLongJump));
        modules.add(new Module("Freecam", 0, k::isFreecam, k::toggleFreecam));
        // Комбат (1)
        modules.add(new Module("KillAura", 1, k::isKillAura, k::toggleKillAura));
        modules.add(new Module("Criticals", 1, k::isCriticals, k::toggleCriticals));
        modules.add(new Module("AutoSword", 1, k::isAutoSword, k::toggleAutoSword));
        modules.add(new Module("InstantAttack", 1, k::isInstantAttack, k::toggleInstantAttack));
        modules.add(new Module("AutoTool", 1, k::isAutoTool, k::toggleAutoTool));
        // Игрок (2)
        modules.add(new Module("NoFall", 2, k::isNoFall, k::toggleNoFall));
        modules.add(new Module("Nuker", 2, k::isNuker, k::toggleNuker));
        modules.add(new Module("ChestStealer", 2, k::isChestStealer, k::toggleChestStealer));
        // Система (3)
        modules.add(new Module("FullBright", 3, k::isFullBright, k::toggleFullBright));
        modules.add(new Module("HideHud", 3, k::isHideHud, k::toggleHideHud));
    }

    @Override
    protected void init() {
        buildModules();
        panelX = 30;
        panelY = 40;
        nameField = null;
        fieldRadius = null;
        fieldSpeed = null;
        fieldRange = null;
        addButtons();
    }

    private static final int ROW_W = 130;
    private static final int ROW_H = 17;

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.textRenderer.drawWithShadow(matrices, "§l§bКотлован §7v2.0  §8[ПКМ/RMB=ввод|Ctrl+LMB]", 6, 6, 0xFFFFFF);

        if (modulePanelH > 0) {
            net.minecraft.client.gui.DrawableHelper.fill(matrices,
                    panelX - 2, panelY + 22, panelX + ROW_W + 2, panelY + 24 + modulePanelH, 0xC011111A);
        }

        super.render(matrices, mouseX, mouseY, delta);

        int lx = panelX + ROW_W + 26;
        int ly = fieldsOffsetY;
        if (nameField != null) {
            textRenderer.drawWithShadow(matrices, "§7Имя (NameSpoof) — пусто = выкл", lx, ly - 12, 0xFFFFFF);
            nameField.render(matrices, mouseX, mouseY, delta);
            ly += 42;
        }
        if (fieldRadius != null) {
            textRenderer.drawWithShadow(matrices, "§7Радиус Nuker (1-8)", lx, ly - 12, 0xFFFFFF);
            fieldRadius.render(matrices, mouseX, mouseY, delta);
            ly += 42;
        }
        if (fieldSpeed != null) {
            textRenderer.drawWithShadow(matrices, "§7Speed множитель (1-10)", lx, ly - 12, 0xFFFFFF);
            fieldSpeed.render(matrices, mouseX, mouseY, delta);
            ly += 42;
        }
        if (fieldRange != null) {
            textRenderer.drawWithShadow(matrices, "§7KillAura дальность (1-10)", lx, ly - 12, 0xFFFFFF);
            fieldRange.render(matrices, mouseX, mouseY, delta);
        }
    }

    private void addButtons() {
        // Категории
        int tx = panelX;
        int maxW = 0;
        for (String c : CATS) {
            int w = textRenderer.getWidth(c) + 18;
            if (w > maxW) maxW = w;
        }
        for (int c = 0; c < CATS.length; c++) {
            final int cat = c;
            this.addButton(new ButtonWidget(tx, panelY, maxW, 18,
                    new LiteralText((selectedCat == c ? "§b" : "§7") + "▌ " + CATS[c]),
                    b -> { selectedCat = cat; this.rebuild(); }));
            tx += maxW + 2;
        }

        // Модули категории
        int y = panelY + 25;
        int count = 0;
        for (Module m : modules) {
            if (m.cat != selectedCat) continue;
            count++;
            boolean on = m.isOn.getAsBoolean();
            this.addButton(new ButtonWidget(panelX, y, ROW_W, ROW_H,
                    new LiteralText((on ? "§a" : "§7") + "▌ " + (on ? "§f" : "§8") + m.name),
                    b -> { m.toggle.run(); this.rebuild(); }));
            y += ROW_H + 2;
        }
        int moduleH = count * (ROW_H + 2) + 2;
        this.modulePanelH = moduleH;

        // Правая панель
        int sx = panelX + ROW_W + 26;
        addSettingsPanel(sx, panelY);
    }

    private void addSettingsPanel(int sx, int sy) {
        int sw = 190;

        this.addButton(new ButtonWidget(sx, sy, sw, 20,
                new LiteralText("§c§l⚠ ТРЕВОГА ⚠"), b -> { k.panic(); this.rebuild(); }));

        this.addButton(new ButtonWidget(sx, sy + 24, sw, 18,
                new LiteralText("§a✅ Сохранить конфиг"), b -> { k.updateConfig(); KotlovanMod.chat("Конфиг сохранён!"); }));

        int yy = sy + 52;
        fieldsOffsetY = yy;

        nameField = new TextFieldWidget(textRenderer, sx, yy, sw, 18, new LiteralText("name"));
        nameField.setMaxLength(16);
        nameField.setText(k.getName());
        nameField.setChangedListener(s -> k.setName(s));
        this.addChild(nameField);
        yy += 42;

        fieldRadius = new TextFieldWidget(textRenderer, sx, yy, sw, 18, new LiteralText("radius"));
        fieldRadius.setMaxLength(4);
        fieldRadius.setText(String.valueOf(k.getNukerRadius()));
        fieldRadius.setChangedListener(s -> {
            try { k.setNukerRadius(Integer.parseInt(s.trim())); } catch (Exception ignored) { }
        });
        this.addChild(fieldRadius);
        yy += 42;

        fieldSpeed = new TextFieldWidget(textRenderer, sx, yy, sw, 18, new LiteralText("speed"));
        fieldSpeed.setMaxLength(6);
        fieldSpeed.setText(String.valueOf(k.getSpeedMul()));
        fieldSpeed.setChangedListener(s -> {
            try { k.setSpeedMul(Double.parseDouble(s.trim().replace(',', '.'))); } catch (Exception ignored) { }
        });
        this.addChild(fieldSpeed);
        yy += 42;

        fieldRange = new TextFieldWidget(textRenderer, sx, yy, sw, 18, new LiteralText("range"));
        fieldRange.setMaxLength(6);
        fieldRange.setText(String.valueOf(k.getKillAuraRange()));
        fieldRange.setChangedListener(s -> {
            try { k.setKillAuraRange(Double.parseDouble(s.trim().replace(',', '.'))); } catch (Exception ignored) { }
        });
        this.addChild(fieldRange);
    }

    private void rebuild() {
        this.init(this.client, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, button);
        if (fieldRadius != null) fieldRadius.mouseClicked(mouseX, mouseY, button);
        if (fieldSpeed != null) fieldSpeed.mouseClicked(mouseX, mouseY, button);
        if (fieldRange != null) fieldRange.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField != null && nameField.isFocused()) return nameField.keyPressed(keyCode, scanCode, modifiers);
        if (fieldRadius != null && fieldRadius.isFocused()) return fieldRadius.keyPressed(keyCode, scanCode, modifiers);
        if (fieldSpeed != null && fieldSpeed.isFocused()) return fieldSpeed.keyPressed(keyCode, scanCode, modifiers);
        if (fieldRange != null && fieldRange.isFocused()) return fieldRange.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (nameField != null && nameField.isFocused()) return nameField.charTyped(chr, keyCode);
        if (fieldRadius != null && fieldRadius.isFocused()) return fieldRadius.charTyped(chr, keyCode);
        if (fieldSpeed != null && fieldSpeed.isFocused()) return fieldSpeed.charTyped(chr, keyCode);
        if (fieldRange != null && fieldRange.isFocused()) return fieldRange.charTyped(chr, keyCode);
        return super.charTyped(chr, keyCode);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
