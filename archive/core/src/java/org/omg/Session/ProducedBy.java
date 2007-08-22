// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

public class ProducedBy
    implements Production, StreamableValue, ValueFactory 
{
    
   /**
    * The Task that is producing the AbstractResource holding this link. 
    */
    
    public Task resource_state;
    
   /**
    * String qualifying the role of the production association.
    */

    public String tag;
    

    //
    // constructors
    //
    
   /**
    * Default constructor for stream internalization.
    */

    public ProducedBy () {
    }

   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * default tag value.
    */

    public ProducedBy( Task resource ) {
	  this( resource, "" );
    }


   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * tag value.
    */

    public ProducedBy( Task resource, String tag ) {
	  this.resource_state = resource;
	  this.tag = tag;
    }


   /**
    * The tag method returns the string value the defines the role of a resource
    * within the scope of a usage relationship.
    * @return  String tagged usage role
    */
    public String tag()
    {
        return this.tag;
    }

   /**
    * The resource operation returns the <code>AbstractResource</code> that 
    * is produced by the <code>Task</code> holding this Link instance.
    * @return  AbstractResource produced by the Task.
    */
    public AbstractResource resource()
    {
	  return this.resource_state;
    }

    //
    // implementation of Streamable
    //
    
    public org.omg.CORBA.TypeCode _type()
    {
        return ProducedByHelper.type();
    }
    
    public void _read(InputStream is)
    {
	  try
	  {
            resource_state = TaskHelper.read(is);
	      tag = org.omg.CORBA.StringValueHelper.read(is);
	  }
	  catch( Throwable e )
	  {
	      e.printStackTrace();
            throw new RuntimeException("more read problems", e );
        }
    }
    
    public void _write(OutputStream os)
    {
        TaskHelper.write( os, resource_state);
        org.omg.CORBA.StringValueHelper.write(os,tag);
    }
    
    //
    // implementation of ValueBase
    //
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list =
    {
	  "IDL:omg.org/Session/ProducedBy:1.0",
    };

    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ProducedBy() );
    }

}
