
package net.osm.session;

import java.io.Serializable;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.AbstractResource;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.Workspace;
import org.omg.Session.Link;

/**
 * Link valuetype held by a Workspace that is contained by 
 * a parent workspace under a strong aggregation relationship.
 */ 
public class IsPartOf extends org.omg.Session.IsPartOf
implements ValueFactory
{
    
    //===============================================================
    // constructors
    //===============================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public IsPartOf() 
    {
    }

   /**
    * Creation of a new IsPartOf link based on a supplied Workspace.
    */
    public IsPartOf( Workspace resource ) 
    {
	  super.resource_state = resource;
    }
    
    //===============================================================
    // Link
    //===============================================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * is part of the resource this Link instance.
    * @return  AbstractResource the resource that is part of the resource 
    * holding this link.
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
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
            return new ComposedOf( resource );
        }
        catch( Throwable e )
        {
            throw new SessionRuntimeException( 
               "Unexpected error occured while attempting to create an inverse link.",
               e );
        }
    } 

    //===============================================================
    // ValueFactory
    //===============================================================

   /**
    * IsPartOf factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) 
    {
        return is.read_value( new IsPartOf() );
    }
}
