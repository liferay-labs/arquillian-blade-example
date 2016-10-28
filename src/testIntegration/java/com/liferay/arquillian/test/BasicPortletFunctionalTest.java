/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.arquillian.test;

import com.google.common.io.Files;
import com.liferay.arquillian.portal.annotation.PortalURL;
import com.liferay.portal.kernel.exception.PortalException;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author Cristina González
 */
@RunAsClient
@RunWith(Arquillian.class)
public class BasicPortletFunctionalTest {

	@PortalURL("arquillian_sample_portlet")
	private URL portlerURL;

	@FindBy(css = "button[type=submit]")
	private WebElement add;

	@Drone
	private WebDriver browser;

	@FindBy(css = "input[id$='firstParameter']")
	private WebElement firstParamter;

	@FindBy(css = "span[class='result']")
	private WebElement result;

	@FindBy(css = "input[id$='secondParameter']")
	private WebElement secondParameter;

	@Deployment
	public static JavaArchive create() throws Exception {
		final File tempDir = Files.createTempDir();

		final ProcessBuilder processBuilder = new ProcessBuilder(
			"./gradlew", "jar", "-Pdir=" + tempDir.getAbsolutePath());

		final Process process = processBuilder.start();

		process.waitFor();

		final File jarFile = new File(
			tempDir.getAbsolutePath() +
				"/com.liferay.arquillian.sample-1.0.0.jar");

		return ShrinkWrap.createFromZipFile(JavaArchive.class, jarFile);
	}

	@Test
	public void testAdd()
		throws IOException, PortalException, InterruptedException {

		browser.get(portlerURL.toExternalForm());

		firstParamter.clear();

		firstParamter.sendKeys("2");

		secondParameter.clear();

		secondParameter.sendKeys("3");

		add.click();

		Thread.sleep(1000);

		Assert.assertEquals("5", result.getText());
	}

	@Test
	public void testInstallPortlet() throws IOException, PortalException {
		browser.get(portlerURL.toExternalForm());

		final String bodyText = browser.getPageSource();

		Assert.assertTrue(
			"The portlet is not well deployed",
			bodyText.contains("Sample Portlet is working!"));
	}

}