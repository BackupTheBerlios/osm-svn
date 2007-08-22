
<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ page errorPage="exception.jsp" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.session.workspace.WorkspaceAdapter" %>
<%@ page import="net.osm.session.workspace.WorkspaceRuntimeException" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%! private String m_profile_ref; %>
<%! private String m_contains_ref; %>
<%! private String m_rename_ref; %>
<%! private String m_new_ref; %>
<%! private String m_add_ref; %>
<%! private String m_remove_ref; %>
<%! private String m_destroy_ref; %>
<%! WorkspaceAdapter m_adapter; %>
<%! String m_view; %>
<%! String m_action; %>
<% 
    m_adapter = (WorkspaceAdapter) pageContext.findAttribute("adapter");
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
            m_adapter = (WorkspaceAdapter) gateway.resolve( "workspace=" + ID );
            request.setAttribute("adapter", m_adapter );
            session.setAttribute("workspace", m_adapter );
        }
        else
        {
            final String error = "Page context adapter attribute is null.";
            throw new NullPointerException( error );
        } 
    }
    catch( Throwable e )
    {
        final String error = "Could not resolve workspace adapter.";
        throw new WorkspaceRuntimeException( error, e );
    }

    //
    // get the view parameter
    //

    m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "contained";

    m_action = (String) request.getAttribute("action");
    if( m_action == null ) m_action = request.getParameter("action");


    //
    // create the header
    //

    String url = m_adapter.getURL();
    m_profile_ref = "<a href=\"" + url + "&view=profile\">profile</a>";
    m_contains_ref = "<a href=\"" + url + "&view=contained\">contents</a>";
    m_new_ref = "<a href=\"" + url + "&view=new\">new</a>";
    m_add_ref = "<a href=\"" + url + "&view=add\">add</a>";
    m_remove_ref = "<a href=\"" + url + "&view=remove\">remove</a>";
    m_rename_ref = "<a href=\"" + url + "&view=rename\">rename</a>";
    m_destroy_ref = "<a href=\"" + url + "&view=destroy\">destroy</a>";

    final String[] options = new String[]
    {
        m_profile_ref,
        m_contains_ref,
    };

    String[] actions;
    if( m_view.equals("profile") )
    {
        actions = new String[]
        {
            "<a href=\"" + url + "&action=preferences\">preferences</a>",
            "<a href=\"" + url + "&action=rename\">rename</a>",
            "<a href=\"" + url + "&action=remove\">remove</a>"
        };
    }
    else if( m_view.equals("contained") )
    {
        actions = new String[]
        {
            "<a href=\"" + url + "&action=preferences\">preferences</a>",
            "<a href=\"" + url + "&action=rename\">rename</a>",
            "<a href=\"" + url + "&action=new\">new</a>",
            "<a href=\"" + url + "&action=upload\">upload</a>",
            "<a href=\"" + url + "&action=remove\">remove</a>"
        };
    }
    else
    {
        actions = new String[]{};
    }

    request.setAttribute("title", m_adapter.getName() );
    request.setAttribute("banner", m_view );
    request.setAttribute("options", options );
    request.setAttribute("actions", actions );
    pageContext.include("header.jsp");

    //
    // create the content
    //

    if( m_action != null )
    {
        if( m_action.equals("rename") )
        {
            pageContext.include("rename-resource.jsp");
        }
        else if( m_action.equals("new") )
        {
            pageContext.include("create-subworkspace.jsp");
        }
        else
        {
            pageContext.include("unavailable.jsp");
        }
    }
    else
    {
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
                <p class="entry">
                  <a href="<osm:adapter feature="URL"/>">
                    <osm:adapter feature="name"/>
                  </a>
                </p>
            </td>
            <td align="right">
              <p class="entry">
                <osm:adapter feature="modificationDate"/>
              </p>
            </td>
          </tr>
          </osm:adapter>
        </table>
        </form>
        <% 
        }
        else
        {
            pageContext.include("unavailable.jsp");
        }
    }
    pageContext.include("/footer.jsp");

%>


