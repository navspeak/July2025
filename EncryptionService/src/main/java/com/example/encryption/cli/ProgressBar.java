package com.example.encryption.cli;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("cli")
public class ProgressBar {

    private static final int BAR_WIDTH = 35;
    private static final boolean TTY = System.console() != null;

    private int total;
    private int current;

    public void start(int total) {
        this.total = total;
        this.current = 0;
        if (TTY) render("");
    }

    public void advance(String filename) {
        current++;
        if (TTY) render(truncate(filename, 25));
        if (current == total && TTY) System.err.println();
    }

    public boolean isTty() {
        return TTY;
    }

    private void render(String filename) {
        int percent = total > 0 ? (int) (current * 100.0 / total) : 0;
        int filled  = total > 0 ? (int) (current * BAR_WIDTH / (double) total) : 0;

        String bar = "=".repeat(filled)
                + (filled < BAR_WIDTH ? ">" : "")
                + " ".repeat(Math.max(0, BAR_WIDTH - filled - 1));

        System.err.printf("\r[%s] %3d%% (%d/%d) %s", bar, percent, current, total, filename);
        System.err.flush();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : "..." + s.substring(s.length() - (max - 3));
    }
}
