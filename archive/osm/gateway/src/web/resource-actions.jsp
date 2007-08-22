<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%@ page import="net.osm.session.resource.AbstractResourceAdapter" %>
<%@ page import="org.apache.struts.action.ActionError" %>

<%! AbstractResourceAdapter m_adapter; %>

<%
    m_adapter = (AbstractResourceAdapter) request.getAttribute("adapter");
%>

<!--
The resource-actions page adds the set of actions that can be applied
against an AbstractResource adapter.
-->

<html:form method="post" action="rename.do">

  <input type=hidden name="base" value="<%= m_adapter.getBase() %>">
  <input type=hidden name="identity" value="<%= m_adapter.getIdentity() %>">
  <input type=hidden name="view" value="rename">
  <table cellpadding="3" border="0">
    <tr>
      <td>
        <p class="command">rename</p>
        <p class="note">Rename the resource to the supplied value.</p>
      </td>
    </tr>
    <tr>
      <td>
        <html:text property="name" size="45"
          value="<%= m_adapter.getName() %>" /> 
      </td>
    </tr>
    <tr>
      <td width="73%">
        <div align="left"><input type=image 
          src="nav/images/buttons/OK.gif" 
          width="70" height="21" > </div>
      </td>
    </tr> 
    <tr>
      <td>
        <html:errors />
      </td>
    </tr>
  </table>

</html:form>

</p>
</hr>
</p>

