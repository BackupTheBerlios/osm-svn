<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>

<%@ page import="net.osm.xweb.Controls,java.util.Vector,java.util.Enumeration" %>

<HTML>

<osm:header title="New Member Registration">
  <osm:style/>
</osm:header>

<BODY id=bodytag scroll=no CLASS="panel">

  <TABLE id=tabletag border=0 cellPadding=0 cellSpacing=0 height="100%" width="100%">
    <TBODY>
      <TR>
        <TD align=left id=tabledatatop vAlign=top>

          <h1>New Member Registration</h1>

          <p>
	    Membership to this domain provides you with access to business communities, 
	    other members of those communities, and a suite of business processes and 
          services supporting cross-enterprise collaboration.  To become a member of this
          community you have to establish an account.  This page contains an account 
	    application form which will be used to establish your membership profile.  
	    Please complete the information in the fields requested below and submit your 
	    request.
	    </p>

          <h4>Member Profile</h4>

<%--
The registration page can be entered with or without initialized parameters.  In the case of a first occurance of the page there are no "name", "organization" or "email" parameters.  Subsequent requests to the page may contain instantiated parameter values which need to be re-entered in the respective fields. The form action redirects to this page following which a validation of field content is executed.  If this validation passes, the page redirects to the validation.jsp page (which lets the user confirm the information entered before proceeding with account establishment).
--%>

    <%
    boolean initial = true;
    String name = request.getParameter("name");
    String organization = request.getParameter("organization");
    String email = request.getParameter("email");
    String note = "";
    Vector problems = new Vector();

    if(( request.getParameter("continue") != null ) || ( request.getParameter("modify") != null ) )
    {
        initial = false;

	  // verify that the name is reasonable

        name = name.trim();
        if( name.length() == 0 )
        {
		problems.add("The name field is empty.");
        }

        organization = organization.trim();
        if( organization.length() == 0 )
        {
		problems.add("The company/organization field is empty.");
        }

        email = email.trim();
        if( email.length() == 0 )
        {
		problems.add("The email field is empty");
        }
        else if( email.indexOf("@") < 0 )
        {
		problems.add("The email address syntax is invalid (missing @ character).");
        }
        else
        {
            if( email.indexOf("@") == 0 )
		{
		    problems.add("The email address is invalid (no account name).");
            }
            else
            {
		    String dns = email.substring( email.indexOf("@") + 1, email.length());
		    if( dns.indexOf(".") < 0 )
		    {
		        problems.add("The email address syntax is invalid (bad DNS domain).");
                }
		    else
                {
		        if( dns.indexOf(".") < 0 )
                    {
		            problems.add("The email address syntax is invalid (bad DNS address).");
                    }
		        else if( dns.startsWith(".") || dns.endsWith(".") )
		        {
			      problems.add("The email address is invalid (incomplete DNS name).");
                    }
		        else if( dns.indexOf(",") > -1 )
		        {
			      problems.add("The email address is invalid (invalid DNS name).");
                    }
                }
            }
	  }

        if( problems.size() == 0 )
        {
	      try
	      {
                // validate the input
	          ServletContext context = pageContext.getServletContext();
	          Controls xweb = (Controls) context.getAttribute( "xweb"  );
	          Manager realm = xweb.getRealmManager();
                realm.resolve( email );

		    // if we get here it means that the email address is already known 
                // by the realm

	    	    String p = "The email address " + email + " is already known within " +
                "this domain. Please verify if you have already established " + 
                "an account associated with this email address. ";
		    problems.add( p );
            }
            catch( UnknownAddress ua )
            {
                // this means that the email address is unknown to the realm so we can 
                // optimistically proceed with collection of account informaition (
                // because the problems vector is 0 in size)
            }
        }
    }

    if( initial )
    {
        %>
        <FORM method="POST" action="registration.jsp" >
  	  <P CLASS="label-required">full-name:<BR/>
        <INPUT type="text" name="name"/>
  	  <P CLASS="label-required">company/organization:<BR/>
        <INPUT type="text" name="organization"/>
  	  <P CLASS="label-required">email address:<BR/>
        <INPUT type="text" name="email"/><BR/><BR/>
        <INPUT type="submit" value="Continue" name="continue"></P>
        </FORM>
        <%
    }
    else
    {
        if((problems.size() > 0) || ( request.getParameter("modify") != null ))
        {

            %>
            <FORM method="POST" action="registration.jsp" >
  	      <P CLASS="label-required">full-name:<BR/>
            <INPUT type="text" name="name" value="<%=name%>"/>
  	      <P CLASS="label-required">company/organization:<BR/>
            <INPUT type="text" name="organization" value="<%=organization%>"/>
  	      <P CLASS="label-required">email address:<BR/>
            <INPUT type="text" name="email" value="<%=email%>"/><BR/><BR/>
            <INPUT type="submit" value="Continue" name="continue"></P>
            </FORM>
            <%

		if((problems.size() > 0))
            { 

                %>
                <P CLASS="label-required">
		    One or problems were identified with the supplied information.
                </P>
		    <%

		    int j = 1;
		    Enumeration enum = problems.elements();
		    while( enum.hasMoreElements() )
                {
		        String error = (String)enum.nextElement();

		        %>
                    <P CLASS="label-required"><%=j%>. <%=error%></P>
                    <%

		        j++;
                }
            }
        }
        else 
        {
            %>
            <jsp:forward page="validation.jsp"/>
            <%
        }
    }
    %>
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
