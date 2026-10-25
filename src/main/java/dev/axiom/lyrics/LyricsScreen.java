package dev.axiom.lyrics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/** Einstellungs-Menü (Taste K). */
public class LyricsScreen extends Screen {
    private TextFieldWidget clientIdField;

    public LyricsScreen() {
        super(Text.literal("Spotify Lyrics"));
    }

    @Override
    protected void init() {
        int w = 220;
        int x = this.width / 2 - w / 2;
        int top = 56;
        int step = Math.max(21, Math.min(24, (this.height - top - 30) / 8));
        int y = top;

        clientIdField = new TextFieldWidget(this.textRenderer, x, y, w, 20, Text.literal("Client ID"));
        clientIdField.setMaxLength(64);
        clientIdField.setPlaceholder(Text.literal("Spotify Client ID einfügen"));
        if (SpotifyAuth.clientId != null) clientIdField.setText(SpotifyAuth.clientId);
        addDrawableChild(clientIdField);
        y += step;

        addDrawableChild(ButtonWidget.builder(Text.literal("Mit Spotify verbinden"), b -> {
            String id = clientIdField.getText().trim();
            if (id.isEmpty() && SpotifyAuth.clientId == null) {
                LyricsState.status = "Bitte zuerst die Client ID eintragen.";
            } else {
                LyricsState.status = "Browser öffnet sich ...";
                SpotifyAuth.login(id.isEmpty() ? null : id);
            }
        }).dimensions(x, y, w, 20).build());
        y += step;

        addDrawableChild(ButtonWidget.builder(Text.literal(enabledLabel()), b -> {
            LyricsState.enabled = !LyricsState.enabled;
            b.setMessage(Text.literal(enabledLabel()));
            LyricsState.saveSettings();
        }).dimensions(x, y, w, 20).build());
        y += step;

        addDrawableChild(ButtonWidget.builder(Text.literal(modeLabel()), b -> {
            LyricsState.mode = (LyricsState.mode + 1) % 2;
            b.setMessage(Text.literal(modeLabel()));
            LyricsState.saveSettings();
        }).dimensions(x, y, w, 20).build());
        y += step;

        addDrawableChild(ButtonWidget.builder(Text.literal(themeLabel()), b -> {
            LyricsState.themeIdx = (LyricsState.themeIdx + 1) % LyricsState.THEMES.length;
            b.setMessage(Text.literal(themeLabel()));
            LyricsState.saveSettings();
        }).dimensions(x, y, w, 20).build());
        y += step;

        addDrawableChild(new ValueSlider(x, y, w, 20, "Größe", 40, 300, LyricsState.size * 100.0,
            v -> { LyricsState.size = (float) (v / 100.0); LyricsState.saveSettings(); },
            v -> Math.round(v) + " %"));
        y += step;

        addDrawableChild(new ValueSlider(x, y, w, 20, "Timing", -1500, 1500, LyricsState.offsetMs,
            v -> { LyricsState.offsetMs = (int) Math.round(v / 10.0) * 10; LyricsState.saveSettings(); },
            v -> (Math.round(v / 10.0) * 10) + " ms"));
        y += step;

        addDrawableChild(ButtonWidget.builder(Text.literal("Fertig"), b -> this.close())
            .dimensions(x, y, w, 20).build());
    }

    private static String enabledLabel() { return "Lyrics: " + (LyricsState.enabled ? "AN" : "AUS"); }
    private static String modeLabel() {
        return "Anzeige: " + (LyricsState.mode == 0 ? "Schwebend (3D)" : "Unten am Bildschirm");
    }
    private static String themeLabel() { return "Farbe: " + LyricsState.theme().name(); }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int maxW = Math.min(this.width - 20, 300);
        g.drawCenteredTextWithShadow(this.textRenderer, "Spotify Lyrics", cx, 10, 0xFFFFFFFF);

        String status = this.textRenderer.trimToWidth("Status: " + LyricsState.status, maxW);
        g.drawCenteredTextWithShadow(this.textRenderer, status, cx, 24, 0xFFB79CFF);

        String song = LyricsState.title.isEmpty() ? "Song: -" : "Song: " + LyricsState.title + " - " + LyricsState.artist;
        g.drawCenteredTextWithShadow(this.textRenderer, this.textRenderer.trimToWidth(song, maxW), cx, 35, 0xFFDDDDDD);

        long pos = Math.max(0, LyricsState.now()) / 1000;
        String info = "Zeilen: " + LyricsState.lines.size() + "   Position: "
            + (pos / 60) + ":" + String.format("%02d", pos % 60)
            + "   " + (LyricsState.playing ? "spielt" : "pausiert");
        g.drawCenteredTextWithShadow(this.textRenderer, info, cx, 46, 0xFFAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Regler mit Text und gespeichertem Wert. */
    private static final class ValueSlider extends SliderWidget {
        private final String label;
        private final double min, max;
        private final DoubleConsumer onChange;
        private final DoubleFunction<String> fmt;

        ValueSlider(int x, int y, int w, int h, String label, double min, double max, double value,
                    DoubleConsumer onChange, DoubleFunction<String> fmt) {
            super(x, y, w, h, Text.empty(), Math.max(0.0, Math.min(1.0, (value - min) / (max - min))));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            this.fmt = fmt;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (fmt == null) return;
            setMessage(Text.literal(label + ": " + fmt.apply(min + (max - min) * this.value)));
        }

        @Override
        protected void applyValue() {
            onChange.accept(min + (max - min) * this.value);
        }
    }
}
