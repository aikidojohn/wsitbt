/**
 * WSIT Build Tools (http://wsitbt.codeplex.com)
 *
 * Copyright (c) 2011 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Application Security, Inc.
 */
package com.appsec.wsitbt.core.util;


import java.io.File;

/**
 * Common utilities used in both ant and maven projects.
 *
 * User: Dan Rollo
 * Date: 8/11/11
 * Time: 3:03 PM
 */
public final class WSITBTUtil
{
    private static final int MKDIR_RETRY_SLEEP_MILLIS = 10;
    static final String MSG_PREFIX_CREATE_DIRECTORIES_FAILED = "Failed to create directories for file: ";

    /**
     * Prevent instantiation.
     */
    private WSITBTUtil()
    {
    }

    /**
     * If the given file does not exist, call mkdirs() on that file, and throw an error if the call does not return true.
     * @param file the path to create.
     */
    public static void doMkDirs(final File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException("The parameter: file must not be null.");
        }

        if (!file.exists()
                // On some versions of Windows, a race condition could cause this to return false.
                // Workaround was a 10ms sleep and retry.
                // See 'ant' mkdirs task for info.
                // http://svn.apache.org/viewvc/ant/core/trunk/src/main/org/apache/tools/ant/taskdefs/Mkdir.java?view=markup
                && !file.mkdirs())
        {

            try {
                Thread.sleep(MKDIR_RETRY_SLEEP_MILLIS);
            } catch (InterruptedException ex) {
                // ignore it
            }

            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();

            if (!file.exists()) {
                throw new IllegalStateException(MSG_PREFIX_CREATE_DIRECTORIES_FAILED + file.getAbsolutePath());
            }
        }
    }

}
