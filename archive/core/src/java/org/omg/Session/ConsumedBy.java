// Sun Dec 17 16:02:38 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class ConsumedBy
    implements Consumption, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //

   /**
    * The <code>Task</code> that is consuming the <code>AbstractResource</code> holding this link.
    */
    
    public Task resource_state;
    
   /**
    * The role of the <code>Task</code> relative to the resource.
    */

    public String tag;
    

    // 
    // constructors
    //

   /**
    * Construct a new non-initialized <code>ConsumedBy</code> link.  This
    * constructor is used during internalization of a new <code>ConsumedBy</code> link
    * from a serialized valuetype stream.
    */

    public ConsumedBy () {
    }

   /**
    * Construct a new <code>ConsumedBy</code> link based on a supplied 
    * <code>Task</code>.
    *
    * @param resource the <code>Task</code> that is consuming the 
    * <code>AbstractResource</code> holding this link.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */

    public ConsumedBy ( Task resource ) {
	  this( resource, "" );
    }

    
   /**
    * Construct a new <code>ConsumedBy</code> link based on a supplied 
    * <code>Task</code> and role name.
    * @param resource the <code>Task</code> that is consuming the 
    * <code>AbstractResource</code> holding this link.
    * @param tag a <code>String</code> declaring the role of the <code>Task</code> 
    * in this association.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */

    public ConsumedBy ( Task resource, String tag ) {

	  if (resource == null) throw new IllegalArgumentException("null Task reference");
        this.resource_state = resource;

	  if(tag == null) {
		this.tag = "";
	  }else {
	      this.tag = tag;
	  }
    }

    //
    // implementation of Tagged
    //
    
   /**
    * The tag method returns the string value that defines the role of a Task
    * within the scope of a consumption relationship.
    * @return String tagged consumption role
    */

    public String tag() {
	  return tag;
    }

    //
    // implementation of Link
    //
    
    /**
     * The resource operation returns the <code>Task</code> that 
     * a <code>AbstractResource</code> holding this link is consumed by.
     * @return <code>AbstractResource</code> that can be narrowed to a <code>Task</code> that 
     * is consuming the <code>AbstractResource</code> holding this link.
     */

    public AbstractResource resource() {
	  return resource_state;
    }

    //
    // implementation of Streamable
    //

   /**
    * Return the value TypeCode.
    */
    public TypeCode _type()
    {
        return org.omg.Session.ConsumedByHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream.
    */
    public void _read(InputStream is)
    {
        resource_state = TaskHelper.read( is );
	  tag = org.omg.CORBA.StringValueHelper.read( is );
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        TaskHelper.write( os, resource_state );
        org.omg.CORBA.StringValueHelper.write( os,tag );
    }
    
    //
    // implementation of ValueBase
    //


    static final String[] _ids_list =
    {
	  "IDL:omg.org/Session/ConsumedBy:1.0",
    };

    public String [] _truncatable_ids()
    {
	  return _ids_list; 
    }
    
    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ConsumedBy() );
    }

}
