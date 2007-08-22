/*
 * @(#)TasksPanel.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.agent;

import java.util.List;
import java.util.Iterator;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.ListSelectionModel;

import org.omg.Session.task_state;

import net.osm.shell.TablePanel;
import net.osm.shell.EntityTable;
import net.osm.shell.Entity;
import net.osm.util.ExceptionHelper;


/**
 * A panel presenting the <code>Tasks</code> owned by a <code>User</code> 
 * in the form of a table.
 */

public class TasksTablePanel extends TablePanel 
{
    //======================================================================
    // state
    //======================================================================

    private UserAgent agent;

    private TableModel data;

    private List actions;

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new TablePanel.
    */
    public TasksTablePanel( UserAgent agent, String role, TableModel data, TableColumnModel columns )
    {
	  super( agent, role, data, columns );
        this.data = data;
	  this.agent = agent;
    }

    //======================================================================
    // ActionHandler
    //======================================================================

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the entity 
    * is selected.
    */
    public List getActions( )
    {
        if( actions != null ) return actions;

	  //
	  // crerate a set of actions based on the criteria exposed by the factory
	  //

        actions = super.getActions();
        Iterator iterator = agent.getServices().iterator();
        while( iterator.hasNext() )
        {
            actions.add( iterator.next() );
        }
        return actions;
    }


    //======================================================================
    // ClipboardHandler
    //======================================================================

   /**
    * Method invoked by the shell to determine if the current selection
    * of tasks within the panel can be removed. 
    * @return boolean - true if the panel can delete the selected links
    * @see #handleDelete
    */
    public boolean canDelete()
    {
        return ( !getSelectionModel().isSelectionEmpty() );
    }

