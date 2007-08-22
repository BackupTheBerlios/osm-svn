
package net.osm.web;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import javax.servlet.jsp.tagext.*;
import java.util.Enumeration;
import java.io.IOException;

public class StyleTagHandler extends TagSupport
{

    public StyleTagHandler( )
    {
            super();
    }

    public int doEndTag( ) throws JspException
    {
	  try
	  {
            JspWriter out = pageContext.getOut();
		String linkHeader = "<LINK rel=\"stylesheet\" type=\"text/css\" href=\"";
            String path = ((HttpServletRequest)pageContext.getRequest()).getContextPath() + "/css/xweb.css"  + "\"";
		String linkTail = " title=\"index\"" + " />";
	      out.println( linkHeader + path + linkTail );
        }
	  catch( Exception e )
        {
	       throw new JspException( e.getMessage() );
        }
	  return super.doEndTag();
    }
}
