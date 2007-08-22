
package org.omg.CollaborationFramework;

import java.io.Serializable;
import java.util.LinkedList;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CommunityFramework.Criteria;

/**
 * Declaration of processor consumption and production usage constraints within 
 * a contained ProcessorModel. An implementation of ResourceFactory is responsible 
 * for assessing the type of Model contained within a ProcessorCriteria to determine 
 * the type of Process to create. For example, a ProcessorCriteria containing an instance of
 * CollaborationModel will return a type of CollaborationProcessor. 
 */

public class ProcessorCriteria extends Criteria
{

    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/ProcessorCriteria:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * The model that qualifies and controls processor behaviour. 
    */
    public ProcessorModel model;
    
    //==========================================================
    // constructors
    //==========================================================
    
   /**
    * Null argument constructor used during stream internalization.
    */
    public ProcessorCriteria( ){}

   /**
    * Creation of a processor criteria based on a supplied Configuration instance.
    */
    public ProcessorCriteria( Configuration conf ) 
    {
        super( conf );
        try
	  {
            String name = conf.getName();
		if( name.equals( "collaboration" ) )
		{
		    model = new CollaborationModel( conf );
	      }
		else if( name.equals( "vote" ) )
		{
		    model = new VoteModel( conf );
	      }
		else if( name.equals( "engagement" ) )
		{
		    model = new EngagementModel( conf );
	      }
	      else
		{
		    model = new ProcessorModel( conf );
	      }
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException(
		  "Failed to create a configured processor criteria.", e );
	  }
    }
    
   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return ProcessorCriteriaHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        super._read(is);
        model = ProcessorModelHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        super._write(os);
        ProcessorModelHelper.write(os, model);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * ProcessorCriteria factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ProcessorCriteria() );
    }
}
