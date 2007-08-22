
package net.osm.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.omg.CORBA.ORB;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;

import net.osm.xweb.XWEB;
import net.osm.xweb.Controls;
import net.osm.agent.Agent;
import net.osm.agent.AgentServer;
import net.osm.agent.AgentService;
import net.osm.agent.AbstractResourceAgent;
import net.osm.agent.LinkAgent;

public class AgentTag extends BodyTagSupport
{

    private static final boolean trace = false;

    private static final String KEY = XWEB.IDENTIFIER;

    private static final String get = "get";

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

   /**
    * The mode of execution - either INSPECTION, DELEGATION or ITERATION.
    */
    protected int mode = INSPECTION;

   /**
    * The current ORB.
    */
    private ORB orb;

   /**
    * The agent factory.
    */
    private static AgentService server;


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
    protected String keyword;


   /**
    * The value assigned from the <code>reference</code> attribute, used to establish
    * a new web agent instance based on the tag's baseClass.
    */
    protected Object reference;

   /**
    * Internal reference to the body content.
    */
    protected BodyContent body;


   /**
    * The object against which generated getter methods will be invoked to return
    * values to be used in the context of the current mode of execution.
    */
    protected Object delegate;


   /**
    * The iterator that the tag uses to establish a delegate.  This 
    * value is established if a <code>expand</code> tag attribute is
    * delcared.  The generated getter method is used to establish the
    * iterator value.
    */
    protected Iterator iterator;


   /**
    * The value to be entered into a page on the start tag phase if the mode 
    * is ITERATION and there is at least one entry in the list.
    */
    protected String header;


   /**
    * The value to be entered into a page on the end tag phase if the mode 
    * is ITERATION and there was at least one entry in the list.
    */
    protected String footer;

   /**
    * If the tag declares a value of 'true' under the recursive attribute then
    * the body needs to be re-evaluated (i.e. it contains supplementary jsp
    * tags).
    */
    protected boolean recursive = false;


    //=========================================================================
    // Constructor
    //=========================================================================

    public AgentTag( )
    {
        super();
    }

