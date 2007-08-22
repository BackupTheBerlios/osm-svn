// Sun Dec 17 15:21:20 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
* Consumes is a Link held by a Task that references an AbstractResource it is 
* consuming.  The inverse of this association is the Link ConsumedBy, held by 
* the consumed AbstractResource, referencing the Task that is consuming it. 
*/

public class Consumes
implements Consumption, StreamableValue, ValueFactory
{
    
    //
    //  state members
    //
    
   /**
    * The AbstractResource that is consumed by the Task holding this link.
    */

    public AbstractResource resource_state;
    
   /**
    * The role of the resource.
    */

    public String tag;
    

    // 
    // Constructors
    //

   /**
    * Construct a new non-initialized <code>Consumes</code> link.  This
    * constructor is used during internalization of a new <code>Consumes</code> link
    * from a serialized valuetype stream.
    */

    public Consumes () {
    }

   /**
    * Construct a new <code>Consumes</code> link based on a supplied 
    * <code>AbstractResource</code> and default usage role.
    * @param resource the <code>AbstractResource</code> that is consumed by the 
    * <code>Task</code> holding this link.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */

    public Consumes ( AbstractResource resource ) {
	  this( resource, "" );
    }

    
   /**
    * Construct a new <code>Consumes</code> link based on a supplied 
    * <code>AbstractResource</code> and role name.
    * @param resource the <code>AbstractResource</code> that is consumed by the 
    * <code>Task</code> holding this link.
    * @param tag a <code>String</code> declaring the role of the role of the 
    * resource in this association.
    * @exception <code>IllegalArgumentException</code> 
    * if resource is <code>null</code>.
    */

    public Consumes ( AbstractResource resource, String tag ) {

	  if (resource == null) throw new IllegalArgumentException("null AbstractResource reference");
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
    * The tag method returns the string value that defines the role of a resource
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
     * The resource operation returns the <code>AbstractResource</code> that 
     * a <code>Task</code> is consuming.
     * @return AbstractResource that this link declares a consumption dependency on.
     */

    public AbstractResource resource() {
	  return resource_state;
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return ConsumesHelper.type();
    }
    
    public void _read(InputStream is)
    {
	  resource_state = org.omg.Session.AbstractResourceHelper.read(is);
	  tag = org.omg.CORBA.StringValueHelper.read(is);
    }
    
    public void _write(OutputStream os)
    {
        org.omg.Session.AbstractResourceHelper.write(os,resource_state);
	  org.omg.CORBA.StringValueHelper.write(os,tag);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list =
    {
	  "IDL:omg.org/Session/Consumes:1.0",
    };

    public String [] _truncatable_ids()
    {
	  return _ids_list;
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Consumes() );
    }

}
