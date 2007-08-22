
<!--
The resolver.jsp page establishes the request scoped 'adapter' attribute
based on a 'resolve' parameter.
-->

<%@ page import="java.util.Enumeration" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%@ page import="net.osm.session.HomeAdapter" %>

<% 

    Enumeration paramNames = request.getParameterNames();
    System.out.println("RESOLVER-REQUEST-PARAMETERS" );
    while( paramNames.hasMoreElements() )
    {
        String param = (String) paramNames.nextElement();
        System.out.println("\t" + param + ": " + request.getParameter( param ) );
    }

    Enumeration attNames = request.getAttributeNames();
    System.out.println("RESOLVER-REQUEST-ATTRIBUTES" );
    while( attNames.hasMoreElements() )
    {
        String param = (String) attNames.nextElement();
        System.out.println("\t" + param );
    }

    Enumeration sessionNames = session.getAttributeNames();
    System.out.println("RESOLVER-SESSION-ATTRIBUTES" );
    while( sessionNames.hasMoreElements() )
    {
        String param = (String) sessionNames.nextElement();
        System.out.println("\t" + param );
    }

    final String base = request.getParameter("base");
    final String identity = request.getParameter("identity");

    if(( base != null ) && ( identity != null )) try
    {
        final HomeAdapter gateway = (HomeAdapter) 
          application.getAttribute("net.osm.session");
        request.setAttribute("adapter", gateway.resolve( base + "=" + identity ));
        pageContext.forward( "/" + base );
    }
    catch( Throwable e )
    {
        final String error = "Resolver referral error.";
        throw new GatewayRuntimeException( error, e );
    }
    else
    {
        throw new GatewayRuntimeException(
          "Could not resole an adapter relative to the supplied params.");
    }


%>

