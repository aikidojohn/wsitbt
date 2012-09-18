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
package com.appsecinc.ant;

import java.io.File;

import com.appsec.wsitbt.core.util.WSITBTUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.appsec.wsitbt.core.wsit.WsitDocument;

/**
 * <pRemoves WSIT specific configuration from the file.</p>
 * <p>
 * The task takes the WSDL file specified strips any existing WSIT configuration.
 * </p>
 * <p>
 * To use this task, include a taskdef for the appsecinc-anttasks in your build.xml
 * </p>
 * <pre>
 * <code>
 * &lt;taskdef resource="com/appsecinc/ant/appsecinc-anttasks.properties" classpath="appsecinc-anttasks.jar" /&gt;
 * </code>
 * </pre>
 * <p>
 * You can then use the task
 * </p>
 * <pre>
 * <code>
 * &lt;wsit-clean
 *     todir="src/WEB-INF"
 *     file="src/wsdl/MyService.wsdl"
 * /&gt;
 * </code>
 * </pre>
 *
 * @author jhite
 */
public final class WsitCleanTask extends Task
{
    private File file;
    private File todir;
    private File tofile;

    public void execute()
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

            log("Stripping wsit configuration from " + file.getAbsolutePath());
            WsitDocument wsitDoc = WsitDocument.parse(file);
            wsitDoc.stripWsitConfiguration();

            log("Saving file to " + output.getAbsolutePath());
            wsitDoc.save(output);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }

    private void checkProperties()
    {
        if (null == todir && null == tofile)
        {
            throw new BuildException("one of todir or tofile must be set");
        }
        if (null == file)
        {
            throw new BuildException("The required attribute file must be set");
        }
        if (!file.exists())
        {
            throw new BuildException("The file specified by file attribute must exist. file: " + file.getAbsolutePath());
        }
    }

    /**
     * Required - the file to strip WSIT configuration from. Must be a wsit configuration file
     * (starts with the wsdl document element).
     *
     * @param file
     */
    public void setFile(File file)
    {
        this.file = file;
    }

    /**
     * Required - The configuration files will be written to this directory. Either this
     * or tofile must be specified.
     *
     * @param output
     */
    public void setTodir(File output)
    {
        this.todir = output;
    }

    /**
     * Required - The configuration files will be written to this file. Either this or
     * todir must be specified.
     *
     * @param output
     */
    public void setTofile(File output)
    {
        this.tofile = output;
    }
}
