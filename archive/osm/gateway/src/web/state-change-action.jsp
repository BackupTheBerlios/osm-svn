<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%@ page import="net.osm.session.task.TaskAdapter" %>
<%@ page import="org.apache.struts.action.ActionError" %>
<%@ page import="org.omg.Session.task_state" %>

<%! TaskAdapter m_adapter; %>
<%! task_state m_state; %>
<%! boolean startFlag; %>
<%! boolean suspendFlag; %>
<%! boolean resumeFlag; %>
<%! boolean stopFlag; %>

<%! String refreshFlag = ""; %>

<%
    m_adapter = (TaskAdapter) request.getAttribute("adapter");
    m_state = (task_state) m_adapter.getState();
    if( m_state == task_state.notstarted )
    {
        startFlag = true;
        suspendFlag = false;
        resumeFlag = false;
        stopFlag = false;
        refreshFlag = "";
    }
    else if( m_state == task_state.running )
    {
        startFlag = false;
        suspendFlag = true;
        resumeFlag = false;
        stopFlag = true;
        refreshFlag = "";
    }
    else if( m_state == task_state.suspended )
    {
        startFlag = false;
        suspendFlag = false;
        resumeFlag = true;
        stopFlag = true;
        refreshFlag = "";
    }
    else
    {
        startFlag = false;
        suspendFlag = false;
        resumeFlag = false;
        stopFlag = false;
        refreshFlag = "disabled";
    }
%>

<!--
The state-change-action page enables modification of the state of a task.
-->

<html:form method="post" action="stateChange.do">

  <input type=hidden name="base" value="<%= m_adapter.getBase() %>">
  <input type=hidden name="identity" value="<%= m_adapter.getIdentity() %>">
  <input type=hidden name="view" value="stateChange">
  <table cellpadding="3" border="0">
    <tr>
      <td>
        <p class="command">task state change</p>
        <p class="note">Change the current state of the task.</p>
        <p class="note">Current state: <%= m_adapter.getState() %></p>
      </td>
    </tr>
    <tr>
      <table cellpadding="3" border="0">
        <tr>
          <td>
            <html:submit property="state" value="start" disabled="<%= !startFlag %>"/>
          </td>
          <td>
            <html:submit property="state" value="suspend" disabled="<%= !suspendFlag %>"/>
          </td>
          <td>
            <html:submit property="state" value="resume" disabled="<%= !resumeFlag %>"/>
          </td>
          <td>
            <html:submit property="state" value="stop" disabled="<%= !stopFlag %>"/>
          </td>
        </tr>
      </table>
    </tr> 
    <tr>
      <td>
        <html:errors />
      </td>
    </tr>
  </table>
</html:form>


<form>
  <table cellpadding="3" border="0" width="100%">
    <tr height="2" bgcolor="lightsteelblue">
      <td height="2"></td>
    </tr>
    <tr>
      <td align="left">
        <input type="button" <%= refreshFlag %> 
    onClick="location = 'task?resolve=<%= m_adapter.getIdentity() %>&view=state';" 
    value="Refresh"/>
      </td>
    </tr>
  </table>
</form>

</p>
</hr>
</p>

