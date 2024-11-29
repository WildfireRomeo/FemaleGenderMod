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
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.keys.BooleanConfigKey;
import com.wildfire.main.config.keys.ConfigKey;
import com.wildfire.main.config.keys.FloatConfigKey;
import com.wildfire.main.config.keys.GenderConfigKey;

public class Configuration extends AbstractConfiguration {

	private static final String CONFIG_DIR = "WildfireGender";
	public static final Configuration DEFAULTS;

	private final boolean allowSaving;

	private Configuration() {
		this("__DEFAULTS", false);
	}

	public Configuration(String cfgName, boolean allowSaving) {
		super(CONFIG_DIR, cfgName);
		this.allowSaving = allowSaving;
	}

	public final ConfigKey<Gender> gender = register(new GenderConfigKey("gender"));
	public final ConfigKey<Float> bustSize = register(new FloatConfigKey("bust_size", 0.6F, 0, 0.8f));
	public final ConfigKey<Boolean> hurtSounds = register(new BooleanConfigKey("hurt_sounds", true));

	public final ConfigKey<Float> breastsXOffset = register(new FloatConfigKey("breasts_xOffset", 0.0F, -1, 1));
	public final ConfigKey<Float> breastsYOffset = register(new FloatConfigKey("breasts_yOffset", 0.0F, -1, 1));
	public final ConfigKey<Float> breastsZOffset = register(new FloatConfigKey("breasts_zOffset", 0.0F, -1, 0));
	public final ConfigKey<Boolean> breastsUniboob = register(new BooleanConfigKey("breasts_uniboob", true));
	public final ConfigKey<Float> breastsCleavage = register(new FloatConfigKey("breasts_cleavage", 0, 0, 0.1F));

	public final ConfigKey<Boolean> breastPhysics = register(new BooleanConfigKey("breast_physics", true));
	public final ConfigKey<Boolean> armorPhysicsOverride = register(new BooleanConfigKey("armor_physics_override", false));
	public final ConfigKey<Boolean> showInArmor = register(new BooleanConfigKey("show_in_armor", true));
	public final ConfigKey<Float> bounceMultiplier = register(new FloatConfigKey("bounce_multiplier", 0.333F, 0, 0.5f));
	public final ConfigKey<Float> floppyMultiplier = register(new FloatConfigKey("floppy_multiplier", 0.75F, 0.25f, 1));

	public final ConfigKey<Float> voicePitch = register(new FloatConfigKey("voice_pitch", 1F, 0.8f, 1.2f));

	@Override
	public boolean supportsSaving() {
		return super.supportsSaving() && allowSaving;
	}

	static {
		DEFAULTS = new Configuration();
		DEFAULTS.values().forEach(ConfigKey::makeImmutable);
	}
}
