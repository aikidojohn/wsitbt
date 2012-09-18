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
import java.net.URL;

import junit.framework.Assert;

import org.apache.tools.ant.BuildException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.appsec.wsitbt.core.wsit.Namespace;

public class WsitServiceTaskTest 
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
	public void testPolicyGetsMerged() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setWsdl(wsdl);
		task.setPolicy(policy);
		task.setOutputdir(tempDir);
		task.setClass("com.appsec.PingService");
		task.setBindingpolicy("WSTrustPolicy");
		task.setInputpolicy("InputPolicy");
		task.setOutputpolicy("OutputPolicy");
		task.setFaultpolicy("FaultPolicy");
		
		task.execute();
		
		File wsit = new File(tempDir.getAbsolutePath() + "/wsit-com.appsec.PingService.xml");
		Assert.assertTrue(wsit.exists());
		
		Document doc = TestUtils.loadXmlFile(wsit);
		NodeList nodes = doc.getElementsByTagNameNS(Namespace.WSP, "Policy");
		Assert.assertEquals(19, nodes.getLength());
			
		nodes = doc.getElementsByTagNameNS(Namespace.WSDL, "binding");
		Assert.assertEquals(1, nodes.getLength());
		
		Element binding = (Element)nodes.item(0);
		nodes = binding.getElementsByTagNameNS(Namespace.WSP, "PolicyReference");
		Assert.assertEquals(9, nodes.getLength());
	}
	
	@Test
	public void testOutputWsdlOption() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setWsdl(wsdl);
		task.setPolicy(policy);
		task.setOutputdir(tempDir);
		task.setClass("com.appsec.PingService");
		task.setBindingpolicy("WSTrustPolicy");
		task.setInputpolicy("InputPolicy");
		task.setOutputpolicy("OutputPolicy");
		task.setFaultpolicy("FaultPolicy");
		task.setUpdatewsdl(true);
		
		task.execute();
		
		File wsit = new File(tempDir.getAbsolutePath() + "/wsit-com.appsec.PingService.xml");
		Assert.assertTrue(wsit.exists());
		
		File newwsdl = new File(tempDir.getAbsolutePath() + "/PingService.xml");
		Assert.assertTrue(newwsdl.exists());
		
		Document doc = TestUtils.loadXmlFile(newwsdl);
		//Check that the policy references exist in the wsdl
		NodeList nodes = doc.getElementsByTagNameNS(Namespace.WSP, "Policy");
		Assert.assertEquals(19, nodes.getLength());
			
		nodes = doc.getElementsByTagNameNS(Namespace.WSDL, "binding");
		Assert.assertEquals(1, nodes.getLength());
		
		Element binding = (Element)nodes.item(0);
		nodes = binding.getElementsByTagNameNS(Namespace.WSP, "PolicyReference");
		Assert.assertEquals(9, nodes.getLength());
		
		//check that there is no WSIT configuration in the wsdl
		nodes = doc.getElementsByTagNameNS(Namespace.SC, "KeyStore");
		Assert.assertEquals(0, nodes.getLength());
		
		nodes = doc.getElementsByTagNameNS(Namespace.SC, "TrustStore");
		Assert.assertEquals(0, nodes.getLength());
		
		nodes = doc.getElementsByTagNameNS(Namespace.SC, "ValidatorConfiguration");
		Assert.assertEquals(0, nodes.getLength());

		nodes = doc.getElementsByTagNameNS(Namespace.SC, "Validator");
		Assert.assertEquals(0, nodes.getLength());
	}
	
	@Test
	public void testTaskChecksOutputdirRequired() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setPolicy(policy);
		task.setWsdl(wsdl);
		task.setClass("com.appsec.PingService");
		task.setBindingpolicy("WSTrustPolicy");
		task.setInputpolicy("InputPolicy");
		task.setOutputpolicy("OutputPolicy");
		task.setFaultpolicy("FaultPolicy");
		
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
		URL url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setPolicy(policy);
		task.setOutputdir(tempDir);
		task.setClass("com.appsec.PingService");
		task.setBindingpolicy("WSTrustPolicy");
		task.setInputpolicy("InputPolicy");
		task.setOutputpolicy("OutputPolicy");
		task.setFaultpolicy("FaultPolicy");
		
		try {
			task.execute();
			Assert.fail("Expected exception when wsdl not set");
		}
		catch (BuildException e) {
		}
		
		task.setWsdl(new File("C:/FileDoesNotExit"));
		try {
			task.execute();
			Assert.fail("Expected exception when wsdl does not exist");
		}
		catch (BuildException e) {
		}
	}
	
	
	@Test
	public void testTaskChecksPolicyRequired() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setWsdl(wsdl);
		task.setOutputdir(tempDir);
		task.setClass("com.appsec.PingService");
		task.setBindingpolicy("WSTrustPolicy");
		task.setInputpolicy("InputPolicy");
		task.setOutputpolicy("OutputPolicy");
		task.setFaultpolicy("FaultPolicy");
		
		try {
			task.execute();
			Assert.fail("Expected exception when policy not set");
		}
		catch (BuildException e) {
		}
		
		task.setPolicy(new File("C:/FileDoesNotExit"));
		try {
			task.execute();
			Assert.fail("Expected exception when policy does not exist");
		}
		catch (BuildException e) {
		}
	}
	
	@Test
	public void testTaskChecksBindingPolicyRequired() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setWsdl(wsdl);
		task.setOutputdir(tempDir);
		task.setClass("com.appsec.PingService");
		task.setInputpolicy("InputPolicy");
		task.setOutputpolicy("OutputPolicy");
		task.setFaultpolicy("FaultPolicy");
		
		try {
			task.execute();
			Assert.fail("Expected exception when policy not set");
		}
		catch (BuildException e) {
		}
	}
	
	@Test
	public void testTaskPoliciesAreOptional() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setOutputdir(tempDir);
		task.setPolicy(policy);
		task.setWsdl(wsdl);
		task.setClass("com.appsec.PingService");
		task.setBindingpolicy("WSTrustPolicy");
		
		try {
			task.execute();
		}
		catch (BuildException e) {
			Assert.fail("Exception not expected when binding policy references are not specified.");
		}
	}
	
	@Test
	public void testTaskChecksClassRequired() throws Exception
	{
		URL url = WsitServiceTaskTest.class.getResource("PingService.xml");
		File wsdl = new File(url.toURI());
		
		url = WsitServiceTaskTest.class.getResource("ServicePolicy.xml");
		File policy = new File(url.toURI());
		
		WsitServiceTask task = new WsitServiceTask();
		task.setWsdl(wsdl);
		task.setPolicy(policy);
		task.setOutputdir(tempDir);
		task.setBindingpolicy("WSTrustPolicy");
		task.setInputpolicy("InputPolicy");
		task.setOutputpolicy("OutputPolicy");
		task.setFaultpolicy("FaultPolicy");
		
		try {
			task.execute();
			Assert.fail("Expected exception when class not specified");
		}
		catch (BuildException e) {
		}
	}
}
