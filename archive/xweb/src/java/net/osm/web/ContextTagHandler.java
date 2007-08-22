
package net.osm.web;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import javax.servlet.jsp.tagext.*;
import java.util.Enumeration;
import java.io.IOException;

public class ContextTagHandler extends BodyTagSupport
{

    private static final String br = "<br/>"; 

    public ContextTagHandler( )
    {
        super();
    }


    public int doEndTag( ) throws JspException
    {
        try
	  {
             report( pageContext );
        }
        catch(Exception e)
        {
            throw new JspTagException("Error: " + e.getMessage()); 
        }
        return super.doEndTag();
    }

    public void report( PageContext context ) throws IOException
    {
        JspWriter out = context.getOut();
        out.println("<h1>PageContext</h1>");

        out.println("<h3>Application Scope</h3>");
	  Enumeration enum = context.getAttributeNamesInScope( PageContext.APPLICATION_SCOPE );
	  if( enum.hasMoreElements() ) 
        {
            out.println("<h4>Attribute:</h4>");
            out.println("<p>");
            while( enum.hasMoreElements() )
            {
	          String name = (String) enum.nextElement();
                out.println("ATTRIBUTE: " + name + ": " + 
			context.getAttribute( name, PageContext.APPLICATION_SCOPE ) + "</br>");
            }
            out.println("</p>");
        }

        out.println("<h3>Page Scope</h3>");
	  Enumeration enum2 = context.getAttributeNamesInScope( PageContext.PAGE_SCOPE );
	  if( enum2.hasMoreElements() ) 
        {
            out.println("<h4>Attribute:</h4>");
            out.println("<p>");
            while( enum2.hasMoreElements() )
            {
	          String name = (String) enum2.nextElement();
                out.println("ATTRIBUTE: " + name + ": " + 
			context.getAttribute( name, PageContext.PAGE_SCOPE ) + "</br>");
            }
            out.println("</p>");
        }

        out.println("<h3>Request Scope</h3>");
	  Enumeration enum3 = context.getAttributeNamesInScope( PageContext.REQUEST_SCOPE );
	  if( enum3.hasMoreElements() ) 
        {
            out.println("<h4>Attribute:</h4>");
            out.println("<p>");
            while( enum3.hasMoreElements() )
            {
	          String name = (String) enum3.nextElement();
                out.println("ATTRIBUTE: " + name + ": " + 
			context.getAttribute( name, PageContext.REQUEST_SCOPE ) + "</br>");
            }
            out.println("</p>");
        }

        report( context.getRequest() );
        report( context.getResponse() );
        report( context.getServletContext() );
        report( context.getServletConfig() );
        report( context.getSession() );
        out.println("<p><br/><br/></p>");
    }

    public void report( ServletRequest request ) throws IOException
    {		
        JspWriter out = pageContext.getOut();
        out.println("<h3>ServletRequest</h3>");
        out.println("<p>");
        out.println("REMOTE: " + request.getRemoteHost() + "<br/>");
        out.println("SECURE: " + request.isSecure() + "</br>");

	  if( request instanceof HttpServletRequest )
        {
            HttpServletRequest r = (HttpServletRequest) request;
            out.println("AUTH-TYPE: " + r.getAuthType() + "<br/>");
            out.println("CONTEXT: " + r.getContextPath() + "<br/>");
            out.println("PATH-INFO: " + r.getPathInfo() + "<br/>");
            out.println("PATH-TRANS: " + r.getPathTranslated() + "<br/>");
            out.println("QUERY: " + r.getQueryString() + "<br/>");
            out.println("USER: " + r.getRemoteUser() + "<br/>");

            java.security.Principal principal = r.getUserPrincipal();
		if( principal != null )
            {
                out.println("PRINCIPAL: " + principal.getName());
            }

            out.println("</p>");
            out.println("<h4>Header</h4>");
            out.println("<p>");
            Enumeration enum = r.getHeaderNames();
            while( enum.hasMoreElements() )
            {
	          String name = (String) enum.nextElement();
                out.println("HEADER/" + name + ": " + r.getHeader( name ) + "<br/>");
            }
        }
        out.println("</p>");

        Enumeration enum2 = request.getParameterNames();
	  if( enum2.hasMoreElements() ) 
        {
            out.println("<h4>Parameters:</h4>");
            out.println("<p>");
            while( enum2.hasMoreElements() )
            {
	          String name = (String) enum2.nextElement();
                out.println("PARAM/" + name + ": " + request.getParameter( name ) + "</br>");
            }
            out.println("</p>");
        }
    }

    public void report( ServletResponse response ) throws IOException
    {		
        JspWriter out = pageContext.getOut();
        out.println("<h3>ServletResponse</h3>");
        out.println("<p>");
        out.println("LOCALE: " + response.getLocale().toString() + "<br/>");
        out.println("COMMITED: " + response.isCommitted() + "<br/>");
        out.println("</p>");
    }

    public void report( ServletConfig config ) throws IOException
    {		
        JspWriter out = pageContext.getOut();
        out.println("<h3>ServletConfig</h3>");
        out.println("<p>");
        out.println("NAME: " + config.getServletName() + "<br/>");
        out.println("</p>");
        out.println("<h4>InitParameters</h4>");
        out.println("<p>");
        Enumeration enum = config.getInitParameterNames();
        while( enum.hasMoreElements() )
        {
	      String name = (String) enum.nextElement();
            out.println("PARAM/" + name + ": " + config.getInitParameter( name ) + "</br>");
        }
        out.println("</p>");
    }

    public void report( ServletContext context ) throws IOException
    {		
        JspWriter out = pageContext.getOut();
        out.println("<h3>ServletContext</h3>");

        out.println("<h4>Attributes</h4>");
        out.println("<p>");
        Enumeration enum = context.getAttributeNames();
        while( enum.hasMoreElements() )
        {
	      String name = (String) enum.nextElement();
            out.println("ATTR/" + name + ": " + context.getAttribute( name ) + "</br>");
        }
        out.println("</p>");

        Enumeration enum2 = context.getInitParameterNames();
	  if( enum2.hasMoreElements() )
        {
            out.println("<h4>InitParameters</h4>");
            out.println("<p>");
            while( enum2.hasMoreElements() )
            {
	          String name = (String) enum2.nextElement();
                out.println("ATTR/" + name + ": " + context.getInitParameter( name ) + "</br>");
            }
            out.println("</p>");
        }
    }

    public void report( HttpSession session ) throws IOException
    {		
        JspWriter out = pageContext.getOut();
        out.println("<h3>Session</h3>");
        out.println("<h4>Attributes</h4>");
        Enumeration enum = session.getAttributeNames();
	  if( enum.hasMoreElements() )
        {
            out.println("<p>");
            while( enum.hasMoreElements() )
            {
	          String name = (String) enum.nextElement();
                out.println("ATTR/" + name + ": " + session.getAttribute( name ) + "</br>");
            }
            out.println("</p>");
	  }
	  else 
	  {
            out.println("<p>NO ATTRIBUTES</P>");
        }
    }

}
