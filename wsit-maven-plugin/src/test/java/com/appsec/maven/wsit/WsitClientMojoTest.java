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
import java.net.URL;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.appsec.wsitbt.core.wsit.Namespace;
import com.appsec.wsitbt.core.holders.SecureConversation;

public class WsitClientMojoTest 
{	
	private File tempDir;
	private MavenProject project;
	
	@Before
	public void setUp()
	{
		tempDir = TestUtils.createRandomTempDir();
		project = new MavenProject(new Model());
	}
	
	@After
	public void tearDown()
	{
		TestUtils.deleteDir(tempDir);
	}

	@Test
	public void testConfiguresCallbackHandlers() throws Exception
	{
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Callback config = new Callback();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		config.setSamlHandler("com.appsec.SAMLHandler");
		task.setCallback(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "CallbackHandler");
		assertEquals(3, nodes.getLength());
		
		for (int i=0; i<nodes.getLength(); i++) {
			Element cb = (Element)nodes.item(i);
			String name = cb.getAttribute("name");
			if (name.equals("samlHandler")) {
				assertEquals("com.appsec.SAMLHandler", cb.getAttribute("classname"));
			}
			else if (name.equals("usernameHandler")) {
				assertEquals("com.appsec.UsernameHandler", cb.getAttribute("classname"));
			}
			else if (name.equals("passwordHandler")) {
				assertEquals("com.appsec.PasswordHandler", cb.getAttribute("classname"));
			}
			else {
				fail("Unexpected CallbackHandler: " + name);
			}
		}
	}
	
	@Test
	public void testConfiguresKeyStore() throws Exception
	{
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Keystore config = new Keystore();
		config.setAlias("test");
		config.setAliasSelector("com.appsec.AliasSelector");
		config.setCallbackHandler("com.appsec.CallbackHandler");
		config.setKeypass("changeit");
		config.setStorepass("changeitagain");
		config.setLocation("c:\\key.store");
		config.setType("JKS");
		task.setKeystore(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "KeyStore");
		assertEquals(1, nodes.getLength());
		
		Element cb = (Element)nodes.item(0);
		assertEquals("test", cb.getAttribute("alias"));
		assertEquals("com.appsec.AliasSelector", cb.getAttribute("aliasSelector"));
		assertEquals("com.appsec.CallbackHandler", cb.getAttribute("callbackHandler"));
		assertEquals("changeit", cb.getAttribute("keypass"));
		assertEquals("changeitagain", cb.getAttribute("storepass"));
		assertEquals("c:\\key.store", cb.getAttribute("location"));
		assertEquals("JKS", cb.getAttribute("type"));
	}
	
	@Test
	public void testConfiguresTrustStore() throws Exception
	{
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Truststore config = new Truststore();
		config.setPeeralias("test");
		config.setCallbackHandler("com.appsec.CallbackHandler");
		config.setCertSelector("com.appsec.CertSelector");
		config.setStorepass("changeitagain");
		config.setLocation("c:\\key.store");
		config.setType("JKS");
		task.setTruststore(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "TrustStore");
		assertEquals(1, nodes.getLength());
		
		Element cb = (Element)nodes.item(0);
		assertEquals("test", cb.getAttribute("peeralias"));
		assertEquals("com.appsec.CertSelector", cb.getAttribute("certSelector"));
		assertEquals("com.appsec.CallbackHandler", cb.getAttribute("callbackHandler"));
		assertEquals("changeitagain", cb.getAttribute("storepass"));
		assertEquals("c:\\key.store", cb.getAttribute("location"));
		assertEquals("JKS", cb.getAttribute("type"));
	}
	
	@Test
	public void testConfiguresCertStore() throws Exception
	{
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Certstore config = new Certstore();
		config.setCallbackHandler("com.appsec.CallbackHandler");
		config.setCertSelector("com.appsec.CertSelector");
		task.setCertstore(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "CertStore");
		assertEquals(1, nodes.getLength());
		
		Element cb = (Element)nodes.item(0);
		assertEquals("com.appsec.CertSelector", cb.getAttribute("certSelector"));
		assertEquals("com.appsec.CallbackHandler", cb.getAttribute("callbackHandler"));
	}
	
    @Test
    public void testConfiguresSecureConversation() throws Exception
    {
        URL url = WsitClientMojoTest.class.getResource("TestService.xml");
        File file = new File(url.toURI());
        WsitClientMojo task = new WsitClientMojo();
        task.setWsdl(file);

        task.setOutputdir(tempDir);

        SecureConversation config = new SecureConversation();
        config.setLifetime(5000);
        config.setRenewExpiredSCT(true);
        task.setSecureconversation(config);
        task.setProject(project);
        task.execute();

        Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
        NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
        assertEquals(1, nodes.getLength());

        Element policyElem = (Element) nodes.item(0);
        nodes = policyElem.getElementsByTagNameNS(Namespace.SCC, "SCClientConfiguration");
        assertEquals(1, nodes.getLength());

        Element scElem = (Element) nodes.item(0);
        assertEquals("true", scElem.getAttribute("renewExpiredSCT"));

        nodes = scElem.getElementsByTagNameNS(Namespace.SCC, "LifeTime");
        assertEquals(1, nodes.getLength());
        Element lifetime = (Element) nodes.item(0);
        assertEquals("5000", lifetime.getFirstChild().getTextContent());
    }

