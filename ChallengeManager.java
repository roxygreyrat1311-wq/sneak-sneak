package com.example.shiftchallenge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

/** Logika challenge: countdown -> running -> finished */
public final class ChallengeManager {
    public enum State { IDLE, COUNTDOWN, RUNNING, FINISHED }

    public static final int COUNTDOWN_MS = 3000;
    public static final int RESULT_SHOW_MS = 8000;

    public static State state = State.IDLE;
    public static int count = 0;
    public static int runTarget = 500;
    public static int runSeconds = 30;
    public static long countdownEndMs, startMs, endMs, finishedAtMs;
    public static boolean won = false;
    public static double lastCps = 0;
    public static boolean newRecord = false;

    private ChallengeManager() { }

    public static boolean isActive() {
        return state == State.COUNTDOWN || state == State.RUNNING;
    }

    public static void start() {
        runTarget = ChallengeConfig.target;
        runSeconds = ChallengeConfig.seconds;
        count = 0;
        won = false;
        newRecord = false;
        countdownEndMs = System.currentTimeMillis() + COUNTDOWN_MS;
        state = State.COUNTDOWN;
    }

    public static void stop() {
        state = State.IDLE;
    }

    public static void toggle() {
        if (isActive()) stop(); else start();
    }

    /** Ambil semua penekanan Shift sejak tick terakhir (bisa >1 per tick). */
    private static int drainPresses(MinecraftClient client) {
        KeyBinding sneak = client.options.sneakKey;
        int n = 0;
        while (sneak.wasPressed()) n++;
        return n;
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) { state = State.IDLE; return; }

        int pressed = drainPresses(client);
        long now = System.currentTimeMillis();

        switch (state) {
            case COUNTDOWN -> {
                if (now >= countdownEndMs) {
                    count = 0; // tekanan sebelum GO tidak dihitung
                    startMs = now;
                    endMs = now + runSeconds * 1000L;
                    state = State.RUNNING;
                }
            }
            case RUNNING -> {
                count += pressed;
                if (count >= runTarget) {
                    finish(true, now - startMs, now);
                } else if (now >= endMs) {
                    finish(false, runSeconds * 1000L, now);
                }
            }
            case FINISHED -> {
                if (now - finishedAtMs > RESULT_SHOW_MS) state = State.IDLE;
            }
            default -> { }
        }
    }

    private static void finish(boolean win, long elapsedMs, long now) {
        won = win;
        finishedAtMs = now;
        lastCps = count / Math.max(0.001, elapsedMs / 1000.0);
        if (lastCps > ChallengeConfig.bestCps) {
            ChallengeConfig.bestCps = lastCps;
            newRecord = true;
            ChallengeConfig.save();
        }
        state = State.FINISHED;
    }
}
