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

import net.osm.dpml.DPML;

/**
 * Declaration of an input resource (consumed) that a processor requires on its associated task.
 */

public class InputDescriptor 
implements UsageDescriptor, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/InputDescriptor:2.1",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The tag value of a usage consumption link to be established
    * on a processor.
    */
    public String tag;

   /**
    * True if the consumption association is mandatory, else the association
    * is optional.
    */
    public boolean required;

   /**
    * If true, an existing consumption relationship with a corresponding tag name
    * can be used to satisfy the constraint.  If false, the existing association must be 
    * replaced with a new association.
    */
    public boolean implied;

   /**
    * The type of resource to be supplied under the usage relationship. 
    */
    public TypeCode type;

   /**
    * Optional Criteria that can be used as a factory argument to automatically 
    * instantiate a input argument value.
    * @since 2.1
    */
    public org.omg.CommunityFramework.Criteria criteria;
    
    //==========================================================
    // constructors
    //==========================================================
    
    /**
    * Null argument constructor used during stream internalization.
    */
    public InputDescriptor( ){}

   /**
    * Creation of a input descriptor based on a supplied Configuration instance.
    */
    public InputDescriptor( Configuration conf )
    {
	  if( conf == null ) throw new RuntimeException("Null configuration.");
        try
	  {
            tag = conf.getAttribute("tag","");
	      required = conf.getAttributeAsBoolean("required", false );
	      implied = conf.getAttributeAsBoolean("implied", false );
	      type = ORB.init().create_interface_tc( 
			conf.getAttribute("type",""), "" );

		Configuration[] children = conf.getChildren();
		if( children.length > 0 )
		{
		    criteria = DPML.buildCriteriaElement( children[0] );
		}
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("Failed to configure a input usage decriptor.", e );
	  }
    }

    //==========================================================
    // InputDescriptor
    //==========================================================

   /**
    * Return the TypeCode of the resource required under this usage descriptor.
    * @return TypeCode
    */
    public TypeCode getType() {
        return type;
    }

   /**
    * Return the TypeCode identifier as a String.
    * @return String
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
    * Return the tag name that this usage descriptor is defining.
    * @return String corresponding to the usage tag
    */
    public String getTag() {
        return tag;
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return InputDescriptorHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        tag = is.read_string();
        required = is.read_boolean();
        implied = is.read_boolean();
        type = org.omg.CollaborationFramework.TypeCodeHelper.read(is);
	  criteria = org.omg.CommunityFramework.CriteriaHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        os.write_string(tag);
        os.write_boolean(required);
        os.write_boolean(implied);
        org.omg.CollaborationFramework.TypeCodeHelper.write(os,type);
	  org.omg.CommunityFramework.CriteriaHelper.write(os,criteria);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * InputDescriptor factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new InputDescriptor() );
    }

}
