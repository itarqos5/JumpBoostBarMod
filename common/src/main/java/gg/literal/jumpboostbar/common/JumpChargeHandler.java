package gg.literal.jumpboostbar.common;

import java.util.function.Consumer;

public interface JumpChargeHandler {
    void startCharging(int chargeTicks, Consumer<Float> onProgress);
    void stopCharging();
    boolean isCharging();
}

