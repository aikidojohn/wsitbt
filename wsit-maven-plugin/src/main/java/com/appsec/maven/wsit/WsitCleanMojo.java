/**
 * WSIT Build Tools (http://aikidojohn.github.com/wsitbt/)
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
package com.appsec.maven.wsit;

import java.io.File;

import com.appsec.wsitbt.core.util.WSITBTUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.appsec.wsitbt.core.wsit.WsitDocument;

/**
 * Strips wsit configuration from a file. The file must be a WSDL file.
 *
 * @author John Hite
 * @goal clean
 * @phase generate-resources
 * @since 1.0
 */
public final class WsitCleanMojo extends AbstractMojo
{
    /**
     * The file to strip WSIT configuration from. Must be a wsit configuration file
     * (starts with the wsdl document element).
     *
     * @parameter expression="wsit-clean.file"
     * @required
     * @since 1.0
     */
    private File file;

    /**
     * The configuration files will be written to this directory. Either this
     * or tofile must be specified.
     *
     * @parameter expression="wsit-clean.todir"
     * @since 1.0
     */
    private File todir;

    /**
     * The configuration files will be written to this file. Either this or
     * todir must be specified.
     *
     * @parameter expression="wsit-clean.tofile"
     * @since 1.0
     */
    private File tofile;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            checkProperties();

            final File output;
            if (null != todir)
            {
                WSITBTUtil.doMkDirs(todir);
                output = new File(todir.getAbsoluteFile(), file.getName());
            }
            else
            {
                output = tofile;
            }

            getLog().info("Stripping wsit configuration from " + file.getAbsolutePath());
            WsitDocument wsitDoc = WsitDocument.parse(file);
            wsitDoc.stripWsitConfiguration();

            getLog().info("Saving file to " + output.getAbsolutePath());
            wsitDoc.save(output);
        }
        catch (MojoFailureException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Failed to strip wsit configuration from file", e);
        }
    }

    private void checkProperties() throws MojoFailureException
    {
        if (null == todir && null == tofile)
        {
            throw new MojoFailureException("one of todir or tofile must be set");
        }
        if (null == file)
        {
            throw new MojoFailureException("The required attribute file must be set");
        }
        if (!file.exists())
        {
            throw new MojoFailureException("The file specified by file attribute must exist. file: " + file.getAbsolutePath());
        }
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public void setTodir(File todir)
    {
        this.todir = todir;
    }

    public void setTofile(File tofile)
    {
        this.tofile = tofile;
    }
}
