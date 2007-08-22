/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 09/04/2001
 */

package net.osm.list;

import java.util.Vector;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.IteratorOperations;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosPersistentState.StorageObject;


/**
 * The AbstractIteratorDelegate is the base implementation class for all 
 * iterators supporting the CosCollection iterator interface.  Due to the 
 * "heavy-weight" nature of the CosCollection iterator spectfication the 
 * implementation declares a non-implement exception for non-core operations. 
 */

public abstract class AbstractIteratorDelegate implements IteratorOperations
{

   /**
    * The PSS iterator underlying the CosCollection iterator.
    */
    protected Iterator iterator;


   /**
    * TypeCode to be used as a filter on values returned from iterator
    * retrival operations and next element tests.
    */
    protected TypeCode type;
 

   /**
    * The last storage object returned from the iterator.
    */
    protected StorageObject store;

    protected boolean trace = false;


    //=========================================================================
    // Constructors
    //=========================================================================

   /**
    * Creation of a new AbstractIteratorDelegate with a null TypeCode filter.
    * @param orb current ORB used to create anys
    * @param iterator referencing underlying persistent list
    */
    public AbstractIteratorDelegate( Iterator iterator )
    {
	  this( iterator, null );
    }

   /**
    * Creation of a new AbstractIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public AbstractIteratorDelegate( LinkedList list, TypeCode type )
    {
	  this( list.iterator(), type );
    }

   /**
    * Creation of a new AbstractIteratorDelegate.
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public AbstractIteratorDelegate( LinkedList list, TypeCode type, boolean trace )
    {
	  this( list.iterator(), type, trace );
    }

   /**
    * Creation of a new AbstractIteratorDelegate.
    * @param iterator referencing underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public AbstractIteratorDelegate( Iterator iterator, TypeCode type )
    {
        this( iterator, type, false );
    }

   /**
    * Creation of a new AbstractIteratorDelegate.
    * @param iterator referencing underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    * @param trace the trace policy
    */
    public AbstractIteratorDelegate( Iterator iterator, TypeCode type, boolean trace )
    {
        this.iterator = iterator;
	  this.type = type;
        this.trace = trace;

	  // the following method invocation will 
	  // cause the iterator to advance to a point where the next entry
	  // is a valid entry in terms of type matching or the first entry
        // if the type is null

        try
        {
            set_to_next_element();
        }
        catch( IteratorInvalid e )
        {
        }
    }


    //=========================================================================
    // CosCollections::Iterator implementation
    //=========================================================================

   /**
    * The iterator is set to the first element in iteration order of the collection it belongs
    * to. If the collection is empty, that is, if no first element exists, the iterator is
    * invalidated.
    * @return  true if the collection it belongs to is not empty.
    */
    public boolean set_to_first_element()
    {
	  try
	  {
            iterator.reset();
	      return set_to_next_element();
	  }
	  catch( IteratorInvalid e )
	  {
	      return false;
        }
    }

   /**
    * Sets the iterator to the next element in the collection in iteration order or invalidates
    * the iterator if no more elements are to be visited. If the iterator is in the state in-between,
    * the iterator is set to its "potential next" element. The iterator must be valid; otherwise, 
    * the exception IteratorInvalid is raised.
    * @return  true if there is a next element.
    */
    public boolean set_to_next_element()
    throws IteratorInvalid
    {
        try
	  {
	      return getNext( );
	  }
	  catch( Exception e )
	  {
		throw new IteratorInvalid();
        }
    }


   /**
    * Sets the iterator to the element n movements away in collection iteration order or
    * invalidates the iterator if there is no such element. If the iterator is in the state in-between
    * the movement to the "potential next" element is the first of the n movements.
    * The iterator must be valid; otherwise, the exception IteratorInvalid is raised.
    * @return  true if there is such an element.
    */
    public boolean set_to_next_nth_element(int n)
    throws IteratorInvalid
    {
        boolean result = false;
	  for( int i = 0; i < n; i++ )
	  {
	      result = getNext( );
	  }
	  return result;
    }


