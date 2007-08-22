<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%@ page import="net.osm.gateway.ActionContext" %>
<%@ page import="net.osm.session.resource.AbstractResourceAdapter" %>
<%@ page import="org.apache.struts.action.ActionError" %>

<%! AbstractResourceAdapter m_adapter; %>
<%
    m_adapter = (AbstractResourceAdapter) request.getAttribute("adapter");
    session.setAttribute("resource", m_adapter );

    // create a session level activity context

    ActionContext context = new ActionContext();
    context.put( "resource", m_adapter );
    String key = context.getId();
    session.setAttribute( key , context );
    String testURL = "corbaloc::home.osm.net:2506/GATEWAY";
%>

<!--
Prepares a form supporting the renaming of an AbstractResource.
-->

<html:form method="post" action="rename.do">

  <input type=hidden name="key" value="<%= key %>">
  <input type=hidden name="url" value="<%= m_adapter.getURL() %>">
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

