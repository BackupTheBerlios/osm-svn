// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class AccessedBy
implements Access, StreamableValue, ValueFactory
{
    //============================
    // static
    //============================
    
    /**
     * Return the truncatable ids
     */
     static final String[] _ids_list =
     {
         "IDL:omg.org/Session/AccessedBy:1.0",
     };

    //============================
    // state
    //============================
    
    public User resource_state;
    
    //============================
    // constructors
    //============================
    
   /**
    * Default constructor for stream internalization.
    */
    public AccessedBy () 
    {
    }

   /**
    * Creation of a new AccessedBy link based on a supplied User.
    */
    public AccessedBy( User resource ) 
    {
	  this.resource_state = resource;
    }

    //============================
    // AccessedBy
    //============================

   /**
    * The resource operation returns the <code>User</code> that 
    * is authorized to access the Workspace holding this link.
    * @return  AbstractResource representing the user holding the right of access.
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
    }

   /**
    * Return the value TypeCode
    */
    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.Session.AccessedByHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(org.omg.CORBA.portable.InputStream is)
    {
        resource_state = org.omg.Session.UserHelper.read(is);
    }
   
   /**
    * Marshal the value into an OutputStream
    */ 
    public void _write(org.omg.CORBA.portable.OutputStream os)
    {
        org.omg.Session.UserHelper.write(os, resource_state);
    }
    
   /**
    * Return the truncatable ids
    */        
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * AccessedBy factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new AccessedBy() );
    }
}
