package gg.literal.jumpboostbar.common;

public final class JumpBoostBarCommon {
    public static final String MOD_ID = "jumpboostbar";

    private JumpBoostBarCommon() {
    }

    public static String startupMessage(String platformName) {
        return "[JumpBoostBar] " + platformName + " bootstrap complete.";
    }
}
