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
<%@ page import="net.osm.session.task.TaskAdapter" %>
<%@ page import="net.osm.session.task.TaskRuntimeException" %>
<%@ page import="net.osm.session.user.UserAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%! private String m_profile_ref; %>
<%! private String m_consumed_ref; %>
<%! private String m_produced_ref; %>
<%! private String m_rename_ref; %>
<%! private String m_destroy_ref; %>
<%! private String m_state_ref; %>
<%! TaskAdapter m_adapter; %>
<%! UserAdapter m_owner; %>
<%! String m_view; %>

<% 
    m_adapter = (TaskAdapter) pageContext.findAttribute("adapter");
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
            m_adapter = (TaskAdapter) gateway.resolve( "task=" + ID );
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
        final String error = "Could not resolve task adapter.";
        throw new TaskRuntimeException( error, e );
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
    m_consumed_ref = "<a href=\"" + url + "&view=consumed\">consuming</a>";
    m_produced_ref = "<a href=\"" + url + "&view=produced\">producing</a>";
    m_rename_ref = "<a href=\"" + url + "&view=rename\">rename</a>";
    m_destroy_ref = "<a href=\"" + url + "&view=destroy\">delete</a>";
    m_state_ref = "<a href=\"" + url + "&view=state\">state</a>";

    String[] options = new String[]
    {
        m_profile_ref,
        m_consumed_ref,
        m_produced_ref
    };

    String[] actions = new String[]
    {
        m_state_ref,
        m_rename_ref,
        m_destroy_ref
    };

    try
    {
        m_owner = m_adapter.getOwner();
        request.setAttribute("title", m_adapter.getName() );
        request.setAttribute("options", options );
        request.setAttribute("actions", actions );
    }
    catch( Throwable e )
    {
        final String error = "Internal task adapter navigation error.";
        throw new TaskRuntimeException( error, e );
    }

    request.setAttribute("home", "/gateway/principal?view=tasks" );
    request.setAttribute("banner", m_view );
    pageContext.include("header.jsp");
 
    //
    // create the content
    //

    if( m_view.equals("profile") )
    {
        %>
        <form name="profile">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td align="left">
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
              <p class="entry">description:</p>
            </td>
            <td>
              <p class="entry">
                <osm:adapter feature="description"/>
              </p>
            </td>
          </tr>
          <tr>
            <td align="left">
              <p class="entry">owner:</p>
            </td>
            <td>
              <p class="entry">
                <osm:adapter resolve="owner">
                  <a href="<osm:adapter feature="URL"/>">
                    <osm:adapter feature="name"/>
                  </a>
                </osm:adapter>
              </p>
            </td>
          </tr>
          <tr>
            <td align="left">
              <p class="entry">processor:</p>
            </td>
            <td>
              <p class="entry">
                <osm:adapter resolve="processor">
                  <a href="<osm:adapter feature="URL"/>">
                    <osm:adapter feature="name"/>
                  </a>
                </osm:adapter>
              </p>
            </td>
          </tr>
          <tr>
            <td>
              <p class="entry">state:</p>
            </td>
            <td>
              <p class="entry"><osm:adapter feature="state"/></P>
            </td>
          </tr>
        </table>
        </form>
        <% 
    }
    else if( m_view.equals("consumed") )
    {
        %>
        <form name="consumed">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td width="30%">
              <p class="column">name</p>
            </td>
          </tr>
          <tr height="2" bgcolor="lightsteelblue">
            <td height="2"></td>
          </tr>
          <osm:adapter expand="consumed">
          <tr>
            <td width="30%">
              <p><a href="<osm:adapter feature="URL"/>"><osm:adapter feature="name"/></a></p>
            </td>
          </tr>
          </osm:adapter>
        </table>
        <% 
    }
    else if( m_view.equals("produced") )
    {
        %>
        <form name="produced">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td width="30%">
              <p class="column">name</p>
            </td>
          </tr>
          <tr height="2" bgcolor="lightsteelblue">
            <td height="2"></td>
          </tr>
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <osm:adapter expand="produced">
          <tr>
            <td width="30%">
              <p><a href="<osm:adapter feature="URL"/>"><osm:adapter feature="name"/></a></p>
            </td>
          </tr>
          </osm:adapter>
        </table>
        <% 
    }
    else if( m_view.equals("rename") )
    {
        pageContext.include("resource-actions.jsp");
    }
    else if( m_view.equals("state") )
    {
        pageContext.include("state-change-action.jsp");
    }
    else
    {
        pageContext.include("unavailable.jsp");
    }

    pageContext.include("/footer.jsp");

%>

