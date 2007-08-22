<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>
<html>

<osm:header title="Login Error">
  <osm:style/>
</osm:header>

<BODY>

  <h1>login error</h1>

  <P>
  The username and password combination supplied during login was 
  not recognised.
  </P>
  <p><a href="<%=request.getHeader("referer")%>">Please try again.</a></P>

</BODY>
</HTML>
