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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.appsec.wsitbt.core.util.WSITBTUtil;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.appsec.wsitbt.core.wsit.Namespace;
import com.appsec.wsitbt.core.wsit.WsitDocument;
import com.appsec.wsitbt.core.holders.SecureConversation;

/**
 * Generates wsit client configuration files.
 *
 * @author John Hite
 * @goal client
 * @phase generate-resources
 * @since 1.0
 */
public final class WsitClientMojo extends AbstractMojo
{
    /**
     * The WSDL to create client configuration for.
     *
     * @parameter expression="${wsit-client.wsdl}"
     * @required
     * @since 1.0
     */
    private File wsdl;

    /**
     * A document containing a Policy definition to be
     * used in the client configuration.
     *
     * @parameter expression="${wsit-client.policy}"
     * @since 1.0
     */
    private File policy;

    /**
     * The configuration files will be written to this directory.
     *
     * @parameter expression="${wsit-client.outputdir}" default-value="${project.build.directory}/generated-sources/resources"
     * @required
     * @since 1.0
     */
    private File outputdir;


    /**
     * <p>Required if a policy is set - Specifies the id of the
     * desired policy.</p>
     * <p/>
     * <p>If the is specified and policy is not set, this will be
     * used as the id of the automatically generated policy.</p>
     *
     * @parameter expression="${wsit-client.bindingpolicy}"
     * @since 1.0
     */
    private String bindingpolicy;

    /**
     * Overrides the output file name.
     *
     * @parameter expression="${wsit-client.outputfile}"
     * @since 1.0
     */
    private String outputfile;

    /**
     * Optional callback configuration.
     *
     * @parameter
     * @since 1.0
     */
    private Callback callback;

    /**
     * Optional keystore configuration.
     *
     * @parameter
     * @since 1.0
     */
    private Keystore keystore;

    /**
     * Optional truststore configuration.
     *
     * @parameter
     * @since 1.0
     */
    private Truststore truststore;

    /**
     * Optional certstore configuration
     *
     * @parameter
     * @since 1.0
     */
    private Certstore certstore;

    /**
     * Optional Secure Conversation configuration.
     *
     * @parameter
     * @since 1.0
     */
    private SecureConversation secureconversation;

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
            //Validate properties.
            checkProperties();

            WSITBTUtil.doMkDirs(outputdir);

            //If outputfile is not specified, set it to the name of the wsdl file with a .xml extension.
            if (outputfile == null)
            {
                outputfile = wsdl.getName();
                outputfile = outputfile.substring(0, outputfile.lastIndexOf('.')) + ".xml";
            }

            final File wsitOut = new File(outputdir.getAbsoluteFile(), outputfile);
            FileUtils.copyFile(wsdl, wsitOut);

            WsitDocument wsitDoc = WsitDocument.parse(wsitOut, true);

            if (null != policy)
            {
                getLog().debug("Using policies from " + policy.getAbsolutePath());
                wsitDoc.mergePolicy(policy);
            }
            else
            {
                Document defaultPolicy = createDefaultPolicyDocument();
                wsitDoc.mergePolicy(defaultPolicy);
            }

            wsitDoc.setBindingPolicy(this.bindingpolicy);

            getLog().info("Creating " + wsitOut.getAbsolutePath());
            wsitDoc.save(wsitOut);

            //Create a new wsit-client.xml or update it if it exists.
            final File clientOut = new File(outputdir.getAbsoluteFile(), "wsit-client.xml");

            final WsitDocument clientDoc;
            if (clientOut.exists())
            {
                getLog().info("Updating WSIT client configuration " + clientOut.getAbsolutePath());
                clientDoc = WsitDocument.parse(clientOut);
            }
            else
            {
                getLog().info("Creating WSIT client configuration " + clientOut.getAbsolutePath());
                clientDoc = WsitDocument.newDocument("mainclientconfig");
            }

