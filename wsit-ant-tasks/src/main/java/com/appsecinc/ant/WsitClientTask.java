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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.appsec.wsitbt.core.util.WSITBTUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.appsec.wsitbt.core.wsit.Namespace;
import com.appsec.wsitbt.core.wsit.WsitDocument;
import com.appsec.wsitbt.core.holders.SecureConversation;

/**
 * <p>Creates WSIT client configuration files from a WSDL and policy information.</p>
 * <p>
 * The task takes the WSDL file specified strips any existing policy information from it
 * and merges in the policy information provided. The task can take a policy file and
 * merge it in or it can create a policy for specifying callback handlers automatically.
 * </p>
 * <p>
 * If you specify a policy, the policy will be used. You must also specify a bindingpolicy
 * to use. The bindingpolicy must be a policy id that exists in the policy document provided.
 * </p>
 * <p/>
 * <p>
 * If you only need to specify callback handlers, the task can automatically construct
 * a policy for you. Simply provide any of the callback handler properties
 * (samlhandler, passwordhandler, usernamehandler) and a policy will be automatically
 * created and merged into the WSIT configuration file.
 * </p>
 * <p/>
 * <p>
 * The task will output two files, the first is the client configuration for the service
 * that the WSDL defines. The second is the main WSIT client configuration file called
 * wsit-client.xml. If a wsit-client.xml file already exists in the output folder, the
 * file will be updated.
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
 * &lt;wsit-client
 *     outputdir="src/META-INF"
 *     wsdl="src/wsdl/MyService.wsdl"&gt;
 *
 *     &lt;callback
 *         usernameHandler="com.appsec.UsernameHandler"
 *         passwordHandler="com.appsec.PasswordHandler" /&gt;
 *
 *     &lt;keystore
 *         aliasSelector="com.appsec.AliasSelector"
 *         callbackHandler="com.appsec.KeystoreHandler" /&gt;
 *
 *     &lt;truststore ..../&gt;
 *     &lt;certstore ..../&gt;
 * &lt;/wsit-client&gt;
 * </code>
 * </pre>
 * <p>
 * This code will produce two files in <code>src/META-INF</code>: <code>MyService.xml</code>
 * and <code>wsit-client.xml</code>. This configuration tells any MyService clients to
 * use the specified Username Handler and Password Handler.
 * </p>
 * <p/>
 * <p>
 * The inner tags (callback, keystore, truststore, and certstore) each allow you to specify
 * security configuration to support different security scenarios. Not all of the tags are required
 * for every security scenario. For a good list of security
 * scenarios, please see http://xwss.java.net/articles/security_config.html
 * </p>
 *
 * @author jhite
 */
public final class WsitClientTask extends Task
{
    private File wsdl;
    private File policy;
    private File outputdir;
    private String outputfile;
    private String bindingpolicy;
    private CallbackConfig callback;
    private KeyStoreConfig keystore;
    private TrustStoreConfig truststore;
    private CertStoreConfig certstore;
    private SecureConversation secureconversation;

    public void execute()
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
            FileUtils.getFileUtils().copyFile(wsdl, wsitOut);

            WsitDocument wsitDoc = WsitDocument.parse(wsitOut, true);

            if (null != policy)
            {
                log("Using policies from " + policy.getAbsolutePath(), Project.MSG_VERBOSE);
                wsitDoc.mergePolicy(policy);
            }
            else
            {
                Document defaultPolicy = createDefaultPolicyDocument();
                wsitDoc.mergePolicy(defaultPolicy);
            }

            wsitDoc.setBindingPolicy(this.bindingpolicy);

            log("Creating " + wsitOut.getAbsolutePath());
            wsitDoc.save(wsitOut);

            //Create a new wsit-client.xml or update it if it exists.
            final File clientOut = new File(outputdir.getAbsoluteFile(), "wsit-client.xml");

            final WsitDocument clientDoc;
            if (clientOut.exists())
            {
                log("Updating WSIT client configuration " + clientOut.getAbsolutePath());
                clientDoc = WsitDocument.parse(clientOut);
            }
            else
            {
                log("Creating WSIT client configuration " + clientOut.getAbsolutePath());
                clientDoc = WsitDocument.newDocument("mainclientconfig");
            }

            clientDoc.importWsitDocument(wsitDoc, wsitOut.getName());
            clientDoc.save(clientOut);
        }
        catch (RuntimeException e)
        {
            throw new BuildException(e);
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
        if (null == wsdl)
        {
            throw new BuildException("The required attribute wsdl must be set");
        }
        if (!wsdl.exists())
        {
            throw new BuildException("The file specified by wsdl attribute must exist. wsdl: " + wsdl.getAbsolutePath());
        }
        if (null == bindingpolicy && policy != null)
        {
            this.log("A policy is provided but a bindingpolicy is not specified", Project.MSG_WARN);
        }
        if (null != policy && null == callback && null == keystore && null == truststore && null == certstore)
        {
            this.log("No WSIT Configuration has been specified", Project.MSG_WARN);
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

    private void configureSecureConversation(Document wsitDoc, Element parent)
    {
        Element sc = wsitDoc.createElementNS(Namespace.SCC, "scc:SCClientConfiguration");
        sc.setAttributeNS(Namespace.WSPP, "wspp:visibility", "private");
        sc.setAttribute("renewExpiredSCT", String.valueOf(secureconversation.isRenewExpiredSCT()));
        parent.appendChild(sc);

        Element lifetime = wsitDoc.createElementNS(Namespace.SCC, "scc:LifeTime");
        lifetime.appendChild(wsitDoc.createTextNode(String.valueOf(secureconversation.getLifetime())));
        sc.appendChild(lifetime);
    }

    /**
     * Required - The WSDL to create client configuration for.
     *
     * @param wsdl
     */
    public void setWsdl(File wsdl)
    {
        this.wsdl = wsdl;
    }

    /**
     * Optional - A document containing a Policy definition to be
     * used in the client configuration.
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
     * <p>Required if a policy is set - Specifies the id of the
     * desired policy.</p>
     * <p/>
     * <p>If the is specified and policy is not set, this will be
     * used as the id of the automatically generated policy.</p>
     *
     * @param bindingpolicy
     */
    public void setBindingpolicy(String bindingpolicy)
    {
        this.bindingpolicy = bindingpolicy;
    }

    /**
     * Optional - overrides the output file name.
     *
     * @param output
     */
    public void setOutputfile(String output)
    {
        this.outputfile = output;
    }

    public void addConfiguredCallback(CallbackConfig cb)
    {
        this.callback = cb;
    }

    public void addConfiguredKeystore(KeyStoreConfig ks)
    {
        this.keystore = ks;
    }

    public void addConfiguredTruststore(TrustStoreConfig ts)
    {
        this.truststore = ts;
    }

    public void addConfiguredCertstore(CertStoreConfig cs)
    {
        this.certstore = cs;
    }

    public void addConfiguredSecureConversation(SecureConversation sc)
    {
        this.secureconversation = sc;
    }
}
