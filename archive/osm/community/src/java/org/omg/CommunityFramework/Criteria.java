
package org.omg.CommunityFramework;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Vector;
import java.awt.Component;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JComponent;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CosLifeCycle.NVP;
import org.omg.CosLifeCycle.CriteriaHelper;

/**
 * Criteria is an abstract interface supported by valuetypes that define 
 * factory creation criteria for concrete resource types defined within 
 * Community and Collaboration frameworks.  A Criteria specialisation is 
 * defined for each concrete resource type (refer ResourceFactory Required 
 * Criteria Support).  ExternalCriteria is a special case of Criteria used 
 * to describe a reference to an external artefact (such as an XML document) 
 * that can be resolved in an implementation specific manner.
 */

public class Criteria extends Control
implements StreamableValue, ValueFactory
{
    //==========================================================
    // static
    //==========================================================

    private static final String criteriaIconPath = "org/omg/CommunityFramework/criteria.gif";

   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/Criteria:1.0",
    };

    //==========================================================
    // state
    //==========================================================


   /**
    * Implementation specific criteria used as supplementary information 
    * by a ResourceFactory implementation. 
    */
    public NVP[] values;

    ORB orb = ORB.init();

    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public Criteria () 
    {
    }
    
   /**
    * Creation of a new Criteria based on a supplied Configuration instance.
    */
    public Criteria ( Configuration conf ) 
    {
	  super( conf );
        try
	  {
		Configuration[] context = conf.getChildren("context");
            values = new NVP[ context.length ];
		for( int i=0; i< context.length; i++ )
		{
		    values[i] = createNVP( context[i] );
	      }
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException(
			"Failed to configure criteria.", e );
	  }
    }

   /**
    * Creation of a new Criteria based on a label and a description.
    */
    public Criteria( String label, String note ) 
    {
	  this( label, note, new NVP[0] );
    }

   /**
    * Creation of a new Criteria based on a label and a description and NVP sequence.
    */
    public Criteria( String label, String note, NVP[] values ) 
    {
	  super( label, note );
        this.values = values;
    }

    //==========================================================
    // impementation
    //==========================================================

    private NVP createNVP( Configuration conf ) throws Exception
    {
        String name = conf.getAttribute( "name", "" );
        Any value = orb.create_any();

	  Configuration[] children = conf.getChildren( "context" );
	  if( children.length == 0 )
	  {
		//
		// this is a terminal context object
		//

		value.insert_string( conf.getAttribute("value") );
	  }
	  else
	  {

		//
		// this is a nested context object
		//

		Vector v = new Vector();
		for( int i=0; i<children.length; i++ )
		{
		    v.add( createNVP( children[i] ));
		}
		NVP[] array = (NVP[]) v.toArray( new NVP[0] );
		CriteriaHelper.insert( value, array );
	  }
        return new NVP( name, value );
    }

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return CriteriaHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        super._read(is);
        values = ArgumentsHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        super._write(os);
        ArgumentsHelper.write(os, values);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * Criteria factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) 
    {
        return is.read_value( new Criteria() );
    }
}
