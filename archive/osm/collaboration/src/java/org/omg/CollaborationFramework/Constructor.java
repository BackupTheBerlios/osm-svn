// Thu Nov 23 07:22:02 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.CriteriaHelper;
import org.omg.CommunityFramework.GenericCriteria;
import org.omg.CommunityFramework.AgencyCriteria;
import org.omg.CommunityFramework.CommunityCriteria;
import org.omg.CommunityFramework.ExternalCriteria;
import org.omg.CommunityFramework.UserCriteria;
import org.omg.CommunityFramework.MessageCriteria;

/**
The Constructor directive directs a Collaboration implementation to create a new resource based
on the supplied criteria and associate the resource under a new named Consumption link on the
coordinating Task using the target value as the links tag value.
*/

public class Constructor
implements Directive, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
	"IDL:omg.org/CollaborationFramework/Constructor:1.0"
    };

    //==========================================================
    // state
    //==========================================================

    /**
    * The name of a Usage Link to be created and added to the
    * coordinating Task (replacing any existing usage link of the same name),
    * using the supplied criteria.
    */
    public String target;

    /**
    * An instance of Criteria describing the resource to be created.
    */
    public Criteria criteria;
    
    //==========================================================
    // constructors
    //==========================================================

    /**
    * Null argument constructor used during stream internalization.
    */
    public Constructor () {
    }

   /**
    * Creation of a new Constructor based on a supplied Configuration instance.
    */
    public Constructor( Configuration config ) 
    {
        try
	  {

         	// <!ELEMENT create (%criteria;) >
	      // <!ATTLIST create
		//   %target;
	      // >

		target = config.getAttribute("target");
		Configuration[] children = config.getChildren();
		if( children.length != 1 ) throw new RuntimeException(
		  "missing criteria element");
		Configuration c = children[0];
		String name = c.getName();

	      // <!ENTITY % criteria             
            //   "(user|generic|community|agency|encounter|processor|external|
            //    vote|engagement|collaboration)">

		if( name.equals("user") )
		{
		    criteria = new UserCriteria( c );
		}
		else if( name.equals("generic") ) 
		{
		    criteria = new GenericCriteria( c );
	      }
		else if( name.equals("message") ) 
		{
		    criteria = new MessageCriteria( c );
	      }
		else if( name.equals("community") ) 
		{
		    criteria = new CommunityCriteria( c );
	      }
		else if( name.equals("agency") ) 
		{
		    criteria = new AgencyCriteria( c );
	      }
		else if( name.equals("encounter") ) 
		{
		    criteria = new EncounterCriteria( c );
	      }
		else if( name.equals("external") ) 
		{
		    criteria = new ExternalCriteria( c );
	      }
	      else
	      {
		    criteria = new ProcessorCriteria( c );
            }
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("unable to create configured Constructor", e );
	  }
    }

    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return ConstructorHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        target = LabelHelper.read(is);
        criteria = CriteriaHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        LabelHelper.write(os, target);
        CriteriaHelper.write(os, criteria);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * Constructor factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Constructor() );
    }

}
