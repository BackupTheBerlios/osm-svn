
package net.osm.shell.control.activity;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import javax.security.auth.x500.X500Principal;

import net.osm.shell.control.field.LabelPanel;
import net.osm.util.IconHelper;
import net.osm.util.ExceptionHelper;


/**
 * The <code>DiodeDialog</code> class is a modal dialog
 * containing a set of diode lights indicating the progress of 
 * an activitiy through a set of stages.  The class contains
 * a title, status message, diode light panel and cancel 
 * button.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class DiodeDialog extends AbstractAction implements Closable, ActivityCallback
{

    //==========================================================
    // static
    //==========================================================

    private String dialogTitle = "Key Generation";

    private static final String radioGrayPath = "net/osm/shell/control/activity/image/gray.gif";
    private static final String radioGreenPath = "net/osm/shell/control/activity/image/green.gif";
    private static final String radioRedPath = "net/osm/shell/control/activity/image/red.gif";

    private static final ImageIcon radioGrayIcon = IconHelper.loadIcon( radioGrayPath );
    private static final ImageIcon radioGreenIcon = IconHelper.loadIcon( radioGreenPath );
    private static final ImageIcon radioRedIcon = IconHelper.loadIcon( radioRedPath );
 
    //==========================================================
    // state
    //==========================================================
 
   /**
    * Debug trace status.
    */
    private boolean trace = false;

   /**
    * Dialog containing the dialog display.
    */
    private JDialog dialog;

   /**
    * Parent JDialog.
    */
    private JDialog parent;

   /**
    * The component in the parent from which the parent is resolved
    * and from which positioning of the dialog is established.
    */
    private Component source;

   /**
    * The dialog cancel button.
    */
    private JButton cancel;

   /**
    * The dialog cancel action.
    */
    private CancelAction cancelAction;

   /**
    * Field containing the status message.
    */
    private LabelPanel status;

   /**
    * The number of diodes to display.
    */
    private int count;

   /**
    * Diode container.
    */
    private JPanel diodes;

   /**
    * Title to display in the dialog.
    */
    private String title;

   /**
    * The activity to execute.
    */
    private final Activity activity;


    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new <code>KeyPairCreationDialog</code> that provides
    * feedback on the progress of key generation.
    * 
    * @param name the name of the action
    * @param component logically triggering the action
    * @param count the number of diodes to display
    */
    public DiodeDialog( String name, Component source, String title, int count, final Activity activity )
    {
        super( name );

        if( activity == null ) throw new RuntimeException(
	    "Cannot create diode dialog with a null activity.");

        this.source = source;
        this.count = count;
        this.title = title;
        this.activity = activity;

        setEnabled( false );
        activity.setCallback( this );
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Triggered when the component hosting this action is trigger, 
    * resulting in the display of the diode dialog box.
    */
    public void actionPerformed( ActionEvent event )
    {
	  if( event.getSource() == source ) try
	  {
            if( dialog == null ) dialog = createDialog();
            final ComponentRunner worker = new ComponentRunner() {
                public Object construct() 
                {
                    try
                    {
		            activity.execute();
                    }
                    catch( Exception e )
                    {
                        ExceptionHelper.printException("Component runner:", e );
                    }
		        finally
		        {
                        return activity;
	              }
                }
            };
            worker.start();
	      dialog.setVisible( true );
        }
        catch( Exception e )
        {
            ExceptionHelper.printException("Unable to execute activity.", e );
        }
    }

    //==========================================================
    // Closable
    //==========================================================

   /**
    * Close the supporting instance.
    */
    public void close( CancelAction source )
    {
        if( source == cancelAction ) dialog.setVisible( false );
    }

    //==========================================================
    // Activity Callback
    //==========================================================

   /**
    * Called by the activiity to declare its readiness to execute.
    */
    public void setEnabled( boolean status )
    {
        super.setEnabled( status );
    }

   /**
    * Called by the activity to declare execution status.
    */
    public void setMessage( String message )
    {
        status.setText( "Status: " + message );
    }

   /**
    * Updates the status of the diode display.
    *
    * @param n the diode current in progress
    */
    public void setProgress( int n )
    {

        //
        // all diodes presceeding the position n should be set to
        // green indicating successfull completion - the diode
        // at position n should flash on and off indicating 
        // progress underway (flashing not implemented yet)
        //

        for( int i=0; i<n; i++ )
        {
            JLabel diode = (JLabel) diodes.getComponent( i );
		diode.setIcon( radioGreenIcon );
        }

	  try
	  {
            JLabel target = (JLabel) diodes.getComponent( n );
            target.setIcon( radioGreenIcon );
	  }
	  catch( Exception e )
	  {
	  }

        for( int j=(n+1); j<count; j++ )
        {
            JLabel diode = (JLabel) diodes.getComponent( j );
		diode.setIcon( radioGrayIcon );
        }

        if( n >= count ) dialog.setVisible( false );
    }

    //==========================================================
    // internals
    //==========================================================

   /**
    * Internal method to create the dialog and internal components.
    * @see actionPerformed
    */
    private JDialog createDialog()
    {

        //
        // Create the panel title and line separator
        //

        JLabel label = new JLabel( title );
        label.setFont( new Font("Dialog", 0, 18 ));
        label.setBorder( new EmptyBorder( 35, 10, 14, 10 ));
        JPanel labelHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        labelHolder.add( label );
        JPanel line = createSeparator( 280 );

        //
        // Package the label and the line into a header
        //

        JPanel header = new JPanel( new BorderLayout() );
        header.add( labelHolder, BorderLayout.NORTH );
        header.add( line, BorderLayout.SOUTH );

        //
        // create the body containing the status field
        // and radio buttons.
        //

        status = new LabelPanel( "Status: " );
        Box body = new Box( BoxLayout.Y_AXIS );
        body.add( status );

	  diodes = new JPanel( new FlowLayout( FlowLayout.LEFT ));
        for( int i=0; i<count; i++ )
        {
            JLabel diode = new JLabel( radioGrayIcon );
		diodes.add( diode );
        }

        body.add( diodes );
        body.add( Box.createVerticalGlue() );
        body.setBorder( new EmptyBorder(0,10,0,10));
        
        //
        // Create the button controls
        //

        cancel = new JButton( "Cancel" );
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        buttonPanel.add( cancel );
        Box buttonHolder = new Box( BoxLayout.Y_AXIS );
        buttonHolder.add( createSeparator( 280 ) );
        buttonHolder.add( buttonPanel );

        //
        // Package header, body and buttons
        //

        JPanel holder = new JPanel( new BorderLayout() );
	  holder.add( header, BorderLayout.NORTH );
	  holder.add( body, BorderLayout.CENTER );
	  holder.add( buttonHolder, BorderLayout.SOUTH );

        //
        // Create the dialog and set the content
        //

	  dialog = new JDialog( getDialog( source ) , dialogTitle, true );
        dialog.setContentPane( holder );
	  dialog.setSize( 300, 210 );
        dialog.setResizable( false );
        dialog.setLocationRelativeTo( source );

        //
        // Add the action handler
        //

        cancelAction = new CancelAction( "Cancel", cancel, this );
        cancel.setAction( cancelAction );

        return dialog;
    }

    private JDialog getDialog( Component component )
    {
       if( parent == null ) parent = component instanceof JDialog ? (JDialog) component
              : (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, component);
       return parent;
    }

    private JPanel createSeparator( int width )
    {
        //
        // Create a line seperator
        //

        JPanel line = new JPanel();
        line.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
        line.setPreferredSize( new Dimension( width, 2 ));
	  JPanel lineHolder = new JPanel( );
        lineHolder.add( line );
        lineHolder.setBorder( new EmptyBorder( 3, 10, 3, 10 ));
        return line;
    }

}
