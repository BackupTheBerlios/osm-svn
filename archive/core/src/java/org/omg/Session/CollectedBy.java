// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class CollectedBy
implements Containment, StreamableValue, ValueFactory
{

    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/Session/CollectedBy:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * A reference to an AbstractResource implementing the Master interface.
    * An implementation of Master may hold 0..1 ControlledBy link instances
    * representing the parent processor.
    */
    public Workspace resource_state;
    
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public CollectedBy () 
    {
    }

   /**
    * Creation of a new Collects link based on a supplied Workspace.
    */

    public CollectedBy( Workspace resource )
    {
	  this.resource_state = resource;
    }

    //==========================================================
    // CollectedBy
    //==========================================================

   /**
    * The resource operation returns the <code>Workspace</code> 
    * that collects the resource holding this link.
    * @return  AbstractResource
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return CollectedByHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = WorkspaceHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        WorkspaceHelper.write(os, resource_state);
    }
            
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * CollectedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new CollectedBy() );
    }
}
