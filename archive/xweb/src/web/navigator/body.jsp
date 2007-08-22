<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>
<%@ page import="net.osm.xweb.Controls,net.osm.agent.*,org.omg.CORBA.ORB" %>

<%

    net.osm.agent.Agent agent = null;
    Controls xweb = (Controls) application.getAttribute( "xweb" );
    AgentService factory = xweb.getAgentService();
    ORB orb = xweb.getOrb();

    //
    // This page is invoked by a client to present a resource within a particular view.
    // The resource to present is resolved through one of the following:
    // (1) an IOR request attribute
    // (2) a current session 'agent' attribute
    // (3) the root community
    //
    // The view to present the requested resource is declared under the HTTP request 
    // attribute 'view'.  If no request attribute is supplied then a default view is 
    // established based on the type of object.
    //

    String agentRequest = request.getParameter("agent");
    String iorRequest = request.getParameter("ior");
    String rootRequest = request.getParameter("root");

    //
    // establish the session target
    //

    if( rootRequest != null )
    {
        session.setAttribute( "agent", factory.getRoot() );
    }
    else if( iorRequest != null )
    {
        session.setAttribute( "agent", factory.resolve( orb.string_to_object( request.getParameter("ior"))));
    }
    else if( agentRequest != null )
    {
        session.setAttribute( "agent", factory.resolve( agentRequest ) );
    }

    //
    // make sure the agent is not null
    //

    if( session.getAttribute("agent") == null ) session.setAttribute( "agent", factory.getRoot() );
    agent = (Agent) session.getAttribute("agent");

    //
    // get the view parameter
    //

    String view = "default";
    if( request.getParameter("view") != null ) view = request.getParameter("view");

%>
<HTML>

<osm:header title="">
  <osm:style/>
  <script type="text/javascript" src="tools/xweb.js"></script>
</osm:header>

