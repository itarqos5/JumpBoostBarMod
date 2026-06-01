/*
 * Copyright (C) 2026  literal
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gg.literal.jumpboostbar.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class FabricBossBarOverlay {
    private boolean visible;
    private float progress;
    private Component title = Component.empty();

    public FabricBossBarOverlay() {
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    public void update(Component title, float progress) {
        this.title = title;
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    private void onHudRender(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (!visible) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        int width = client.getWindow().getGuiScaledWidth();
        int x = (width / 2) - 91;
        int y = 12;
        int barWidth = 182;
        int fillWidth = Math.max(0, Math.min(barWidth, Math.round(barWidth * progress)));

        graphics.fill(x, y, x + barWidth, y + 10, 0xAA222222);
        graphics.fill(x, y, x + fillWidth, y + 10, 0xAA00AA00);

        graphics.drawCenteredString(client.font, title, width / 2, y - 10, 0xFFFFFF);
    }
}
