<%
    String[] options = new String[]
    {
       "<a href=\"welcome.jsp?view=overview\">overview</a>",
       "<a href=\"welcome.jsp?view=help\">help</a>",
       "<a href=\"osm.jsp\">osm</a>"
    };

    String m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "overview";

    request.setAttribute("options", options );
    request.setAttribute("title", "Gateway" );
    request.setAttribute("banner", m_view );
    pageContext.include("header.jsp");

    if( m_view.equals("overview") )
    {
        %>
<form name="overview">
  <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
    <tr>
      <td>
        <p>The gateway allows users to access, navigate and manage tasks, business processes, and resources published under shared and public workspaces.</p>
        <p>The service provides support for:
        <UL>
          <LI> Listing and presentation of available business services
          <LI> Access to a principal user account and desktop
          <LI> Creation, navigate and management of workspaces and resources
          <LI> Creation and management of tasks and association of business processes
        </UL>
        </p>
     </td>
   </tr>
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
