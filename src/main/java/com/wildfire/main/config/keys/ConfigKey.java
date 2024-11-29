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

package com.wildfire.main.config.keys;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

public abstract class ConfigKey<TYPE> {

    private boolean isImmutable = false;

    protected final String key;
    protected final TYPE defaultValue;
    protected TYPE value;

    protected ConfigKey(String key, TYPE defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public TYPE getDefault() {
        return defaultValue;
    }

    public TYPE get() {
        return value;
    }

    public void set(TYPE value) {
        if(isImmutable) {
            throw new UnsupportedOperationException("Immutable keys may not be modified");
        }
        if(validate(value)) this.value = value;
    }

    @ApiStatus.Internal
    public final void apply(JsonObject obj) {
        JsonElement element = obj.get(key);
        if(element != null) {
            set(read(element));
        }
    }

    protected abstract TYPE read(JsonElement element);

    @ApiStatus.Internal
    public abstract void save(JsonObject object);

    public boolean validate(TYPE value) {
        return value != null;
    }

    @ApiStatus.Internal
    public final void makeImmutable() {
        this.isImmutable = true;
    }
}