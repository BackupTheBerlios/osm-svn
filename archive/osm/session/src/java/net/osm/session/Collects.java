// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.Link;


/**
 * Link valuetype held by a Workspace containing a reference to
 * resource contained withing the workspace.
 */ 
public class Collects extends org.omg.Session.Collects
implements ValueFactory
{
    //====================================
    // constructors
    //====================================
    
   /**
    * Default constructor for stream internalization.
    */
    public Collects () 
    {
    }

   /**
    * Creation of a new Collects link based on a supplied AbstractResource.
    */

    public Collects( AbstractResource resource ) 
    {
	  super.resource_state = resource;
    }

    //====================================
    // Collects
    //====================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * can be narrowed to a <code>Workspace</code> that holding this link.
    * @return  AbstractResource representing the collection source.
    */
    public AbstractResource resource()
    {
	  return super.resource_state;
    }
    
   /**
    * Factory operation through which the inverse link can be created.
    * @param resource the <code>AbstractResource</code> to bind as the 
    * source of the inverse relationship.
    */
    public Link inverse( final AbstractResource resource )
    {
        try
        {
            return new CollectedBy( WorkspaceHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

    //====================================
    // ValueFactory
    //====================================
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Collects() );
    }


}
