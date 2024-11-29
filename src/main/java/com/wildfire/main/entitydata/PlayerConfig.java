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
import com.wildfire.main.config.Configuration;
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

	public Configuration config() {
		return cfg;
	}

	public boolean hasHurtSounds() {
		return cfg.hurtSounds.get();
	}

	public SyncStatus getSyncStatus() {
		return this.syncStatus;
	}

	public JsonObject toJson() {
		return cfg.toJson();
	}

	public boolean hasLocalConfig() {
		return cfg.exists();
	}

	public void loadFromDisk(boolean markForSync) {
		if(!hasLocalConfig()) return;
		syncStatus = SyncStatus.CACHED;
		cfg.load();
		if(markForSync) {
			needsSync = true;
		}
	}

	public void save() {
		cfg.save();
		needsSync = true;
		needsCloudSync = true;
	}

	public void updateFromJson(JsonObject json) {
		cfg.apply(json);
		syncStatus = SyncStatus.SYNCED;
	}

	@Override
	public boolean hasJacketLayer() {
		throw new UnsupportedOperationException("PlayerConfig does not support #hasJacketLayer(); use PlayerEntity#isPartVisible instead");
	}

	// this shouldn't ever be called on players, but just to be safe, override with a noop.
	@Override
	public void readFromStack(@NotNull ItemStack chestplate) {}

	public enum SyncStatus {
		CACHED, SYNCED, UNKNOWN
	}
}
