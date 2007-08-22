
package net.osm.shell.control.wizard;

import java.util.LinkedList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingUtilities;

import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;


/**
 * The <code>Wizard</code> is an abstract dialog supporting 
 * the capture of information through a progressive sequence of 
 * steps during which is user is prompted for information in the 
 * the creation of a potentially complex information set.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class Wizard extends AbstractAction
{

    //==========================================================
    // static
    //==========================================================

   /**
    * Selection event identifier.
    */
    private static int SELECT_EVENT_ID = 0;
    private static synchronized int incrementSelectEvent( )
    {
       SELECT_EVENT_ID++;
       return SELECT_EVENT_ID;
    }

    //==========================================================
    // state
    //==========================================================

    private static final String path = "net/osm/shell/control/wizard/default.gif";
    private static final ImageIcon defaultIcon = IconHelper.loadIcon( path );
    private static final Color leftBackground = new Color(230,230,220);

    public static final int CANCEL = 0;
    public static final int PREV = 1;
    public static final int NEXT = 2;
    public static final int FINISH = 3;
    public static final int CLOSE = 4;

    private static boolean trace = false;

    private JButton cancel = new JButton("Cancel");
    private JButton prev = new JButton("Prev");
    private JButton next = new JButton("Next");
    private JButton finish = new JButton("Finish");

   /**
    * The frame window.
    */
    private ImageIcon icon = defaultIcon;

   /**
    * Monitors dialog for a close window event and notifies 
    * this instance via the notifyClosed method.
    */
    private WizardMonitor monitor;

   /**
    * Modal dialog.
    */
    private JDialog dialog;

   /**
    * Cancel actions.
    */
    private Action cancelAction;

   /**
    * Prev actions.
    */
    private Action prevAction;

   /**
    * Next actions.
    */
    private Action nextAction;

   /**
    * Finish actions.
    */
    private Action finishAction;

   /**
    * The JLabel containing the subject string.
    */
    private JLabel label;

   /**
    * The JLabel containing the subject icon.
    */
    private JLabel iconLabel;

   /**
    * Message text.
    */
    private JTextArea textHolder;

   /**
    * The right panel.
    */
    private JPanel right;

   /**
    * Singleton body component inside the right panel.
    */
    private Component body;

   /**
    * Default window title.
    */
    private String title = "Wizard";

   /**
    * Default subject line.
    */
    private String subject = "";

   /**
    * Default message.
    */
    private String message = 
		"Welcome to the Wizard." +
		" The wizard provides support for the capture of complex " +
            " information from a user.  This class serves as a base class " +
            "for application specific wizards.";

   /**
    * Cached reference to the currrent page.
    */
    private Page page;


    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creates a new modal Wizard dialog panel.
    * 
    * @param name the name to assign to the dialog window
    * @param frame the frame Frame that this modal dialog will be attached to
    * @param pages a sequence of actions 
    */
    public Wizard( Frame frame  )
    {
        super( "Wizard" );
        putValue("command", null );
        putValue("page", null );

        //
        // create the buttons, button panel and actions
        //

        cancelAction = new DefaultAction( "Cancel", CANCEL, this );
        prevAction = new DefaultAction( "Prev", PREV, this );
        nextAction = new DefaultAction( "Next", NEXT, this );
        finishAction = new DefaultAction( "Finish", FINISH, this );

        cancel.setAction( cancelAction );
        prev.setAction( prevAction );
        next.setAction( nextAction );
        finish.setAction( finishAction );
	  next.setDefaultCapable( true );

        JPanel command = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
        command.add( cancel );
        command.add( prev );
        command.add( next );
        command.add( finish );

        //
        // create the banner
        //

        label = new JLabel( subject );
        label.setFont( new Font("Dialog", 0, 12 ));
        JPanel banner = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        banner.add( label );

        //
        // create the icon
        //

        iconLabel = new JLabel( icon ) ;
        iconLabel.setBorder( new EmptyBorder( 35, 0, 30, 0 ) );
        JPanel iconHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ));
        iconHolder.setBackground( leftBackground );
        iconHolder.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
        iconHolder.add( iconLabel );

        //
        // create the message text
        //

        textHolder = new JTextArea();
        textHolder.setBorder( new EmptyBorder( 3, 10, 10, 10 ) );
        textHolder.setBackground( leftBackground );
        textHolder.setLineWrap( true );
        textHolder.setWrapStyleWord( true );
        textHolder.setEditable(false);
        textHolder.setFont( new Font("Dialog", 0, 11 ));
        textHolder.setText( message );

        //
        // package the icon and text into a panel
        //

	  JPanel left = new JPanel( new BorderLayout() );
        left.setBackground( leftBackground );
	  left.setPreferredSize( new Dimension(200,300) );
        left.setBorder( new EtchedBorder( EtchedBorder.LOWERED ));
        left.add( iconHolder, BorderLayout.NORTH );
        left.add( textHolder, BorderLayout.CENTER );
        
        //
        // create right panel
        //

	  right = new JPanel( new BorderLayout() );
	  right.setPreferredSize( new Dimension(400,300) );
        right.setBorder( new EtchedBorder( BevelBorder.LOWERED ));

        //
        // package the left and right components
        //

        JPanel main = new JPanel( new BorderLayout() );
        main.add( left, BorderLayout.WEST );
        main.add( right, BorderLayout.EAST );

        //
        // package the banner, main and command panel
        //

	  JPanel content = new JPanel( new BorderLayout() );
        content.add( banner, BorderLayout.NORTH );
        content.add( main, BorderLayout.CENTER );
        content.add( command, BorderLayout.SOUTH );

        //
        // package into a dialog
        //

        monitor = new WizardMonitor( this );
	  dialog = new JDialog( frame , title, true );
        dialog.setContentPane( content );
	  dialog.setSize( 610, 450 );
        dialog.setResizable( false );
        dialog.setLocationRelativeTo( null );
        dialog.addWindowListener( monitor );
        dialog.getRootPane().setDefaultButton( next );
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Establishes the modal dialog.
    */
    public void actionPerformed( ActionEvent event )
    {
	  dialog.setVisible( true );
    }

    //==========================================================
    // Wizard
    //==========================================================

    public void setIcon( ImageIcon icon )
    {
        iconLabel.setIcon( icon );
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public void setSubject( String subject )
    {
	  label.setText( subject );
    }

    public void setMessage( String message )
    {
        textHolder.setText( message );
    }

    public void setEnabled( int id, boolean value )
    {
	  if( id == CANCEL )
        {
            cancelAction.setEnabled( value );
        }
        else if( id == PREV )
        {
            prevAction.setEnabled( value );
        }
        else if( id == NEXT )
        {
            nextAction.setEnabled( value );
        }
        else if( id == FINISH )
        {
            finishAction.setEnabled( value );
        }
    }

    public Page getPage()
    {
        return page;
    }

    public void setPage( Page page ) throws WizardException
    {
	  try
	  {
	      right.removeAll();
            right.add( page.getPanel() );
            right.validate();
            right.repaint();
	      page.actionPerformed( 
			new ActionEvent( this, incrementSelectEvent() , "select" ) );
		this.page = page;
		putValue("page", this.page );
	  }
	  catch( Exception e )
	  {
		e.printStackTrace();
            throw new WizardException("Unexpected exception while setting page.", e );
	  }
    }

    protected void notifyActionPerformed( int id )
    {
        putValue("command", new PageEvent( page, id ) );
    }

    protected void windowClosing( WindowEvent event )
    {
        putValue("command", new PageEvent( page, CLOSE ) );
    }

    public void setVisible( boolean value )
    {
        dialog.setVisible( value );
    }

    public void setDefaultButton( int id )
    {
	  if( id == CANCEL )
        {
            dialog.getRootPane().setDefaultButton( cancel );
        }
        else if( id == PREV )
        {
            dialog.getRootPane().setDefaultButton( prev );
        }
        else if( id == NEXT )
        {
            dialog.getRootPane().setDefaultButton( next );
        }
        else if( id == FINISH )
        {
            dialog.getRootPane().setDefaultButton( finish );
        }
    }
}
