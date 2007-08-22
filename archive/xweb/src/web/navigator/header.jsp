<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>
<%@ page import="net.osm.xweb.Controls,net.osm.agent.*" %>

<%
    Agent agent = null;
    if( session.getAttribute("agent") != null )
    {
        agent = (Agent) session.getAttribute("agent");
    }
    else
    {
        Controls xweb = (Controls) application.getAttribute( "xweb" );
        agent = xweb.getRoot();
    }

%>
<HTML>

<osm:header title="header">
  <osm:style/>
  <link type="text/css" rel="StyleSheet" href="tools/cb2.css" />
  <script type="text/javascript" language="JavaScript1.5" src="tools/ieemu.js"></script> 
  <script type="text/javascript" src="tools/cb2.js"></script>
</osm:header>

<BODY style="margin:0; padding:0; background:buttonface;" >

  <osm:agent entity="<%=agent%>">

  <table width="100%" cellpadding="0" cellspacing="0">
    <tr>
      <td align=left>
        <table border=0 class="toolBar" id="buttons" cellpadding="2">
          <tr>
	      <%-- Always include the home button --%>
            <td tabindex="1" id="home" onclick="parent.setLocation('body.jsp?root=true&view=contents')">
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Home</p></td></tr>
              </table>
            </td>
            <%

            if( agent instanceof AbstractResourceAgent )
            {

            %>
            <td tabindex="1" id="properties" onclick="parent.setLocation('body.jsp?view=properties')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Properties</p></td></tr>
              </table>
            </td>
            <%

              if( ( agent instanceof CommunityAgent ) )
              {

            %>
            <td tabindex="1" id="model" onclick="parent.setLocation('body.jsp?view=model')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Model</p></td></tr>
              </table>
            </td>
            <%

              }
              if( agent instanceof WorkspaceAgent )
              {

            %>
            <td tabindex="1" id="contents" onclick="parent.setLocation('body.jsp?view=contents')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Contents</p></td></tr>
              </table>
            </td>
            <%

              }
              if( agent instanceof UserAgent )
              {

            %>
            <td tabindex="1" id="messages" onclick="parent.setLocation('body.jsp?view=messages')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Messages</p></td></tr>
              </table>
            </td>
            <td tabindex="1" id="tasks" onclick="parent.setLocation('body.jsp?view=tasks')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Tasks</p></td></tr>
              </table>
            </td>
            <%

              }
              if( agent instanceof TaskAgent  )
              {

            %>
            <td tabindex="1" id="model" onclick="parent.setLocation('body.jsp?view=model')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Model</p></td></tr>
              </table>
            </td>
            <%

              }
            }
		if( agent instanceof CommunityAgent )
            {

            %>
            <td tabindex="1" id="members" onclick="parent.setLocation('body.jsp?view=members')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/user.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Members</p></td></tr>
              </table>
            </td>
            <%

		}
		if( (agent instanceof WorkspaceAgent) || (agent instanceof UserAgent))
            {

            %>
            <td tabindex="1" id="processes" onclick="parent.setLocation('body.jsp?view=services')" >
              <table height="100%" cellpadding="0" cellspacing="0">
                <tr><td align=center><img src="image/default.gif" align="absmiddle" class="icon"/></td></tr>
                <tr><td align=center><p class="button-label">Services</p></td></tr>
              </table>
            </td>
            <%

            }

            %>
          </tr>
        </table>
      </td>
      <%

      if( agent instanceof AbstractResourceAgent )
      {

      %>
      <td align=right >
        <p style="font: 8pt verdana, sans-serif; margin-top:0;">
	    	name: <osm:agent feature="name"/><br/>
	    	type: <osm:agent feature="type"/><br/>
	    	accessed: <osm:agent feature="access"/></p>
      </td>
      <%

      }
	else
      {

      %>
      <td align=right >
        <p style="font: 8pt verdana, sans-serif; margin-top:0;">
		name: unknown<br/>
		type: unknown<br/>
		accessed: unknown</p>
      </td>
      <%

      }

      %>
      <td onclick="parent.location='../main'" >
		<IMG border=0 src="image/nothing.gif" width="10px"/>
      </td>
    </tr>
  </table>
  </osm:agent>

  <script type="text/javascript"> 
    var table = document.getElementById("buttons"); 
    for (var i = 0; i < table.rows[0].cells.length; i++) createButton(table.rows[0].cells[i]);
  </script> 

</body>

</html>

