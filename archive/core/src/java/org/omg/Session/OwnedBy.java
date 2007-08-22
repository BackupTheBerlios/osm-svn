// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;


public class OwnedBy
implements Ownership, StreamableValue, ValueFactory
{
    
    //==========================================
    // static
    //==========================================

   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list =
    {
        "IDL:omg.org/Session/OwnedBy:1.0",
    };
        
    //==========================================
    // state
    //==========================================
    
    public User resource_state;


    //==========================================
    // constructor
    //==========================================

   /**
    * Default constructor for stream internalization.
    */
    public OwnedBy () 
    {
    }

   /**
    * Creation of a new OwnedBy link based on a supplied User.
    */
    public OwnedBy( User resource ) 
    {
	  this.resource_state = resource;
    }

    //==========================================
    // OwnedBy
    //==========================================

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * can be narrowed to a <code>User</code> that is the owner of the 
    * <code>Task</code> holding this link.
    * @return  AbstractResource representing the owner of the Task.
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
    }

    //
    // implementation of Streamable
    //
    
   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return OwnedByHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = UserHelper.read( is );
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        UserHelper.write( os, resource_state );
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * OwnedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new OwnedBy() );
    }

}
