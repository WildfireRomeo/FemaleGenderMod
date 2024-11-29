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
import com.wildfire.main.config.keys.ConfigKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Generic configuration implementation, storing key-value pairs from a JSON file.
 */
public abstract class AbstractConfiguration extends ForwardingMap<String, ConfigValue<?>> {

	private static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);

	private final File file;
	private final JsonObject json = new JsonObject();
	private final Map<String, ConfigValue<?>> values = new HashMap<>();

	protected AbstractConfiguration(String fileName) {
		this.file = FabricLoader.getInstance().getConfigDir().resolve(fileName + ".json").toFile();
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
		this.file = saveDir.resolve(cfgName + ".json").toFile();
	}

	public static boolean supportsSaving() {
		return FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER;
	}

	protected <TYPE> ConfigValue<TYPE> register(ConfigKey<TYPE> key) {
		var value = new ConfigValue<>(this, key);
		this.values.put(key.getKey(), value);
		return value;
	}

	@Deprecated
	public <TYPE> void set(ConfigKey<TYPE> key, TYPE value) {
		key.save(json, value);
	}

	@Deprecated
	public <TYPE> TYPE get(ConfigKey<TYPE> key) {
		return key.read(json);
	}

	@Deprecated
	public <TYPE> void setDefault(ConfigKey<TYPE> key) {
		if(!json.has(key.getKey())) {
			set(key, key.getDefault());
		}
	}

	@Deprecated
	public void removeParameter(ConfigKey<?> key) {
		removeParameter(key.getKey());
	}

	@Deprecated
	public void removeParameter(String key) {
		json.remove(key);
	}

	protected void setDefaults() {
		values.values().forEach(ConfigValue::setDefault);
	}

	public boolean exists() {
		return file.exists();
	}

	public void save() {
		if(!supportsSaving()) return;
		try(FileWriter writer = new FileWriter(file); JsonWriter jsonWriter = new JsonWriter(writer)) {
			jsonWriter.setIndent("\t");
			ADAPTER.write(jsonWriter, json);
		} catch (IOException e) {
			WildfireGender.LOGGER.error("Failed to save config file", e);
		}
	}

	public void load() {
		if(!supportsSaving() || !file.exists()) return;
		try(FileReader configurationFile = new FileReader(file)) {
			JsonObject obj = new Gson().fromJson(configurationFile, JsonObject.class);
			for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				json.add(entry.getKey(), entry.getValue());
			}
		} catch(IOException e) {
			WildfireGender.LOGGER.error("Failed to load config file", e);
		}
	}

	@Override
	protected final @NotNull @Unmodifiable Map<String, ConfigValue<?>> delegate() {
		return Collections.unmodifiableMap(values);
	}

	public final JsonObject json() {
		return json;
	}
}
