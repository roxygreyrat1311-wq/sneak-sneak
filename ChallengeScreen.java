package com.example.shiftchallenge;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

/** Menu pengaturan challenge */
public class ChallengeScreen extends Screen {
    private ValueSlider targetSlider;
    private ValueSlider secondsSlider;

    public ChallengeScreen() {
        super(Text.literal("Shift Challenge"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 4;

        targetSlider = new ValueSlider(cx - 100, y, 200, 20, "Target Shift", 10, 2000, 10,
                ChallengeConfig.target, v -> ChallengeConfig.target = v);
        secondsSlider = new ValueSlider(cx - 100, y + 24, 200, 20, "Waktu (detik)", 5, 120, 1,
                ChallengeConfig.seconds, v -> ChallengeConfig.seconds = v);
        addDrawableChild(targetSlider);
        addDrawableChild(secondsSlider);

        // Preset cepat
        addPreset(cx - 100, y + 52, 62, "500/30", 500, 30);
        addPreset(cx - 31, y + 52, 62, "300/30", 300, 30);
        addPreset(cx + 38, y + 52, 62, "100/10", 100, 10);

        addDrawableChild(ButtonWidget.builder(Text.literal("MULAI"), b -> {
            ChallengeConfig.save();
            this.close();
            ChallengeManager.start();
        }).dimensions(cx - 100, y + 100, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Tutup"), b -> this.close())
                .dimensions(cx - 100, y + 124, 200, 20).build());
    }

    private void addPreset(int x, int y, int w, String label, int target, int seconds) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
            targetSlider.setInt(target);
            secondsSlider.setInt(seconds);
        }).dimensions(x, y, w, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);
        int cx = this.width / 2;
        int y = this.height / 4;

        ctx.drawCenteredTextWithShadow(this.textRenderer, "SHIFT CHALLENGE", cx, y - 22, 0xFFFFAA00);

        double need = ChallengeConfig.target / (double) ChallengeConfig.seconds;
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                String.format("Butuh sekitar %.1f shift/detik", need), cx, y + 78, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                String.format("Rekor kamu: %.1f shift/detik", ChallengeConfig.bestCps), cx, y + 89, 0xFFAAAAAA);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        ChallengeConfig.save();
    }

    /** Slider dengan nilai integer dan step */
    private static class ValueSlider extends SliderWidget {
        private final String label;
        private final int min, max, step;
        private final IntConsumer onChange;

        ValueSlider(int x, int y, int w, int h, String label, int min, int max, int step,
                    int initial, IntConsumer onChange) {
            super(x, y, w, h, Text.empty(), (initial - min) / (double) (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.onChange = onChange;
            updateMessage();
        }

        private int getInt() {
            double raw = min + this.value * (max - min);
            int v = (int) (Math.round(raw / step) * step);
            return ChallengeConfig.clamp(v, min, max);
        }

        void setInt(int v) {
            this.value = (ChallengeConfig.clamp(v, min, max) - min) / (double) (max - min);
            updateMessage();
            applyValue();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + getInt()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(getInt());
        }
    }
}
