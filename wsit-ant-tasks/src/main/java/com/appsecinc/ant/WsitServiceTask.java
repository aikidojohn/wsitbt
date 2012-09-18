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
package com.appsecinc.ant;

import java.io.File;

import com.appsec.wsitbt.core.util.WSITBTUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

import com.appsec.wsitbt.core.wsit.WsitDocument;

/**
 * <p>Creates WSIT service configuration file from a WSDL and a Policy fragment.</p>
 * <p>
 * The task takes the WSDL file specified strips any existing policy information from it
 * and merges in the Policy fragment.
 * </p>
 * <p>
 * The bindingpolicy attribute specifies the id of the Policy to be applied to the binding.
 * This is usually the Policy that defines the Security Policy that the service requires and
 * any server side configuration required by that Policy (such as keystore locations).
 * </p>
 * <p>
 * The inputpolicy and outputpolicy can be used to apply additional Policies to the input
 * and output operations as well.
 * </p>
 * <p>
 * The class attribute specifies the full name of the web service implementation class.
 * The task will create a file in the outputdir named wsit-[class].xml. So if the value of the
 * class attribute is <code>"com.appsec.ping.PingService"</code> then the output file will be named
 * <code>wsit-com.appsec.ping.PingService.xml</code>. Metro looks on the classpath for WSIT configuration
 * files with this format.
 * </p>
 * <p/>
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
 * &lt;wsit-service
 *     outputdir="src/WEB-INF"
 *     wsdl="src/wsdl/MyService.wsdl"
 *     policy="src/policy/WSTrustPolicy.xml"
 *     class="com.appsec.MyService"
 *     bindingpolicy="TrustPolicy"
 *     inputpolicy="SignedInputPolicy"
 *     outputpolicy="SignedOutputPolicy"
 * /&gt;
 * </code>
 * </pre>
 *
 * @author jhite
 */
public final class WsitServiceTask extends Task
{
    private File wsdl;
    private File policy;
    private File outputdir;
    private String classname;
    private String bindingpolicy;
    private String inputpolicy;
    private String outputpolicy;
    private String faultpolicy;
    private boolean updatewsdl;
    private File wsdloutdir;

    public void execute()
    {
        try
        {
            checkProperties();

            WSITBTUtil.doMkDirs(outputdir);
            final File output = new File(outputdir.getAbsoluteFile(), "wsit-" + classname + ".xml");
            log("Creating " + output.getAbsolutePath());

            FileUtils.getFileUtils().copyFile(wsdl, output);

            WsitDocument wsitDoc = WsitDocument.parse(output, true);
            wsitDoc.mergePolicy(policy);
            wsitDoc.setBindingPolicy(bindingpolicy);
            wsitDoc.setInputPolicy(inputpolicy);
            wsitDoc.setOutputPolicy(outputpolicy);
            wsitDoc.setFaultPolicy(faultpolicy);

            wsitDoc.save(output);

            if (this.updatewsdl)
            {
                final File wsdlout;
                if (this.wsdloutdir != null)
                {
                    WSITBTUtil.doMkDirs(wsdloutdir);
                    wsdlout = new File(wsdloutdir.getAbsolutePath(), wsdl.getName());
                }
                else
                {
                    wsdlout = new File(outputdir.getAbsolutePath(), wsdl.getName());
                }
                log("Saving updated wsdl to " + wsdlout.getAbsolutePath());
                wsitDoc.stripWsitConfiguration();
                wsitDoc.save(wsdlout);
            }
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }

    private void checkProperties()
    {
        if (null == outputdir)
        {
            throw new BuildException("The required attribute outputdir must be set");
        }
        if (null == policy)
        {
            throw new BuildException("The required attribute policy must be set");
        }
        if (!policy.exists())
        {
            throw new BuildException("The file specified by policy attribute must exist. policy: " + policy.getAbsolutePath());
        }
        if (null == wsdl)
        {
            throw new BuildException("The required attribute wsdl must be set");
        }
        if (!wsdl.exists())
        {
            throw new BuildException("The file specified by wsdl attribute must exist. wsdl: " + wsdl.getAbsolutePath());
        }
        if (null == classname)
        {
            throw new BuildException("The required attribute class must be set");
        }
        if (null == bindingpolicy)
        {
            throw new BuildException("The required attribute bindingpolicy must be set");
        }
        if (null == inputpolicy)
        {
            this.log("An inputpolicy is not specified", Project.MSG_VERBOSE);
        }
        if (null == outputpolicy)
        {
            this.log("An outputpolicy is not specified", Project.MSG_VERBOSE);
        }
        if (null == faultpolicy)
        {
            this.log("A faultpolicy is not specified", Project.MSG_VERBOSE);
        }
    }

    /**
     * Required - the service's WSDL.
     *
     * @param wsdl
     */
    public void setWsdl(File wsdl)
    {
        this.wsdl = wsdl;
    }

    /**
     * Required - the policy fragment with WSIT configuration to apply.
     *
     * @param policy
     */
    public void setPolicy(File policy)
    {
        this.policy = policy;
    }

    /**
     * Required - The configuration files will be written to this directory.
     *
     * @param output
     */
    public void setOutputdir(File output)
    {
        this.outputdir = output;
    }

    /**
     * Required - the full name (including the package) of the web service implementation class.
     *
     * @param classname
     */
    public void setClass(String classname)
    {
        this.classname = classname;
    }

    /**
     * Required - The id of a Policy in the Policy fragment to apply to the binding.
     * This should be a policy that defines the security requirements of the service.
     *
     * @param bindingpolicy
     */
    public void setBindingpolicy(String bindingpolicy)
    {
        this.bindingpolicy = bindingpolicy;
    }

    /**
     * Optional - Specifies a policy to apply to all input operations
     *
     * @param inputpolicy
     */
    public void setInputpolicy(String inputpolicy)
    {
        this.inputpolicy = inputpolicy;
    }

    /**
     * Optional - Specifies a policy to apply to all output operations
     *
     * @param outputpolicy
     */
    public void setOutputpolicy(String outputpolicy)
    {
        this.outputpolicy = outputpolicy;
    }

    /**
     * Optional - Specifies a policy to apply to all fault operations
     *
     * @param faultpolicy
     */
    public void setFaultpolicy(String faultpolicy)
    {
        this.faultpolicy = faultpolicy;
    }

    /**
     * Optional - updates the wsdl with the policy. This removes any WSIT specific configuration from
     * the policy and applies the policy to the wsdl.
     * Defaults to false.
     *
     * @param updatewsdl
     */
    public void setUpdatewsdl(boolean updatewsdl)
    {
        this.updatewsdl = updatewsdl;
    }

    /**
     * Optional - the output folder for the wsdl. Defaults to the outputdir.
     *
     * @param wsdloutdir
     */
    public void setWsdloutdir(File wsdloutdir)
    {
        this.wsdloutdir = wsdloutdir;
    }
}
