<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>

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