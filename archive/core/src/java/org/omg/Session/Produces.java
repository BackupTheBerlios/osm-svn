// Sun Dec 17 16:53:23 CET 2000

package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
* The Produces link is a link exposed by an Task.  The Produces link 
* contains a reference to the AbstractResource that the Task is 
* producing.
*/

public class Produces
implements Production, StreamableValue, ValueFactory
{
    
    //==========================================
    // static
    //==========================================

   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = {
        "IDL:omg.org/Session/Produces:1.0",
    };
        
    //==========================================
    // state
    //==========================================

   /**
    * The AbstractResource that is produced by the Task holding this link.
    */
    public AbstractResource resource_state;

   /**
    * String qualifying the role of the produced abstract resource relative to the Task.
    */
    public String tag;
    

    //==========================================
    // constructors
    //==========================================
    
   /**
    * Default constructor for stream internalization.
    */
    public Produces () {
    }

   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * default tag value.
    */
    public Produces( AbstractResource resource ) {
	  this( resource, "" );
    }

   /**
    * Creation of a new Produces link based on a supplied AbstractResource and
    * tag value.
    */
    public Produces( AbstractResource resource, String tag ) {
	  this.resource_state = resource;
	  this.tag = tag;
    }

    //==========================================
    // Produces
    //==========================================

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

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return ProducesHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        resource_state = AbstractResourceHelper.read(is);
        tag = org.omg.CORBA.StringValueHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        AbstractResourceHelper.write(os, resource_state);
        org.omg.CORBA.StringValueHelper.write(os,tag);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * Produces factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Produces() );
    }
}
