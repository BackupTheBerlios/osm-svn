// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;


public class Owns
implements Ownership, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
   /**
    * The Task owned by the User holding this link. 
    */

    public Task resource_state;
    

    //
    // constructors
    //
    
   /**
    * Default constructor for stream internalization.
    */

    public Owns () {
    }

   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * default tag value.
    */

    public Owns( Task resource ) {
	  this.resource_state = resource;
    }

   /**
    * The resource operation returns the <code>Task</code> that 
    * is owned by the <code>User</code> holding this Link instance.
    * @return  AbstractResource narrowable to Task that is owned by the user.
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read( org.omg.CORBA.portable.InputStream is )
    {
        resource_state = org.omg.Session.TaskHelper.read(is);
    } 

   /**
    * Marshal the value into an OutputStream
    */
    public void _write( org.omg.CORBA.portable.OutputStream os )
    {
        org.omg.Session.TaskHelper.write(os,resource_state);
    }

   /**
    * Return the value TypeCode
    */
    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.Session.OwnsHelper.type();
    }
    
    //
    // implementation of ValueBase
    //
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list =
    {
        "IDL:omg.org/Session/Owns:1.0",
    };

    public String [] _truncatable_ids()
    {
	  return _ids_list;
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Owns() );
    }

}
