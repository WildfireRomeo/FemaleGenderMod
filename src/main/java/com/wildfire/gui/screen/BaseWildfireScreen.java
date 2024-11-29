/*
    Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
    Copyright (C) 2023 WildfireRomeo

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.wildfire.gui.screen;

import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.config.core.ConfigValue;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.WildfireGender;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class BaseWildfireScreen extends Screen {

    protected final UUID playerUUID;
    protected final Screen parent;

    protected BaseWildfireScreen(Text title, Screen parent, UUID uuid) {
        super(title);
        this.parent = parent;
        this.playerUUID = uuid;
    }

    public @Nullable PlayerConfig getPlayer() {
        return WildfireGender.getPlayerById(this.playerUUID);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    protected Text enabled(ConfigValue<Boolean> config) {
        return enabled(config.get());
    }

    protected Text enabled(boolean condition) {
        return condition ? WildfireLocalization.ENABLED : WildfireLocalization.DISABLED;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
