// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class Accesses
implements Access, StreamableValue, ValueFactory
{
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/Session/Accesses:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The Workspace that a User Accesses.
    */
    public Workspace resource_state;
    
    //==========================================================
    // constructors
    //==========================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public Accesses() 
    {
    }

   /**
    * Creation of a new Accesses link based on a supplied Workspace.
    */
    public Accesses( Workspace resource ) {
	  this.resource_state = resource;
    }

    //==========================================================
    // Accesses
    //==========================================================

   /**
    * The resource operation returns the <code>Workspace</code> that 
    * admistered by the <code>User</code> holding this link.
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
        return AccessesHelper.type();
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
    * Accesses factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Accesses() );
    }

}
