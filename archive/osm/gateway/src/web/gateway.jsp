
<!--
Gateway.
The gateway page handles two basic views:
 1. the gateway 'profile' view containing the name and description 
 2. the gateway 'services' view containing a listing of available services
The gateway page also provides a principal link that redirects
control to a principal.jsp page for rendering of a principal
established by the gateway.
-->

<%@ page errorPage="exception.jsp" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.adapter.ServiceAdapter" %>
<%@ page import="net.osm.chooser.ChooserAdapter" %>
<%@ page import="net.osm.finder.FinderAdapter" %>
<%@ page import="net.osm.factory.FactoryAdapter" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%@ page import="net.osm.session.user.PrincipalAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%! private static final String m_profile_ref = "<a href=\"gateway.jsp\">gateway</a>"; %>
<%! private static final String m_principal_ref = "<a href=\"gateway.jsp?view=principal\">principal</a>"; %>
<%! private static final String m_services_ref = "<a href=\"gateway.jsp?view=services\">services</a>"; %>
<%! private static final String m_edit_ref = "<a href=\"gateway.jsp?view=edit\">edit</a>"; %>
<%! HomeAdapter m_gateway; %>
<%! String m_title; %>
<%! String m_description; %>
<%! HomeAdapter m_adapter; %>
<%! PrincipalAdapter m_principal; %>
<%! String m_view; %>
<%! String m_services_string; %>

<% 

    //
    // get the reference to the gateway
    //

    m_gateway = (HomeAdapter) application.getAttribute("net.osm.session");

    String path = request.getPathInfo();
    if( path != null )
    {
        try
        {
            request.setAttribute( "adapter", m_gateway );
            request.setAttribute( "chooser.path", path );
            Adapter adapter = m_gateway.lookup( path );
            if( adapter instanceof ChooserAdapter )
            {
                request.setAttribute( "adapter", adapter );
                pageContext.forward( "/chooser.jsp" );
            }
            else if( adapter instanceof FactoryAdapter )
            {
                request.setAttribute( "adapter", adapter );
                pageContext.forward( "/factory.jsp" );
            }
            else if( adapter instanceof FinderAdapter )
            {
                request.setAttribute( "adapter", adapter );
                pageContext.forward( "/finder.jsp" );
            }
            else
            {
                request.setAttribute( "adapter", adapter );
                System.out.println("LOCATED SERVICE: " + adapter.getURL() );
                pageContext.forward( "/service.jsp" );
            }
        }
        catch( Throwable e )
        {
            throw new GatewayRuntimeException("Resolution error from path: " + path, e );
        }
    }
    
    if( m_gateway != null )
    {
        try
        {
            m_title = m_gateway.getName();
            m_description = m_gateway.getDescription();
            m_principal = m_gateway.resolve_user( true );
        }
        catch( Throwable e )
        {
            throw new GatewayRuntimeException("Gateway service unavailable.", e );
        }
    }


    //
    // get the view parameter
    //

    m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "profile";

    //
    // generate the appropriate content
    //

    if( m_view.equals("principal") )
    {
        //
        // redirect control to the principal.jsp page
        //

        try
        {
            request.setAttribute("adapter", m_principal );
            request.setAttribute("view","default" );
            pageContext.forward( "/principal.jsp" );
        }
        catch( Throwable e )
        {
            throw new GatewayRuntimeException(
              "Unexpected exception while resolving principal.", e );
        }
    }
    else
    {

        //
        // we are dealing with content generation
        // contruct the header
        //

        String[] options = new String[]
        {
            m_profile_ref,
            m_principal_ref,
            m_services_ref
        };

        String[] actions = new String[]
        {
            m_edit_ref
        };

        request.setAttribute("title", "Gateway");
        request.setAttribute("options", options );
        request.setAttribute("actions", actions );
    }

    if( m_view.equals("services") )
    {
        //
        // contruct the body
        //

        try
        {
            String[] names = m_gateway.getNames();
            StringBuffer buffer = new StringBuffer();
            for( int i=0; i<names.length; i++ )
            {
                ServiceAdapter service = m_gateway.lookup( names[i] );
                buffer.append( "<tr>" );
                buffer.append( "<td valign=\"top\">" );
                buffer.append( "<a href=\"" + service.getURL() + "\">" );
                buffer.append( "<p>" + service.getName() + "</p>" );
                buffer.append( "</a>" );
                buffer.append( "</td>" );
                buffer.append( "<td>" );
                buffer.append( "<p>" + service.getDescription() + "</p>" );
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

        request.setAttribute("banner", "Services" );
        pageContext.include( "header.jsp" );

        %>
        <table border="0" cellPadding="0" cellSpacing="0" width="100%">
          <%= m_services_string %>
        </table>
        <%
    }
    else
    {
        request.setAttribute("banner", "Profile" );
        pageContext.include("header.jsp");

        %>
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td><p>Name</p></td>
            <td><p><%= m_title %></p></td>
          </tr>
          <tr>
            <td><p>Desciption</p></td>
            <td><p><%= m_description %></p></td>
          </tr>
        </table>
        <%
    }

    pageContext.include("/footer.jsp");

%>



