
package org.omg.CollaborationFramework;

import java.io.Serializable;
import java.awt.Component;
import java.util.List;
import java.util.LinkedList;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Control;


/**
 * A Trigger is a valuetype contained by a State that is used to define an activation constraint
 * (referred to as a guard), declarations of implementation actions to fire before action execution
 * (referred to as directives), the action that a collaboration implementation applies to the
 * collaborative state, and an action priority. Trigger labels are candidate arguments to the
 * Collaboration apply operation when the State containing the Trigger is within the active state
 * path. The value of guard is a valuetype that qualifies the functional role of the trigger. Two types
 * of Guard are defined. A Clock, representing a timeout condition that is automatically armed by a
 * Collaboration implementation whenever the containing trigger is a candidate (within the active
 * state path). A second type of Guard is a Launch that contains a mode constraint (one of
 * INITIATOR, RESPONDENT or PARTICIPANT) and a reference to a role that qualifies accessibility of
 * the Trigger relative to Members of an associated Encounter. A Trigger containing a Clock is
 * managed by a Collaboration implementation. A Trigger containing a Launch may be explicitly
 * referenced by a client through the apply operations on the Collaboration interface providing the
 * client meets any mode and role constraints associated with the Trigger.
 */
public class Trigger
extends Control
{
    
    //
    //  state members
    //
    
   /**
    * An implementation of apply is responsible for queuing apply requests
    * relative to trigger priority and invocation order. Higher priority
    * triggers will be fired ahead of lower priority triggers irrespective of apply
    * invocation order. An implementation is responsible for retractions of apply
    * requests following the disassociation of a containing state from the active
    * state path.
    */
    public int priority;

   /**
    * An instance of Clock or Launch that defines the Trigger activation policy.
    */
    public Guard guard;

   /**
    * A sequence of Directive valuetypes that declare modifications (rename,
    * remove, copy and move) to the associated Task usage associations
    * that will be invoked before the action is handled by the Collaboration
    * implementation.
    */
    public Directive[] directives;

   /**
    * An Action valuetype that describes the action to take following client
    * invocation of the apply operation. Argument to apply reference the label
    * that corresponds to the Trigger label state filed inherited from Control.
    */
    public Action action;
  
    
    //
    // constructors
    //
    
   /**
    * Null argument constructor used during stream internalization.
    */
    public Trigger(){}

   /**
    * Creation of a new Trigger instance based on a supplied 
    * configuration argument.
    * @param conf the configuration to apply to the new instance
    */
    public Trigger( CollaborationModel model, Configuration conf ) 
    {
	  super( conf );
	  try
	  {
	      // <!ELEMENT trigger (%guard;,%directive.content;,%action;)>

	      this.priority = conf.getAttributeAsInteger("priority",0);
		Configuration[] children = conf.getChildren();
		List list = new LinkedList();

		for( int i=0; i<children.length; i++ )
	      {
		    Configuration child = children[i];
		    String name = child.getName();

		    // resolve guard
		    // <!ENTITY % guard "(launch|clock)">

		    if( guard == null )
		    {
		        if( name.equals("launch") )
		        {
		            guard = new Launch( model, child );
		        }
		        else if( name.equals("clock") )
		        {
		            guard = new Clock( child );
		        }
		    }
		    else if( action == null )
		    {

			  // could be a Transition, Referral or Compound
	              // <!ENTITY % action "(%transitional;|referral|%compound;)">
	              // <!ENTITY % transitional "(initialization|transition|local|termination)">
	              // <!ENTITY % compound "((external|process|collaboration|vote|engagement), (on+))">

                    if(name.equals("initialization")| 
			    name.equals("transition")| 
			    name.equals("local")| 
			    name.equals("termination")) 
			  {
                	      action = new Transition( model, child );
			  }
                    else if(name.equals("referral"))
                    {
                        action = new Referral( child );
                    }
                    else if( name.equals("external")|
			    name.equals("process")| 
		  	    name.equals("vote")| 
			    name.equals("engagement")| 
			    name.equals("collaboration"))
		        {
                        action = new CompoundTransition( model, child );
                    }
                }
		    else if( name.equals("create") )
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
	      throw new RuntimeException("failed to configure trigger", e );
	  }
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return TriggerHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        super._read(_is);
        priority = _is.read_long();
        guard = GuardHelper.read(_is);
        directives = DirectivesHelper.read(_is);
        action = ActionHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        super._write(_os);
        _os.write_long(priority);
        GuardHelper.write(_os, guard);
        DirectivesHelper.write(_os, directives);
        ActionHelper.write(_os, action);
    }

    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Trigger:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }


    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Trigger() );
    }

}
