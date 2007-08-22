<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ taglib uri="http://home.osm.net/xweb" prefix="osm" %>

<%@ page import="net.osm.xweb.Controls,java.util.Vector,java.util.Enumeration" %>

<HTML>

<osm:header title="Registration Details">
  <osm:style/>
</osm:header>

<BODY id=bodytag CLASS="panel"> 

    <h1>Account Establishment</h1>

    <p>
    Please enter your prefered username and password in the fields below. You will
    need to supply this information when logging into communities under this domain.
    Once your account is established, you will be able to update your profile and 
    account information on-line.
    </p>

    <h4>registration details</h4>

    <p>name: <%=request.getParameter("name") %> </P>
    <p>organization: <%=request.getParameter("organization") %> </P>
    <p>email: <%=request.getParameter("email") %> </P>

    <h4>account details</h4>

    <%
    boolean initial = true;
    boolean duplicate = false;
    String password = request.getParameter("password");
    String verification = request.getParameter("verification");
    String username = request.getParameter("username");
    Vector problems = new Vector();

    ServletContext context = pageContext.getServletContext();
    Controls xweb = (Controls) context.getAttribute( "xweb" );
    int minimum = xweb.getMinimumLoginCharacterCount();
    Manager realm = xweb.getRealmManager();

    if( request.getParameter("continue") != null )
    {
        initial = false;

	  // verify that the name is reasonable

        username = username.trim();
        if( username.length() == 0 )
        {
		problems.add("The username field is empty.");
        }

	  // verify that the password is reasonable

        if( password.length() == 0 )
        {
		problems.add("The password field is empty.");
        }
        else if( password.length() < minimum )
        {
		problems.add("The password must contain at least " + minimum + " characters.");
        }
        else if( !password.equals( verification ) )
        {
		problems.add("The password and verification-passwords do not match.");
        }
            
	  //
        // if there are no problems, try to create an account
        //

        if( problems.size() == 0 )
        {
            try
	      {
		    String email = request.getParameter("email");
                realm.add_entry( username, email, password );
                realm.grant( username, "guest" );

                %>
                <jsp:forward page="establishment.jsp"/>
                <%
	      }
	      catch( DuplicateToken e )
            {
		    String error = "The requested username '" + username + "' is already assigned to an " +
		    "existing member.<br> Please enter an alternative username.";
		    problems.add( error );
	      }
	      catch( DuplicateEmail e )
            {

		    duplicate = true;

		    // the exception means that an account already exists which is an unexpected
		    // condition --> redirect the user to the registration page

                %>
                <FORM method="POST" action="registration.jsp" >
                <input type="hidden" name="name" value="<%=request.getParameter("name")%>" />
                <input type="hidden" name="organization" value="<%=request.getParameter("organization")%>" />
                <input type="hidden" name="email" value="<%=request.getParameter("email")%>" />
                <input type="hidden" name="username" value="<%=request.getParameter("username")%>" />
                <input type="hidden" name="result" value="conflict" />

                <P CLASS="label-required">
	          The email address <%=request.getParameter("email")%> is already known within 
                this domain. Please correct the supplied information under the 
		    registration page.</P>
		    <BR/>
                <p><input type="submit" value="Registration" name="modify"></p>
		    </FORM>
                <%

            }
        }

        if( !duplicate )
        {
            %>
            <FORM method="POST" action="account.jsp" >
            <P CLASS="label-required">username:<BR/>
            <input type="text" name="username" value="<%=request.getParameter("username")%>";/>
            <P CLASS="label-required">password (minimum <%=minimum%> char):<BR/>
            <input type="password" name="password"/></p>
            <P CLASS="label-required">password verification:<BR/>
            <input type="password" name="verification"/></p>

            <input type="hidden" name="name" value="<%=request.getParameter("name")%>" />
            <input type="hidden" name="organization" value="<%=request.getParameter("organization")%>" />
            <input type="hidden" name="email" value="<%=request.getParameter("email")%>" />
            <p><input type="submit" value="Continue" name="continue"></p>
	      </FORM>
            <P CLASS="label-required">
            One or more problems were identified with the supplied information.
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
        // the user hasn't enter the password yet so put up 
        // a standard request

        %>
        <form method="POST" action="account.jsp" >
        <P CLASS="label-required">username:<BR/>
        <input type="text" name="username"/>
        <P CLASS="label-required">password (minimum <%=minimum%> char):<BR/>
        <input type="password" name="password"/></p>
        <P CLASS="label-required">password verification:<BR/>
        <input type="password" name="verification"/></p>
        <input type="hidden" name="name" value="<%=request.getParameter("name")%>" />
        <input type="hidden" name="organization" value="<%=request.getParameter("organization")%>" />
        <input type="hidden" name="email" value="<%=request.getParameter("email")%>" />
        <p><input type="submit" value="Continue" name="continue"></p>
        </form>
        <%
    }

%>
</BODY>
</HTML>
