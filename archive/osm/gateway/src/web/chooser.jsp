
<!--
Gateway body.
-->

<%@ page errorPage="exception.jsp" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.chooser.ChooserAdapter" %>
<%@ page import="net.osm.adapter.ServiceAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%! ChooserAdapter m_adapter; %>
<%! String m_title; %>
<%! String m_description; %>
<%! String m_view; %>
<%! String m_action; %>
<%! String m_services_string; %>
<%! String m_profile_ref; %>
<%! String m_services_ref; %>
<% 

    m_adapter = (ChooserAdapter) pageContext.findAttribute("adapter");
    if( m_adapter != null )
    {
        try
        {
            m_title = m_adapter.getName();
            m_description = m_adapter.getDescription();
        }
        catch( Throwable e )
        {
            throw new GatewayRuntimeException("Chooser profile resolution error.", e );
        }
    }
    else
    {
        String key = request.getParameter("key");
        if( key != null )
        {
            HomeAdapter gateway = (HomeAdapter) 
              application.getAttribute( "net.osm.session" );
            m_adapter = (ChooserAdapter) gateway.resolve( "chooser=" + key );
            request.setAttribute("adapter", m_adapter );
        }
        else
        {
            final String error = "Page context chooser adapter attribute is null.";
            throw new NullPointerException( error );
        } 
    }

    //
    // get the view parameter
    //

    m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "services";

    m_action = (String) request.getAttribute("action");
    if( m_action == null ) m_action = request.getParameter("action");

    //
    // create the header
    //

    //System.out.println("CONTEXT: " + request.getContextPath() );
    //System.out.println("INFO: " + request.getPathInfo() );
    //System.out.println("REQUEST: " + request.getRequestURL() );
    //System.out.println("SERVLET: " + request.getServletPath() );

    String url = m_adapter.getURL();
    String path = request.getContextPath();

    m_profile_ref = "<a href=\"" + path + "/" + url + "?view=profile\">profile</a>";
    m_services_ref = "<a href=\"" + path + "/" + url + "?view=services\">services</a>";

    request.setAttribute("title", m_adapter.getName() );
    request.setAttribute("banner", m_view );
    String[] options = new String[]
    {
        m_profile_ref,
        m_services_ref,
    };
    request.setAttribute("options", options );

    String[] actions = new String[]{ "" };
    if( m_view.equals("services") )
    {
        actions = new String[]
        {
            "<a href=\"" + path + "/" + url + "?view=services&action=prev\">prev</a>",
            "<a href=\"" + path + "/" + url + "?view=services&action=next\">next</a>"
        };
    }
    request.setAttribute("actions", actions );
    pageContext.include("header.jsp");

    //
    // generate the appropriate content
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
          <tr>
            <td valign="top"><p class="entry">page:</p></td>
            <td><p class="entry">chooser.jsp</p></td>
          </tr>
          <tr>
            <td valign="top"><p class="entry">name:</p></td>
            <td><p class="entry"><%= m_title %></p></td>
          </tr>
          <tr>
            <td valign="top"><p class="entry">desciption:</p></td>
            <td><p class="entry"><%= m_description %></p></td>
          </tr>
        </table>
        </form>
        <%
    }
    else if( m_view.equals("services") )
    {
        try
        {
            String[] names = m_adapter.getNames();
            StringBuffer buffer = new StringBuffer();
            for( int i=0; i<names.length; i++ )
            {
                ServiceAdapter service = (ServiceAdapter) m_adapter.lookup( names[i] );
                buffer.append( "<tr>" );
                buffer.append( "<td valign=\"top\" width=\"30%\">" );
                buffer.append( "<a href=\"" + request.getContextPath() + "/" + service.getURL() + "\">" );
                buffer.append( "<p class=\"entry\">" + service.getName() + "</p>" );
                buffer.append( "</a>" );
                buffer.append( "</td>" );
                buffer.append( "<td>" );
                buffer.append( "<p class=\"entry\">" + service.getDescription() + "</p>" );
                buffer.append( "</a>" );
                buffer.append( "</td>" );
                buffer.append( "</tr>" );
            }
            m_services_string = buffer.toString();
        }
        catch( Throwable e )
        {
            final String error = "Exception encountered while preparing service information.";
            throw new GatewayRuntimeException( error, e );
        }
        %>
        <form name="services">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td>
              <p class="column">service</p>
            </td>
            <td align="left">
              <p class="column">description</p>
            </td>
          </tr>
          <tr height="2" bgcolor="lightsteelblue">
            <td colspan="3" height="2"></td>
          </tr>
          <%= m_services_string %>
        </table>
        </form>
        <%
    }
    else
    {
        pageContext.include("unavailable.jsp");
    }
    pageContext.include("/footer.jsp");
%>



