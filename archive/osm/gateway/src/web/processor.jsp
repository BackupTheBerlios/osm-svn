<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>

<!--
The task.jsp source defines handlers for operations dealing with a TaskAdapter.  
The page checks for the presence of a HTTP query with an "resolve" parameter 
and argument value corresponding to the domain identifier.  This value is 
used to establish the adapter within the scope of the page context.
-->

<%@ page errorPage="/exception.jsp" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%@ page import="net.osm.session.task.TaskAdapter" %>
<%@ page import="net.osm.session.processor.ProcessorAdapter" %>
<%@ page import="net.osm.session.processor.ProcessorRuntimeException" %>
<%@ page import="net.osm.session.user.UserAdapter" %>
<%! private String m_profile_ref; %>
<%! ProcessorAdapter m_adapter; %>
<%! TaskAdapter m_coordinator; %>
<%! String m_view; %>

<% 
    m_adapter = (ProcessorAdapter) pageContext.findAttribute("adapter");
    if( m_adapter == null ) try
    {
        //
        // try to resolve the adapter based on a 'resolve' parameter
        // 

        String ID = request.getParameter("resolve");
        if( ID != null )
        {
            HomeAdapter gateway = (HomeAdapter) 
              application.getAttribute( "net.osm.session" );
            m_adapter = (ProcessorAdapter) gateway.resolve( "processor=" + ID );
            request.setAttribute("adapter", m_adapter );
        }
        else
        {
            final String error = "Page context adapter attribute is null.";
            throw new NullPointerException( error );
        } 
    }
    catch( Throwable e )
    {
        final String error = "Could not resolve processor adapter.";
        throw new ProcessorRuntimeException( error, e );
    }

    //
    // get the view parameter
    //

    m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "profile";

    //
    // create the header
    //

    String url = m_adapter.getURL();
    m_profile_ref = "<a href=\"" + url + "\">profile</a>";

    String[] options = new String[]
    {
        m_profile_ref,
    };

    try
    {
        request.setAttribute("title", m_adapter.getName() );
        request.setAttribute("options", options );
        request.setAttribute("banner", m_view );
        pageContext.include("header.jsp");
    }
    catch( Throwable e )
    {
        final String error = "Internal task adapter navigation error.";
        throw new ProcessorRuntimeException( error, e );
    }
    
    //
    // create the content
    //

    if( m_view.equals("profile") )
    {

        %>
        <form name="profile">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td>
              <p class="column">feature</p>
            </td>
            <td align="left">
              <p class="column">value</p>
            </td>
          </tr>
          <tr height="2" bgcolor="lightsteelblue">
            <td colspan="3" height="2"></td>
          </tr>
          <jsp:include page="resource-features.jsp" />
          <tr>
            <td align="left">
              <p class="entry">task</p>
            </td>
            <td>
              <p class="entry">
                <osm:adapter resolve="task">
                  <a href="<osm:adapter feature="URL"/>">
                    <osm:adapter feature="name"/>
                  </a>
                </osm:adapter>
              </p>
            </td>
          </tr>
        </table>
        <% 
    }
    else
    {
        request.setAttribute("banner", m_view );
        pageContext.include("header.jsp");
        %>
        <p>Task content view unavailable or unrecognized: '<%= m_view %>'</p>
        <% 
    }

    pageContext.include("/footer.jsp");

%>

