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
package org.beamproject.server.pages;

import org.beamproject.common.Message;
import org.beamproject.common.MessageField;
import org.beamproject.common.util.Exceptions;

/**
 * This exception is thrown when a problem occurs during processing in incoming
 * message in a {@link Page}. This could be, for example, when a needed
 * {@link MessageField} is messing.
 */
public class MessageException extends RuntimeException {

    public enum ErrorCode {

        /**
         * This code is generally usable when something goes wrong and the other
         * side can without understanding interpret this code.
         */
        ERROR;
    }
    private final static long serialVersionUID = 1L;
    private final ErrorCode errorCode;

    /**
     * Creates a new {@link MessageException}, initialized with an
     * {@link ErrorCode} and a message. The error code will be sent to the other
     * side as encrypted {@link Message}.
     *
     * @param errorCode The error code for the other side.
     * @param message A message that describes why this exception has been
     * thrown.
     * @throws IllegalArgumentException If at least one of the arguments is
     * null.
     */
    public MessageException(ErrorCode errorCode, String message) {
        super(message);
        Exceptions.verifyArgumentsNotNull(errorCode, message);

        this.errorCode = errorCode;
    }

    /**
     * @return The error code associated with this exception.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
