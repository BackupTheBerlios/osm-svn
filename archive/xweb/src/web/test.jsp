<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>
<%@ page import="net.osm.agent.AgentService,org.omg.CommunityFramework.Community,org.omg.CommunityFramework.CommunityHelper,org.omg.Session.AbstractResource,org.omg.Session.AbstractResourceHelper,org.omg.CORBA.ORB,org.omg.Session.UserHelper,org.omg.Session.TaskHelper,org.omg.Session.WorkspaceHelper,org.omg.Session.DesktopHelper" %>

<%
    String title = "Untitled";
    String ior = request.getParameter("ior");
    Controls xweb = (Controls) application.getAttribute( "xweb" );
    org.omg.CORBA.Object root = null;

    if( ior != null )
    {
        ORB orb = xweb.getOrb();
	  root = orb.string_to_object( ior );
    }
    else
    {
        root = xweb.getCommunity();
    }

    if( root._is_a( AbstractResourceHelper.id() ))
    {
        title = AbstractResourceHelper.narrow( root ).name();
    }
    else
    {
        title = "Unknown Object";
    }
%>

<HTML>


<osm:header title="<%=title%>">
  <osm:style/>
</osm:header>

<BODY CLASS="panel">

<%
if( root._is_a( AbstractResourceHelper.id() )) 
{
%>

    <osm:agent reference="<%=root%>">

    <h1><osm:agent feature="type"/>: <%=title%></h1>

    <h4>General Properties</h4>

    <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
    <!--<tr bgcolor="lightsteelblue"><td><p>general properties</p></td></tr>-->
    

        <tr><td><p>name: <osm:agent feature="name" /></P></tr></td>
        <tr><td><p>domain: <a href="/xweb/test.jsp"/><osm:agent feature="domain" /></a></P></tr></td>
        <tr><td><p>kind: <osm:agent feature="kind" /></P></tr></td>
        <tr><td><p>random: <osm:agent feature="random" /></P></tr></td>
        <tr><td><p>created: <osm:agent feature="creation" /></P></tr></td>
        <tr><td><p>modified: <osm:agent feature="modification" /></P></tr></td>
        <tr><td><p>access: <osm:agent feature="access" /></P></tr></td>
        <tr><td><p>produced: <osm:agent feature="producer" /></P></tr></td>
        <%--
        <p>delegate: <osm:agent feature="delegate" /></P>
        --%>

        <%
        if( root._is_a( UserHelper.id() )) 
        {
        %>

        <osm:agent delegate="desktop">
            <tr><td><p>desktop: <a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></tr></td>
        </osm:agent>

	  <%
	  }
	  else if( root._is_a( TaskHelper.id() ))
        {       
	  %>

        <tr><td><p>description: <osm:agent feature="description"/></p></tr></td>
        <tr><td><p>state: <osm:agent feature="state"/></p></tr></td>
        <osm:agent delegate="owner">
            <tr><td><p>owner: <a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></tr></td>
        </osm:agent>

        <osm:agent delegate="processor">
            <tr><td><p>processor: <a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></tr></td>
        </osm:agent>

	  <%
	  }
	  else if( root._is_a( WorkspaceHelper.id() ))
        {       
	  %>

            <%
            if( root._is_a( DesktopHelper.id() ))
            {
            %>

        	<osm:agent delegate="owner">
            	<tr><td><p>owner: <a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></tr></td>
        	</osm:agent>

            <%
		}
            else if( root._is_a( CommunityHelper.id() ))
            {
            %>

        	<tr><td><p>open: <osm:agent feature="open" /></P></tr></td>

        	<%
        	}
        	%>

        <%
        }
        %>

        <%--
        Add the delegate elements with multiple features.
        --%>

	  <%
	  if( root._is_a( CommunityHelper.id() ))
        {
	  %>

        	<osm:agent delegate="model">
		    <tr bgcolor="lightsteelblue"><td><p>model<p></td></tr>
        	    <tr><td><p>label: <osm:agent feature="label" /></P>
        	    <tr><td><p>note: <osm:agent feature="note" /></P>	    
        	    <tr><td><p>exclusive: <osm:agent feature="exclusive" /></P>	    
        	    <tr><td><p>privacy: <osm:agent feature="privacy" /></P>
        	    <osm:agent delegate="role">
                    <tr><td><p>role: <osm:agent feature="label"/></p></tr></td>
                </osm:agent>
       	</osm:agent>

        <%
        }
        %>

        <%--
        Add the expand elements.
        --%>

        <osm:agent expand="containers" 
		header="<tr><td><table border=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><td><p>workspaces containing this resource</p></td></tr>"	
		footer="</table></td></tr>">
            <tr><td><p><a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></td></tr>
        </osm:agent>

        <osm:agent expand="consumers" 
		header="<tr><td><table border=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><td><p>tasks consuming this resource</p></td></tr>"	
		footer="</table></tr></td>">
            <tr><td><p><a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></td></tr>
        </osm:agent>

        <%
        if( root._is_a( UserHelper.id() )) 
        {
        %>

        <osm:agent expand="tasks" 
		header="<tr><td><table border=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><td><p>tasks owned by this user</p></td></tr>"	
		footer="</table></tr></td>">
            <tr><td><p><a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></td></tr>
        </osm:agent>

	  <%
	  }
	  else if( root._is_a( TaskHelper.id() ))
        {
	  %>

        <osm:agent expand="produced" 
		header="<tr><td><table border=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><td><p>resources produced by this task</p></td></tr>"	
		footer="</table></tr></td>">
            <tr><td><p><a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></td></tr>
        </osm:agent>

        <osm:agent expand="consumed" 
		header="<tr><td><table border=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><td><p>resources consumed by this task</p></td></tr>"	
		footer="</table></tr></td>">
            <tr><td><p><a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></td></tr>
        </osm:agent>

	  <%
	  }
	  else if( root._is_a( WorkspaceHelper.id() ))
        {
	  %>

        <osm:agent expand="contents" 
		header="<tr><td><table border=\"0\" width=\"100%\"><tr bgcolor=\"lightsteelblue\"><td><p>resources contained within this workspace</p></td></tr>"	
		footer="</table></tr></td>">
            <tr><td><p><a href="<osm:agent feature="url"/>"><osm:agent feature="name"/></a></p></td></tr>
        </osm:agent>

        <%
        }
        %>

      </osm:agent>

    </td></tr>
    </table>

    <%
    }
    else
    {
    %>

    <p>The type of object supplied is unknown. Please contact your system administrator for additional support.</p>
    <h4>Object Reference</h4>
    <p><%=root%></P>

    <%
    }
    %>


</BODY>

</HTML>
