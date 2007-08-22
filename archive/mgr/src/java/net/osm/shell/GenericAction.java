
package net.osm.shell;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import java.lang.reflect.InvocationTargetException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Method;

import net.osm.util.ExceptionHelper;

/**
 * A utility supporting introspection based invocation of 
 * a method in responce to action activation.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class GenericAction extends AbstractAction 
{

    //==========================================================
    // state
    //==========================================================

   /**
    * Entity against which the action will be invoked.
    */
    protected Object entity;

   /**
    * The name of the method to be invoked on the entity.
    */
    protected String method;

   /**
    * True if the value state member should be supplied as an argument.
    */
    protected boolean booleanArgument;

   /**
    * Boolean argument value.
    */
    protected boolean value;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new generic action given a target entity and 
    * the name of the method to invoke.
    * @param name - the name of the action
    * @param entity - the target entity
    * @param method - the method name to be invoked
    */
    public GenericAction( String name, Object entity, String method ) 
    {
        this( name, entity, method, true );
    }

   /**
    * Creation of a new generic action given a target entity and 
    * the name of the method to invoke.
    * @param name - the name of the action
    * @param entity - the target object
    * @param method - the method name to be invoked
    * @param value - the initial enabled state of the action
    */
    public GenericAction( String name, Object entity, String method, boolean value ) 
    {
        this( name, null, entity, method, value );
    }

   /**
    * Creation of a new generic action given a target entity and 
    * the name of the method to invoke.
    * @param name - the name of the action
    * @param entity - the target object
    * @param method - the method name to be invoked
    * @param value - the initial enabled state of the action
    */
    public GenericAction( String name, Icon icon, Object entity, String method, boolean value ) 
    {
        super( name, icon );
        this.entity = entity;
        this.method = method;
	  this.value = value;
	  this.setEnabled( value );
    }


    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Abstract actionPerformed.
    * @param event the action event
    * @osm.warning Errors are currntly printed to System.out.  This neeeds
    *   to be updated to generate an interactive error/warning dialog
    */
    public void actionPerformed( ActionEvent event )
    {
        try
	  {
		invoke( entity, method );
	  }
	  catch( Exception e )
	  {
		//
		// WARNING: error should result in a interactive error dialog
		// 

	      ExceptionHelper.printException(
		  "GenericAction, Error while invoking '" + method + "'", e, this, true );
	  }
    }

   /**
    * Invokes a method on an object based on a supplied target object and a keyword.  The 
    * implementation prepends the keyword with the 'get' string, and capatilizes the first
    * character of the keyword (as per the Java Beans convention).
    */
    private Object invoke( Object target, String methodName ) 
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
	  if( target == null ) throw new NullPointerException(
		"Null target argument in generic action.");
        Method method = target.getClass().getMethod( methodName, new Class[0] );
	  try
	  {
            return method.invoke( target, new Object[0] );
	  }
	  catch( Throwable e )
	  {
		final String error = "Exception raised as a result of invocation of '" + methodName + "' on ";
		throw new RuntimeException( error + target, e );
	  }
    }

}
