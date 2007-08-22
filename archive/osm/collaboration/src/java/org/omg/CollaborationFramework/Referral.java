
package org.omg.CollaborationFramework;

import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * A Referral is an action that redirects execution to another 
 * existing action object.
 */
public class Referral
implements Action, StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Referral:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The action within the scope of the containing model to 
    * redirect action execution. 
    */
    public Action action;

   /**
    * A sequence of directive statements that declare modifications
    * to be applied to the associated task's input and output 
    * usage associations.
    */
    public Directive[] directives;

    private String target;
    
    //==========================================================
    // constructors
    //==========================================================

   /**
    * Default serialization constructor.
    */
    public Referral( ){}

   /**
    * Creation of a new Referral based on a supplied configuration and model.
    */
    public Referral( Configuration config )
    {
	  // <!ENTITY % directive.content "((create|copy|move|remove)*)" >
        // <!ELEMENT referral %directive.content; >
	  // <!ATTLIST referral
	  //	action IDREF #REQUIRED
	  // >

	  try
	  {
		target = config.getAttribute("action");
		Configuration[] children = config.getChildren();
		List list = new LinkedList();
		for( int i=0; i<children.length; i++ )
	      {
		    Configuration child = children[i];
		    String name = child.getName();
		    if( name.equals("create") )
		    {
		        list.add( new Constructor( child ) );
		    }
		    else if( name.equals("copy") )
		    {
		        list.add( new Duplicate( child ) );
		    }
		    else if( name.equals("move") )
		    {
		        list.add( new Move( child ) );
		    }
		    else if( name.equals("remove") )
		    {
		        list.add( new Remove( child ) );
		    }
	      }
            directives = (Directive[]) list.toArray( new Directive[0] );
		list = null;
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("failed to create referral", e );
	  }
    }

    //==========================================================
    // implementation
    //==========================================================

   /**
    * The initalization phase is invoked by the root collaboration 
    * model after all actions have been established.  During initlaization
    * the references to actions are resolved.
    */
    protected void initalize( CollaborationModel model ) throws Exception
    {
	  if( target == null ) throw new NullPointerException("target has not been set");
        action = model.lookupAction( target );
        if( action == null ) throw new Exception(
		  "unable to resolve an action matching '" + target + "'");
    }
    
    public TypeCode _type()
    {
        return ReferralHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        action = ActionHelper.read(_is);
        directives = DirectivesHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        ActionHelper.write(_os, action);
        DirectivesHelper.write(_os, directives);
    }
        
    public String[] _truncatable_ids() { return _ids_list; }

    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Referral() );
    }

}
