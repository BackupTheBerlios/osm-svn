
package net.osm.gateway.tag;

import java.util.List;
import java.util.Iterator;
import java.util.Enumeration;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.osm.gateway.GatewayRuntimeException;
import net.osm.chooser.UnknownName;
import net.osm.adapter.Adapter;

public class HeaderTag extends BodyTagSupport
{

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The adapter against which generated getter methods will be invoked to return
    * values to be used in the context of the current mode of execution.
    */
    private Adapter m_adapter;

   /**
    * Header title.
    */
    protected String m_title;


    //=========================================================================
    // Tag state
    //=========================================================================

    public void setTitle( String value ) 
    {
	  if( value != null ) m_title = value.trim();
    }

    private String getTitle()
    {
        if( m_title != null ) return m_title;
        Adapter adapter = getAdapter();
        try
        {
            return invoke( adapter, "name" ).toString();
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve adapter title.";
            throw new GatewayRuntimeException( error, e );
        }
    }

    //=========================================================================
    // Tag implementation
    //=========================================================================

   /**
    * The doStartTag implementation handles the establishment of a <code>m_adapter</code>
    * and from this determines if body content shall be expanded or not. 
    */

    public int doStartTag() throws JspException
    {

        JspWriter out = pageContext.getOut();

        try
        {
            m_adapter = getAdapter();
            StringBuffer buffer = new StringBuffer();
            buffer.append( "<html><head>" );
            buffer.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"osm.css\" title=\"index\"/>" );
            buffer.append( "<title>OSM Enterprise Gateway</title>" );
            buffer.append( "<body>" );
            buffer.append( "<p class=\"title\">" + getTitle() + "</p>" );
            out.print( buffer.toString() );
        }
        catch( Throwable e )
        {
            throw new GatewayRuntimeException(
              "Unexpected exception while preparing content header.", e );
        }

        return BodyTag.EVAL_BODY_BUFFERED;
    }

   /**
    * Tag and body rendering is complete and we can now wrap-up any actions for 
    * the tag.  In the case of simple features this involes return the requested 
    * feature value to the output stream.
    */
    public int doEndTag( ) throws JspException
    {
        try
        {
            JspWriter out = pageContext.getOut();
            out.print( "</body></html>" );
            return Tag.EVAL_PAGE;
        }
        catch( Throwable e )
        {
            throw new GatewayRuntimeException(
              "Unexpected exception while finalizing content header.", e );
        }
    }

   /**
    * Clean up state members before disposal.
    */
    public void release()
    {
        m_adapter = null;
    }

    protected Adapter getAdapter()
    {
        if( m_adapter != null ) return m_adapter;
        AdapterTag tag = (AdapterTag) findAncestorWithClass( this, AdapterTag.class );
        if( tag != null ) 
        {
            return tag.getAdapter();
        }
        else
        {
            return (Adapter) pageContext.findAttribute("adapter");
        }
    }

   /**
    * Invokes a method on an adapter based on a supplied target and keyword.  The 
    * implementation prepends the keyword with the 'get' string, and capatilizes the first
    * character of the keyword (as per the Java Beans convention).
    */
    Object invoke( Object target, String keyword ) 
    throws Exception
    {
        if( target == null ) 
        {
            throw new NullPointerException("Illegal null target argument inside the adapter tag handler.");
        }

        try
        {
	      if(( keyword == null ) || (target == null )) return null;
            String methodName = "get" + keyword.substring(0,1).toUpperCase() 
              + keyword.substring(1,keyword.length());
            Method method = target.getClass().getMethod( methodName, new Class[0] );
	      return method.invoke( target, new Object[0] );
        }
        catch( Throwable e )
        {
            throw new JspException( "Invocation exception, keyword: " + keyword 
              + ", class: " + target.getClass().getName(), e );
        }
    }
}
