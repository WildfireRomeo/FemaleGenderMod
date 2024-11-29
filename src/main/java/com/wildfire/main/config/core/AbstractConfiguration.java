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

package com.wildfire.main.config.core;

import com.google.common.collect.ForwardingMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.keys.ConfigKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Generic configuration implementation, storing key-value pairs from a JSON file.
 */
public abstract class AbstractConfiguration extends ForwardingMap<String, ConfigKey<?>> {

	private static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);

	private final Path file;
	private final Map<String, ConfigKey<?>> values = new HashMap<>();

	protected AbstractConfiguration(String fileName) {
		this.file = FabricLoader.getInstance().getConfigDir().resolve(fileName + ".json");
	}

	protected AbstractConfiguration(String directory, String cfgName) {
		Path saveDir = FabricLoader.getInstance().getConfigDir().resolve(directory);
		if(supportsSaving() && !Files.isDirectory(saveDir)) {
			try {
				Files.createDirectory(saveDir);
			} catch(IOException e) {
				WildfireGender.LOGGER.error("Failed to create config directory", e);
			}
		}
		this.file = saveDir.resolve(cfgName + ".json");
	}

	public boolean supportsSaving() {
		return FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER;
	}

	protected <TYPE, KEY extends ConfigKey<TYPE>> KEY register(KEY key) {
		if(containsKey(key.getKey())) {
			throw new IllegalArgumentException("Configuration key " + key.getKey() + " is already registered");
		}
		this.values.put(key.getKey(), key);
		return key;
	}

	public boolean exists() {
		return file.toFile().exists();
	}

	public void save() {
		if(!supportsSaving()) return;
		var json = toJson();
		try(FileWriter writer = new FileWriter(file.toFile()); JsonWriter jsonWriter = new JsonWriter(writer)) {
			jsonWriter.setIndent("\t");
			ADAPTER.write(jsonWriter, json);
		} catch (IOException e) {
			WildfireGender.LOGGER.error("Failed to save config file", e);
		}
	}

	/**
	 * Load the current config from its file on disk
	 */
	public void load() {
		if(!supportsSaving() || !exists()) return;
		try(FileReader configurationFile = new FileReader(file.toFile())) {
			JsonObject obj = new Gson().fromJson(configurationFile, JsonObject.class);
			apply(obj);
		} catch(IOException e) {
			WildfireGender.LOGGER.error("Failed to load config file", e);
		}
	}

	/**
	 * Apply values from the provided {@link JsonObject} to the current config
	 *
	 * @param json The {@link JsonObject} to apply loaded values from
	 */
	public void apply(JsonObject json) {
		values().forEach(key -> key.apply(json));
	}

	@Override
	protected final @NotNull @Unmodifiable Map<String, ConfigKey<?>> delegate() {
		return Collections.unmodifiableMap(values);
	}

	@Deprecated
	public final JsonObject json() {
		return toJson();
	}

	public final JsonObject toJson() {
		var json = new JsonObject();
		this.values.values().forEach(v -> v.save(json));
		return json;
	}
}
