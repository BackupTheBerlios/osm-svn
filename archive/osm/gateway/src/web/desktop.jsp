
<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ page errorPage="exception.jsp" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.session.desktop.DesktopAdapter" %>
<%@ page import="net.osm.session.desktop.DesktopRuntimeException" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%! private String m_profile_ref; %>
<%! private String m_contained_ref; %>
<%! private String m_rename_ref; %>
<%! private String m_new_ref; %>
<%! private String m_add_ref; %>
<%! private String m_remove_ref; %>
<%! DesktopAdapter m_adapter; %>
<%! String m_view; %>
<% 
    m_adapter = (DesktopAdapter) pageContext.findAttribute("adapter");
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
            m_adapter = (DesktopAdapter) gateway.resolve( "desktop=" + ID );
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
        final String error = "Could not resolve desktop adapter.";
        throw new DesktopRuntimeException( error, e );
    }

    //
    // get the view parameter
    //

    m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "contained";

    //
    // create the header
    //

    String url = m_adapter.getURL();
    m_profile_ref = "<a href=\"" + url + "&view=profile\">profile</a>";
    m_contained_ref = "<a href=\"" + url + "&view=contained\">contents</a>";
    m_new_ref = "<a href=\"" + url + "&view=new\">new</a>";
    m_add_ref = "<a href=\"" + url + "&view=add\">add</a>";
    m_remove_ref = "<a href=\"" + url + "&view=remove\">remove</a>";
    m_rename_ref = "<a href=\"" + url + "&view=rename\">rename</a>";

    String[] options = new String[]
    {
        m_profile_ref,
        m_contained_ref,
    };

    String[] actions = new String[]
    {
        m_new_ref,
        m_add_ref,
        m_remove_ref,
        m_rename_ref,
    };

    request.setAttribute("title", m_adapter.getName() );
    request.setAttribute("options", options );
    request.setAttribute("actions", actions );

    //
    // create the content
    //

    request.setAttribute("banner", m_view );
    pageContext.include("header.jsp");

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
        </table>
        </form>
        <% 
    }
    else if( m_view.equals("contained") )
    {
        %>
        <form name="contained">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td width="30%">
              <p class="column">name</p>
            </td>
            <td align="right">
              <p class="column">modified</p>
            </td>
          </tr>
          <tr height="2" bgcolor="lightsteelblue">
            <td colspan="2" height="2"></td>
          </tr>
          <osm:adapter expand="contained">
          <tr>
            <td width="30%">
              <p class="entry"><a href="<osm:adapter feature="URL"/>"><osm:adapter feature="name"/></a></p>
            </td>
            <td align="right">
              <p class="entry"><osm:adapter feature="modificationDate"/></p>
            </td>
          </tr>
          </osm:adapter>
        </table>
        </form>
        <% 
    }
    else if( m_view.equals("rename") )
    {
        pageContext.include("resource-actions.jsp");
    }
    else if( m_view.equals("new") )
    {
        pageContext.include("workspace-actions.jsp");
    }
    else
    {
        pageContext.include("unavailable.jsp");
    }
%>


