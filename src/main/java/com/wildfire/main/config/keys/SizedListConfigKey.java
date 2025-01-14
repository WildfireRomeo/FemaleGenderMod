/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.main.config.keys;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class SizedListConfigKey<TYPE> extends ConfigKey<SizedListConfigKey.LimitedArrayList<TYPE>> {
	private final int maxSize;
	private final ConfigKey<TYPE> inner;

	public SizedListConfigKey(String key, int maxSize, ConfigKey<TYPE> inner) {
		super(key, null);
		this.maxSize = maxSize;
		this.inner = inner;
	}

	@Override
	public LimitedArrayList<TYPE> getDefault() {
		return new LimitedArrayList<>(maxSize);
	}

	@Override
	protected LimitedArrayList<TYPE> read(JsonElement element) {
		var list = new LimitedArrayList<TYPE>(maxSize);
		if(element instanceof JsonArray array) {
			for(var item : array) {
				if(list.size() >= maxSize) break;
				list.add(inner.read(item));
			}
		}
		return list;
	}

	@Override
	public void save(JsonObject object, LimitedArrayList<TYPE> value) {
		var array = new JsonArray();
		for(var item : value) {
			if(array.size() >= maxSize) break;
			// TODO this kinda sucks - it'd be preferable to call a `JsonObject serialize(TYPE)` method instead
			//		of having to do this JsonObject workaround
			var obj = new JsonObject();
			inner.save(obj, item);
			var serialized = obj.get(inner.key);
			if(serialized != null) {
				array.add(serialized);
			}
		}
		object.add(key, array);
	}

	@Override
	public boolean validate(LimitedArrayList<TYPE> value) {
		return super.validate(value) && value.size() <= maxSize;
	}

	public static class LimitedArrayList<T> extends ArrayList<T> {
		public final int maxSize;

		public LimitedArrayList(int maxSize) {
			super(maxSize);
			this.maxSize = maxSize;
		}

		@Override
		public boolean add(T t) {
			if(size() >= maxSize) {
				throw new UnsupportedOperationException("List is at max capacity");
			}
			return super.add(t);
		}
	}
}
