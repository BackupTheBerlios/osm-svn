// Thu Nov 23 07:22:01 CET 2000

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
* Transition contains a state field named usage that contains a UsageDescriptor value. The value
* allows the definition of input and/or output statements (refer UsageDescriptor) during a
* collaborative process execution as a consequence of changes in the collaborative state. A second
* state field named transitional contains a single valuetype derived from the abstract Transitional
* valuetype.
* <p>
* Four types of Transitional valuetypes are defined,
* <ul>
* <li>Initialization, declares a possible initial active-state target
* <li>SimpleTransition, declares a potential a state transition
* <li>LocalTransition, declares a potential transition from the current state to the current state,
* during which side effects such as timeout resetting and Usage references may be modified.
* <li>TerminalTransition, signals termination of the running state of the processor and declares a
* successful or failure result.
* </ul>
*/

public class Transition
implements Action, StreamableValue, ValueFactory
{
    
    //==========================================================
    // state
    //==========================================================

   /**
    * Declaration of the transitional operator – one of Initialization, SimpleTransition,
    * LocalTransition or TerminalTransition.
    */
    public Transitional transitional;

   /**
    * Contains a sequence of UsageDescriptor instance (input and
    * output declarations) that define required or operational arguments to
    * the Collaboration apply operation when the state containing the usage
    * declaration is active.
    */
    public UsageDescriptor[] usage;
    
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Null argument constructor used during stream internalization.
    */
    public Transition(){}

   /**
    * Creates a CompoundTransition based on a supplied CollaborationModel and configuration.
    * @param model CollaborationModel containing the Transition
    * @param node Node - DOM element defining the transition features
    */
    public Transition( CollaborationModel model, Configuration config ) 
    {
	  // <!ENTITY % transitional "(initialization|transition|local|termination)">
   	  // <!ELEMENT initialization (input*) >
	  // <!ELEMENT transition (input*) >
	  // <!ELEMENT local (input*) >
	  // <!ELEMENT termination (output*)  >

	  // collect the usage preconditions (input and outputs and hold these
        // here then create the transitional instance

	  try
	  {
	      List list = new LinkedList();
	      Configuration[] children = config.getChildren();
	      for( int i=0; i<children.length; i++ )
	      {
	          Configuration child = children[i];
		    String name = child.getName();
		    if( name.equals("input"))
		    {
		        list.add( new InputDescriptor( child ) );
		    }
	          else if( name.equals("output"))
		    {
		        list.add( new OutputDescriptor( child ) );
		    }
		}
            usage = (UsageDescriptor[]) list.toArray( new UsageDescriptor[0] );

		String name = config.getName();
		if(name.equals("initialization"))
		{
		    transitional = new Initialization( );
	      }
	      else if( name.equals("transition") )
	      {
	  	    transitional = new SimpleTransition( model, config );
	      }
	      else if( name.equals("local") )
	      {
		    transitional = new LocalTransition( config );
	      }
		else if( name.equals("termination") )
		{
		    transitional = new TerminalTransition( config );
		}
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("failed to configure transition", e );
	  }
    }
 
    //==========================================================
    // implementation
    //==========================================================
    
    public TypeCode _type()
    {
        return TransitionHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        transitional = TransitionalHelper.read(_is);
        usage = UsageDescriptorsHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        TransitionalHelper.write(_os, transitional);
        UsageDescriptorsHelper.write(_os, usage);
    }
        
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Transition:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Transition() );
    }


}
