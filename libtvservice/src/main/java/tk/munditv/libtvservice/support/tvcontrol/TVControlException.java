/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tk.munditv.libtvservice.support.tvcontrol;

import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.ErrorCode;

/**
 *
 */
public class TVControlException extends ActionException {

    public TVControlException(int errorCode, String message) {
        super(errorCode, message);
    }

    public TVControlException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public TVControlException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TVControlException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TVControlException(TVControlErrorCode errorCode, String message) {
        super(errorCode.getCode(), errorCode.getDescription() + ". " + message + ".");
    }

    public TVControlException(TVControlErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getDescription());
    }
}