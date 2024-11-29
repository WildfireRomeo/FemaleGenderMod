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

package com.wildfire.main.config;

import com.wildfire.main.config.core.AbstractConfiguration;
import com.wildfire.main.config.core.ConfigValue;
import com.wildfire.main.config.enums.ShowPlayerListMode;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.config.keys.BooleanConfigKey;
import com.wildfire.main.config.keys.EnumConfigKey;
import com.wildfire.main.config.keys.StringConfigKey;

public class GlobalConfig extends AbstractConfiguration {
    public static final GlobalConfig INSTANCE = new GlobalConfig();

    private GlobalConfig() {
        super("wildfire_gender");
    }

    public final ConfigValue<Boolean> firstTimeLoad = register(new BooleanConfigKey("firstTimeLoad", true));

    public final ConfigValue<Boolean> cloudSyncEnabled = register(new BooleanConfigKey("cloud_sync", false));
    public final ConfigValue<Boolean> automaticCloudSync = register(new BooleanConfigKey("sync_player_data", false));
    // see CloudSync#DEFAULT_CLOUD_URL for the actual default
    public final ConfigValue<String> cloudServer = register(new StringConfigKey("cloud_server", ""));
    public final ConfigValue<SyncVerbosity> syncLogVerbosity = register(new EnumConfigKey<>("sync_log_verbosity", SyncVerbosity.DEFAULT, SyncVerbosity.BY_ID));

    public final ConfigValue<ShowPlayerListMode> alwaysShowList = register(new EnumConfigKey<>("alwaysShowList", ShowPlayerListMode.MOD_UI_ONLY, ShowPlayerListMode.BY_ID));

    static {
        INSTANCE.setDefaults();
        if(!INSTANCE.exists()) {
            INSTANCE.save();
        }
    }
}
