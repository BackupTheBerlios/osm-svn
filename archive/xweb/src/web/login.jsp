<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>

<HTML>

<osm:header title="Login Page">
  <osm:style/>
</osm:header>

<%
    String username = request.getParameter("username");
%>

<BODY id=bodytag scroll=no> 
  <TABLE id=tabletag border=0 cellPadding=0 cellSpacing=0 height="100%" width="100%">
    <TBODY>
      <TR>
        <TD align=left id=tabledatatop vAlign=top>
          <h1>member login</h1>
          <form method="POST" action="j_security_check" >

  	      <P CLASS="label-required">username:<BR/>
		    <%

		    if( username == null )
		    {

                    %>
                    <input type="text" name="j_username"/></P>
                    <%

                }
                else
                {

                    %>
                    <input type="text" name="j_username" value="<%=username%>"/></P>                
                    <%
		    }
                %>

	      <P CLASS="label-required">password:<BR/>
              <input type="password" name="j_password"><br/><br/>
              <input type="submit" value="Continue" name="j_security_check"></P>

          </form>
        </TD>
      </TR>
      <TR>
        <TD align=right id=tabledatabottom vAlign=bottom>
          <P CLASS="home">
	      <SCRIPT LANGUAGE="JAVASCRIPT">
	      <!--
	      document.writeln(location.hostname)
	      //-->
	      </SCRIPT>
          </P>
        </TD>
      </TR>
    </TBODY>
  </TABLE>
</BODY>
</HTML>

