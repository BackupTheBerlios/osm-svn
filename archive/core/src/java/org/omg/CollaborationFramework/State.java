// Thu Nov 23 07:22:01 CET 2000

package org.omg.CollaborationFramework;

import java.io.Serializable;
import java.util.LinkedList;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Label;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.OverlayLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.Box;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CommunityFramework.Control;
import org.omg.CollaborationFramework.Trigger;

/**
 * The primary valuetype used in the construction of a CollaborationModel is the State valuetype. A
 * State is a container of sub-states and Trigger valuetypes. An instance of State has an identifier
 * label (from the inherited Control valuetype), that may be exposed by a CollaborationProcessor
 * under the active_state attribute. A State is activated as a result of a transition action applied
 * through the apply operation or through implicit initialization using the start operation (from the
 * abstract Processor interface inherited by Collaboration).
 * <p>
 * The Collaboration declares an active_state attribute and a corresponding structured event named
 * active. The value of the event and attribute is an identifier of the state referenced in the last valid
 * action (such as an initialization or simple transition). Once an active state has been established,
 * the state containing an active state is considered as active, and as such, its parent, until the root-state
 * is reached. This set of states is referred to as the active state path of the Collaboration
 * processor. For every state in the active state path, all directly contained Triggers are considered
 * as candidates with respect to the apply and apply_arguments operations on CollaborationProcessor. That 
 * is to say that a client may invoke any Trigger exposed by a state in
 * the active state path, providing that preconditions to Trigger activation are satisfied.
 */
public class State extends Control
{
    
    //================================================================
    // state
    //================================================================
    
   /**
    * A sequence of Trigger instances that each define constraint conditions
    * relative to a contained Action.
    */
    public Trigger[] triggers = new Trigger[0];

   /**
    * A sequence of sub-states forming a state hierarchy.
    */
    public State[] states = new State[0];

   /**
    * Transient configuration instance.
    */
    private Configuration config;

    //================================================================
    // constructors
    //================================================================
    
   /**
    * Null argument constructor used during stream internalization.
    */

    public State () {}
    
   /**
    * Creation of a new State instance based on a supplied configuration
    * argument.  Following creation, the invoking client must apply the
    * initalizeTriggers operation to initialize all enclosed triggers
    * before this instance can be deployed.
    * @param conf the configuration to apply to the new instance
    */
    public State( Configuration conf ) 
    {
        super( conf );
	  this.config = conf;
        try
        {

		// build sub-states

	      Configuration[] array = conf.getChildren("state");
            states = new State[ array.length ];
		for( int i=0; i< array.length; i++ )
		{
		    states[i] = new State( array[i] );
	      }
        }
	  catch( Exception e )
	  {
	      throw new RuntimeException(
			"Failed to configure State.", e );
	  }
    }

    //================================================================
    // implementation
    //================================================================

    protected void initalizeTriggers( CollaborationModel model ) throws Exception
    {
        try
	  {
		// build the triggers in this state

		Configuration[] array = config.getChildren("trigger");
            triggers = new Trigger[ array.length ];
		for( int i=0; i< array.length; i++ )
		{
		    triggers[i] = new Trigger( model, array[i] );
	      }
		config = null;

		// initalize the triggers in the sub-states

		for( int i=0; i< states.length; i++ )
		{
		    states[i].initalizeTriggers( model );
	      }
        }
	  catch( Exception e )
	  {
	      throw new Exception("failed to initialize a trigger", e );
	  }
    }

   /**
    * This state contains a set of triggers which in-turn 
    * contain a single action.  If this action is a referral
    * it needs to be initalized now that all actions have been 
    * populated within the model.
    */
    protected void initalizeReferrals( CollaborationModel model ) throws Exception
    {
        try
	  {
		for( int i=0; i< triggers.length; i++ )
		{
		    Trigger trigger = triggers[i];
		    if( trigger.action instanceof Referral )
		    {
			  ((Referral)trigger.action).initalize( model );
		    }
	      }

		// initalize the referrals in the sub-states

		for( int i=0; i< states.length; i++ )
		{
		    states[i].initalizeReferrals( model );
	      }
        }
	  catch( Exception e )
	  {
	      throw new Exception("failed to initialize a referral", e );
	  }
    }


   /**
    * Return a state corresponding to the supplied label.  
    * Returns null if no matching state.
    */
    public State lookupState( String label ) 
    {
        if( this.label.equals( label )) return this;
        if( this.states != null ) {
            for( int i = 0; i< this.states.length; i++) {
                State s = ((State)states[i]).lookupState( label );
                if( s != null) return s;
            }
        }
        return null;
    }

