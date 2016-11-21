# Arquillian Extension for Liferay Example [](id=arquillian-extension-for-liferay-example)

[![Build Status](https://travis-ci.org/liferay-labs/arquillian-blade-example.svg?branch=master)](https://travis-ci.org/liferay-labs/arquillian-blade-example)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5f52f062ff7a4352b97718b7ed4940a0)](https://www.codacy.com/app/cristina-gonzalez/arquillian-blade-example?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=liferay-labs/arquillian-blade-example&amp;utm_campaign=Badge_Grade)
[![Dependency Status](https://www.versioneye.com/user/projects/5832ccbb4ef164004d198677/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/5832ccbb4ef164004d198677)

The
[Arquillian Extension Liferay Example](https://github.com/arquillian/arquillian-extension-liferay/blob/master/arquillian-extension-liferay-example)
project demonstrates how to use the Arquillian Liferay Extension. In this
tutorial, you'll use the example project to learn how the Arquillian Extension
for @product@ works and how to use it in your own projects.

The Arquillian Extension Liferay Example project is executed in the following
environment:

- Liferay 7 Tomcat Bundle
    - JMX enabled and configured

To set up a testing environment like the one used by the Arquillian Extension
for Liferay Example, you need to enable and configure JMX in your Liferay Tomcat
server.

## Enable and Configure JMX in Tomcat [](id=enable-and-configure-jmx-in-tomcat)

You can follow this [guide](https://tomcat.apache.org/tomcat-7.0-doc/monitoring.html#Enabling_JMX_Remote) to enable your JMX configuration in Tomcat.

Here's an example of a `setenv.sh` file that enables JMX in Tomcat on port 8099
without authentication:

    CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF8 -Djava.net.preferIPv4Stack=true -Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Duser.timezone=GMT -Xmx1024m -XX:MaxPermSize=256m"

    JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=8099 -Dcom.sun.management.jmxremote.ssl=false"

    CATALINA_OPTS="${CATALINA_OPTS} ${JMX_OPTS}"

You can customize your `setenv.sh` in a similar way.

## Creating a Liferay Portlet for Testing [](id=creating-a-liferay-portlet-for-testing)

You need a Liferay portlet project that can be used for testing. You can use
your preferred build tool. For example, either Gradle or Maven can be used.
Follow these steps to create a Liferay portlet project using Maven.

For the example [Blade](https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/blade-cli) will be used.

1. Create a MVC Portlet using blade:

    ``blade create -t portlet -p com.liferay.arquillian.sample -c SamplePortlet arquillian-sample-portlet``

2.  Create an OSGi service. This OSGi service is just an example that you'll use
    for testing purposes. It's a simple service that adds two numbers.

    First, create a new interface:

        package com.liferay.arquillian.sample.service;

        public interface SampleService {

            public int add(int a, int b);

        }

    Next, create an implementation for the interface:

        package com.liferay.arquillian.sample.service;

        import org.osgi.service.component.annotations.Component;

        @Component(immediate = true, service = SampleService.class)
        public class SampleServiceImpl implements SampleService {

            @Override
            public int add(int a, int b) {
                return a + b;
            }

        }

3.  Create a Liferay MVC portlet. This portlet will call the service defined in
    the previous step:

        package om.liferay.arquillian.sample.portlet;

        import com.liferay.arquillian.sample.service.SampleService;
        import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
        import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
        import com.liferay.portal.kernel.theme.ThemeDisplay;
        import com.liferay.portal.kernel.util.ParamUtil;
        import com.liferay.portal.kernel.util.WebKeys;

        import javax.portlet.ActionRequest;
        import javax.portlet.ActionResponse;
        import javax.portlet.Portlet;
        import javax.portlet.PortletRequest;
        import javax.portlet.PortletURL;

        import org.osgi.service.component.annotations.Component;
        import org.osgi.service.component.annotations.Reference;

        @Component(
            property = {
                "com.liferay.portlet.display-category=category.sample",
                "com.liferay.portlet.instanceable=false",
                "javax.portlet.display-name=Arquillian Sample Portlet",
                "javax.portlet.init-param.template-path=/",
                "javax.portlet.init-param.view-template=/view.jsp",
                "javax.portlet.name=arquillian_sample_portlet",
                "javax.portlet.resource-bundle=content.Language",
                "javax.portlet.security-role-ref=power-user,user"
            },
            service = Portlet.class
        )
        public class SamplePortlet extends MVCPortlet {

            public void add(ActionRequest actionRequest, ActionResponse actionResponse)
                throws Exception {

                ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
                    WebKeys.THEME_DISPLAY);

                int firstParameter = ParamUtil.getInteger(
                    actionRequest, "firstParameter");
                int secondParameter = ParamUtil.getInteger(
                    actionRequest, "secondParameter");

                int result = _sampleService.add(firstParameter, secondParameter);

                PortletURL portletURL = PortletURLFactoryUtil.create(
                    actionRequest, "arquillian_sample_portlet", themeDisplay.getPlid(),
                    PortletRequest.RENDER_PHASE);

                portletURL.setParameter(
                    "firstParameter", String.valueOf(firstParameter));
                portletURL.setParameter(
                    "secondParameter", String.valueOf(secondParameter));
                portletURL.setParameter("result", String.valueOf(result));

                actionRequest.setAttribute(WebKeys.REDIRECT, portletURL.toString());
            }

            @Reference(unbind = "-")
            public void setSampleService(SampleService sampleService) {
                _sampleService = sampleService;
            }

            private SampleService _sampleService;

        }

    This portlet needs a `view.jsp` file:

        <%@ include file="/init.jsp" %>

        <%
        int firstParameter = ParamUtil.getInteger(request, "firstParameter", 1);
        int secondParameter = ParamUtil.getInteger(request, "secondParameter", 1);
        int result = ParamUtil.getInteger(request, "result");
        %>

        <portlet:actionURL name="add" var="portletURL" />

        <p>
            <b>Sample Portlet is working!</b>
        </p>

        <aui:form action="<%= portletURL %>" method="post" name="fm">

            <aui:input inlineField="<%= true %>" label="" name="firstParameter" size="4" type="int" value="<%= firstParameter %>" />
            <span> + </span>
            <aui:input inlineField="<%= true %>" label="" name="secondParameter" size="4" type="int" value="<%= secondParameter %>" />
            <span> = </span>
            <span class="result"><%= result %></span>

            <aui:button type="submit" value="add" />
        </aui:form>

4.  In your build.gradle file, configure the jar task, so it can be called with a ´dir´ parameter

        ...
        jar {
            if (project.hasProperty('dir')) {
                destinationDir = file(dir);
            }
        }
        ...

## Create a Simple Test in Liferay with the Arquillian Liferay Extension [](id=create-a-simple-test-in-liferay-with-the-arquillian-liferay-extension)

Now that you've configured JMX in Tomcat and created a portlet project, you're
ready to create Liferay tests with the Arquillian Liferay Extension.

1.  Add dependencies to your dependencies file, in this case 'build.gradle':

        dependencies {
            testIntegrationCompile group: "com.liferay.arquillian", name: "com.liferay.arquillian.arquillian-container-liferay", version: "1.0.5"
            testIntegrationCompile group: "junit", name: "junit", version: "4.12"
            testIntegrationCompile group: "org.jboss.arquillian.junit", name: "arquillian-junit-container", version: "1.1.11.Final"
         }

2.  Create simple integration tests using the Arquillian Liferay Extension.

        package com.liferay.arquillian.test;
        
        import com.liferay.arquillian.containter.remote.enricher.Inject;
        import com.liferay.arquillian.sample.service.SampleService;
        import com.liferay.portal.kernel.exception.PortalException;
        import com.liferay.shrinkwrap.osgi.api.BndProjectBuilder;

        import java.io.File;
        import java.io.IOException;

        import org.jboss.arquillian.container.test.api.Deployment;
        import org.jboss.arquillian.junit.Arquillian;
        import org.jboss.shrinkwrap.api.ShrinkWrap;
        import org.jboss.shrinkwrap.api.spec.JavaArchive;

        import org.junit.Assert;
        import org.junit.Test;
        import org.junit.runner.RunWith;

        @RunWith(Arquillian.class)
        public class BasicPortletIntegrationTest {

            @Deployment
            public static JavaArchive create() {
               		File tempDir = Files.createTempDir();

                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(
                        "./gradlew", "jar", "-Pdir=" + tempDir.getAbsolutePath());

                    Process process = processBuilder.start();

                    process.waitFor();
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }

                File jarFile = new File(
                    tempDir.getAbsolutePath() +
                        "/com.liferay.websocket.example.echo-1.0.0.jar");

                if (!jarFile.exists()) {
                    try {
                        jarFile.createNewFile();
                    }
                    catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                }

                return ShrinkWrap.createFromZipFile(JavaArchive.class, jarFile);    
            }

            @Test
            public void testAdd() throws IOException, PortalException {
                int result = _sampleService.add(1, 3);

                Assert.assertEquals(4, result);
            }

            @Inject
            private SampleService _sampleService;

        }

## Create Simple Functional Tests Using the Arquillian Liferay Extension [](id=create-simple-functional-tests-using-the-arquillian-liferay-extension)

To create a functional test in Liferay with the Arquillian Liferay Extension,
follow this
[guide](http://arquillian.org/guides/functional_testing_using_graphene/).

1.  Add dependencies to `build.gradle`:

        dependencies {
        ...
        	testIntegrationCompile group: "org.jboss.arquillian.graphene", name: "graphene-webdriver", version: "2.1.0.Final"
        ...
        }	

2.  Next, you need to set up `arquillian.xml` in order to select a browser (in this example Firefox will be used). 
Add the following to your project's `arquillian.xml` file in the `src/testIntegration/resources` directory.

        <?xml version="1.0" encoding="UTF-8"?>
        <arquillian xmlns="http://jboss.org/schema/arquillian"
                                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd">

                <extension qualifier="webdriver">
                        <property name="browser">firefox</property>
                </extension>

        </arquillian>

4.  Create a portlet functional test:

        package com.liferay.arquillian.test;

        import com.liferay.arquillian.containter.remote.enricher.Inject;
        import com.liferay.arquillian.portal.annotation.PortalURL;
        import com.liferay.arquillian.sample.service.SampleService;
        import com.liferay.portal.kernel.exception.PortalException;
        import com.liferay.shrinkwrap.osgi.api.BndProjectBuilder;

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

        import org.openqa.selenium.By;
        import org.openqa.selenium.WebDriver;
        import org.openqa.selenium.WebElement;
        import org.openqa.selenium.support.FindBy;

        @RunAsClient
        @RunWith(Arquillian.class)
        public class BasicPortletFunctionalTest {

            @Deployment
            public static JavaArchive create() {
               	File tempDir = Files.createTempDir();

                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(
                        "./gradlew", "jar", "-Pdir=" + tempDir.getAbsolutePath());

                    Process process = processBuilder.start();

                    process.waitFor();

                    BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                    String line = bufferedReader.readLine();

                    while (line != null) {
                        System.out.println(line);

                        line = bufferedReader.readLine();
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }

                File jarFile = new File(
                    tempDir.getAbsolutePath() +
                        "/com.liferay.websocket.example.echo-1.0.0.jar");

                if (!jarFile.exists()) {
                    try {
                        jarFile.createNewFile();
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                return ShrinkWrap.createFromZipFile(JavaArchive.class, jarFile);    
            }

            @Test
            public void testAdd() throws IOException, PortalException {
                browser.get(_portlerURL.toExternalForm());

                firstParamter.clear();

                firstParamter.sendKeys("2");

                secondParameter.clear();

                secondParameter.sendKeys("3");

                add.click();

                Assert.assertEquals("5", result.getText());
            }

            @Test
            public void testInstallPortlet() throws IOException, PortalException {
                browser.get(_portlerURL.toExternalForm());

                String bodyText = browser.findElement(By.tagName("body")).getText();

                Assert.assertTrue(
                    "The portlet is not well deployed",
                    bodyText.contains("Sample Portlet is working!"));
            }

            @PortalURL("arquillian_sample_portlet")
	        private URL _portlerURL;

            @Inject
            private SampleService _sampleService;

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

        }

5.  Configure the `ArquillianResource`:

    If you want to inject the URL of the container (e.g., Tomcat) using the
    annotation `@ArquillianResource`, you can use one of these solutions (if
    you are using the Arquillian Liferay Extension):

    1. Create a deployment method in your test class.
    2. Configure Arquillian using the graphene url property (via
       `arquillian.xml`, `arquillian.properties` or System Properties).

        <?xml version="1.0" encoding="UTF-8"?>
        <arquillian xmlns="http://jboss.org/schema/arquillian"
                                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd">
                ...
                <extension qualifier="graphene">
                        <property name="url">http://localhost:8080</property>
                </extension>
                ...
        </arquillian>

## Get a Coverage Report [](id=create-a-jacoco-profile)

[JaCoCo](http://eclemma.org/jacoco/) is a code coverage library for Java.

1.  Use the JaCoCo Gradle Plugin with the dependencies `org.jacoco.core` and
    `arquillian-jacoco` in your `build.gradle`file:

        ...
        apply plugin: 'jacoco'

        jacoco {
            toolVersion = '0.7.4.201502262128'
        }

        testIntegration { finalizedBy jacocoTestReport }

        jacocoTestReport {
            group = "Reporting"
            reports {
                xml.enabled true
                csv.enabled false
                html.destination "${buildDir}/reports/coverage"
            }
            executionData = files('build/jacoco/testIntegration.exec')
        }
        ...
        
        dependencies {
            ...
           	testIntegrationCompile group:"org.jboss.arquillian.extension", name: "arquillian-jacoco", version: "1.0.0.Alpha8"
	        testIntegrationCompile group:"org.jacoco", name: "org.jacoco.core", version: "0.7.4.201502262128"
            ...
        }
        ...

2.  Generate a Jacoco report in HTML:

        gradlew testIntegration

## Running Tests with the Liferay Arquillian Extension [](id=running-tests-with-the-liferay-arquillian-extension)

That's it! If you've download the
[Arquillian Blade Example](https://github.com/liferay-labs/arquillian-blade-example),
use the following command to run the tests:

    gradlew testIntegration

This command can take a long time to execute since Gradle needs to download
Tomcat and Liferay, install Liferay into Tomcat, start Liferay, and then deploy
and run your tests.
