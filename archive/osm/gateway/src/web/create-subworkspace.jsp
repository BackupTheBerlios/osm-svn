<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%@ page import="net.osm.session.workspace.WorkspaceAdapter" %>
<%@ page import="net.osm.gateway.ActionContext" %>

<%! WorkspaceAdapter m_adapter; %>
<%
    m_adapter = (WorkspaceAdapter) request.getAttribute("adapter");
    session.setAttribute("workspace", m_adapter );

    // create a session level activity context

    ActionContext context = new ActionContext();
    context.put( "workspace", m_adapter );
    String key = context.getId();
    session.setAttribute( key , context );

%>

<!--
Form containing parameters supporting creation of a sub-workspace 
within the current workspace.
-->

<html:form method="post" action="sub-workspace.do">
  <input type=hidden name="key" value="<%= key %>">
  <table cellpadding="3" border="0">
    <tr>
      <td>
        <p class="command">create sub-workspace</p>
        <p class="note">Creates a new workspace within this workspace.</p>
      </td>
    </tr>
    <tr>
      <td>
        <html:text property="name" size="45" value="Untitled Workspace" /> 
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
        <html:errors/>
      </td>
    </tr> 
  </table>
</html:form>