   /**
    * Return a state corresponding to the supplied label.  
    * Returns null if no matching state.
    * @param label the name of a trigger holding the action
    */
    public Action lookupAction( String label ) 
    {
        //
	  // check the actions held by the local triggers
	  //

        for( int i = 0; i< triggers.length; i++) 
        {
            Trigger trigger = triggers[i];
		if( trigger.label.equals( label ) ) return trigger.action; 
        }

        //
        // otherwise, apply the lookup action on all of the substates
        //
	  
        for( int i = 0; i< this.states.length; i++) 
        {
            Action action = states[i].lookupAction( label );
            if( action != null) return action;
        }
        return null;
    }

    //
    // implementation of Streamable
    //
    
    public TypeCode _type()
    {
        return StateHelper.type();
    }
    
    public void _read(InputStream _is)
    {
        super._read(_is);
        triggers = TriggersHelper.read(_is);
        states = StatesHelper.read(_is);
    }
    
    public void _write(OutputStream _os)
    {
        super._write(_os);
        TriggersHelper.write(_os, triggers);
        StatesHelper.write(_os, states);
    }
    
    //
    // implementation of ValueBase
    //
    
    static final String[] _ids_list = { 
        "IDL:omg.org/CollaborationFramework/State:1.0",
    };
    
    public String[] _truncatable_ids() { return _ids_list; }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new State() );
    }

    // 
    // supplimentary functions
    //

   /**
    * The <code>getStateView</code> method returns a component through which a state can be presented.
    * under a human user interface.  The implementation currently returns a rounded rectangle.
    */
/*
    public Component getStateView() {
        if( stateView == null ) stateView = new StateView();
        return stateView;
    }
*/    
    // The Graphic contained class presents a state in the form of a rounded
    // rectangle.
/*    
    private class StateView extends JPanel {
        
        double arcw = 35;
        double arch = 35;
        boolean firstTime = true;
        RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
        BufferedImage bi;
        Graphics2D big;
        Rectangle area;
        Box ss;
        Dimension minimum = new Dimension( 100, 100);
        Font font = new Font("Dialog", 0, 12);
        
        public StateView( ) {
            
            try{
                setBorder( new EmptyBorder( 15, 15, 15, 15 ));
                setLayout( new BorderLayout( ));
                setName( label );
                setSize( minimum );
 
                // add a label containing the state label
		    JLabel statelabel = getLabelView();
                statelabel.setBorder( new EmptyBorder( 5, 6, 15, 20 ));
                statelabel.setFont(font);
                statelabel.setForeground(Color.black);
		    statelabel.setToolTipText( note );
                add( statelabel , BorderLayout.NORTH );
                
                // add a panel containing all of the sub-states
                ss = new Box( BoxLayout.X_AXIS );
                ss.setBackground(Color.white);
                for( int i =  0; i < states.length; i++ ) {
                    ss.add(((State)states[i]).getStateView() );
                }
                add( ss, BorderLayout.CENTER );
                
                // add the graphic elements describing the triggers connection 
                // points associated with this state - triggers themselves need 
                // to be constructed on a seperate layer above the state 
                // presentation
                for( int i = 0; i< triggers.length; i++ ) {
			  // not implemented
                    //System.out.println("\tCAUTION: org.omg.CollaborationFramework.State - draw " 
			  //	+ triggers[i].label );
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            update(g);
        }
        
        public void update(Graphics g){
            try{
            Graphics2D g2 = (Graphics2D)g;
            Dimension dim = getSize();
            int w = dim.width;
            int h = dim.height;
            if(firstTime){
                
                // set-up the buffered impage and buffered impage graphic
                bi = (BufferedImage)createImage(w, h);
                big = bi.createGraphics();
                big.setColor(Color.black);
                big.setStroke(new BasicStroke(5.0f));
                big.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                big.setBackground(Color.white);
                
                // create the rounded rectangle that represents the State instance -
                // note that the rectangle is slightly set in so that the full 
                // graphics of the bounding line are not clipped
                
                area = new Rectangle(dim);
                rect.setRoundRect( 5, 5, area.width - 10, area.height -10 , arcw, arch);
                firstTime = false;
            }
            
            // Clears the rectangle that was previously drawn.
            big.setColor(Color.white);
            big.clearRect(0, 0, area.width, area.height);
            // Draws and fills the newly positioned rectangle to the buffer.
            big.setPaint(Color.black);
            big.draw(rect);
            // Draws the buffered image to the screen.
            g2.drawImage(bi, 0, 0, this);
            } catch (Exception e) {
                  System.out.println("ERROR net.osm.State.Graphic.update: " + e.getMessage());
            } 
        }
    }
*/
}
