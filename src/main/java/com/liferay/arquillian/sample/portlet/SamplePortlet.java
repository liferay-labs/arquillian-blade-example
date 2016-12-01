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

package com.liferay.arquillian.sample.portlet;

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

/**
 * @author Cristina González
 */
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

	public void add(
		final ActionRequest actionRequest,
		final ActionResponse actionResponse) {

		final ThemeDisplay themeDisplay =
			(ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

		final int firstParameter = ParamUtil.getInteger(
			actionRequest, "firstParameter");
		final int secondParameter = ParamUtil.getInteger(
			actionRequest, "secondParameter");

		final long result = _sampleService.add(firstParameter, secondParameter);

		final long plid = themeDisplay.getPlid();

		final PortletURL portletURL = PortletURLFactoryUtil.create(
			actionRequest, "arquillian_sample_portlet", plid,
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter(
			"firstParameter", String.valueOf(firstParameter));
		portletURL.setParameter(
			"secondParameter", String.valueOf(secondParameter));
		portletURL.setParameter("result", String.valueOf(result));

		actionRequest.setAttribute(WebKeys.REDIRECT, portletURL.toString());
	}

	@Reference(unbind = "-")
	public void setSampleService(final SampleService sampleService) {
		this.sampleService = sampleService;
	}
	private SampleService _sampleService;

	public SampleService getSampleService() {
		return sampleService;
	}
}