/*
 * @(#)Panel.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.shell;

/**
 * Interface defining operations required on a object that is capable of 
 * supporting interaction with a clipboard.
 */

public interface ClipboardHandler
{
   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be cleared (that is to say that the relationships 
    * defining liks between the primary entity and the exposed entities 
    * can be deleted.
    * @return boolean - true if the panel can delete the selected links
    * @see #handleDelete
    */
    public boolean canDelete();

   /**
    * Request to a handler to to process link deletion based on the current selection.
    * @return boolean true if the deletion action was completed sucessfully
    * @see #canDelete
    */
    public boolean handleDelete();

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be trasfered to the clipboard.  If the method
    * returns true the shell will enable the Edit/Cut menu item and on 
    * user selection will invoke the handleCut operation on the handler.
    * @return boolean - true if the panel will accept the candidate object
    * @see #handleCut
    */
    public boolean canCut();

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard.
    * @return Object[] array of cut entities
    * @see #canCut
    */
    public Object[] handleCut();

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be duplicated on the clipboard.
    * @return boolean - true if the panel supports copying of the current selection
    * @see #handleCopy
    */
    public boolean canCopy();

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard in response to a user copy request.
    * @return Object[] array of cut entities
    * @see #canCopy
    */
    public Object[] handleCopy();


   /**
    * Method invoked by the shell to determine if the current clipboard 
    * content is a valid candidate for pasting into this panel.  The 
    * default implementation return false.  Classes derived from this base 
    * class must override this method and the corresponding <code>handlePaste</code>
    * to provide support for specialized cases.
    * @param array - the clipboard content
    * @return boolean - true if the panel will accept the candidate object
    * @see #handlePaste
    */
    public boolean canPaste( Object[] array );

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel.
    * @param array - the clipboard content
    * @see #canPaste
    */
    public boolean handlePaste( Object[] array );

   /**
    * Method invoked by the shell to determine if the current clipboard 
    * content is a valid candidate for pasting into this panel using the 
    * Paste Special case.  The default implementation return false.  
    * Classes derived from this base class must override this method and 
    * the corresponding <code>handlePaste</code> to provide support for 
    * specialized cases.
    * @param array the clipboard content
    * @return boolean - true if the panel will accept the content
    * @see #handlePasteSpecial
    */
    public boolean canPasteSpecial( Object[] array );

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel using the Paste Special
    * context.
    * @param array the clipboard content
    * @see #canPasteSpecial
    */
    public boolean handlePasteSpecial( Object[] array );

}
