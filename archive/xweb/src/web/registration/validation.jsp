<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>

<HTML>

<osm:header title="Registration Details">
  <osm:style/>
</osm:header>

<BODY id=bodytag scroll=no CLASS="panel"> 

  <TABLE id=tabletag border=0 cellPadding=0 cellSpacing=0 height="100%" width="100%">
    <TBODY>
      <TR>
        <TD align=left id=tabledatatop vAlign=top>

          <h1>Validating Your Registration</h1>
          <p>
          You have supplied the following information in response to the 
          registration process.  Please verify that the information presented below 
          is correct. If there is an error please correct the details using the 
          MODIFY button.  If the information is correct please CONTINUE.
          </p>

          <h4>registration details</h4>

          <form method="POST" action="registration.jsp" >
          <p>name: <%= request.getParameter("name") %> </P>
          <p>organization: <%= request.getParameter("organization") %> </P>
          <p>email: <%= request.getParameter("email") %> </P>
          <input type="hidden" name="name" value="<%=request.getParameter("name")%>" />
          <input type="hidden" name="organization" value="<%=request.getParameter("organization")%>" />
          <input type="hidden" name="email" value="<%=request.getParameter("email")%>" /><BR/>
          <p><input type="submit" name="modify" value="Modify"></P>
          </form>
          </P>

          <form method="POST" action="account.jsp" >
          <input type="hidden" name="name" value="<%=request.getParameter("name")%>" />
          <input type="hidden" name="organization" value="<%=request.getParameter("organization")%>" />
          <input type="hidden" name="email" value="<%=request.getParameter("email")%>" />
          <p><input type="submit" name="initial" value="Continue"></p>
          </form>

  	<P><BR/></P></TD></TR>
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