   /**
    * Retrieves the current element and returns it via the output parameter element.
    * The iterator must point to an element of the collection; otherwise, the exception
    * IteratorInvalid or IteratorInBetween is raised.
    * @return  true if an element was retrieved.
    */
    public abstract boolean retrieve_element(AnyHolder element)
    throws IteratorInvalid, IteratorInBetween;


   /**
    * Retrieves the element pointed to and returns it via the output parameter element.
    * The iterator is moved to the next element in iteration order. If there is a next
    * element more is set to true. If there are no more next elements, the iterator is
    * invalidated and more is set to false. The iterator must be valid and point to an 
    * element; otherwise, the exception IteratorInvalid or IteratorInBetween is raised.
    * @return  true if an element was retrieved.
    */
    public boolean retrieve_element_set_to_next( AnyHolder element, BooleanHolder more)
    throws IteratorInvalid, IteratorInBetween
    {
	  boolean result = retrieve_element( element );
	  more.value = getNext();
        return result;
    }


   /**
    * Retrieves at most the next n elements in iteration order of the iterator's collection
    * and returns them as sequence of anys via the output parameter result. Counting
    * starts with the element the iterator points to. The iterator is moved behind the last
    * element retrieved. If there is an element behind the last element retrieved, more is
    * set to true. If there are no more elements behind the last element retrieved or there
    * are less than n elements for retrieval, the iterator is invalidated and more is set to
    * false. If the value of n is 0, all elements in the collection are retrieved until the end
    * is reached. The iterator must be valid and point to an element; otherwise, the exception
    * IteratorInvalid or IteratorInBetween is raised.
    *
    * @return  true if at least one element is retrieved.
    */
    public boolean retrieve_next_n_elements(int n, AnySequenceHolder result, BooleanHolder more)
    throws IteratorInvalid, IteratorInBetween
    {
	  int j = 0;
	  Vector vector = new Vector();
	  while( j < n )
        {
	      AnyHolder element = new AnyHolder();
		if( retrieve_element_set_to_next( element, more) )
	      {
                vector.add( element.value );
		    j++;
            }
		else
		{
		    break;
		}
        }

	  // return the elements in the vector as any sequence
        result.value = (Any[]) vector.toArray( new Any[j] );
	  if( j > 0 ) return true;
        return false;
    }


   /**
    * Compares the given iterator test with this iterator.
    * <ul>
    * <li>If they are not equal, the element pointed to by this iterator is retrieved and
    * returned via the output parameter element, the iterator is moved to the next
    * element, and true is returned.
    * <li> If they are equal, the element pointed to by this iterator is retrieved and
    * returned via the output parameter element, the iterator is not moved to the
    * next element, and false is returned.
    * </ul>
    * The iterator and the given iterator test each must be valid and point to an element;
    * otherwise, the exception IteratorInvalid or IteratorInBetween is raised.
    *<p>NOT IMPLEMENTED</P>
    *
    * @return  true if this iterator is not equal to the test iterator at the beginning of the
    * operation.
    */
    public boolean not_equal_retrieve_element_set_to_next(
      org.omg.CosCollection.Iterator test, AnyHolder element)
    throws IteratorInvalid, IteratorInBetween
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Removes the element pointed to by this iterator and sets the iterator in-between.
    * The iterator must be valid and point to an element of the collection; otherwise, the
    * exception IteratorInvalid or IteratorInBetween is raised.
    * The iterator must not have the const designation; otherwise, the exception
    * IteratorInvalid is raised. Other valid iterators pointing to the removed element 
    * go in-between. All other iterators keep their state.
    *<p>NOT IMPLEMENTED</P>
    */
    public void remove_element()
    throws IteratorInvalid, IteratorInBetween
    {
        throw new NO_IMPLEMENT();
    }


