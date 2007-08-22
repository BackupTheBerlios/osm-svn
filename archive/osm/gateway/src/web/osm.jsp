<%
    String[] options = new String[]
    {
       "<a href=\"osm.jsp?view=about\">about</a>",
       "<a href=\"osm.jsp?view=contacts\">contact</a>"
    };

    String m_view = (String) request.getAttribute("view");
    if( m_view == null ) m_view = request.getParameter("view");
    if( m_view == null ) m_view = "default";
    if( m_view.equals("default") ) m_view = "about";

    request.setAttribute("options", options );
    request.setAttribute("title", "OSM" );
    request.setAttribute("banner", m_view );
    pageContext.include("header.jsp");

    if( m_view.equals("about") )
    {
        %>
<form name="about">
  <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
    <tr>
      <td>
        <p>
OSM provides support of cross-enterprise business process definition, execution and management.  OSM has developed a totally new model based approach that lets you describe your business processes and collaboration policy – something that dramatically changes the way you look at the problems of cross-enterprise service and process federation – it's something that changes the way you think about supply chain management and opens up completely new horizons in terms of cost effective and manageable publication, deployment and service maintenance.
        </p>
     </td>
   </tr>
 </table>
</form>
        <%
    }
    else if( m_view.equals("contacts") ) 
    {
        %>
<form name="contacts">
  <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
    <tr>
      <td>
        <p>
OSM SARL<br>
Registered Office<br>
20 rue Thibaud, 75014 Paris, France<br>
RCS PARIS 97 B 05228</p> 
        </p>
        <p>
telephone + 33 1 4044 4116 </br>
facsimile: +33 (01) 53 90 11 21</br>
<a href="http://www.osm.net">http://home.osm.net</a></br>
<a href="mailto:mcconnell@osm.net">email</a>
        </p>
        </hr>
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
