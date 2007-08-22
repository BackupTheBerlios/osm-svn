
package net.osm.util;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * The <code>ActiveList</code> is a LinkedList that produces 
 * <code>ListEvent</code>s on the addition and removal of 
 * objects from/to the list.  In addition to classic list
 * operations, the implementation includes logging support.
 * 
 */
public class ActiveList extends LinkedList implements List
{

    //=========================================================================
    // static
    //=========================================================================

    private static final boolean trace = false;

    //=========================================================================
    // state
    //=========================================================================

    private LinkedList listeners = new LinkedList();

    private Logger logger;

    private boolean disposed = false;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Creation of a new initially empty ActiveList.
    * @param logger the logging channel 
    */
    public ActiveList( Logger logger )
    {
	  if( logger == null ) 
	  {
		final String error = "null logger constructor argument";
	      throw new NullPointerException( error );
	  }
        this.logger = logger;
    }

   /**
    * Creation of a new ActiveList based on a supplied array of objects.  Object
    * in the array will be added to the list in the same order.
    * @param array an array of objects to be added to the list
    */
    public ActiveList( Logger logger, Object[] array )
    {
	  this( logger );
	  for( int i=0; i<array.length; i++ )
	  {
		add( array[i] );
	  }
    }

    //================================================================
    // LogEnabled
    //================================================================
    
    /**
     * Returns the current logging channel.
     * @return the Logger
     */
    protected final Logger getLogger()
    {
        if( logger != null ) return logger;
	  throw new IllegalStateException("logger has not been configured");
    }


    //=========================================================================
    // ListHandler
    //=========================================================================

   /**
    * Adds a <code>ListListener</code>.
    */
    public void addListListener( ListListener listener )
    {
	  if( disposed ) return;
        synchronized( listeners )
	  {
		listeners.add( listener );
	  }
    }

   /**
    * Removes a <code>ListListener</code>.
    */
    public void removeListListener( ListListener listener )
    {
        synchronized( listeners )
	  {
		listeners.remove( listener );
	  }
    }

    //=========================================================================
    // List
    //=========================================================================

    public void add( int index, Object object )
    {
	  if( disposed ) return;
        super.add( index, object );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "add " + object  );
	  fireListEvent( new ListEvent( this, object, ListEvent.ADD ));
    }

    public boolean add( Object object )
    {
	  if( disposed ) return false;
        boolean result = super.add( object );
	  if( result ) 
        {
	      if( getLogger().isDebugEnabled() ) getLogger().debug( "add: " + object );
	      fireListEvent( new ListEvent( this, object, ListEvent.ADD ));
        }
        return result;
    }

    public boolean remove( Object object )
    {
        boolean result = super.remove( object );
	  if( disposed ) return result;
	  if( result ) 
        {
	      if( getLogger().isDebugEnabled() ) getLogger().debug( "remove " + object );
	      fireListEvent( new ListEvent( this, object, ListEvent.REMOVE ));
        }
        return result;
    }

    //=========================================================================
    // Disposable
    //=========================================================================

    public synchronized void dispose()
    {
	  disposed = true;
        while( size() > 0 )
	  {
		remove( get(0) );
	  }
    }

    //=========================================================================
    // internal
    //=========================================================================

   /**
    * Proceses context events on this handler by dispatching a 
    * ContextEvent to all registered listeners.
    */
    public void fireListEvent( ListEvent event )
    {
        synchronized( listeners ) 
        {
	      try
	      {
	          Iterator iterator = listeners.iterator();
                switch( event.getMode() )
                {
                  case ListEvent.ADD:
                    while( iterator.hasNext() ) 
                    {
				try
				{
                            if( getLogger().isDebugEnabled() ) getLogger().debug("fire add event" );
                            ListListener listener = (ListListener) iterator.next();
	  	                listener.addObject( event );
				}
				catch( Throwable e )
				{
			      }
                    }
		        break;
                  case ListEvent.REMOVE:
                    while( iterator.hasNext() ) 
                    {
				try
				{
                            if( getLogger().isDebugEnabled() ) getLogger().debug("fire remove event" );
                            ListListener listener = (ListListener) iterator.next();
	  	                listener.removeObject( event );
				}
				catch( Throwable e )
				{
			      }
                    }
		        break;
		    }
            }
            catch( Exception e )
            {
                if( getLogger().isErrorEnabled() ) getLogger().error( 
			"exception occured inside fireContextEvent, event: " + event , e);
            }
        }
    }

    public boolean equals( Object other )
    {
	  if( !this.getClass().equals( other.getClass() )) return false;
	  return ( System.identityHashCode( this ) == System.identityHashCode( other ) );
    }

    public String toString()
    {
        return getClass().getName() + 
          "[" +	
          "id=" + System.identityHashCode( this ) + " " + 
          "size=" + size() +
          "]";
    }
}
