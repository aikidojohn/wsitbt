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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.appsec.wsitbt.core.wsit.Namespace;

public class WsitCleanTaskTest 
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
	public void testRemovesAllWSITConfiguration() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitCleanTask task = new WsitCleanTask();
		task.setFile(policy);
		task.setTodir(tempDir);
		
		task.execute();
		
		File file = new File(tempDir.getAbsolutePath() + "/ServicePolicy.xml");
		Assert.assertTrue(file.exists());
		
		Document doc = TestUtils.loadXmlFile(file);
		NodeList nodes = doc.getElementsByTagNameNS(Namespace.SC, "KeyStore");
		Assert.assertEquals(0, nodes.getLength());
		
		nodes = doc.getElementsByTagNameNS(Namespace.SC, "TrustStore");
		Assert.assertEquals(0, nodes.getLength());
		
		nodes = doc.getElementsByTagNameNS(Namespace.SC, "ValidatorConfiguration");
		Assert.assertEquals(0, nodes.getLength());

		nodes = doc.getElementsByTagNameNS(Namespace.SC, "Validator");
		Assert.assertEquals(0, nodes.getLength());
	}
	
	@Test
	public void testRenameOutputFile() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitCleanTask task = new WsitCleanTask();
		task.setFile(policy);
		
		File file = new File(tempDir.getAbsolutePath() + "/ServicePolicy.wsdl");
		task.setTofile(file);
		
		task.execute();
		
		
		Assert.assertTrue(file.exists());
	}
}
