// Sun Dec 17 16:53:23 CET 2000

package net.osm.session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.Link;
import org.omg.Session.WorkspaceHelper;

/**
 * Link valuetype held by a Workspace containing a reference a 
 * subsidary workspace that is strongly aggregated by the parent.
 * I.e. removal of the parent workspace imples removal of the 
 * subsidary workspace.
 */ 
public class ComposedOf extends org.omg.Session.ComposedOf
implements ValueFactory
{
    
    //================================================
    // constructor
    //================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public ComposedOf () 
    {
    }

   /**
    * Creation of a new <code>ComposedOf</code> link based on a supplied AbstractResource.
    */
    public ComposedOf( AbstractResource resource ) 
    {
	  super.resource_state = resource;
    }

    //========================================================================
    // Link
    //========================================================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that this 
    * composing resource is referencing.
    * @return  AbstractResource the resource composed by the resource holding this link
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
            return new IsPartOf( WorkspaceHelper.narrow( resource ));
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 


    //================================================
    // ValueFactory
    //================================================
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ComposedOf() );
    }


}
