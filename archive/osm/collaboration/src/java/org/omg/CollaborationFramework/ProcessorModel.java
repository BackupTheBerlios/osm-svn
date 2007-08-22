
package org.omg.CollaborationFramework;

import java.io.Serializable;
import java.util.LinkedList;
import java.awt.Color;
import javax.swing.Icon;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CommunityFramework.Model;
import org.omg.CommunityFramework.Control;

public class ProcessorModel extends Control
implements Model
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/ProcessorModel:1.0",
    };

    //==========================================================
    // state
    //==========================================================
    
   /**
    * Array of UsageDescriptor constraints that form part of the 
    * <code>ProcessorModel</code>.
    */
    public UsageDescriptor[] usage;

    //==========================================================
    // constructors
    //==========================================================

   /**
    * Default constructor for stream internalization.
    */
    public ProcessorModel( ){}

   /**
    * Creation of a processor model based on a supplied Configuration instance.
    */
    public ProcessorModel( Configuration conf )
    {
        super( conf );
        try
	  {
            Configuration[] inputs = conf.getChildren("input");
            Configuration[] outputs = conf.getChildren("output");

	      int j = inputs.length + outputs.length;
	      usage = new UsageDescriptor[j];

            int k = inputs.length;
	      for( int i=0; i<k; i++ )
            {
		    usage[i] = new InputDescriptor( inputs[i] );
            }

	      //int m = k-1;
		int m = k;
	      for( int i=0; i<outputs.length; i++ )
            {
		    usage[m + i] = new OutputDescriptor( outputs[i] );
            }
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException("Failed to create a configured processor model.", e );
	  }
    }

    //==========================================================
    // implementation
    //==========================================================

   /**
    * Return the value TypeCode
    */
    public TypeCode _type()
    {
        return ProcessorModelHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream is)
    {
        super._read(is);
        usage = UsageDescriptorsHelper.read(is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream os)
    {
        super._write(os);
        UsageDescriptorsHelper.write(os, usage);
    }
    
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }
    
   /**
    * ProcessorModel factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new ProcessorModel() );
    }

    // 
    // implementation of Item
    //
    
    /**
    * A ProcessorModel is a base type to several derived model types
    * (VotingModel, EngagementModel and CollaborationModel).  
    * ProcessorModel is presented as set of views including the 
    * Control view and a view of a set of Usage declarations
    * (resource input and output declarations).
    */
/*
    public LinkedList getViews( ) {
        if( views == null ) {
		views = new LinkedList();
		views.add( getUsageView());
        }
        return views;
    }

    public JScrollPane getUsageView() {

	  if( usageView != null ) return usageView;

	  UsageTable usageTable = new UsageTable( );
	  TableColumnModel columns = new DefaultTableColumnModel();

	  // type column
	  TableColumn type = new TableColumn(0, 40, new CellRenderer(), null );
	  type.setHeaderValue("");
	  type.setPreferredWidth( 40 );
	  type.setMaxWidth( 40 );
	  type.setMinWidth( 40 );
	  type.setResizable( false );
	  columns.addColumn( type );

	  // direction column
	  TableColumn direction = new TableColumn(1, 100, new CellRenderer(), null );
	  direction.setHeaderValue("Direction");
	  direction.setPreferredWidth( 100 );
	  direction.setMinWidth( 80 );
	  columns.addColumn( direction );

	  // tag column
	  TableColumn tag = new TableColumn(2, 300, new CellRenderer(), null );
	  tag.setHeaderValue("Tag");
	  tag.setPreferredWidth( 300 );
	  tag.setMinWidth( 100 );
	  columns.addColumn( tag );

	  // the table
	  JTable table = new JTable(usageTable, columns ); 
	  table.getTableHeader().setReorderingAllowed(false);
	  table.setRowHeight(35);

	  // scroll pane container
	  usageView = new JScrollPane(table);
        usageView.setName("Usage");
        usageView.getViewport().setBackground( Color.white );
	  return usageView;
    }

    private class UsageTable extends AbstractTableModel {

        private UsageTable ( ) {
		super();
        }

        public int getColumnCount() { 
	 	return 3;
	  }

	  // direction, tag, type, required, implied
 	  public int getRowCount() { 
		return java.lang.reflect.Array.getLength(usage); 
	  }

	  public Object getValueAt(int row, int col) { 

		Object result = "";
		UsageDescriptor u = usage[row];
		boolean input = ( u instanceof InputDescriptor );

	      switch(col){
		    case 0 : return u;
		    case 1 : 
			  if( input ) {
			      result = "INPUT";
		        } else {
			      result = "OUTPUT";
		        }
			  break;
		     case 2: return u.getTag();
		     default: 
		     break;
            }
		return result;		    
	  }
    }


    private class CellRenderer extends DefaultTableCellRenderer {

        private JLabel panel;

	  public CellRenderer()
	  {
	      super();
	  }

	  public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c)
	  {
		JLabel panel;
		if( c != 0 ) {
		    panel = (JLabel) super.getTableCellRendererComponent( t, v, s, f, r, c );
		    panel.setBorder( new EmptyBorder( 0, 5, 0, 5 ));
            } else {
                panel = new JLabel();
		    UsageDescriptor u = (UsageDescriptor) v;
	 	    String id = u.getID();
		    panel.setIcon( Utility.iconFromIdentifier( id ) );
	 	    panel.setToolTipText( id );
		    panel.setBorder( new EmptyBorder( 2, 5, 2, 5 ));
		}
		return panel;
        }
    }
*/
}
