// Thu Nov 23 07:22:02 CET 2000

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
Map is a valuetype that defines mapping between a processor completion status and, a set of directives and action to perform given a matching completion criteria.  Map is used by CompoundTransition to declare multiple transition possibilities based on the outcome of the execution of sub-process.
*/

public class Map
implements StreamableValue, ValueFactory
{
    
    //================================================
    // static
    //================================================

    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/Map:1.0"
    };

    //================================================
    // state
    //================================================

   /**
    * ResultClass containing a boolean <code>value</value>.
    */
    public ResultClass _class;

   /**
    * ResultID containing an implementation specific int <code>value</value>.
    */
    public ResultID code;

   /**
    * A sequence of Directive valuetypes that declare modifications (rename,
    * remove, copy and move) to the associated Task usage associations
    * that will be invoked before the action is handled by the Collaboration
    * implementation.
    */
    public Directive[] directives = new Directive[0];

   /**
    * The action to invoke.
    */
    public Action action;


    //================================================
    // constructor
    //================================================
    
    /**
    * Null argument constructor used during stream internalization.
    */
    public Map( ){}

   /**
    * Creation of a new Map based on the supplied configuration.
    */
    public Map( CollaborationModel model, Configuration conf )
    {
        List list = new LinkedList();
        try
	  {
		this._class = new ResultClass( conf.getAttributeAsBoolean("class", true));
		this.code = new ResultID( conf.getAttributeAsInteger("code", 0));
		Configuration[] children = conf.getChildren();
		for( int i=0; i<children.length; i++ )
	      {
		    Configuration child = children[i];
		    String name = child.getName();

		    if( action == null )
		    {
		        if( name.equals( "initialization" )| 
			    name.equals("transition")| 
			    name.equals("local")| 
			    name.equals("termination") )
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

		    if( name.equals("copy") )
		    {
                    list.add( new Duplicate( child ));
		    }
		    else if( name.equals( "move" ) )
		    {
                    list.add( new Move( child ));
		    }
		    else if( name.equals( "remove" ) )
		    {
                    list.add( new Remove( child ));
		    }
		    else if( name.equals( "create" ) )
		    {
                    list.add( new Constructor( child ));
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

    //================================================
    // implementation
    //================================================

    public TypeCode _type()
    {
        return MapHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        _class = ResultClassHelper.read(_is);
        code = ResultIDHelper.read(_is);
        directives = DirectivesHelper.read(_is);
        action = ActionHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        ResultClassHelper.write(_os, _class);
        ResultIDHelper.write(_os, code);
        DirectivesHelper.write(_os, directives);
        ActionHelper.write(_os, action);
    }
    
    public String[] _truncatable_ids() { return _ids_list; }
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Map() );
    }
}
