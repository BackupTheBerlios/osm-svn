
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

import net.osm.chooser.UnknownName;
import net.osm.adapter.Adapter;

public class AdapterTag extends BodyTagSupport
{
    //=========================================================================
    // static
    //=========================================================================

   /**
    * Static value used to delcare that the tag is in an INSPECTION mode
    * in which case the string value of the <code>feature</code> tag attribute will 
    * will be returned from tag evaluation.
    */
    protected static final int INSPECTION = 0;

   /**
    * Static value used to delcare that the tag is in an DELEGATION mode in 
    * which case the tag will establish a delegate Object based on the 
    * instance returned from a getter method based on the supplied 
    * <code>delegate</code> tag attribute.
    */
    protected static final int DELEGATION = 1;

   /**
    * Static value used to delcare that the tag is in an ITERATION mode in 
    * which case the tag will establish a delegate Object based on the 
    * the first value returned from an iterator that is returned from a getter
    * method based on the supplied <code>expand</code> tag attribute.
    */
    protected static final int ITERATION = 2;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * A value controlling the mechanism to source the adapter.  If null, the 
    * adapoter is resolved based on the enclosing adapter.  Otherwise, the adater
    * resolves to the gateway.
    */
    private String m_source;


   /**
    * The adapter against which generated getter methods will be invoked to return
    * values to be used in the context of the current mode of execution.
    */
    private Adapter m_adapter;

   /**
    * The name of the feature, delegate or extent that this tag establishes.
    * A trimmed and capatilized version of the keyword is prepended with 
    * the 'get' string to form a method name.  A method with zero arguments is then  
    * invoked against the tag's web agent.  For example, a feature argument of "name" 
    * will be used to construct the "getName" method. The implementations use of the 
    * value returned from this method is mode dependent.  If the mode is INSPECTION
    * then a string representation is supplied to the web page. If the mode is 
    * DELEGATION or ITERATION the value of the delegate web agent is replaced.  In
    * the case of DELEGATION the delegate web agent is replaced by the value returned 
    * from the derived getter method. In the case of ITERATION the delegate web agent 
    * value is replaced by the first object returned from an iterator returned from
    * the derived getter method.
    */
    private String m_keyword;

   /**
    * The mode of execution.  
    * One of DELEGATION, ITERATION or the default INSPECTION mode.
    */
    private int m_mode = INSPECTION;

   /**
    * The iterator that the tag uses to establish a adapter.  This 
    * value is established if a <code>expand</code> tag attribute is
    * delcared.  The generated getter method is used to establish the
    * iterator value.
    */
    protected Iterator m_iterator;

   /**
    * Header value.
    */
    protected String m_header;

   /**
    * Footer value.
    */
    protected String m_footer;

   /**
    * Supplimentary parameters.
    */
    protected String m_params;


    //=========================================================================
    // Tag state
    //=========================================================================

    public void setFeature( String value ) 
    {
	  if( value != null ) m_keyword = value.trim();
	  m_mode = INSPECTION;
    }

    public void setExpand( String value ) 
    {
	  if( value != null ) m_keyword = value.trim().toLowerCase();
	  m_mode = ITERATION;
    }

    public void setResolve( String value ) 
    {
	  if( value != null ) m_keyword = value.trim().toLowerCase();
	  m_mode = DELEGATION;
    }

    public void setHeader( String header )
    {
        m_header = header;
    }

    public void setFooter( String footer )
    {
        m_footer = footer;
    }

    public void setParams( String params )
    {
        m_params = params;
    }

    public void setSource( String source )
    {
        m_source = source;
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
        }
        catch( Throwable e )
        {
            throw new JspException("Could not resolve adapter.", e );
        }

        if( m_header != null ) try
        {
            out.print( m_header );
        }
        catch( Throwable e )
        {
            throw new JspException("Unexpected IO exception.", e );
        }

        //
        // check if the mode is ITERATION as a result of an expand request
        // and if so, resolve an iterator and update the adapter to 
        // reference the first entry in the iteration
        //