    @Test
    public void testConfiguresSecureConversationDefaults() throws Exception
    {
        URL url = WsitClientMojoTest.class.getResource("TestService.xml");
        File file = new File(url.toURI());
        WsitClientMojo task = new WsitClientMojo();
        task.setWsdl(file);

        task.setOutputdir(tempDir);

        SecureConversation config = new SecureConversation();
        task.setSecureconversation(config);
        task.setProject(project);
        task.execute();

        Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
        NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
        assertEquals(1, nodes.getLength());

        Element policyElem = (Element) nodes.item(0);
        nodes = policyElem.getElementsByTagNameNS(Namespace.SCC, "SCClientConfiguration");
        assertEquals(1, nodes.getLength());

        Element scElem = (Element) nodes.item(0);
        assertEquals("false", scElem.getAttribute("renewExpiredSCT"));

        nodes = scElem.getElementsByTagNameNS(Namespace.SCC, "LifeTime");
        assertEquals(1, nodes.getLength());
        Element lifetime = (Element) nodes.item(0);
        assertEquals(SecureConversation.DEFAULT_LIFETIME + "", lifetime.getFirstChild().getTextContent());
    }

	@Test
	public void testPolicyReferencesAreSet() throws Exception
	{
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Callback config = new Callback();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.setCallback(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "binding");
		assertEquals(1, nodes.getLength());
		
		nodes = ((Element)nodes.item(0)).getElementsByTagNameNS(Namespace.WSP, "PolicyReference");
		assertEquals(1, nodes.getLength());
		
		Element policyRef = (Element)nodes.item(0);
		String uri = policyRef.getAttribute("URI");
		assertEquals("#Client_BindingPolicy", uri);
	}
	
	@Test
	public void testWsitClientCreated() throws Exception
	{
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Callback config = new Callback();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.setCallback(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/wsit-client.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "import");
		assertEquals(1, nodes.getLength());
		
		Element importElem = (Element)nodes.item(0);
		String namespace = importElem.getAttribute("namespace");
		String location = importElem.getAttribute("location");
		assertEquals("test1", namespace);
		assertEquals("TestService.xml", location);
	}
	
	@Test
	public void testWsitClientMerged() throws Exception
	{
		File clientSrc = new File(WsitClientMojoTest.class.getResource("wsit-client.xml").toURI());
		File clientDest = new File(tempDir.getAbsolutePath() + "/wsit-client.xml");
		FileUtils.copyFile(clientSrc, clientDest);
		
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Callback config = new Callback();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.setCallback(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/wsit-client.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "import");
		assertEquals(2, nodes.getLength());
		
		for (int i=0; i<nodes.getLength(); i++) {
			Element importElem = (Element)nodes.item(i);
			String namespace = importElem.getAttribute("namespace");
			String location = importElem.getAttribute("location");
			if (namespace.equals("test1")) {
				assertEquals("TestService.xml", location);
			}
			else if (namespace.equals("http://ping/")) {
				assertEquals("PingService.xml", location);
			}
			else {
				fail("Unexpected import of namespace " + namespace);
			}
		}
	}
	
	@Test
	public void testNoDuplicateImports() throws Exception
	{
		File clientSrc = new File(WsitClientMojoTest.class.getResource("wsit-client.xml").toURI());
		File clientDest = new File(tempDir.getAbsolutePath() + "/wsit-client.xml");
		FileUtils.copyFile(clientSrc, clientDest);
		
		URL url = WsitClientMojoTest.class.getResource("PingService.xml");
		File file = new File(url.toURI());
		WsitClientMojo task = new WsitClientMojo();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		Callback config = new Callback();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.setCallback(config);
		task.setProject(project);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/wsit-client.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "import");
		assertEquals(1, nodes.getLength());
		
		for (int i=0; i<nodes.getLength(); i++) {
			Element importElem = (Element)nodes.item(i);
			String namespace = importElem.getAttribute("namespace");
			String location = importElem.getAttribute("location");
			if (namespace.equals("http://ping/")) {
				assertEquals("PingService.xml", location);
			}
			else {
				fail("Unexpected import of namespace " + namespace);
			}
		}
	}
	
	@Test
	public void testTaskChecksOutputdirRequired() throws Exception
	{
		URL url = WsitClientMojoTest.class.getResource("TestService.xml");
		File wsdl = new File(url.toURI());
		
		WsitClientMojo task = new WsitClientMojo();

		task.setProject(project);
		task.setWsdl(wsdl);
		
		try {
			task.execute();
			fail("Expected exception when no outputdir set");
		}
		catch (MojoFailureException e) {
		}
	}
	
	@Test
	public void testTaskChecksWsdlRequired() throws Exception
	{		
		WsitClientMojo task = new WsitClientMojo();
		task.setOutputdir(tempDir);
		task.setProject(project);
		
		try {
			task.execute();
			fail("Expected exception when wsdl not set");
		}
		catch (MojoFailureException e) {
		}
		
		task.setWsdl(new File("C:/DoesNotExist"));
		
		try {
			task.execute();
			fail("Expected exception when wsdl does not exist");
		}
		catch (MojoFailureException e) {
		}
	}
}
