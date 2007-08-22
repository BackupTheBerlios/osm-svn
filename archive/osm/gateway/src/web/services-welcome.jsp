<%
    String[] options = new String[]
    {
       "<a href=\"services-welcome.jsp?view=overview\">overview</a>",
       "<a href=\"services-welcome.jsp?view=usage\">usage</a>"
    };

    String m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "overview";

    request.setAttribute("options", options );
    request.setAttribute("title", "Services" );
    request.setAttribute("banner", m_view );
    pageContext.include("header.jsp");

    if( m_view.equals("overview") )
    {
        %>
<form name="overview">
  <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
    <tr>
      <td>
        <p>
Business services exposed by the gateway provide support for locating
and executing business service that can be assigned to users through 
tasks.  Task bring together input resources, an owning user, and a 
service that handles business process execution resulting in the 
potential creation of new resources. 
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

