<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>

<HTML>

<osm:header title="Registration Details">
  <osm:style/>
</osm:header>

<BODY CLASS="panel">

  <h1>Account Establishment</h1>

  <P>Your account is established.</P>

  <h4>account details</h4>

  <P CLASS="label-required">name: <%= request.getParameter("name") %></P>
  <P CLASS="label-required">organization: <%= request.getParameter("organization") %></P>
  <P CLASS="label-required">email: <%= request.getParameter("email") %></P>
  <P CLASS="label-required">username: <%= request.getParameter("username") %></P>

</BODY>
</HTML>
