
package net.osm.agent;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import javax.swing.Action;
import java.awt.event.ActionEvent;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceHelper;

import net.osm.util.ExceptionHelper;
import net.osm.audit.AuditService;

/**
 * GenericResourceAgent is a an agent encapsulating a remote reference to a 
 * GenericResource.  The resource is used as a container of a valuetype, and
 * as such may appear within a human interface as a resource reflecting the 
 * the type of value that the resource contains (e.g. a digital certificate 
 * or pdf resource).
 * @author Stephen McConnell
 */
public class GenericResourceAgent extends AbstractResourceAgent
{

    //=========================================================================
    // state
    //=========================================================================

    protected GenericResource generic;
    private String constraint;
 
    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
        try
        {
	      this.generic = GenericResourceHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new RuntimeException( 
		  "GenericResourceAgent. Bad primary object reference.", local );
        }
    }

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * The <code>getType</code> method returns the resource kind, a human 
    * friendly representation of an IDL identifier.
    */
    public String getType( )
    {
	  final String type = "Generic Resource";
        return type;
    }

    //=========================================================================
    // GenericResourceAgent
    //=========================================================================

   /**
    * Gets the IDL identifier of the generic resource constraint.
    * @return String the IDL identifier of the type of valuetype that can
    * be maintained within this generic resource.
    */
    public Serializable getConstraint()
    {
        if( constraint == null ) try
	  {
		constraint = generic.constraint();
        }
        catch( Throwable e )
        {
            throw new RuntimeException("GenericResourceAgent. Unable to retrieve constraint.", e );
        }
        return constraint;
    }

   /**
    * Gets the value that the generic resource is holding.
    * @return Any the <code>Any</code> holding the value
    */
    public Serializable getValue()
    {
	  try
	  {
		return generic.value();
        }
        catch( Throwable e )
        {
            throw new RuntimeException("GenericResourceAgent. Unable to retrieve value.", e );
        }
    }

   /**
    * Sets the value that the generic resource is holding.
    * @param value the <code>Serializable</code> value held by the resource
    */
    public void setValue( Serializable value )
    {
	  try
	  {
		generic.set_value( value );
        }
        catch( Throwable e )
        {
            throw new RuntimeException("GenericResourceAgent. Unable to set value.", e );
        }
    }

   /**
    * Gets the locked status of the generic resource
    * @return boolean true if the resource is locked
    */
    public boolean getLocked()
    {
	  try
	  {
		return generic.locked();
        }
        catch( Throwable e )
        {
            throw new RuntimeException("GenericResourceAgent. Unable to resolve locked state.", e );
        }
    }

   /**
    * Sets the locked status of the generic resource to the supplied value
    * @param value true to lock the resource, false to unlock the resource.
    */
    public void setLocked( boolean value )
    {
	  try
	  {
		generic.locked( value);
        }
        catch( Throwable e )
        {
            throw new RuntimeException(	
		  "GenericResourceAgent. Remote exception while attempting to modify the locked state.", e );
        }
    }

   /**
    * Gets the template status of the generic resource
    * @return boolean true if the resource is a template
    */
    public boolean getTemplate()
    {
	  try
	  {
		return generic.template();
        }
        catch( Throwable e )
        {
            throw new RuntimeException("GenericResourceAgent. Unable to resolve locked state.", e );
        }
    }

 
   /**
    * Sets the template status of the generic resource to the supplied value
    * @param value enable (true) or disable (false) template mode 
    */
    public void setTemplate( boolean value )
    {
	  try
	  {
		generic.template( value );
        }
        catch( Throwable e )
        {
            throw new RuntimeException(	
		  "GenericResourceAgent. Remote exception while attempting to modify the template status.", e );
        }
    }
}
