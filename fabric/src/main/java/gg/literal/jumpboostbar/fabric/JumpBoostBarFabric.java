package gg.literal.jumpboostbar.fabric;

import gg.literal.jumpboostbar.common.JumpBoostBarCommon;
import net.fabricmc.api.DedicatedServerModInitializer;

public final class JumpBoostBarFabric implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        System.out.println(JumpBoostBarCommon.startupMessage("Fabric"));
    }
}