   /**
    * Removes the element pointed to by this iterator and moves the iterator to the next
    * element. The iterator must be valid and point to an element of the collection; otherwise, the
    * exception IteratorInvalid is raised. The iterator must not have the const designation; 
    * otherwise, the exception IteratorInvalid is raised.
    *<p>NOT IMPLEMENTED</P>
    * @return  true if a next element exists.
    */
    public boolean remove_element_set_to_next()
    throws IteratorInvalid, IteratorInBetween
    {
        throw new NO_IMPLEMENT();
    }


   /**
    * Removes at most the next n elements in iteration order of the iterator?s collection.
    * Counting starts with the element the iterator points to. The iterator is moved to the
    * next element behind the last element removed. If there are no more elements behind
    * the last element removed or there are less than n elements for removal, the iterator
    * is invalidated. If the value of n is 0, all elements in the collection are removed until
    * the end is reached. The output parameter actual_number is set to the actual
    * number of elements removed. If the value of n is 0, all elements in the collection
    * are removed until the end is reached. The iterator must be valid and point to an 
    * element; otherwise, the exception IteratorInvalid or IteratorInBetween is raised.
    * The iterator must not have the const designation; otherwise, the exception
    * IteratorInvalid is raised. Other valid iterators pointing to removed elements 
    * go in-between. All other iterators keep their state.
    *<p>NOT IMPLEMENTED</P>
    * @return  true if the iterator is not invalidated.
    */
    public boolean remove_next_n_elements(int n, org.omg.CORBA.IntHolder actual_number)
    throws IteratorInvalid, IteratorInBetween
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Compares this iterator with the given iterator test. If they are not equal the element
    * this iterators points to is removed and the iterator is set to the next element, and
    * true is returned. If they are equal the element pointed to is removed, the iterator is
    * set in-between, and false is returned. This iterator and the given iterator test 
    * must be valid otherwise the exception IteratorInvalid or IteratorInBetween is raised.
    * This iterator and the given iterator test must not have a const designation
    * otherwise the exception IteratorInvalid is raised. Other valid iterators pointing to 
    * removed elements go in-between. All other iterators keep their state.
    *<p>NOT IMPLEMENTED</P>
    * @return  true if this iterator and the given iterator test are not equal when the
    * operations starts.
    */
    public boolean not_equal_remove_element_set_to_next(org.omg.CosCollection.Iterator test)
    throws IteratorInvalid, IteratorInBetween
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Replaces the element pointed to by the given element. The iterator must be valid and 
    * point to an element; otherwise, the exception IteratorInvalid or IteratorInBetween is 
    * raised. The iterator must not have a const designation; otherwise, the exception
    * IteratorInvalid is raised. The element must be of the expected element type; 
    * otherwise, the ElementInvalid exception is raised. The given element must have the 
    * same positioning property as the replaced element; otherwise, the exception 
    * ElementInvalid is raised.
    *<p>NOT IMPLEMENTED</P>
    */
    public void replace_element(org.omg.CORBA.Any element)
    throws IteratorInvalid, IteratorInBetween, ElementInvalid
    {
        throw new NO_IMPLEMENT();
    }


