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
package com.appsec.maven.wsit;

import java.io.File;

import com.appsec.wsitbt.core.util.WSITBTUtil;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.appsec.wsitbt.core.wsit.WsitDocument;

/**
 * Generates wsit service configuration files.
 *
 * @author John Hite
 * @goal service
 * @phase generate-resources
 * @since 1.0
 */
public final class WsitServiceMojo extends AbstractMojo
{
    /**
     * The service's WSDL.
     *
     * @parameter expression="wsit-service.wsdl"
     * @required
     * @since 1.0
     */
    private File wsdl;

    /**
     * The policy fragment with WSIT configuration to apply.
     *
     * @parameter expression="wsit-service.policy"
     * @required
     * @since 1.0
     */
    private File policy;

    /**
     * The configuration files will be written to this directory.
     *
     * @parameter expression="wsit-service.outputdir" default-value="${project.build.directory}/generated-sources/resources"
     * @required
     * @since 1.0
     */
    private File outputdir;

    /**
     * The full name (including the package) of the web service implementation class.
     *
     * @parameter expression="wsit-service.classname"
     * @required
     * @since 1.0
     */
    private String classname;

    /**
     * The id of a Policy in the Policy fragment to apply to the binding.
     * This should be a policy that defines the security requirements of the service.
     *
     * @parameter expression="wsit-service.bindingpolicy"
     * @required
     * @since 1.0
     */
    private String bindingpolicy;

    /**
     * Specifies a policy to apply to all input operations
     *
     * @parameter expression="wsit-service.inputpolicy"
     * @since 1.0
     */
    private String inputpolicy;

    /**
     * Specifies a policy to apply to all output operations
     *
     * @parameter expression="wsit-service.outputpolicy"
     * @since 1.0
     */
    private String outputpolicy;

    /**
     * Specifies a policy to apply to all fault operations
     *
     * @parameter expression="wsit-service.outputpolicy"
     * @since 1.0
     */
    private String faultpolicy;

    /**
     * Updates the wsdl with the policy. This removes any WSIT specific configuration from
     * the policy and applies the policy to the wsdl.
     *
     * @parameter expression="wsit-service.updatewsdl" default-value="false"
     * @since 1.0
     */
    private boolean updatewsdl;

    /**
     * The output folder for the wsdl. Defaults to the outputdir.
     *
     * @parameter expression="wsit-service.wsdloutdir"
     * @since 1.0
     */
    private File wsdloutdir;

    /**
     * @parameter expression="${project}"
     * @readonly
     * @since 1.0
     */
    private MavenProject project;


    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            checkProperties();

            WSITBTUtil.doMkDirs(outputdir);
            final File output = new File(outputdir.getAbsoluteFile(), "wsit-" + classname + ".xml");
            getLog().info("Creating " + output.getAbsolutePath());

            FileUtils.copyFile(wsdl, output);

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
                getLog().info("Saving updated wsdl to " + wsdlout.getAbsolutePath());
                wsitDoc.stripWsitConfiguration();
                wsitDoc.save(wsdlout);
            }

            project.addCompileSourceRoot(outputdir.getAbsolutePath());
            Resource resource = new Resource();
            resource.setDirectory(outputdir.getAbsolutePath());
            project.addResource(resource);
        }
        catch (MojoFailureException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Failed to generate wsit configuration", e);
        }
    }

    private void checkProperties() throws MojoFailureException
    {
        if (null == outputdir)
        {
            throw new MojoFailureException("The required attribute outputdir must be set");
        }
        if (null == policy)
        {
            throw new MojoFailureException("The required attribute policy must be set");
        }
        if (!policy.exists())
        {
            throw new MojoFailureException("The file specified by policy attribute must exist. policy: " + policy.getAbsolutePath());
        }
        if (null == wsdl)
        {
            throw new MojoFailureException("The required attribute wsdl must be set");
        }
        if (!wsdl.exists())
        {
            throw new MojoFailureException("The file specified by wsdl attribute must exist. wsdl: " + wsdl.getAbsolutePath());
        }
        if (null == classname)
        {
            throw new MojoFailureException("The required attribute class must be set");
        }
        if (null == bindingpolicy)
        {
            throw new MojoFailureException("The required attribute bindingpolicy must be set");
        }
        if (null == inputpolicy)
        {
            getLog().debug("An inputpolicy is not specified");
        }
        if (null == outputpolicy)
        {
            getLog().debug("An outputpolicy is not specified");
        }
        if (null == faultpolicy)
        {
            getLog().debug("An faultpolicy is not specified");
        }
    }

    public void setWsdl(File wsdl)
    {
        this.wsdl = wsdl;
    }

    public void setPolicy(File policy)
    {
        this.policy = policy;
    }

    public void setOutputdir(File outputdir)
    {
        this.outputdir = outputdir;
    }

    public void setClassname(String classname)
    {
        this.classname = classname;
    }

    public void setBindingpolicy(String bindingpolicy)
    {
        this.bindingpolicy = bindingpolicy;
    }

    public void setInputpolicy(String inputpolicy)
    {
        this.inputpolicy = inputpolicy;
    }

    public void setOutputpolicy(String outputpolicy)
    {
        this.outputpolicy = outputpolicy;
    }
    
    public void setFaultpolicy(String faultpolicy)
    {
        this.faultpolicy = faultpolicy;
    }

    public void setUpdatewsdl(boolean updatewsdl)
    {
        this.updatewsdl = updatewsdl;
    }

    public void setWsdloutdir(File wsdloutdir)
    {
        this.wsdloutdir = wsdloutdir;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

}