   /**
    * Request to a handler to process removal of the current selection 
    * of tasks.
    * @see #canDelete
    */
    public boolean handleDelete()
    {

        //
        // for all of the selected entities, remove the links to the 
        // primary entity
        //

        if( !( data instanceof EntityTable ) ) return false;
        synchronized( data )
	  {
		int[] rows = getSelectedRows();
   		for( int i=rows.length-1; i>-1; i--)
	  	{
		    try
		    {
		        final LinkAgent link = (LinkAgent) ((EntityTable)data).getEntityAtRow( rows[i] );
		        ((AbstractResourceAgent)link.getTarget()).remove();
		    }
		    catch( CannotRemoveException cr )
		    {
			  // beep + message ?
			  ExceptionHelper.printException( "Failed to remove a task.", cr );
		    }
		}
	  }
	  return true;
    }

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be trasfered to the clipboard.  This method always 
    * returns false as cutting a task implies disassiciation from its owning User.
    * The alternative it to copy the task from the Task panel to a workspace.
    * @return boolean always returns false
    * @see #handleCut
    */
    public boolean canCut()
    {
        return false;
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard.  This operation always returns an empty object array.
    * @return Object[] array of cut entities
    * @see #canCut
    */
    public Object[] handleCut()
    {
        return new Object[0];
    }

   /**
    * Method invoked by the shell to determine if the current Task selection
    * within the panel can passed to the clipboard.
    * @return boolean - true if the current selection is not empty 
    * @see #handleCopy
    */
    public boolean canCopy()
    {
        return !getSelectionModel().isSelectionEmpty();
    }

   /**
    * Request to return an array of TaskAgent instances to be placed
    * on the clipboard in response to a user copy request.
    * @return Object[] array of TaskAgent instances
    * @see #canCopy
    */
    public Object[] handleCopy()
    {
        if( !( data instanceof EntityTable ) ) return new Object[0];
        synchronized( data )
	  {
		int[] rows = getSelectedRows();
		Object[] result = new Object[ rows.length ];
   		for( int i=0; i<rows.length; i++ )
	  	{
		    final LinkAgent link = (LinkAgent) ((EntityTable)data).getEntityAtRow( rows[i] );
		    result[i] = link.getTarget();
		}
	      return result;
	  }
    }

   /**
    * Method invoked by the shell to determine if the current clipboard 
    * content is a valid candidate for pasting into this panel.  The 
    * implementation always return false (as task are created and bound 
    * to a specific user). 
    * @param array the clipboard content (ignored)
    * @return boolean always returns false
    * @see #handlePaste
    */
    public boolean canPaste( Object[] array )
    {
        return false;
    }

   /**
    * Request issued by the desktop to a task panel to handle the pasting of 
    * the current clipboard content into the panel.  The implementation 
    * does nothing aside from return a false value.
    * @param array - the clipboard content
    * @return boolean always returns false
    * @see #canPaste
    */
    public boolean handlePaste( Object[] array )
    {
	  return false;
    }

   /**
    * Method invoked by the shell to determine if the current clipboard 
    * content is a valid candidate for pasting into this task panel using the 
    * Paste Special case.  The implementation will return false unless the
    * clipboard contains a single resource and that resource is a candidate to
    * a single usage constraint.
    * @param array the clipboard content
    * @return boolean - true if the panel will accept the content
    * @see #handlePasteSpecial
    */
    public boolean canPasteSpecial( Object[] array )
    {
        if( array.length == 1 ) return canPasteSpecial( array[0] );
        return false;
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the a single currently selected
    * task
    * context.
    * @param array the clipboard content
    * @return boolean - true if the paste operation was successful
    * @see #canPasteSpecial
    */
    public boolean handlePasteSpecial( Object[] array )
    {
        if( array.length == 1 ) return handlePasteSpecial( array[0] );
        return false;
    }

   /**
    * Method invoked by the shell to determine if the current clipboard 
    * content is a valid candidate for pasting into this panel using the 
    * Paste Special case.  The task panel supports the addition of 
    * consumption relationships between non-started tasks and resources
    * contained in the clipboard.
    *
    * @param object - the candidate object from the clipboard
    * @return boolean - true if the panel will accept the candidate object
    * @see #handlePasteSpecial
    */
    private boolean canPasteSpecial( Object object )
    {
        if( object == null ) return false;
        if( !( object instanceof AbstractResourceAgent )) return false;

        //
        // we have a selection in the table which means
	  // we have a potential single selection
        //
        
        TaskAgent task = getSingleTask();
        if( task != null )
        {
            //
	      // make sure the task is an a valid state for usage change
	      //

		if( validateTaskStateForChange( task ) )
		{
		    //
		    // the task is in a valid state to handle assignment of 
		    // new usage associations - to validate this we need to 
	          // get the task's processor criteria usage declarations
		    // and check that there is an available "slot" for 
		    // adding the clipboard resource - if there is we can 
		    // enable the Paste Special menu item
		    //

		    ProcessorModelAgent model = task.getModel();
		    List usageList = model.getDescriptors();
                
		    // at this point we need to make a decision concerning the 
		    // strict compliance with the T/S spec as opposed to pragmatic
		    // user-friendlness - the T/S spec says that any resource can be
	          // added to a task but this is not practicle because the task is 
		    // a view of a processor and the process has to declare what's 
		    // acceptable as candidate resource - therefore, we only allow
		    // association of resources under tags exposed by the processor 
		    // model criteria
			
		    if( usageList.size() == 0 ) return false;

		    // if the usage length is > 0 then if any of the usage types
		    // match the type from the clipboard, then the clipboard object 
		    // is a candidate for binding under a tagged usage constraint, 
		    // possibly replacing an existing usage link or supplimenting 
		    // existing links

		    return verifyCandidate( (AbstractResourceAgent) object, usageList );
	      }
	  }
        return false;
    }

   /**
    * Internal utility to return a single task or null if the selection is empty or a 
    * multiple selection exists.
    * @return TaskAgent representing the single selection or null if non single selection
    * has been established
    */
    private TaskAgent getSingleTask()
    {
	  if( !( data instanceof EntityTable ) ) return null;
	  int[] rows = getSelectedRows();
        if( rows.length != 1 ) return null;
        return (TaskAgent) ((LinkAgent)((EntityTable)data).getEntityAtRow( rows[0] )).getTarget();
    }

   /**
    * Internal utility to validate that a task is in an appropriate state for 
    * modification of its usage associations.
    * @param task the task to verify
    * @return boolean true if the task is not started or suspended
    */
    private boolean validateTaskStateForChange( TaskAgent task )
    {
        return(( task.getTaskState() == task_state.notstarted ) || 
	     ( task.getTaskState() == task_state.suspended ));
    }

   
    private boolean verifyCandidate( AbstractResourceAgent agent, List list )
    {
	  Iterator iterator = list.iterator();
	  while( iterator.hasNext() )
	  {
		UsageDescriptorAgent usage = (UsageDescriptorAgent)iterator.next();
		if( usage.isaCandidate( agent ) ) return true;
	  }
        return false;
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel using the Paste Special
    * context.  The implemetation of <code>handlePasteSpecial</code> applies
    * the operation to a single selection within the panel.  A null or multiple 
    * selection case results in an error condition.
    *
    * @param object - the clipboard object to be included in the panel
    * @see #canPasteSpecial
    * @osm.warning The current implementation does not support pasting of clipboard 
    *   resources when the number of input descriptors is greater than 1.  This
    *   requires the addition of a dialog enabling selection of a candidate tag
    *   that the resource is to be associated under.  Addition HCI support is 
    *   needed in the properties display to indicate the role of consumed resources
    *   relative to the task/processor. 
    */
    private boolean handlePasteSpecial( Object object )
    {
        try
	  {
            if( object == null ) throw new RuntimeException("Cannot add a null object.");
            if( !( object instanceof AbstractResourceAgent )) throw new RuntimeException(
		    "Object is not an AbstractResourceAgent.");
		AbstractResourceAgent resource = (AbstractResourceAgent) object;

            //
            // this is a request to add the supplied object to 
	      // a task
	      //

            TaskAgent task = getSingleTask();
	      if( task == null ) throw new RuntimeException(
              "No current single task selection.");

	      synchronized( task )
	      {
                if( !validateTaskStateForChange( task ) ) throw new RuntimeException(
                  "Task is in an invalidate state for modifification of usage associations.");
                ProcessorModelAgent model = task.getModel();
		    List list = model.getDescriptors();
                if( list.size() == 0 ) throw new RuntimeException(
                  "Processor is resource independent.");
		    List candidates = model.getInputCandidates( resource );
                if( candidates.size() == 0 ) throw new RuntimeException(
                  "Processor does not support associations with the type of resource held by the clipboard.");

		    //
	          // Getting here means that everything is set for an association to
	          // take place.  If the usage constraint is singluar, then we can add the 
	          // resource without further user intervention, otherwise, we need to get 
		    // the user to declare which tag the resource is to be associated under
		    //

		    if( candidates.size() == 1 )
		    {
			  //
			  // we can add the resource directly using the profile 
			  // described by the single usage descriptor
			  //

			  UsageDescriptorAgent usage = (UsageDescriptorAgent) list.get(0);
		        task.addConsumed( usage, resource );
		    }
		    else
	          {

			  // WARNING:
			  // multiple usage descriptions means we need to present
			  // a choice dialog to the user - implementation pending
			  // 

			  System.out.println("option count: " + candidates.size() );
		        Iterator iterator = candidates.iterator();
		        while( iterator.hasNext() )
		        {
			      System.out.println("option: " + ((UsageDescriptorAgent)iterator.next()).getTag() );
	              }
		    }
		}
	  }
	  catch( Exception e )
	  {
            // beep
		// do we show an error message ?
		ExceptionHelper.printException("Cannot paste object.", e, this, true );
		return false;
	  }

        return true;
    }

}
