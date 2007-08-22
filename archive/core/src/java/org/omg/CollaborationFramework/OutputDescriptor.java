// Thu Nov 23 07:22:02 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;

/**
* OutputDescriptor defines the production relationships that will be 
* established by the processor on the controlling Task that it is 
* associated to under the ControlledBy link.
*/

public class OutputDescriptor
implements UsageDescriptor, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/OutputDescriptor:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The tag identifying the role of the output
    */
    public String tag;

   /**
    * The type of output argument.
    */
    public TypeCode type;
    
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public OutputDescriptor( )
    {
    }

   /**
    * Creation of a output descriptor based on a supplied Configuration instance.
    */
    public OutputDescriptor( Configuration conf )
    {
	  if( conf == null ) throw new RuntimeException("null configuration argument");
        try
	  {
            tag = conf.getAttribute("tag","");
	      type = ORB.init().create_interface_tc( 
			conf.getAttribute("type",""), "" );
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("Failed to create configured output usage decriptor.", e );
	  }
    }

    //==========================================================
    // OutputDescriptor
    //==========================================================

   /**
    * Return the value TypeCode
    */
    public TypeCode getType() {
        return type;
    }

   /**
    * Returns the string IDL identifier of the type of resource that will be bound
    * as an output of a process exposing this descriptor.
    */
    public String getID() {
	  String result;
        try{
	      result = getType().id();
	  } catch (Exception e) {
	      result = "unknown";
        }
        return result;
    }

   /**
    * Returns the  role identifier of the resource that will be bound
    * as an output of a process exposing this descriptor.
    */
    public String getTag() {
        return tag;
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return OutputDescriptorHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        tag = is.read_string();
        type = org.omg.CollaborationFramework.TypeCodeHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        os.write_string(tag);
        org.omg.CollaborationFramework.TypeCodeHelper.write(os,type);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * OutputDescriptor factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new OutputDescriptor() );
    }

}
