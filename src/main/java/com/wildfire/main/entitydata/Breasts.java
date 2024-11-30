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

import com.wildfire.main.config.Configuration;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Record class representing an entity's breast appearance settings
 */
public record Breasts(Vector3fc offset, boolean uniboob, float cleavage) {
    public Breasts(Configuration config) {
        this(config.breastsXOffset.get(), config.breastsYOffset.get(), config.breastsZOffset.get(), config.breastsUniboob.get(), config.breastsCleavage.get());
    }

    public Breasts(float x, float y, float z, boolean uniboob, float cleavage) {
        this(new Vector3f(x, y, z), uniboob, cleavage);
    }

    public static final PacketCodec<ByteBuf, Breasts> CODEC = PacketCodec.tuple(
            PacketCodecs.VECTOR_3F, breasts -> new Vector3f(breasts.offset()),
            PacketCodecs.BOOL, Breasts::uniboob,
            PacketCodecs.FLOAT, Breasts::cleavage,
            Breasts::new
    );

    /**
     * Copy the provided {@link Vector3fc offsets} to the provided {@link Configuration}
     */
    public static void updateOffsets(Configuration config, Vector3fc offsets) {
        config.breastsXOffset.set(offsets.x());
        config.breastsYOffset.set(offsets.y());
        config.breastsZOffset.set(offsets.z());
    }

    /**
     * Copy settings from the provided {@link Breasts breasts data} onto the provided {@link Configuration}
     */
    public static void applyTo(Configuration config, Breasts breasts) {
        updateOffsets(config, breasts.offset());
        config.breastsCleavage.set(breasts.cleavage());
        config.breastsUniboob.set(breasts.uniboob());
    }
}