   /**
    * Replaces the element pointed to by this iterator by the given element and sets the
    * iterator to the next element. If there are no more elements, the iterator is
    * invalidated. The iterator must be valid and point to an element; otherwise, the 
    * exception IteratorInvalid or IteratorInBetween is raised. The iterator must not 
    * have a const designation; otherwise, the exception IteratorInvalid is raised.
    * The element must be of the expected element type; otherwise, the ElementInvalid
    * exception is raised. The given element must have the same positioning property as 
    * the replaced element; otherwise, the exception ElementInvalid is raised.
    *<p>NOT IMPLEMENTED</P>
    * @return  true if there is a next element.
    */
    public boolean replace_element_set_to_next(org.omg.CORBA.Any element)
        throws IteratorInvalid, IteratorInBetween, ElementInvalid
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Replaces at most as many elements in iteration order as given in elements by the
    * given elements. Counting starts with the element the iterator points to. If there are
    * less elements in the collection left to be replaced than the given number of elements
    * as many elements as possible are replaced and the actual number of elements
    * replaced is returned via the output parameter actual_number. The iterator is moved 
    * to the next element behind the last element replaced. If there are no more elements 
    * behind the last element replaced or the number of elements in the collection to be 
    * replaced is less than the number given elements, the iterator is invalidated.
    * The iterator must be valid and point to an element; otherwise, the exception
    * IteratorInvalid or IteratorInBetween is raised. The elements given must be of the 
    * expected type; otherwise, the exception ElementInvalid is raised.
    * For each element the positioning property of the replaced element must be the same
    * as that of the element replacing it; otherwise, the exception ElementInvalid is
    * raised.
    *<p>NOT IMPLEMENTED</P>
    * @return  true if there is another element behind the last element replaced.
    */
    public boolean replace_next_n_elements(org.omg.CORBA.Any[] elements, org.omg.CORBA.IntHolder actual_number)
    throws IteratorInvalid, IteratorInBetween, ElementInvalid
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Compares this iterator and the given iterator test. If they are not equal, the element
    * pointed to by this iterator is replaced by the given element, the iterator is set to the
    * next element, and true is returned. If they are equal, the element pointed to by this
    * iterator is replaced by the given element, the iterator is not set to the next element,
    * and false is returned.  This iterator and the given iterator must be valid and point to 
    * an element each; otherwise, the exception IteratorInvalid or IteratorInBetween is 
    * raised. This iterator must not have a const designation; otherwise, the exception
    * IteratorInvalid is raised. The element must be of the expected element type; 
    * otherwise, the ElementInvalid exception is raised. The given element must have the 
    * same positioning property as the replaced element; otherwise, the exception 
    * ElementInvalid is raised.
    *<p>NOT IMPLEMENTED</P>
    * @return  true if this iterator and the given iterator test are not equal before the
    * operations starts.
    */
    public boolean not_equal_replace_element_set_to_next(
      org.omg.CosCollection.Iterator test, org.omg.CORBA.Any element)
    throws IteratorInvalid, IteratorInBetween, ElementInvalid
    {
        throw new NO_IMPLEMENT();
    }


   /**
    * Adds an element to the collection that this iterator points to and sets the iterator to
    * the added element. The exact semantics depends on the properties of the collection
    * for which this iterator is created. If the collection supports unique elements or keys 
    * and the element or key is already contained in the collection, adding is ignored and 
    * the iterator is just set to the element or key already contained. In sequential 
    * collections, the element is always added as last element. In sorted collections, the 
    * element is added at a position determined by the element or key value.
    * If the collection is a Map and contains an element with the same key as the given
    * element, then this element has to be equal to the given element; otherwise, the
    * exception ElementInvalid is raised. All other iterators keep their state.
    *<p>NOT IMPLEMENTED</P>
    * @return  true if the element was added. The element to be added must be of the
    * expected type; otherwise, the exception ElementInvalid is raised.
    */
    public boolean add_element_set_iterator(org.omg.CORBA.Any element)
    throws ElementInvalid
    {
        throw new NO_IMPLEMENT();
    }


   /**
    * Adds the given elements to the collection that this iterator points to. The elements
    * are added in the order of the input sequence of elements and the delivered semantics
    * is consistent with the semantics of the add_element_set_iterator operation. It is
    * essentially a sequence of add_element_set_iterator operations. The output
    * parameter actual_number is set to the number of elements added.
    *<p>NOT IMPLEMENTED</P>
    */
    public boolean add_n_elements_set_iterator(
      org.omg.CORBA.Any[] elements, org.omg.CORBA.IntHolder actual_number)
    throws ElementInvalid
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Sets the iterator to the state invalid, that is, "pointing to nothing". You may also say
    * that the iterator, in some sense, is set to NULL.
    * 
    */
    public void invalidate()
    {
        iterator.reset();
    }


