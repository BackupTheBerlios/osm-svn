// Thu Nov 23 07:22:01 CET 2000

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
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.CriteriaHelper;
import org.omg.CommunityFramework.ExternalCriteria;


/**
 * CompoundTransition introduces the notion of a
 * transition where the target is defined by the result 
 * of the execution of a subsidiary processor. An
 * implementation of Collaboration on triggering a 
 * CompoundTransition, uses a factory Criteria
 * instance defined under the criteria field to establish 
 * a new sub-processor to the current processor. he result 
 * of the sub-process execution is exposed by an instance 
 * of Completion (refer Completion valuetype). Completion 
 * contains a result identifier (refer ResultClass and 
 * ResultID). This identifier is used to establish the 
 * Action to apply based on a result to action mapping.
 */

public class CompoundTransition
implements Action, StreamableValue, ValueFactory
{
    
    //================================================
    // static
    //================================================

    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/CompoundTransition:1.0",
    };

    //================================================
    // state
    //================================================

    /**
    * An instance of Criteria that is to be used as the criteria for sub-process establishment 
    * under a ResourceFactory.
    */
    public Criteria criteria;

    /**
    * A sequence of Map instances defining the actions to be applied in the event
    * of an identified result status. An implementation is responsible for
    * ensuring a complete mapping of all possible sub-process result states to
    * actions within the parent processor prior to initialization (refer verify
    * operation on Collaboration interface).
    */
    public Map[] mapping = new Map[0];
    
    //================================================
    // constructor
    //================================================

   /**
    * Null argument constructor used during stream internalization.
    */
    public CompoundTransition() {}

   /**
    * Creation of a new CompoundTransition based on a supplied configuration.
    */
    public CompoundTransition( CollaborationModel model, Configuration config ) 
    {

	  // <!ENTITY % compound "((external|process|collaboration|vote|engagement), (on+))">
	  // <!ENTITY % transitional "(initialization|transition|local|termination)">
	  // <!ENTITY % action "(%transitional;|referral|%compound;)">
	  // <!ENTITY % directive.content "((create|copy|move|remove)*)" >
	  // <!ELEMENT on (%directive.content;,%action;) >
	  // <!ENTITY % compound "((external|process|collaboration|vote|engagement), (on+))">

	  // The critera state member corresponds to a child element of external,
        // process, collaboration, vote or enagement.  In addition, there may be
        // any number of 'on' elements, each describing a conditional directive.

	  try
	  {
		Configuration[] children = config.getChildren();
		List list = new LinkedList();
		for( int i=0; i<children.length; i++ )
	      {
		    Configuration child = children[i];
		    String name = child.getName();

		    if( name.equals("on"))
		    {
			  list.add( new Map( model, child ));
		    }
		    else
		    {
		        if( criteria == null )
		        {
                        if( name.equals("external"))
			      {
				    criteria = new ExternalCriteria( child );
		            }
		  	      else if( name.equals("processor") | name.equals("collaboration") 
				  | name.equals("vote") | name.equals("enagement") )
			      {
				    criteria = new ProcessorCriteria( child );
		            }
			  }
		    }
	      }
            mapping = (Map[]) list.toArray( new Map[0] );
		list = null;
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("failed to configure trigger", e );
	  }
    }

    //================================================
    // implementation
    //================================================

    public TypeCode _type()
    {
        return CompoundTransitionHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        criteria = CriteriaHelper.read(_is);
        mapping = MappingHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        CriteriaHelper.write(_os, criteria);
        MappingHelper.write(_os, mapping);
    }
        
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new CompoundTransition() );
    }
}
