<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%@ page import="net.osm.session.user.PrincipalAdapter" %>
<%@ page import="net.osm.session.task.TaskAdapter" %>
<%@ page import="net.osm.session.resource.AbstractResourceAdapter" %>
<%@ page import="org.apache.struts.action.ActionError" %>

<%! PrincipalAdapter m_adapter; %>

<%
    m_adapter = (PrincipalAdapter) request.getAttribute("adapter");
%>

<!--
The new-task-actions page enables creation of a new task.
-->

<html:form method="post" action="createTask.do">

  <input type=hidden name="base" value="<%= m_adapter.getBase() %>">
  <input type=hidden name="identity" value="<%= m_adapter.getIdentity() %>">
  <input type=hidden name="view" value="createTask">
  <table cellpadding="3" border="0">
    <tr>
      <td>
        <p class="command">create task</p>
        <p class="note">Creation of a new task.</p>
      </td>
    </tr>
    <tr>
      <td>
        <html:text property="name" size="45"
          value="Untitled Task" /> 
      </td>
    </tr>
    <tr>
      <td>
        <html:select property="service" value="0">
          <html:option value="/appliance/hello">Hello Service</html:option>
          <html:option value="AAAAA"/>
          <html:option value="CCCCCC"/>
        </html:select> 
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