	  if( m_mode == ITERATION ) 
        {
		try
		{
		    Object object = invoke( m_adapter, m_keyword );

		    if( object instanceof List )
		    {
			  m_iterator = ((List) object).iterator();
		    }
		    else
		    {
		        m_iterator = (Iterator) object;
		    }

                if( m_iterator.hasNext() )
                {
		        m_adapter = (Adapter) m_iterator.next();
                }
                else
                {
                    m_adapter = null;
                }

	  	    if( m_adapter != null )
                {
		        return BodyTag.EVAL_BODY_BUFFERED;
	  	    }
		    else
		    {
		        return Tag.SKIP_BODY;
                }
		}
            catch( Throwable e )
            {
                 final String error = "Unexpected exception while accessing iterator with key: " 
                   + m_keyword;
                 throw new JspException( error, e );
            }
        }
	  else if( m_mode == DELEGATION )
	  {
		try
	 	{
		    //
		    // replace the current adapter with the value returned from the 
		    // result of a keyword invocation
		    //

		    m_adapter = (Adapter) invoke( m_adapter, m_keyword );
	  	    if( m_adapter != null )
                {
		        return BodyTag.EVAL_BODY_BUFFERED;
	  	    }
		    else
		    {
		        return Tag.SKIP_BODY;
                }
		}
		catch( Throwable e )
            {
                 final String error = "Unexpected exception while resolving delegated adapter "
                  + "from keyword: " + m_keyword;
                 throw new JspException( error, e );
            }
	  }

        return BodyTag.EVAL_BODY_BUFFERED;
    }

   /**
    * The doAfterBody method is invoked if the EVAL_BODY is enabled.  We use this 
    * method to determine if the iterator needs to be shuffled onto the next value
    * (and thereby possibly causing body iteration).  Otherwise control will
    * move to the doEndTag method.
    */
    public int doAfterBody() throws JspException
    {
 
	  //
	  // Otherwise, make sure the result of body evaluation is written out
        // and access action based on the mode of operation.
        //

	  BodyContent body = getBodyContent();
	  try
        {
		body.writeOut(getPreviousOut());
 	  }
	  catch (IOException e) 
	  { 
		throw new JspException("Unexpected IO Exception.", e );
	  }

        //
        // In the case of ITERATION mode we need to set the adapter to the next value 
        // in the iteration and if that value is not-null we cause the body content to 
        // to be re-evaluated with a different adapter value.
	  //

	  if( m_mode == ITERATION ) 
	  {
	      try
            {
	          body.clearBody();

                if(( m_iterator != null ) && m_iterator.hasNext() )
                {
		        m_adapter = (Adapter) m_iterator.next();
			  if( m_adapter != null )
			  {
			      return IterationTag.EVAL_BODY_AGAIN;
                    }
			  else
			  {
	                  return SKIP_BODY;
                    }
	          }
	      }
	      catch( Throwable e )
            {
		    throw new JspException(
                  "Unexpected exception while resolving next iteration.", e );
            }
        }
	  
	  //
	  // Otherwise there is nothing more to to do becuase the body has already be evaluated
	  // relative to the established delegate.
        //

        return SKIP_BODY;
    }

   /**
    * Tag and body rendering is complete and we can now wrap-up any actions for 
    * the tag.  In the case of simple features this involes return the requested 
    * feature value to the output stream.
    */
    public int doEndTag( ) throws JspException
    {
        JspWriter out = pageContext.getOut();

	  if(( m_mode == INSPECTION ) && (m_keyword != null ))
        {

            //
            // Under INSPECTION mode we defer writing out until now.
            // The following code handles log tag feature as well as
            // introspection based resolution of the delegate web agent.
            // 

            if( m_keyword.equals("this") )
            {
		    try
		    {
                    out.print( m_adapter );
		    }
	          catch( Throwable e )
                {
                    final String error = "Unexpected exception while handling self inspection.";
                    throw new JspException( error, e );
                }
            }
		else
		{
		    try
		    {
			  out.print( invoke( m_adapter, m_keyword ));
		    }
	          catch( Throwable e )
                {
                    if( m_adapter != null )
                    {
                        final String error = "Unexpected exception while invoking keyword: '" 
                          + m_keyword + "' against adapter class: '" 
                          + m_adapter.getClass().getName()
                          + "'.";
                        throw new JspException( error, e );
                    }
                    else
                    {
                        final String error = "Illegal attempt to resolve a feature against a null adapter."; 
                        throw new JspException( error, e );
                    }
                }
            }
        }

        if( m_footer != null ) try
	  {
            out.print( m_footer );
        }
        catch(java.io.IOException e )
        {
		throw new JspException("Unexpected IO Exception.", e );
        }

        return Tag.EVAL_PAGE;
    }

   /**
    * Clean up state members before disposal.
    */
    public void release()
    {
        m_adapter = null;
        m_iterator = null;
        m_header = null;
        m_footer = null;
        m_params = null;
        m_source = null;
    }

    protected Adapter getAdapter()
    {
        if( m_adapter != null ) return m_adapter;
        if( m_source == null )
        {
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
        else
        {
            return (Adapter) pageContext.getServletContext().getAttribute("net.osm.session");
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

        // if the keyword is "URL" then we need to some special processing 
        // based on the target type

        if( keyword.equals("URL") )
        {
            System.out.println("URL lookup for target: " + target.getClass().getName() );
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
