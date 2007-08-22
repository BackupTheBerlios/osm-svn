
<%@ page language="java" %>
<%@ taglib uri="http://home.osm.net/web" prefix="osm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%@ page import="net.osm.session.resource.AbstractResourceAdapter" %>

<%! AbstractResourceAdapter m_adapter; %>

<%
    m_adapter = (AbstractResourceAdapter) request.getAttribute("adapter");
    if( m_adapter == null )
    {
        System.out.println("Null m_adapter value.");
        throw new RuntimeException( "rename referral null adapter" );
    }
%>

<html:html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<LINK REL="stylesheet" TYPE="text/css" HREF="../css/clustmgr-style.css">
<html:base/>
</head>

<body background="../nav/images/jmenu/PaperTexture.gif" >

<html:errors/>

<html:form method="post" action="rename.do">
  <input type=hidden name="base" value="<%= m_adapter.getBase() %>">
  <input type=hidden name="identity" value="<%= m_adapter.getIdentity() %>">

  <table cellpadding="3" border="0">
    <tr>
      <td>
        <p class="command">rename</p>
        <p class="note">Rename the resource to the supplied value.</p>
      </td>
    </tr>
    <tr>
      <td>
        <html:text property="name" size="45"
          value="<%= m_adapter.getName() %>" /> 
      </td>
    </tr>
    <tr>
      <td width="73%">
        <div align="left"><input type=image 
          src="nav/images/buttons/OK.gif" 
          width="70" height="21" > </div>
      </td>
    </tr> 
  </table>

</html:form>

<p>&nbsp;</p>

</body>
</html:html>