   /**
    * Returns true if the Iterator is valid, that is points to an element of the collection or
    * is in the state in-between.
    */
    public boolean is_valid()
    {
        return store != null;
    }


   /**
    * Returns true if the iterator is in the state in-between.
    */
    public boolean is_in_between()
    {
        return false;
    }

   /**
    * Returns true if this iterator can operate on the given collection.
    * <p>NOT IMPLEMENTED</P>
    */
    public boolean is_for(org.omg.CosCollection.Collection collector)
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Returns true if this iterator is created with 'const' designation.
    */
    public boolean is_const()
    {
        return false;
    }

   /**
    * Returns true if the given iterator points to the identical element as this iterator.
    * <p>NOT IMPLEMENTED</P>
    */
    public boolean is_equal(org.omg.CosCollection.Iterator test)
    throws IteratorInvalid
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Creates a copy of this iterator.
    * <p>NOT IMPLEMENTED</P>
    */
    public org.omg.CosCollection.Iterator _clone()
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Assigns the given iterator to this iterator.
    * The given iterator must be created for the same collection as this iterator; otherwise,
    * the exception IteratorInvalid is raised.
    * <p>NOT IMPLEMENTED</P>
    */
    public void assign(org.omg.CosCollection.Iterator from_where)
    throws IteratorInvalid
    {
        throw new NO_IMPLEMENT();
    }

   /**
    * Destroys this iterator.
    */
    public void destroy()
    {
	  this.store = null;
	  this.iterator = null;
	  try
	  {
	      finalize();
	  }
	  catch( Throwable e )
	  {
	  }
    }

    //=========================================================================
    // Internal
    //=========================================================================

   /**
    * Test to see if the next entry in the list backing the iterator contains 
    * a resource maching the supplied type.  The side effect of a failure to 
    * match is that the iterator steps down the list until a matching type
    * is located, or returns false if match found.
    */
    protected boolean hasNext(  )
    {

	  if( !this.iterator.has_next() ) return false;
	  
	  //
        // a type constraint has been supplied and a next element exists 
        // so we need to check if the next entry matches the type, if not, 
        // move the iterator to the next, and do another hasNext test
	  //

        if( trace ) System.out.println("\tDELEGATE/hasNext (true) ");
	  try
        {
	      StorageObject s = this.iterator.peek();
            if( trace ) System.out.println("\tDELEGATE/peek: " + s );
		if(( s != null ) && evaluate( s, type ) )
		{
                if( trace ) System.out.println("\t\tDELEGATE/evaluate: true" );
		    return true;
		}
	      else
		{
                if( trace ) System.out.println("\t\tDELEGATE/evaluate: false" );
		    this.iterator.next();
		    return hasNext( );
		}
	  }
	  catch( Throwable e )
        {
            if( trace ) System.out.println("\tDELEGATE/hasNext: error: " + e );
		return false;
        }
    }

   /**
    * Returns true if the supplied LinkStorage object is equivilent with 
    * the supplied type (this implementation applies the test to the 
    * resource contained within the link within the storage object).
    */
    public abstract boolean evaluate( StorageObject s, TypeCode t );

   /**
    * Move the iterator to the next entry and return true if a new next 
    * entry exists.  A side effect of the method invocation is that the 
    * protected state member <code>store</code> is updated.
    * @return boolean true if there is a new next entry
    */
    protected boolean getNext( ) throws IteratorInvalid
    {
	  try
	  {
	      if( hasNext() )
	      {
	          store = this.iterator.next();
		    return true;
	      }
	      else
	      {
	 	    store = null;
		    return false;
		}
	  }
        catch( NoEntry e )
	  {
		store = null;
            throw new IteratorInvalid();
	  }
        catch( Throwable e )
	  {
		store = null;
            throw new IteratorInvalid();
	  }
    }
}
