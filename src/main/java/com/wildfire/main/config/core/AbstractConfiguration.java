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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.keys.ConfigKey;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Generic configuration implementation, storing key-value pairs from a JSON file in {@link ConfigKey}s
 */
public abstract class AbstractConfiguration extends ForwardingMap<String, ConfigKey<?>> {

	private static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);

	private final Path file;
	private final Map<String, JsonElement> unrecognized = new HashMap<>();
	private final Map<String, ConfigKey<?>> values = new HashMap<>();

	protected AbstractConfiguration(String... path) {
		this.file = resolvePath(path);
	}

	protected Path resolvePath(String... names) {
		var items = new ArrayList<>(List.of(names));
		var fileName = items.removeLast();
		var path = FabricLoader.getInstance().getConfigDir();
		while(!items.isEmpty()) {
			path = path.resolve(items.removeFirst());
		}
		return path.resolve(fileName + ".json");
	}

	/**
	 * Check if this configuration allows saving
	 *
	 * @implNote Defaults to {@link WildfireHelper#onClient()}
	 *
	 * @return {@code true} if this config allows loading and saving to/from disk
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean supportsSaving() {
		return WildfireHelper.onClient();
	}

	/**
	 * Register a new {@link ConfigKey} for use in this configuration
	 */
	protected <TYPE, KEY extends ConfigKey<TYPE>> KEY register(@NotNull KEY key) {
		if(containsKey(key.getKey())) {
			throw new IllegalArgumentException("Configuration key " + key.getKey() + " is already registered");
		}
		this.values.put(key.getKey(), key);
		return key;
	}

	/**
	 * @return {@code true} if the file for this config exists on disk
	 */
	public boolean exists() {
		return file.toFile().exists();
	}

	/**
	 * Saves the current config to its file on disk
	 */
	public void save() {
		if(!supportsSaving()) return;

		if(Files.notExists(file.getParent())) {
			try {
				Files.createDirectories(file.getParent());
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		var json = toJson();
		unrecognized.forEach(json::add);
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
			unrecognized.clear();
			obj.asMap().keySet().stream()
					.filter(key -> !containsKey(key))
					.forEach(key -> unrecognized.put(key, obj.get(key)));
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
	protected final @NotNull @UnmodifiableView Map<String, ConfigKey<?>> delegate() {
		return Collections.unmodifiableMap(values);
	}

	/**
	 * @return A new {@link JsonObject} containing all the values saved in this configuration
	 */
	public final @NotNull JsonObject toJson() {
		var json = new JsonObject();
		this.values().forEach(v -> v.save(json));
		return json;
	}
}