<BODY style="margin-left:0; margin-right:0; margin-top:3; padding:0;" onLoad="parent.frames.header.window.location='header.jsp'">

  <%

  if( view.equals("default") )
  {
      if( agent instanceof WorkspaceAgent )
      {
          view = "contents";
      }
      else
      {
          view = "properties";
      }
  }

  %>
  <h1><%=view%></h1>
  <osm:agent entity="<%=agent%>">
  <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
  <%

  if( view.equals("properties") )
  {
      if( agent instanceof AbstractResourceAgent )
      {

    %>

    <!--
    List of general properties of an AbstractResource.
    -->

    <tr bgcolor="lightsteelblue"><td><p class="property">general properties</p></td></tr>
    
    <tr><td><p class="property">name: <osm:agent feature="name"/></P><tr></td>
    <tr><td><p class="property">domain: <osm:agent feature="domain"/></P></tr></td>
    <tr><td><p class="property">kind: <osm:agent feature="kind"/></P></tr></td>
    <tr><td><p class="property">random: <osm:agent feature="random"/></P></tr></td>
    <tr><td><p class="property">created: <osm:agent feature="creation"/></P></tr></td>
    <tr><td><p class="property">modified: <osm:agent feature="modification"/></P></tr></td>
    <tr><td><p class="property">access: <osm:agent feature="access"/></P></tr></td>
    <tr><td><p class="property">connected: <osm:agent feature="active"/></P></tr></td>
    <osm:agent delegate="producer">
    <tr><td><p class="property">produced by: <a href="body.jsp?ior=<osm:agent feature="ior"/>"><osm:agent feature="name"/></a></p></td></tr>
    </osm:agent>
    <%

          if( agent instanceof UserAgent )
          {
 
    %>

    <!--
    Additional properties from User
    -->

    <tr><td><p class="property">connected: <osm:agent feature="connected"/></P></tr></td>
    <osm:agent delegate="desktop">
      <tr><td><p class="property">desktop: <a href="body.jsp?ior=<osm:agent feature="ior"/>"><osm:agent feature="name"/></a></p></tr></td>
    </osm:agent>

    <%

          }
          else if( agent instanceof TaskAgent )
          {
       
    %>

    <!--
    Additional properties from Task
    -->

    <tr><td><p class="property">description: <osm:agent feature="description"/></P></tr></td>
    <tr><td><p class="property">state: <osm:agent feature="state"/></P></tr></td>
    <osm:agent delegate="owner">
    <tr><td><p class="property">owner: <a href="body.jsp?ior=<osm:agent feature="ior"/>"><osm:agent feature="name"/></a></p></tr></td>
    </osm:agent>
    <osm:agent delegate="processor">
    <tr><td><p class="property">processor: <a href="body.jsp?ior=<osm:agent feature="ior"/>"><osm:agent feature="name"/></a></p></tr></td>
    </osm:agent>
    <%

          }
          else if( agent instanceof WorkspaceAgent )
          {       
              if( agent instanceof DesktopAgent )
              {

    %>

    <!--
    Additional properties from Desktop
    -->

    <osm:agent delegate="owner">
    <tr><td><p class="property">owner: <a href="body.jsp?ior=<osm:agent feature="ior"/>"><osm:agent feature="name"/></a></p></tr></td>
    </osm:agent>
    <%

              }
              else if( agent instanceof CommunityAgent )
              {
            
    %>

    <!--
    Additional properties from Community
    -->

    <tr><td><p class="property">open: <osm:agent feature="open" /></P></tr></td>
    <tr><td><p class="property">quorum: <osm:agent feature="quorum" /></P></tr></td>
    <%

              }
          }

          //
          // PREPARE EXPANDED ASSOCIATIONS
          // Firstly declare AbstractResource contained-by and consumed-by links
          //

    %>
    <!--
    Include the list of workspaces holding a reference to this resource.
    -->

    <osm:agent expand="containers" header="<tr><td>&nbsp;</td></tr><tr bgcolor=\"lightsteelblue\"><td><p class=\"property\">workspaces referencing this resource</p></td></tr>">
    <tr valign="top"><td><a href="body.jsp?ior=<osm:agent feature="ior"/>"/><p class="property"><osm:agent feature="name"/></p></a></td></tr>
    </osm:agent>

    <!--
    Include the list of tasks using this resource.
    --!>

    <osm:agent expand="consumers" header="<tr><td>&nbsp;</td></tr><tr bgcolor=\"lightsteelblue\"><td><p class=\"property\">tasks consuming this resource</p></td></tr>">
    <tr valign="top"><td><a href="body.jsp?ior=<osm:agent feature="ior"/>"/><p class="property"><osm:agent feature="name"/></p></a></td></tr>
    </osm:agent>
    <%

          if( agent instanceof TaskAgent )
          {

              //
              // Add task related links.
              //

    %>

    <!--
    Include the list of resources consumed by this task
    -->

    <osm:agent expand="consumed" header="<tr><td>&nbsp;</td></tr><tr bgcolor=\"lightsteelblue\"><td><p class=\"property\">resources consumed by this task</p></td></tr>">
    <tr valign="top"><td><a href="body.jsp?ior=<osm:agent feature="ior"/>"/><p class="property"><osm:agent feature="name"/></p></a></td></tr>
    </osm:agent>

    <!--
    Include the list of resources produced by this task
    --!>

    <osm:agent expand="produced" header="<tr><td>&nbsp;</td></tr><tr bgcolor=\"lightsteelblue\"><td><p class=\"property\">resources consumed by this task</p></td></tr>">
    <tr valign="top"><td><a href="body.jsp?ior=<osm:agent feature="ior"/>"/><p class="property"><osm:agent feature="name"/></p></a></td></tr>
    </osm:agent>
    <%

          }
	    else if( agent instanceof UserAgent )
	    {

    %>

    <!--
    Include the list of workspaces this user can access
    --!>

    <osm:agent expand="workspaces" header="<tr><td>&nbsp;</td></tr><tr bgcolor=\"lightsteelblue\"><td><p class=\"property\">protected workspaces access rights</p></td></tr>">
    <tr valign="top"><td><a href="body.jsp?ior=<osm:agent feature="ior"/>"/><p class="property"><osm:agent feature="name"/></p></a></td></tr>
    </osm:agent>
    <%

          }
	    else if( agent instanceof WorkspaceAgent )
	    {

    %>

    <!--
    Include the list of users that can access this workspace
    --!>

    <osm:agent expand="accessedBy" header="<tr><td>&nbsp;</td></tr><tr bgcolor=\"lightsteelblue\"><td><p class=\"property\">users with access to this workspace</p></td></tr>">
    <tr valign="top"><td><a href="body.jsp?ior=<osm:agent feature="ior"/>"/><p class="property"><osm:agent feature="type"/>, <osm:agent feature="name"/></p></a></td></tr>
    </osm:agent>
    <%

          }
      }
      else
      {

    %>

    <!--
    Cannot list properties because the root agent type is unknown.
    -->

    <tr><td><p class="property">Properties unknown.</P><tr></td>
    <%

      }
  }
  else if( view.equals("model" ))
  {
      if( agent instanceof CommunityAgent )
      {

          //
          // Membership Model view
          //

    %>

    <!--
    Listing membership model features.
    -->

    <osm:agent delegate="model">

    <tr>
      <td valign="top"><table border="0" cellPadding="0" cellSpacing="0" width="100%">
        <tr bgcolor="lightsteelblue"><td><p class="property">model</p></td></tr>
        <tr><td><p class="property">label: <osm:agent feature="label" /></P></td></tr>
        <tr><td><p class="property">note: <osm:agent feature="note" /></P></td></tr>	    
        <tr><td><p class="property">exclusive: <osm:agent feature="exclusive" /></P></td></tr>	    
        <tr><td><p class="property">privacy: <osm:agent feature="privacy" /></P></td></tr>
      </table></td>

      <osm:agent delegate="role">

      <td valign="top" align="right" valign="top">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%">
          <tr bgcolor="lightsteelblue">
            <td valign="top" colspan="2"><p class="entry">role</P></td>
	    </tr>
	    <tr>
            <td></td>
            <td><table border="0" width="100%" cellPadding="0" cellSpacing="0" >
              <tr><td colspan="2"><p class="entry">label: <osm:agent feature="label"/></p></td></tr>
              <tr><td colspan="2"><p class="entry">note: <osm:agent feature="note"/></p></td></tr>
              <tr><td colspan="2"><p class="entry">floor: <osm:agent feature="floor"/>, ceiling: <osm:agent feature="ceiling"/>, connected: <osm:agent feature="connectedPolicy"/>, deferred: <osm:agent feature="deferralPolicy"/></p></td></tr>
              <tr><td colspan="2"><p class="entry">abstract: <osm:agent feature="abstract"/></p></td></tr>

              <osm:agent expand="roles">

              <tr bgcolor="lightsteelblue">
                <td valign="top" width="40" align="right"><p class="entry">&nbsp;</P></td>
	          <td><p class="entry">role</p></td>
	        </tr>
	        <tr>
                <td></td>
                <td><table border="0" width="100%" cellPadding="0" cellSpacing="0" >
                  <tr><td colspan="2"><p class="entry">label: <osm:agent feature="label"/></p></td></tr>
                  <tr><td colspan="2"><p class="entry">note: <osm:agent feature="note"/></p></td></tr>
              <tr><td colspan="2"><p class="entry">floor: <osm:agent feature="floor"/>, ceiling: <osm:agent feature="ceiling"/>, connected: <osm:agent feature="connectedPolicy"/>, deferred: <osm:agent feature="deferralPolicy"/></p></td></tr>
                  <tr><td colspan="2"><p class="entry">abstract: <osm:agent feature="abstract"/></p></td></tr>

                  <osm:agent expand="roles">

                  <tr bgcolor="lightsteelblue">
                    <td valign="top" width="40" align="right"><p class="entry">&nbsp;</P></td>
	              <td><p class="entry">role</p></td>
	            </tr>
	            <tr>
                    <td></td>
                    <td><table border="0" width="100%" cellPadding="0" cellSpacing="0" >
                      <tr><td colspan="2"><p class="entry">label: <osm:agent feature="label"/></p></td></tr>
                      <tr><td colspan="2"><p class="entry">note: <osm:agent feature="note"/></p></td></tr>
              <tr><td colspan="2"><p class="entry">floor: <osm:agent feature="floor"/>, ceiling: <osm:agent feature="ceiling"/>, connected: <osm:agent feature="connectedPolicy"/>, deferred: <osm:agent feature="deferralPolicy"/></p></td></tr>
                      <tr><td colspan="2"><p class="entry">abstract: <osm:agent feature="abstract"/></p></td></tr>

                    </table></td>
                  </tr>

                  </osm:agent>

		    </table></td>
              </tr>

              </osm:agent>

	      </table></td>
          </tr>
        </table>
      </td>
      </osm:agent>

    </osm:agent>
    <%

      }
      else if( ( agent instanceof TaskAgent ) || ( agent instanceof ProcessorAgent ) )
      {

          //
          // Processor Model view (extracted from the Processor attached to the Task)
          //

    %>

    <!--
    Listing the processor model features from the Processor attached to the Task.
    -->

    <osm:agent delegate="model">
    <tr><td>&nbsp;</td></tr>
    <tr bgcolor="lightsteelblue"><td><p class="property">model</p></td></tr>
    <tr><td><p class="property">delegate: <osm:agent feature="delegate" /></P>
    </osm:agent>
    <%

      }
  }
  else if( view.equals("processes"))
  {

    %>
  
    <!--
    Listing subprocesses.
    -->

    <tr><td><p class="property">Processor list not currently available.</P>
    <%

  }
  else if( view.equals("messages"))
  {

    %>
  
    <!--
    Listing messages queued against the user.
    -->

    <osm:agent expand="messages" header="<tr><td><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><th></th><th align=\"left\"><p class=\"entry\">priority</p></th><th align=\"left\"><p class=\"entry\">classification</p></th><th align=\"left\"><p class=\"entry\">subject</p></th><th align=\"left\"><p class=\"entry\">message</p></th></tr>" footer="</table></td></tr>">
    <tr valign="top">
	<td width="54px" valign="center" align="center">
	  <img src="image/default.gif" style="padding-left=30; padding-top:2;"/></td>
	<td><p class="entry"><osm:agent feature="priority"/></P></td>
	<td><p class="entry"><osm:agent feature="classification"/></P></td>
	<td><p class="entry"><osm:agent feature="subject"/></P></td>
	<td><p class="entry"><osm:agent feature="message"/></P></td>
    </tr>
    </osm:agent>
    <%

  }
  else if( view.equals("members"))
  {

    %>

    <!--
    Listing the member bound to this membership.
    -->

    <osm:agent expand="members" header="<tr><td><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><th align=\"left\"><p class=\"entry\">name</p></th><th align=\"right\"><p class=\"entry\">connected</p></th></tr>" footer="</table></td></tr>">
    <tr valign="top">
	<td width="54px" valign="center" align="center">
        <a href="body.jsp?ior=<osm:agent feature="ior"/>">
	  <img border="0" src="image/user.gif" style="padding-left=30; padding-top:2;"/></a></td>
	<td><a href="body.jsp?ior=<osm:agent feature="ior"/>">
	  <p class="entry"><osm:agent feature="name"/></P></a></td>
	<td align="right"><p class="entry"><osm:agent feature="connected"/></P></td>
    </tr>
    </osm:agent>
    <%

  }
  else if( view.equals("services"))
  {

    %>

    <!--
    Listing the criteria exposed by a factory
    -->

    <osm:agent expand="services" header="<tr><td><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><th></th><th align=\"left\"><p class=\"entry\">label</p></th><th align=\"left\"><p class=\"entry\">note</p></th><th align=\"left\"><p class=\"entry\">type</p></th></tr>" footer="</table></td></tr>">

    <tr valign="top">
	<td width="54px" valign="center" align="center">
	  <img src="image/default.gif" style="padding-left=30; padding-top:2;"/></td>
	<td><p class="entry"><osm:agent feature="label"/></P></td>
	<td><p class="entry"><osm:agent feature="note"/></P></td>
	<td><p class="entry"><osm:agent feature="type"/></P></td>
    </tr>
    </osm:agent>
    <%


  }
  else
  {

	//
	// The request view type has not be handled above.
	// Try to generate a list based on an iteration extent based on the view name.
	//

      if( agent instanceof AbstractResourceAgent )
      {

    %>

    <!--
    Listing resources associated under the the get[<%=view%>] method.
    -->

    <osm:agent expand="<%=view%>" header="<tr bgcolor=\"lightsteelblue\"><th></th><th align=\"left\"><p class=\"entry\">name</p><th align=\"right\"><p class=\"entry\">modified</p></th></tr>">
	<tr valign="top">
    	<td width="54px" valign="center" align="center">
        <a href="body.jsp?ior=<osm:agent feature="ior"/>">
	  <img src="image/default.gif" border="0" style="padding-left=30; padding-top:2;"/></a></td>
    	<td><a href="body.jsp?ior=<osm:agent feature="ior"/>">
	  <p class="entry"><osm:agent feature="name"/></p></a></td>
      <td align=right width="230px"><p class="entry"><osm:agent feature="modification"/></P></td>
    </tr>
    </a>
    </osm:agent>
    <%

      }
      else
      {
      %>

        <tr><td><p class="property">Relationship unknown.</P><tr></td>

      <%
      }
  }

      %>
  </osm:agent>
  </table>

</body>

</html>

