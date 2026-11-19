package com.example.shiftchallenge;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Setting yang disimpan ke config/shiftchallenge.properties */
public final class ChallengeConfig {
    public static int target = 500;
    public static int seconds = 30;
    public static double bestCps = 0.0;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("shiftchallenge.properties");
    }

    public static void load() {
        try {
            if (!Files.exists(path())) return;
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(path())) { p.load(in); }
            target = clamp(Integer.parseInt(p.getProperty("target", "500")), 10, 2000);
            seconds = clamp(Integer.parseInt(p.getProperty("seconds", "30")), 5, 120);
            bestCps = Double.parseDouble(p.getProperty("bestCps", "0"));
        } catch (Exception ignored) { }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("target", String.valueOf(target));
            p.setProperty("seconds", String.valueOf(seconds));
            p.setProperty("bestCps", String.valueOf(bestCps));
            try (OutputStream out = Files.newOutputStream(path())) { p.store(out, "Shift Challenge"); }
        } catch (Exception ignored) { }
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
