package gg.literal.jumpboostbar.paper;

public enum BarMode {
    XP,
    BOSSBAR;

    public static BarMode fromConfig(String value) {
        if (value == null) {
            return XP;
        }

        return switch (value.trim().toLowerCase()) {
            case "bossbar", "boss_bar", "boss-bar" -> BOSSBAR;
            default -> XP;
        };
    }

    public String configValue() {
        return name().toLowerCase();
    }
}
