
<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ page errorPage="/exception.jsp" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.session.user.PrincipalAdapter" %>
<%@ page import="net.osm.session.user.UserRuntimeException" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%! private static final String m_profile_ref = 
      "<a href=\"principal?view=profile\">profile</a>"; %>
<%! private static final String m_desktop_ref = 
      "<a href=\"principal?view=desktop\">desktop</a>"; %>
<%! private static final String m_tasks_ref = 
      "<a href=\"principal?view=tasks\">tasks</a>"; %>
<%! private static final String m_messages_ref = 
      "<a href=\"principal?view=messages\">messages</a>"; %>
<%! private static final String m_services_ref = 
      "<a href=\"principal?view=services\">services</a>"; %>
<%! PrincipalAdapter m_adapter; %>
<%! String m_view; %>
<%! String m_action; %>
<%! String m_name; %>
<% 
    m_adapter = (PrincipalAdapter) pageContext.findAttribute("principal");
    if( m_adapter == null )
    {
        try
        {
            HomeAdapter gateway = 
              (HomeAdapter) application.getAttribute("net.osm.session");
            m_adapter = gateway.resolve_user( true );
            session.setAttribute("principal", m_adapter );
        }
        catch( Throwable e )
        {
            final String error = "Could not find principal adapter.";
            throw new UserRuntimeException( error, e );
        }
    }
    String path = request.getPathInfo();
    System.out.println("PRINCIPAL-PATH: " + path );
    request.setAttribute("adapter", m_adapter );
    m_name = m_adapter.getName();

    //
    // get the view parameter
    //

    m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "profile";

    //
    // get the action parameter
    //

    m_action = (String) request.getAttribute("action");
    if( m_action == null ) m_action = request.getParameter("action");

    //
    // proceed with content generation
    //

    request.setAttribute("title", m_name );
    request.setAttribute("banner", m_view );
    String[] options = new String[] 
    {
        m_profile_ref,
        m_desktop_ref,
        m_tasks_ref,
        m_messages_ref,
        m_services_ref
    };

    //
    // setup the command options
    // depending on the selected view and handle any transient
    // actions
    //

    String[] actions = null;
    if( m_view.equals("profile") )
    {
        if( m_action != null )
        {
            if( m_action.equals("connect") )
            {
                m_adapter.connect();
                m_action = null;
            }
            else if( m_action.equals("disconnect") )
            {
                m_adapter.disconnect();
                m_action = null;
            }
        }

        String connection;
        if( m_adapter.getConnected() )
        {
            connection = 
              "<a href=\"principal?view=profile&action=disconnect\">disconnect</a>";
        }
        else
        {
            connection = 
              "<a href=\"principal?view=profile&action=connect\">connect</a>";
        }
        actions = new String[]
        {
            "<a href=\"principal?view=profile&action=preferences\">preferences</a>",
            connection
        };
    }
    else if( m_view.equals("services") )
    {
        actions = new String[]
        {
            "<a href=\"principal?view=services&action=select\">select</a>",
            "<a href=\"principal?view=services&action=favorites\">favorites</a>",
            "<a href=\"principal?view=services&action=search\">search</a>"
        };
    }
    else if( m_view.equals("desktop") || m_view.equals("home") )
    {
        actions = new String[]
        {
            "<a href=\"principal?view=desktop&action=preferences\">preferences</a>",
            "<a href=\"principal?view=desktop&action=new\">new</a>",
            "<a href=\"principal?view=desktop&action=favorites\">upload</a>"
        };
    }
    else if( m_view.equals("messages") )
    {
        actions = new String[]
        {
            "<a href=\"principal?view=messages&action=preferences\">preferences</a>"
        };
    }
    else if( m_view.equals("tasks") )
    {
        actions = new String[]
        {
            "<a href=\"principal?view=tasks&action=preferences\">preferences</a>"
        };
    }
    else
    {
        actions = new String[]
        {
            ""
        };
    }

    request.setAttribute("options", options );
    request.setAttribute("actions", actions );
    pageContext.include("header.jsp");

    if( m_view.equals("profile") )
    {
        if( m_action != null )
        {
            pageContext.include("unavailable.jsp");
        }
        else
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
            <td>
              <p class="entry">connected:</p>
            </td>
            <td>
              <p class="entry"><osm:adapter feature="connected"/></P>
            </td>
          </tr>
        </table>
        </form>
        <% 
        }
    }
    else if( m_view.equals("tasks") )
    {
        if( m_action != null )
        {
            pageContext.include("unavailable.jsp");
        }
        else
        {
        %>
        <form name="tasks">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td align="left">
              <p class="column">name</p>
            </td>
            <td align="left">
              <p class="column">state</p>
            </td>
            <td align="right">
              <p class="column">modified</p>
            </td>
          </tr>
          <tr height="2" bgcolor="lightsteelblue">
            <td colspan="3" height="2"></td>
          </tr>
          <osm:adapter expand="tasks">
          <tr>
            <td>
              <p class="entry">
                <a href="<osm:adapter feature="URL"/>">
                  <osm:adapter feature="name"/>
                </a>
              </p>
            </td>
            <td align="left">
              <p class="entry">
                <a href="<osm:adapter feature="URL"/>&view=state">
                  <osm:adapter feature="state"/>
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
    }
    else if( m_view.equals("desktop") || m_view.equals("home") )
    {
        if( m_action != null )
        {
            if( m_action.equals("new") )
            {
                request.setAttribute( "adapter", m_adapter.getDesktop() );
                pageContext.include("create-subworkspace.jsp");
            }
            else
            {
                pageContext.include("unavailable.jsp");
            }
        }
        else
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
          <osm:adapter expand="contents">
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
    }
    else if( m_view.equals("services") )
    {
        %>
        <form name="services">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
          <tr>
            <td width="30%">
              <p class="column">name</p>
            </td>
            <td align="left">
              <p class="column">description</p>
            </td>
          </tr>
          <tr height="2" bgcolor="lightsteelblue">
            <td colspan="2" height="2"></td>
          </tr>
          <osm:adapter expand="services">
          <tr>
            <td width="30%" valign="top">
              <p class="entry"><a href="<osm:adapter feature="URL"/>"><osm:adapter feature="name"/></a></p>
            </td>
            <td align="left">
              <p class="entry"><osm:adapter feature="description"/></p>

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

    pageContext.include("/footer.jsp");

%>


