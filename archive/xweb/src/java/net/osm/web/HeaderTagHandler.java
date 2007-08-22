
package net.osm.web;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import javax.servlet.jsp.tagext.*;
import java.util.Enumeration;
import java.io.IOException;

public class HeaderTagHandler extends BodyTagSupport
{

    String lead = "OSM";
    String title = "Untitled";

    public HeaderTagHandler( )
    {
        super();
    }

    public void setTitle( String value )
    {
	  if( value != null ) this.title = value; 
    }

    public int doStartTag( ) throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
		out.println( "<HEAD>" );
		out.println( "  <TITLE>" + this.lead + ": " + this.title + "</TITLE>" );
		out.println( "  <META content=\"text/html; charset=iso-8859-1\" http-equiv=Content-Type />" );
        }
        catch(java.io.IOException e)
        {
            throw new JspTagException("IO Error: " + e.getMessage()); 
        }
        return super.doStartTag();
    }

    public int doEndTag( ) throws JspException
    {
	  try
	  {
		if( bodyContent != null )
            {
                bodyContent.writeOut( bodyContent.getEnclosingWriter() );
            }
            JspWriter out = pageContext.getOut();
		out.println( "</HEAD>" );
        }
	  catch( Exception e )
        {
	       throw new JspException( e.getMessage() );
        }
	  return super.doEndTag();
    }
}