    //=========================================================================
    // Atrribute setters and getters
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */ 
    public void setReference( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"Attempt to set AgentTag reference to a null value");
        this.reference = value;
	  try
	  {
            this.delegate = getAgentService().resolve( this.reference );
	  }
	  catch( Throwable e )
	  {
		throw new RuntimeException(
		  "unexpected exception while requesting object resolution");
	  }
    }

   /**
    * Set the agent that is to be presented.
    */ 
    public void setAgent( Agent value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"Attempt to set AgentTag agent to a null value");
        this.delegate = value;
    }

   /**
    * Set the agent that is to be presented.
    */ 
    public void setEntity( Agent value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"Attempt to set AgentTag agent to a null value");
        this.delegate = value;
    }

   /**
    * Set the recurvise state of the tag.
    */ 
    public void setRecursive( String value ) 
    {
        if( value.equals("true") ) this.recursive = true;        
    }

   /**
    */
    public void setFeature( String value ) 
    {
	  if( value != null ) keyword = value.trim();
	  mode = INSPECTION;
    }

    public void setDelegate( String value )
    {
	  if( value != null ) keyword = value.trim().toLowerCase();
	  mode = DELEGATION;
    }

    public void setExpand( String value ) 
    {
	  if( value != null ) keyword = value.trim().toLowerCase();
	  mode = ITERATION;
    }

   /**
    * Setter method for the value to be used as a header in a page 
    * if the mode is ITERATION and there is at least one entry in the  
    * list.
    */
    public void setHeader( String value ) 
    {
	  if( value != null ) header = value;
    }

   /**
    * Setter method for the value to be used as a footer in a page 
    * if the mode is ITERATION and there is at least one entry in the  
    * list.
    */
    public void setFooter( String value ) 
    {
	  if( value != null ) footer = value;
    }

   /**
    * Returns the agent seen by child tags - resolved through 
    * navigation up the tag hierachy until a tag contains a delegate
    * agent of the appropriate type.
    */

    public Object getAgent()
    {
	  if( delegate != null ) return delegate;
        AgentTag tag = (AgentTag) findAncestorWithClass( this, AgentTag.class );
        if( tag == null ) return null;
	  return tag.getAgent( );
    }


    //=========================================================================
    // Tag implementation
    //=========================================================================

   /**
    * The doStartTag implementation handles the establishment of a <code>delegate</code>
    * and from this determines if body content shall be expanded or not.  If the mode
    * of execution is INSPECTION (i.e. a feature request) then body evaluation is implicit.
    * If the mode of execution is DELEGATION, then the body will be evaluated if the 
    * keyword maps to a corresponding getter method on the delegate web agent.
    * In the case of the ITERATION mode, body evaluation is enabled if the result of
    * generated getter method returns an iterator, and, the iterator returns a non-null 
    * object.
    */

    public int doStartTag() throws JspException
    {

        JspWriter out = pageContext.getOut();

        //
        // establish the delegate
        //

        if( delegate == null )
	  {

		//
		// establish the delegate by navigating the tag hierachy
		//

            this.delegate = getAgent( );
            if( this.delegate == null ) 
            {
                throw new JspException("No enclosing web agent." );
            }
        }

        // 
        // given the delegate and a keyword, we can update the iterator and delegate 
        // values.  In the case of "publication" access - we don't neeed to do anything more.
        // In the case of DELEGATION mode - we need to replace the delegate based on the 
        // value returned from the generated getter method.  In the case of "ITERATION" we 
        // need to set the iterator value and replace the delegate value with the first 
        // entry in the iteration.
        //

	  if( mode == ITERATION ) 
        {
		try
		{
		    Object object = invoke( delegate, keyword );
		    if( object instanceof List )
		    {
			  iterator = ((List) object).iterator();
		    }
		    else
		    {
		        iterator = (Iterator) object;
		    }

		    Object next = iterator.next();
		    if( next instanceof LinkAgent )
		    {
		        delegate = ((LinkAgent)next).getTarget();
		    }
		    else
		    {
			  delegate = next;
		    }

	  	    if( delegate != null )
                {
			  //
			  // Add a header if supplied and enable body evaluation
			  //

			  if( header != null ) pageContext.getOut().print( header );
		        return BodyTag.EVAL_BODY_BUFFERED;
	  	    }
		    else
		    {
		        return Tag.SKIP_BODY;
                }
		}
		catch( java.lang.reflect.InvocationTargetException ite )
	      {
		    try
	          {
		        out.print(
				"ITERATION_INVOCATION_ERROR:" + 
				"<BR/>delegate: " + delegate + 
				"<BR/>keyword: " + keyword + 
				"<BR/>"
		        );
		        reportException( out, ite.getTargetException() );
		    }
		    catch( Exception _ )
		    {
	          }
		}
		catch( Exception e )
		{
		    return Tag.SKIP_BODY;
		}
	  }
	  else if( mode == DELEGATION )
	  {
		try
	 	{
		    //
		    // replace the current delegate with the value returned from the 
		    // result of a keyword invocation
		    //

		    delegate = invoke( delegate, keyword );
	  	    if( delegate != null )
                {
			  if( header != null ) pageContext.getOut().print( header );
		        return BodyTag.EVAL_BODY_BUFFERED;
	  	    }
		    else
		    {
		        return Tag.SKIP_BODY;
                }
		}
		catch( java.lang.reflect.InvocationTargetException ite )
	      {
		    try
		    {
		        out.print(
				"DELEGATION_INVOCATION_ERROR:" + 
				"<BR/>delegate: " + delegate + 
				"<BR/>keyword: " + keyword + 
				"<BR/>"
		        );
		        reportException( out, ite.getTargetException() );
		    }
		    catch( Exception _ )
		    {
	          }
		}
		catch( Exception e )
		{
		    e.printStackTrace();
		    return Tag.SKIP_BODY;
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

    public int doAfterBody() throws JspTagException
    {

	  if( this.recursive ) 
        {
	      this.recursive = false;
            return IterationTag.EVAL_BODY_AGAIN;
        }

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
		throw new JspTagException("AgentTag: " + e.getMessage());
	  }

        //
        // In the case of ITERATION mode we need to set the delegate to the next value 
        // in the iteration and if that value is not-null we cause the body content to 
        // to be re-evaluated with a different delgate value.
	  //

	  if( mode == ITERATION ) 
	  {
	      try
            {
	          body.clearBody();

                if( this.iterator != null ) if( iterator.hasNext() )
                {
		        Object next = iterator.next();
		        if( next instanceof LinkAgent )
		        {
		            delegate = ((LinkAgent)next).getTarget();
		        }
		        else
		        {
			      delegate = next;
		        }

			  if( delegate != null )
			  {
			      return IterationTag.EVAL_BODY_AGAIN;
                    }
			  else
			  {
	                  return SKIP_BODY;
                    }
	          }
	      }
	      catch( Exception e )
            {
                throw new JspTagException( "AgentTag: " + e.getMessage() );
            }
        }
	  
	  //
	  // Otherwise there is nothing more to to do becuase the body has already be evaluated
	  // relative to the established delegate.
        //

        return SKIP_BODY;
    }

   /**
    * Tag and body rendering is complete and we can now wrap -up any actions for 
    * the tag.  In the case of simple features this involes return the requested 
    * feature value to the output stream.
    */

    public int doEndTag( ) throws JspException
    {

        JspWriter out = pageContext.getOut();

	  if(( mode == INSPECTION ) && (keyword != null )) try
        {

            //
            // Under INSPECTION mode we defer writing out until now.
            // The following code handles log tag feature as well as
            // introspection based resolution of the delegate web agent.
            // 

            if( keyword.equals("this") )
            {
                out.print( this );
            }
            else if( keyword.equals("delegate") )
            {
                out.print( delegate );
            }
            else if( keyword.equals("iterator") )
            {
                out.print( iterator );
            }
            else if( keyword.equals("mode") )
            {
		    if( mode == ITERATION )
		    {
                    out.print( "ITERATION" );
                }
		    else if( mode == DELEGATION )
		    {
                    out.print( "DELEGATION" );
                }
		    else
		    {
                    out.print( "INSPECTION" );
                }
            }
		else
		{
		    try
		    {
			  out.print( invoke( delegate, keyword ));
		    }
		    catch( java.lang.reflect.InvocationTargetException ite )
	          {
		        out.print(
				"INSPECTION_INVOCATION_ERROR:" + 
				"<BR/>delegate: " + delegate + 
				"<BR/>keyword: " + keyword + 
				"<BR/>"
			  );
			  reportException( out, ite.getTargetException() );
		    }
		    catch( Exception generalException )
	          {
		        out.print(
				"INSPECTION_ERROR: " + 
				"<BR/>delegate: " + delegate + 
				"<BR/>keyword: " + keyword + 
				"<BR/>"
			  );
			  reportException( out, generalException );
		    }
            }
        }
        catch(java.io.IOException e)
        {
            throw new JspTagException("IO Error: " + e.getMessage() ); 
        }
        catch(Exception e)
        {
		try
		{
                out.print( "AGENT_TAG_ERROR: " + e.toString() );
		    e.printStackTrace();
            }
            catch(java.io.IOException io)
            {
                throw new JspTagException("IO Error: " + io.getMessage() ); 
            }
        }
        else if( mode == ITERATION )
        {
            // 
            // Under the ITERATION mode the content of the tag body should have 
            // already been written out to the page.  If there is a footer 
            // value then write this out now.
            //

		if( footer != null ) try
	      {
                out.print( footer );
            }
            catch(java.io.IOException io)
            {
                throw new JspTagException("IO Error: " + io.getMessage() ); 
            }
        }
        return Tag.EVAL_PAGE;
    }

   /**
    * Clean up state members before disposal.
    */

    public void release()
    {
        this.orb = null;
        this.delegate = null;
        this.iterator = null;
    }


    //========================================================
    // Internal methods
    //========================================================

   /**
    * Returns the current ORB.
    */

    public ORB getOrb()
    {
	  return getAgentService().getOrb();
    }

   /**
    * Returns the agent service.
    */

    public AgentService getAgentService()
    {
	  if( server != null ) return server;
	  server = ((Controls) super.pageContext.getServletContext().getAttribute("xweb")).getAgentService();
	  //server = AgentServer.getAgentService();
	  return server;
    }

   /**
    * report an exception 
    */

    public void reportException( JspWriter out, Throwable throwable )
    {
	  try
	  {
	      out.print( "cause: " + throwable.toString());
            if( throwable instanceof Throwable )
            {
		    Throwable cause = ((Throwable)throwable).getCause();
		    out.print( "<BR/>" );
		    reportException( out, cause );
            }
        }
	  catch( Exception e )
	  {
	  }
    }

   /**
    * Invokes a method on an object based on a supplied target object and a keyword.  The 
    * implementation prepends the keyword with the 'get' string, and capatilizes the first
    * character of the keyword (as per the Java Beans convention).
    */

    Object invoke( Object target, String keyword ) 
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
	  if(( keyword == null ) || (target == null )) return null;
        String methodName = get + keyword.substring(0,1).toUpperCase() + keyword.substring(1,keyword.length());
        Method method = target.getClass().getMethod( methodName, new Class[0] );
	  Object result = method.invoke( target, new Object[0] );
	  if( result instanceof List )
	  {
            if( trace ) System.out.println("INVOKE/LIST: " + keyword + ", " + ((List)result).size() );
        }
	  else
	  {
            if( trace ) System.out.println("INVOKE: " + keyword + ", " + result );
        }
        return result;
    }
}
