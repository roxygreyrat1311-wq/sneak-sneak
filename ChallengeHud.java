package com.example.shiftchallenge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

/** Tampilan counter di layar saat challenge berjalan */
public final class ChallengeHud {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int GOLD = 0xFFFFAA00;
    private static final int GRAY = 0xFFAAAAAA;

    private ChallengeHud() { }

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        if (ChallengeManager.state == ChallengeManager.State.IDLE) return;

        TextRenderer tr = mc.textRenderer;
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        float cx = w / 2f;
        long now = System.currentTimeMillis();

        switch (ChallengeManager.state) {
            case COUNTDOWN -> {
                long left = Math.max(0, ChallengeManager.countdownEndMs - now);
                int sec = (int) Math.ceil(left / 1000.0);
                drawScaled(ctx, tr, "Bersiap...", cx, h * 0.22f, 1.5f, GRAY);
                drawScaled(ctx, tr, String.valueOf(Math.max(1, sec)), cx, h * 0.30f, 6f, GOLD);
                drawScaled(ctx, tr, ChallengeManager.runTarget + " shift / " + ChallengeManager.runSeconds + " detik",
                        cx, h * 0.52f, 1.2f, WHITE);
            }
            case RUNNING -> {
                long total = ChallengeManager.runSeconds * 1000L;
                long elapsed = now - ChallengeManager.startMs;
                long left = Math.max(0, total - elapsed);
                double expected = Math.min(1.0, elapsed / (double) total);
                double actual = Math.min(1.0, ChallengeManager.count / (double) ChallengeManager.runTarget);
                int color = actual >= expected ? GREEN : RED;

                drawScaled(ctx, tr, String.valueOf(ChallengeManager.count), cx, 18, 5f, color);
                drawScaled(ctx, tr, "/ " + ChallengeManager.runTarget, cx, 62, 1.5f, GRAY);
                drawScaled(ctx, tr, String.format("%.1fs", left / 1000.0), cx, 80, 2.5f, WHITE);

                int barW = 200;
                int x1 = (int) cx - barW / 2;
                // bar progress shift
                ctx.fill(x1 - 1, 106, x1 + barW + 1, 116, 0xFF000000);
                ctx.fill(x1, 107, x1 + (int) (barW * actual), 115, color);
                // bar waktu
                ctx.fill(x1 - 1, 120, x1 + barW + 1, 126, 0xFF000000);
                ctx.fill(x1, 121, x1 + (int) (barW * (1.0 - expected)), 125, 0xFF5555FF);
            }
            case FINISHED -> {
                boolean won = ChallengeManager.won;
                drawScaled(ctx, tr, won ? "MENANG!" : "WAKTU HABIS!", cx, 18, 4f, won ? GOLD : RED);
                drawScaled(ctx, tr, ChallengeManager.count + " / " + ChallengeManager.runTarget + " shift",
                        cx, 58, 2f, WHITE);
                drawScaled(ctx, tr, String.format("%.1f shift/detik", ChallengeManager.lastCps), cx, 78, 1.5f, GRAY);
                if (ChallengeManager.newRecord) {
                    drawScaled(ctx, tr, "REKOR BARU!", cx, 94, 1.5f, GOLD);
                }
            }
            default -> { }
        }
    }

    private static void drawScaled(DrawContext ctx, TextRenderer tr, String text, float cx, float y, float scale, int color) {
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(cx, y, 0);
        m.scale(scale, scale, 1f);
        ctx.drawCenteredTextWithShadow(tr, text, 0, 0, color);
        m.pop();
    }
}
