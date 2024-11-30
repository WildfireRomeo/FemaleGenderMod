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

package com.wildfire.main;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WildfireLocalization {
    public static final Text ENABLED = Text.translatable("wildfire_gender.label.enabled").formatted(Formatting.GREEN);
    public static final Text DISABLED = Text.translatable("wildfire_gender.label.disabled").formatted(Formatting.RED);
    public static final Text OFF = Text.translatable("wildfire_gender.label.off");
    public static final Text YES = Text.translatable("wildfire_gender.label.yes").formatted(Formatting.GREEN);
    public static final Text NO = Text.translatable("wildfire_gender.label.no").formatted(Formatting.RED);

    public static final Text SYNC_LOG_AUTHENTICATING = Text.translatable("wildfire_gender.sync_log.authenticating");
    public static final Text SYNC_LOG_AUTHENTICATION_FAILED = Text.translatable("wildfire_gender.sync_log.authentication_failed");
    public static final Text SYNC_LOG_AUTHENTICATION_SUCCESS = Text.translatable("wildfire_gender.sync_log.authentication_success");
    public static final Text SYNC_LOG_REAUTHENTICATING = Text.translatable("wildfire_gender.sync_log.reauthenticating");
    public static final Text SYNC_LOG_ATTEMPTING_SYNC = Text.translatable("wildfire_gender.sync_log.attempting_sync");
    public static final Text SYNC_LOG_SYNC_SUCCESS = Text.translatable("wildfire_gender.sync_log.sync_success");
    public static final Text SYNC_LOG_SYNC_TOO_FREQUENTLY = Text.translatable("wildfire_gender.sync_log.sync_too_frequently");
    public static final Text SYNC_LOG_FAILED_TO_SYNC_DATA = Text.translatable("wildfire_gender.sync_log.failed_to_sync_data");
    public static final Text SYNC_LOG_SYNC_TO_CLOUD = Text.translatable("wildfire_gender.sync_log.sync_to_cloud");

    public static final Text SYNC_LOG_GET_SINGLE_PROFILE = Text.translatable("wildfire_gender.sync_log.get_single_profile");
    public static final Text SYNC_LOG_GET_MULTIPLE_PROFILES = Text.translatable("wildfire_gender.sync_log.get_multiple_profiles");
}
