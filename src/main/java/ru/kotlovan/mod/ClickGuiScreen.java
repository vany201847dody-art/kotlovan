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

    private static final String[] CATS = {"Движение", "Комбат", "Игрок", "Рендер", "Система"};
    private static final int[] CAT_COLOR = {0x00e5ff, 0xff4d6d, 0xa3ff12, 0xffd60a, 0xff7ac8};

    private final KotlovanClient k = KotlovanMod.client();
    private int panelX;
    private int panelY;
    private int selectedCat = 0;

    private List<Module> modules;
    private TextFieldWidget nameField;
    private TextFieldWidget fieldRadius;
    private TextFieldWidget fieldSpeed;
    private TextFieldWidget fieldRange;
    private TextFieldWidget fieldTimer;
    private TextFieldWidget fieldVelocity;
    private int fieldsOffsetY;

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
        modules.add(new Module("Timer", 0, k::isTimer, k::toggleTimer));
        modules.add(new Module("AirJump", 0, k::isAirJump, k::toggleAirJump));
        modules.add(new Module("Spider", 0, k::isSpider, k::toggleSpider));
        modules.add(new Module("LongJump", 0, k::isLongJump, k::toggleLongJump));
        modules.add(new Module("FastLadder", 0, k::isFastLadder, k::toggleFastLadder));
        modules.add(new Module("SafeWalk", 0, k::isSafeWalk, k::toggleSafeWalk));
        modules.add(new Module("NoWeb", 0, k::isNoWeb, k::toggleNoWeb));
        modules.add(new Module("Velocity", 0, k::isVelocity, k::toggleVelocity));
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
        // Рендер (3)
        modules.add(new Module("FullBright", 3, k::isFullBright, k::toggleFullBright));
        modules.add(new Module("ESP", 3, k::isEsp, k::toggleEsp));
        modules.add(new Module("Tracers", 3, k::isTracers, k::toggleTracers));
        modules.add(new Module("X-Ray", 3, k::isXray, k::toggleXray));
        modules.add(new Module("NoRender", 3, k::isNoRender, k::toggleNoRender));
        // Система (4)
        modules.add(new Module("HideHud", 4, k::isHideHud, k::toggleHideHud));
    }

    @Override
    protected void init() {
        buildModules();
        panelX = 30;
        panelY = 46;
        nameField = null;
        fieldRadius = null;
        fieldSpeed = null;
        fieldRange = null;
        fieldTimer = null;
        fieldVelocity = null;
        addButtons();
    }

    private static final int ROW_W = 150;
    private static final int ROW_H = 18;

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // Неоновый заголовок
        neonFill(matrices, panelX - 4, panelY - 32, panelX + 470, panelY - 28, 0xff00e5ff, 0xff00e5ff);

        this.textRenderer.drawWithShadow(matrices, "§l§bКотлован §7v2.0 §8— §7" + CATS[selectedCat],
                panelX - 2, panelY - 24, 0xFFFFFF);

        // Подложка модулей
        int count = 0;
        for (Module m : modules) if (m.cat == selectedCat) count++;
        int moduleH = count * (ROW_H + 2) + 2;
        neonFill(matrices, panelX - 4, panelY + 22, panelX + ROW_W + 2, panelY + 26 + moduleH, 0xcc0a0d18, 0xcc07101e);

        super.render(matrices, mouseX, mouseY, delta);

        // Поля настроек с подписями
        int sx = panelX + ROW_W + 26;
        int ly = fieldsOffsetY;
        TextFieldWidget[] fields = {nameField, fieldRadius, fieldSpeed, fieldRange, fieldTimer, fieldVelocity};
        String[] labels = {"Имя (NameSpoof): пусто = выкл", "Радиус Nuker", "Speed x", "KA Дальность", "Timer x (1-10)", "Velocity (0-1)"};
        for (int i = 0; i < fields.length; i++) {
            TextFieldWidget f = fields[i];
            if (f == null) continue;
            neonFill(matrices, sx - 6, ly - 12, sx + 186, ly + 20, 0xcc0a0d18, 0xcc07101e);
            this.textRenderer.drawWithShadow(matrices, "§7" + labels[i], sx, ly - 10, 0x88aabb);
            f.render(matrices, mouseX, mouseY, delta);
            ly += 42;
        }
    }

    private void neonFill(MatrixStack m, int x1, int y1, int x2, int y2, int c1, int c2) {
        net.minecraft.client.gui.DrawableHelper.fill(m, x1, y1, x2, y2, 0x33000000);
        this.fillGradient(m, x1, y1, x2, y2, c1, c2);
    }

    private void addButtons() {
        // Категории (верхняя неоновая лента)
        int tx = panelX;
        int maxW = 0;
        for (String c : CATS) {
            int w = textRenderer.getWidth(c) + 20;
            if (w > maxW) maxW = w;
        }
        for (int c = 0; c < CATS.length; c++) {
            final int cat = c;
            boolean sel = selectedCat == c;
            int col = CAT_COLOR[c];
            String hex = String.format("#%06X", col);
            this.addButton(new ButtonWidget(tx, panelY, maxW, 18,
                    new LiteralText((sel ? "§l§b" : "§7") + "▌ " + CATS[c]),
                    b -> { selectedCat = cat; this.rebuild(); }));
            tx += maxW + 2;
        }

        // Модули категории — неоновые
        int y = panelY + 25;
        int count = 0;
        for (Module m : modules) {
            if (m.cat != selectedCat) continue;
            count++;
            boolean on = m.isOn.getAsBoolean();
            String prefix = on ? "§a●" : "§8○";
            this.addButton(new ButtonWidget(panelX, y, ROW_W, ROW_H,
                    new LiteralText(prefix + " " + (on ? "§f" : "§7") + m.name),
                    b -> { m.toggle.run(); this.rebuild(); }));
            y += ROW_H + 2;
        }

        // Правая панель: настройки + кнопки
        int sx = panelX + ROW_W + 26;
        addSettingsPanel(sx, panelY);
    }

    private void addSettingsPanel(int sx, int sy) {
        int sw = 180;

        this.addButton(new ButtonWidget(sx, sy, sw, 20,
                new LiteralText("§c§l⚠ ТРЕВОГА ⚠"), b -> { k.panic(); this.rebuild(); }));

        this.addButton(new ButtonWidget(sx, sy + 24, sw, 18,
                new LiteralText("§a✓ Сохранить конфиг"), b -> { k.updateConfig(); KotlovanMod.chat("Конфиг сохранён!"); }));

        int yy = sy + 54;
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
        yy += 42;

        fieldTimer = new TextFieldWidget(textRenderer, sx, yy, sw, 18, new LiteralText("timer"));
        fieldTimer.setMaxLength(4);
        fieldTimer.setText(String.valueOf(k.getTimerMul()));
        fieldTimer.setChangedListener(s -> {
            try { k.setTimerMul(Integer.parseInt(s.trim())); } catch (Exception ignored) { }
        });
        this.addChild(fieldTimer);
        yy += 42;

        fieldVelocity = new TextFieldWidget(textRenderer, sx, yy, sw, 18, new LiteralText("velocity"));
        fieldVelocity.setMaxLength(4);
        fieldVelocity.setText(String.valueOf(k.getVelocityReduce()));
        fieldVelocity.setChangedListener(s -> {
            try { k.setVelocityReduce(Double.parseDouble(s.trim().replace(',', '.'))); } catch (Exception ignored) { }
        });
        this.addChild(fieldVelocity);
    }

    private void rebuild() {
        this.init(this.client, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        TextFieldWidget[] fields = {nameField, fieldRadius, fieldSpeed, fieldRange, fieldTimer, fieldVelocity};
        for (TextFieldWidget f : fields) {
            if (f != null) f.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        TextFieldWidget[] fields = {nameField, fieldRadius, fieldSpeed, fieldRange, fieldTimer, fieldVelocity};
        for (TextFieldWidget f : fields) {
            if (f != null && f.isFocused()) return f.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        TextFieldWidget[] fields = {nameField, fieldRadius, fieldSpeed, fieldRange, fieldTimer, fieldVelocity};
        for (TextFieldWidget f : fields) {
            if (f != null && f.isFocused()) return f.charTyped(chr, keyCode);
        }
        return super.charTyped(chr, keyCode);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
