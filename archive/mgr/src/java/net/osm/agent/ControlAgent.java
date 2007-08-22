
package net.osm.agent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Action;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CommunityFramework.Control;

import net.osm.util.IconHelper;
import net.osm.shell.View;
import net.osm.shell.ScrollView;
import net.osm.shell.StaticFeature;

/**
 * The <code>ControlAgent</code> class is a base class for all
 * agents hosting valuetypes based on the <code>org.omg.CommunityFramework.Control</code>
 * specification.  A control exposes a label and a descrive note and is used extensively within 
 * the Community and Collaboration frameworks within policy defintions.
 */
public class ControlAgent extends ValueAgent
{

    private static String path = "net/osm/agent/image/control.gif";
    private static ImageIcon icon = IconHelper.loadIcon( path );

   /**
    * The object reference to the Control that this agents 
    * represents.
    */
    protected Control control;

   /**
    * The graphical representation of the control features.
    */
    private View propertyView;

   /**
    * Linked list of views incorporating supertype views.
    */
    private LinkedList list;

    private LinkedList properties;

    private List features;


    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    * Default constructor.
    */
    public ControlAgent(  )
    {
    }

   /**
    * Creation of a new ControlAgent based on a supplied Control instance.
    *
    * @param instance of Control
    * @see org.omg.CommunityFramework.Control
    */
    public ControlAgent( Logger logger, Object object )
    {
	  super( object );
	  enableLogging( logger );
        try
        {
            this.control = (Control) object;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ControlAgent/setPrimary - bad type.");
        }
    }

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the control instance this agent is wrapping.
    */
    public void setPrimary( Object value ) 
    {
        super.setPrimary( value );
	  try
	  {
		this.control = (Control) value;
		putValue( Action.NAME, getName() );
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ControlAgent/setPrimary - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns the label of the control.
    * @see #getLabel
    */
    public String getName()
    {
        return getLabel();
    }

   /**
    * Returns the label of the control.
    */
    public String getLabel()
    {
	  return control.label;
    }

   /**
    * Returns the note of the control.
    */
    public String getNote()
    {
	  return control.note;
    }

    //=========================================================================
    // Entity implementation
    //=========================================================================

   /**
    * Returns a list of <code>Features</code> instances to be presented under 
    * the Model dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        if( features != null ) return features;
        List list = super.getFeatures();
	  try
	  {
	      list.add( new StaticFeature("label", getLabel() ));
	      list.add( new StaticFeature("note", getNote() ));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
    }
}
