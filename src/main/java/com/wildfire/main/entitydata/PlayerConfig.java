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

package com.wildfire.main.entitydata;

import com.google.gson.JsonObject;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A version of {@link EntityConfig} backed by a {@link Configuration} for use with players
 */
public class PlayerConfig extends EntityConfig {

	public boolean needsSync;
	public boolean needsCloudSync;
	public SyncStatus syncStatus = SyncStatus.UNKNOWN;

	public PlayerConfig(UUID uuid) {
		super(uuid, new Configuration(uuid.toString(), true));
	}

	// this shouldn't ever be called on players, but just to be safe, override with a noop.
	@Override
	public void readFromStack(@NotNull ItemStack chestplate) {}

	public Configuration getConfig() {
		return cfg;
	}

	public void updateGender(Gender value) {
		cfg.gender.set(value);
	}

	public void updateBustSize(float value) {
		cfg.bustSize.set(value);
	}

	public boolean hasHurtSounds() {
		return cfg.hurtSounds.get();
	}

	public void updateVoicePitch(float value) {
		cfg.voicePitch.set(value);
	}

	public void updateHurtSounds(boolean value) {
		cfg.hurtSounds.set(value);
	}

	public void updateBreastPhysics(boolean value) {
		cfg.physics.set(value);
	}

	public boolean getArmorPhysicsOverride() {
		return cfg.armorPhysicsOverride.get();
	}

	public void updateArmorPhysicsOverride(boolean value) {
		cfg.armorPhysicsOverride.set(value);
	}

	public boolean showBreastsInArmor() {
		return cfg.showInArmor.get();
	}

	public void updateShowBreastsInArmor(boolean value) {
		cfg.showInArmor.set(value);
	}

	public void updateBounceMultiplier(float value) {
		cfg.bounceMultiplier.set(value);
	}

	public void updateFloppiness(float value) {
		cfg.floppyMultiplier.set(value);
	}

	public SyncStatus getSyncStatus() {
		return this.syncStatus;
	}

	/**
	 * @deprecated Use {@link #toJson()} instead
	 */
	@Deprecated
	public static JsonObject toJsonObject(PlayerConfig plr) {
		return plr.toJson();
	}

	public JsonObject toJson() {
		return cfg.toJson();
	}

	public boolean hasLocalConfig() {
		return cfg.exists();
	}

	public void loadFromDisk(boolean markForSync) {
		this.syncStatus = SyncStatus.CACHED;
		cfg.load();
		if(markForSync) {
			this.needsSync = true;
		}
	}

	/**
	 * @deprecated Use {@link #loadFromDisk(boolean)} instead
	 */
	@Deprecated
	public static PlayerConfig loadCachedPlayer(UUID uuid, boolean markForSync) {
		PlayerConfig plr = WildfireGender.getPlayerById(uuid);
		if (plr != null && plr.hasLocalConfig()) {
			plr.loadFromDisk(markForSync);
		}
		return plr;
	}

	public void save() {
		cfg.save();
		this.needsSync = true;
		this.needsCloudSync = true;
	}

	@Override
	public boolean hasJacketLayer() {
		throw new UnsupportedOperationException("PlayerConfig does not support #hasJacketLayer(); use PlayerEntity#isPartVisible instead");
	}

	public void updateFromJson(JsonObject json) {
		this.cfg.apply(json);
		this.syncStatus = SyncStatus.SYNCED;
	}

	public enum SyncStatus {
		CACHED, SYNCED, UNKNOWN
	}
}
