/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-server.
 *
 * beam-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.server.util;

import java.util.Arrays;
import java.util.Map;
import org.beamproject.common.util.Exceptions;

/**
 * This class provides a wrapper for byte arrays. This way, byte arrays can be
 * stored as keys in {@link Map}s.
 */
public class ComparableBytes {

    byte[] bytes;

    public ComparableBytes(byte[] bytes) {
        Exceptions.verifyArgumentsNotNull(bytes);

        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Compares this object to the other object. The objects are only equals if
     * both are of the same type and both have exactly the same bytes wrapped.
     *
     * @param other Another object to compare with this one.
     * @return true, if the bytes are exactly equally, otherwise false.
     */
    @Override
    public boolean equals(Object other) {
        if (bytes == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != this.getClass()) {
            return false;
        }

        ComparableBytes otherBytes = (ComparableBytes) other;
        return Arrays.equals(bytes, otherBytes.bytes);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Arrays.hashCode(this.bytes);
        return hash;
    }
}
