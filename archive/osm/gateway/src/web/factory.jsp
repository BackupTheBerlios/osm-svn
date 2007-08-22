
<!--
Gateway body.
-->

<%@ page errorPage="exception.jsp" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.adapter.ServiceAdapter" %>
<%@ page import="net.osm.factory.FactoryAdapter" %>
<%@ page import="net.osm.factory.FactoryRuntimeException" %>
<%! FactoryAdapter m_adapter; %>
<%! String m_title; %>
<%! String m_description; %>
<%! String m_view; %>
<%! String m_action; %>
<% 

    m_adapter = (FactoryAdapter) pageContext.findAttribute("adapter");
    if( m_adapter != null )
    {
        try
        {
            m_title = m_adapter.getName();
            m_description = m_adapter.getDescription();
        }
        catch( Throwable e )
        {
            throw new FactoryRuntimeException("Chooser profile resolution error.", e );
        }
    }
    else
    {
        final String error = "Page context factory adapter attribute is null.";
        throw new NullPointerException( error );
    }

    //
    // get the view and action parameter
    //

    m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "form";

    m_action = (String) request.getAttribute("action");
    if( m_action == null ) m_action = request.getParameter("action");

    //
    // create the header
    //

    String url = m_adapter.getURL();
    String path = request.getContextPath();
    String ref = request.getRequestURL().toString();
    String[] options = new String[]
    {
        "<a href=\"" + path + "/" + url + "?view=profile\">profile</a>",
        "<a href=\"" + path + "/" + url + "?view=form\">form</a>"
    };
    String[] actions = new String[]
    {
        "<a href=\"" + path + "/" + url 
          + "?view=profile&action=add\">add to favorites</a>"
    };
    request.setAttribute("title", m_adapter.getName() );
    request.setAttribute("banner", m_view );
    request.setAttribute("options", options );
    request.setAttribute("actions", actions );
    pageContext.include("header.jsp");

    //
    // generate the content
    //

    if( m_view.equals("form") )
    {
        if( m_action != null )
        {
            pageContext.include("unavailable.jsp");
        }
        else
        {
            pageContext.include("parameters.jsp");
        }
    }
    else if( m_view.equals("profile") )
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
          <tr>
            <td valign="top"><p class="entry">page:</p></td>
            <td><p class="entry">factory.jsp</p></td>
          </tr>
          <tr>
            <td valign="top"><p class="entry">name:</p></td>
            <td><p class="entry"><%= m_title %></p></td>
          </tr>
          <tr>
            <td valign="top"><p class="entry">url:</p></td>
            <td><p class="entry"><a href=" <%= ref %>"><%= ref %></a></p></td>
          </tr>
          <tr>
            <td valign="top"><p class="entry">desciption:</p></td>
            <td><p class="entry"><%= m_description %></p></td>
          </tr>
        </table>
        </form>
        <%
        }
    }
    else
    {
        pageContext.include("unavailable.jsp");
    }
    pageContext.include("/footer.jsp");

%>



