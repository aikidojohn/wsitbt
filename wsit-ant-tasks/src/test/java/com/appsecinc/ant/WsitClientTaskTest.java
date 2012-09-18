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
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.appsec.wsitbt.core.wsit.Namespace;
import com.appsec.wsitbt.core.holders.SecureConversation;

public class WsitClientTaskTest 
{	
	private File tempDir;
	
	@Before
	public void setUp()
	{
		tempDir = TestUtils.createRandomTempDir();
	}
	
	@After
	public void tearDown()
	{
		TestUtils.deleteDir(tempDir);
	}

	@Test
	public void testConfiguresCallbackHandlers() throws Exception
	{
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		CallbackConfig config = new CallbackConfig();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		config.setSamlHandler("com.appsec.SAMLHandler");
		task.addConfiguredCallback(config);
		
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		Assert.assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "CallbackHandler");
		Assert.assertEquals(3, nodes.getLength());
		
		for (int i=0; i<nodes.getLength(); i++) {
			Element cb = (Element)nodes.item(i);
			String name = cb.getAttribute("name");
			if (name.equals("samlHandler")) {
				Assert.assertEquals("com.appsec.SAMLHandler", cb.getAttribute("classname"));
			}
			else if (name.equals("usernameHandler")) {
				Assert.assertEquals("com.appsec.UsernameHandler", cb.getAttribute("classname"));
			}
			else if (name.equals("passwordHandler")) {
				Assert.assertEquals("com.appsec.PasswordHandler", cb.getAttribute("classname"));
			}
			else {
				Assert.fail("Unexpected CallbackHandler: " + name);
			}
		}
	}
	
	@Test
	public void testConfiguresKeyStore() throws Exception
	{
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		KeyStoreConfig config = new KeyStoreConfig();
		config.setAlias("test");
		config.setAliasSelector("com.appsec.AliasSelector");
		config.setCallbackHandler("com.appsec.CallbackHandler");
		config.setKeypass("changeit");
		config.setStorepass("changeitagain");
		config.setLocation("c:\\key.store");
		config.setType("JKS");
		task.addConfiguredKeystore(config);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		Assert.assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "KeyStore");
		Assert.assertEquals(1, nodes.getLength());
		
		Element cb = (Element)nodes.item(0);
		Assert.assertEquals("test", cb.getAttribute("alias"));
		Assert.assertEquals("com.appsec.AliasSelector", cb.getAttribute("aliasSelector"));
		Assert.assertEquals("com.appsec.CallbackHandler", cb.getAttribute("callbackHandler"));
		Assert.assertEquals("changeit", cb.getAttribute("keypass"));
		Assert.assertEquals("changeitagain", cb.getAttribute("storepass"));
		Assert.assertEquals("c:\\key.store", cb.getAttribute("location"));
		Assert.assertEquals("JKS", cb.getAttribute("type"));
	}
	
	@Test
	public void testConfiguresTrustStore() throws Exception
	{
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		TrustStoreConfig config = new TrustStoreConfig();
		config.setPeeralias("test");
		config.setCallbackHandler("com.appsec.CallbackHandler");
		config.setCertSelector("com.appsec.CertSelector");
		config.setStorepass("changeitagain");
		config.setLocation("c:\\key.store");
		config.setType("JKS");
		task.addConfiguredTruststore(config);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		Assert.assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "TrustStore");
		Assert.assertEquals(1, nodes.getLength());
		
		Element cb = (Element)nodes.item(0);
		Assert.assertEquals("test", cb.getAttribute("peeralias"));
		Assert.assertEquals("com.appsec.CertSelector", cb.getAttribute("certSelector"));
		Assert.assertEquals("com.appsec.CallbackHandler", cb.getAttribute("callbackHandler"));
		Assert.assertEquals("changeitagain", cb.getAttribute("storepass"));
		Assert.assertEquals("c:\\key.store", cb.getAttribute("location"));
		Assert.assertEquals("JKS", cb.getAttribute("type"));
	}
	
	@Test
	public void testConfiguresCertStore() throws Exception
	{
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		CertStoreConfig config = new CertStoreConfig();
		config.setCallbackHandler("com.appsec.CallbackHandler");
		config.setCertSelector("com.appsec.CertSelector");
		task.addConfiguredCertstore(config);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
		Assert.assertEquals(1, nodes.getLength());
		
		Element policyElem = (Element)nodes.item(0);
		nodes = policyElem.getElementsByTagNameNS(Namespace.SC1, "CertStore");
		Assert.assertEquals(1, nodes.getLength());
		
		Element cb = (Element)nodes.item(0);
		Assert.assertEquals("com.appsec.CertSelector", cb.getAttribute("certSelector"));
		Assert.assertEquals("com.appsec.CallbackHandler", cb.getAttribute("callbackHandler"));
	}
	
    @Test
    public void testConfiguresSecureConversation() throws Exception
    {
        URL url = WsitClientTaskTest.class.getResource("TestService.xml");
        File file = new File(url.toURI());
        WsitClientTask task = new WsitClientTask();
        task.setWsdl(file);

        task.setOutputdir(tempDir);

        SecureConversation sc = new SecureConversation();
        sc.setLifetime(5000);
        sc.setRenewExpiredSCT(true);
        task.addConfiguredSecureConversation(sc);
        task.execute();

        Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
        NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
        Assert.assertEquals(1, nodes.getLength());

        Element policyElem = (Element) nodes.item(0);
        nodes = policyElem.getElementsByTagNameNS(Namespace.SCC, "SCClientConfiguration");
        Assert.assertEquals(1, nodes.getLength());

        Element scElem = (Element) nodes.item(0);
        Assert.assertEquals("true", scElem.getAttribute("renewExpiredSCT"));

        nodes = scElem.getElementsByTagNameNS(Namespace.SCC, "LifeTime");
        Assert.assertEquals(1, nodes.getLength());
        Element lifetime = (Element) nodes.item(0);
        Assert.assertEquals("5000", lifetime.getFirstChild().getTextContent());
    }

    @Test
    public void testConfiguresSecureConversationDefaults() throws Exception
    {
        URL url = WsitClientTaskTest.class.getResource("TestService.xml");
        File file = new File(url.toURI());
        WsitClientTask task = new WsitClientTask();
        task.setWsdl(file);

        task.setOutputdir(tempDir);

        SecureConversation sc = new SecureConversation();
        task.addConfiguredSecureConversation(sc);
        task.execute();

        Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
        NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSP, "Policy");
        Assert.assertEquals(1, nodes.getLength());

        Element policyElem = (Element) nodes.item(0);
        nodes = policyElem.getElementsByTagNameNS(Namespace.SCC, "SCClientConfiguration");
        Assert.assertEquals(1, nodes.getLength());

        Element scElem = (Element) nodes.item(0);
        Assert.assertEquals("false", scElem.getAttribute("renewExpiredSCT"));

        nodes = scElem.getElementsByTagNameNS(Namespace.SCC, "LifeTime");
        Assert.assertEquals(1, nodes.getLength());
        Element lifetime = (Element) nodes.item(0);
        Assert.assertEquals(SecureConversation.DEFAULT_LIFETIME + "", lifetime.getFirstChild().getTextContent());
    }

	@Test
	public void testPolicyReferencesAreSet() throws Exception
	{
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		CallbackConfig config = new CallbackConfig();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.addConfiguredCallback(config);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/TestService.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "binding");
		Assert.assertEquals(1, nodes.getLength());
		
		nodes = ((Element)nodes.item(0)).getElementsByTagNameNS(Namespace.WSP, "PolicyReference");
		Assert.assertEquals(1, nodes.getLength());
		
		Element policyRef = (Element)nodes.item(0);
		String uri = policyRef.getAttribute("URI");
		Assert.assertEquals("#Client_BindingPolicy", uri);
	}
	
	@Test
	public void testWsitClientCreated() throws Exception
	{
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		CallbackConfig config = new CallbackConfig();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.addConfiguredCallback(config);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/wsit-client.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "import");
		Assert.assertEquals(1, nodes.getLength());
		
		Element importElem = (Element)nodes.item(0);
		String namespace = importElem.getAttribute("namespace");
		String location = importElem.getAttribute("location");
		Assert.assertEquals("test1", namespace);
		Assert.assertEquals("TestService.xml", location);
	}
	
	@Test
	public void testWsitClientMerged() throws Exception
	{
		File clientSrc = new File(WsitClientTaskTest.class.getResource("wsit-client.xml").toURI());
		File clientDest = new File(tempDir.getAbsolutePath() + "/wsit-client.xml");
		FileUtils.getFileUtils().copyFile(clientSrc, clientDest);
		
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		CallbackConfig config = new CallbackConfig();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.addConfiguredCallback(config);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/wsit-client.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "import");
		Assert.assertEquals(2, nodes.getLength());
		
		for (int i=0; i<nodes.getLength(); i++) {
			Element importElem = (Element)nodes.item(i);
			String namespace = importElem.getAttribute("namespace");
			String location = importElem.getAttribute("location");
			if (namespace.equals("test1")) {
				Assert.assertEquals("TestService.xml", location);
			}
			else if (namespace.equals("http://ping/")) {
				Assert.assertEquals("PingService.xml", location);
			}
			else {
				Assert.fail("Unexpected import of namespace " + namespace);
			}
		}
	}
	
	@Test
	public void testNoDuplicateImports() throws Exception
	{
		File clientSrc = new File(WsitClientTaskTest.class.getResource("wsit-client.xml").toURI());
		File clientDest = new File(tempDir.getAbsolutePath() + "/wsit-client.xml");
		FileUtils.getFileUtils().copyFile(clientSrc, clientDest);
		
		URL url = WsitClientTaskTest.class.getResource("PingService.xml");
		File file = new File(url.toURI());
		WsitClientTask task = new WsitClientTask();
		task.setWsdl(file);

		task.setOutputdir(tempDir);
		
		CallbackConfig config = new CallbackConfig();
		config.setUsernameHandler("com.appsec.UsernameHandler");
		config.setPasswordHandler("com.appsec.PasswordHandler");
		task.addConfiguredCallback(config);
		task.execute();
		
		Document wsitConfig = TestUtils.loadXmlFile(new File(tempDir.getAbsolutePath() + "/wsit-client.xml"));
		NodeList nodes = wsitConfig.getElementsByTagNameNS(Namespace.WSDL, "import");
		Assert.assertEquals(1, nodes.getLength());
		
		for (int i=0; i<nodes.getLength(); i++) {
			Element importElem = (Element)nodes.item(i);
			String namespace = importElem.getAttribute("namespace");
			String location = importElem.getAttribute("location");
			if (namespace.equals("http://ping/")) {
				Assert.assertEquals("PingService.xml", location);
			}
			else {
				Assert.fail("Unexpected import of namespace " + namespace);
			}
		}
	}
	
	@Test
	public void testTaskChecksOutputdirRequired() throws Exception
	{
		URL url = WsitClientTaskTest.class.getResource("TestService.xml");
		File wsdl = new File(url.toURI());
		
		WsitClientTask task = new WsitClientTask();

		task.setWsdl(wsdl);
		
		try {
			task.execute();
			Assert.fail("Expected exception when no outputdir set");
		}
		catch (BuildException e) {
		}
	}
	
	@Test
	public void testTaskChecksWsdlRequired() throws Exception
	{		
		WsitClientTask task = new WsitClientTask();
		task.setOutputdir(tempDir);
		
		try {
			task.execute();
			Assert.fail("Expected exception when wsdl not set");
		}
		catch (BuildException e) {
		}
		
		task.setWsdl(new File("C:/DoesNotExist"));
		
		try {
			task.execute();
			Assert.fail("Expected exception when wsdl does not exist");
		}
		catch (BuildException e) {
		}
	}
}
