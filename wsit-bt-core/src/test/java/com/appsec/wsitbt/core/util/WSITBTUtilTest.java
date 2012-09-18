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

import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * User: Dan Rollo
 * Date: 8/11/11
 * Time: 3:10 PM
 */
public class WSITBTUtilTest
{
    @Test (expected = IllegalArgumentException.class)
    public void testDoMkDirsWithNull()
    {
        //noinspection NullableProblems
        WSITBTUtil.doMkDirs(null);
    }

    @Test (expected = IllegalStateException.class)
    public void testDoMkDirsWithFailedCreation()
    {
        final File bogusPath = mock(File.class);

        WSITBTUtil.doMkDirs(bogusPath);
    }

    @Test
    public void testDoMkDirsWithExistingFile()
    {
        final File existingPath = mock(File.class);
        when(existingPath.exists()).thenReturn(true);

        WSITBTUtil.doMkDirs(existingPath);

        //noinspection ResultOfMethodCallIgnored
        verify(existingPath).exists();
        verifyNoMoreInteractions(existingPath);
    }

    @Test
    public void testDoMkDirs()
    {
        final File myPath = new File("oneDir", "twoDir");
        myPath.deleteOnExit();
        myPath.getParentFile().deleteOnExit();

        assertFalse("Path should not exist at start of test: " + myPath.getAbsolutePath(), myPath.exists());

        try
        {
            WSITBTUtil.doMkDirs(myPath);

            assertTrue("Expected path was not created: " + myPath.getAbsolutePath(), myPath.exists());
            assertTrue("Expected path is not a directory: " + myPath.getAbsolutePath(), myPath.isDirectory());
        }
        finally
        {
            //noinspection ResultOfMethodCallIgnored
            myPath.delete();
            //noinspection ResultOfMethodCallIgnored
            myPath.getParentFile().delete();
        }
    }

    @Test
    public void testDoMkDirsRetry()
    {
        final File myPath = mock(File.class);

        assertFalse(myPath.exists());
        try
        {
            WSITBTUtil.doMkDirs(myPath);
            fail("create mock dir should fail");
        }
        catch (IllegalStateException e)
        {
            assertEquals(WSITBTUtil.MSG_PREFIX_CREATE_DIRECTORIES_FAILED + null, e.getMessage());
        }

        //noinspection ResultOfMethodCallIgnored
        verify(myPath, times(2)).mkdirs();
    }

}