            clientDoc.importWsitDocument(wsitDoc, wsitOut.getName());
            clientDoc.save(clientOut);

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
            throw new MojoExecutionException("wsit-client failed to generate client config.", e);
        }
    }

    private void checkProperties() throws MojoFailureException
    {
        if (null == outputdir)
        {
            throw new MojoFailureException("The required attribute outputdir must be set");
        }
        if (null == wsdl)
        {
            throw new MojoFailureException("The required attribute wsdl must be set");
        }
        if (!wsdl.exists())
        {
            throw new MojoFailureException("The file specified by wsdl attribute must exist. wsdl: " + wsdl.getAbsolutePath());
        }
        if (null == bindingpolicy && policy != null)
        {
            getLog().warn("A policy is provided but a bindingpolicy is not specified");
        }
        if (null != policy && null == callback && null == keystore && null == truststore && null == certstore)
        {
            getLog().warn("No WSIT Configuration has been specified");
        }
    }

    /**
     * Constructs a wsp:Policy document using the task properties.
     *
     * @return
     * @throws ParserConfigurationException
     */
    private Document createDefaultPolicyDocument() throws ParserConfigurationException
    {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        dbfac.setNamespaceAware(true);
        dbfac.setValidating(false);
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document wsitDoc = docBuilder.newDocument();

        if (null == bindingpolicy)
        {
            bindingpolicy = "Client_BindingPolicy";
        }

        Element def = wsitDoc.createElementNS(Namespace.WSDL, "definitions");

        Element policyElem = wsitDoc.createElementNS(Namespace.WSP, "wsp:Policy");
        policyElem.setAttributeNS(Namespace.WSU, "wsu:Id", bindingpolicy);

        Element exactlyOneElem = wsitDoc.createElementNS(Namespace.WSP, "wsp:ExactlyOne");
        policyElem.appendChild(exactlyOneElem);

        Element allElem = wsitDoc.createElementNS(Namespace.WSP, "wsp:All");
        exactlyOneElem.appendChild(allElem);

        if (null != this.callback)
        {
            configureCallbackHandlers(wsitDoc, allElem);
        }

        if (null != this.keystore)
        {
            configureKeyStore(wsitDoc, allElem);
        }

        if (null != this.truststore)
        {
            configureTrustStore(wsitDoc, allElem);
        }

        if (null != this.certstore)
        {
            configureCertStore(wsitDoc, allElem);
        }

        if (null != this.secureconversation)
        {
            configureSecureConversation(wsitDoc, allElem);
        }

        def.appendChild(policyElem);
        wsitDoc.appendChild(def);

        return wsitDoc;
    }

    private void configureKeyStore(Document wsitDoc, Element parent)
    {
        Element ks = wsitDoc.createElementNS(Namespace.SC1, "sc1:KeyStore");
        ks.setAttributeNS(Namespace.WSPP, "wspp:visibility", "private");
        parent.appendChild(ks);

        if (null != keystore.getAlias())
        {
            ks.setAttribute("alias", keystore.getAlias());
        }
        if (null != keystore.getAliasSelector())
        {
            ks.setAttribute("aliasSelector", keystore.getAliasSelector());
        }
        if (null != keystore.getCallbackHandler())
        {
            ks.setAttribute("callbackHandler", keystore.getCallbackHandler());
        }
        if (null != keystore.getKeypass())
        {
            ks.setAttribute("keypass", keystore.getKeypass());
        }
        if (null != keystore.getLocation())
        {
            ks.setAttribute("location", keystore.getLocation());
        }
        if (null != keystore.getStorepass())
        {
            ks.setAttribute("storepass", keystore.getStorepass());
        }
        if (null != keystore.getType())
        {
            ks.setAttribute("type", keystore.getType());
        }
    }

    private void configureTrustStore(Document wsitDoc, Element parent)
    {
        Element ts = wsitDoc.createElementNS(Namespace.SC1, "sc1:TrustStore");
        ts.setAttributeNS(Namespace.WSPP, "wspp:visibility", "private");
        parent.appendChild(ts);

        if (null != truststore.getPeeralias())
        {
            ts.setAttribute("peeralias", truststore.getPeeralias());
        }
        if (null != truststore.getCertSelector())
        {
            ts.setAttribute("certSelector", truststore.getCertSelector());
        }
        if (null != truststore.getCallbackHandler())
        {
            ts.setAttribute("callbackHandler", truststore.getCallbackHandler());
        }
        if (null != truststore.getLocation())
        {
            ts.setAttribute("location", truststore.getLocation());
        }
        if (null != truststore.getStorepass())
        {
            ts.setAttribute("storepass", truststore.getStorepass());
        }
        if (null != truststore.getType())
        {
            ts.setAttribute("type", truststore.getType());
        }
    }

    public void configureCertStore(Document wsitDoc, Element parent)
    {
        Element cs = wsitDoc.createElementNS(Namespace.SC1, "sc1:CertStore");
        cs.setAttributeNS(Namespace.WSPP, "wspp:visibility", "private");
        parent.appendChild(cs);

        if (null != certstore.getCallbackHandler())
        {
            cs.setAttribute("callbackHandler", certstore.getCallbackHandler());
        }
        if (null != certstore.getCertSelector())
        {
            cs.setAttribute("certSelector", certstore.getCertSelector());
        }
    }

    /**
     * Add callback handler configuration
     *
     * @param wsitDoc
     * @param parent
     */
    private void configureCallbackHandlers(Document wsitDoc, Element parent)
    {
        Element sc = wsitDoc.createElementNS(Namespace.SC1, "sc1:CallbackHandlerConfiguration");
        sc.setAttributeNS(Namespace.WSPP, "wspp:visibility", "private");
        parent.appendChild(sc);

        if (null != callback.getSamlHandler())
        {
            Element handler = wsitDoc.createElementNS(Namespace.SC1, "sc1:CallbackHandler");
            handler.setAttribute("classname", callback.getSamlHandler());
            handler.setAttribute("name", "samlHandler");
            sc.appendChild(handler);
        }
        if (null != callback.getUsernameHandler())
        {
            Element handler = wsitDoc.createElementNS(Namespace.SC1, "sc1:CallbackHandler");
            handler.setAttribute("classname", callback.getUsernameHandler());
            handler.setAttribute("name", "usernameHandler");
            sc.appendChild(handler);
        }
        if (null != callback.getPasswordHandler())
        {
            Element handler = wsitDoc.createElementNS(Namespace.SC1, "sc1:CallbackHandler");
            handler.setAttribute("classname", callback.getPasswordHandler());
            handler.setAttribute("name", "passwordHandler");
            sc.appendChild(handler);
        }
        if (null != callback.getJmacCallbackHandler())
        {
            Element handler = wsitDoc.createElementNS(Namespace.SC1, "sc1:CallbackHandler");
            handler.setAttribute("classname", callback.getJmacCallbackHandler());
            handler.setAttribute("name", "jmacCallbackHandler");
            sc.appendChild(handler);
        }
        if (null != callback.getXwssCallbackHandler())
        {
            Element handler = wsitDoc.createElementNS(Namespace.SC1, "sc1:CallbackHandler");
            handler.setAttribute("classname", callback.getXwssCallbackHandler());
            handler.setAttribute("name", "xwssCallbackHandler");
            sc.appendChild(handler);
        }
    }

    public void configureSecureConversation(Document wsitDoc, Element parent)
    {
        Element sc = wsitDoc.createElementNS(Namespace.SCC, "scc:SCClientConfiguration");
        sc.setAttributeNS(Namespace.WSPP, "wspp:visibility", "private");
        sc.setAttribute("renewExpiredSCT", String.valueOf(secureconversation.isRenewExpiredSCT()));
        parent.appendChild(sc);

        Element lifetime = wsitDoc.createElementNS(Namespace.SCC, "scc:LifeTime");
        lifetime.appendChild(wsitDoc.createTextNode(String.valueOf(secureconversation.getLifetime())));
        sc.appendChild(lifetime);
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

    public void setBindingpolicy(String bindingpolicy)
    {
        this.bindingpolicy = bindingpolicy;
    }

    public void setOutputfile(String outputfile)
    {
        this.outputfile = outputfile;
    }

    public void setCallback(Callback callback)
    {
        this.callback = callback;
    }

    public void setKeystore(Keystore keystore)
    {
        this.keystore = keystore;
    }

    public void setTruststore(Truststore truststore)
    {
        this.truststore = truststore;
    }

    public void setCertstore(Certstore certstore)
    {
        this.certstore = certstore;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

    public void setSecureconversation(SecureConversation secureconversation)
    {
        this.secureconversation = secureconversation;
    }
}
