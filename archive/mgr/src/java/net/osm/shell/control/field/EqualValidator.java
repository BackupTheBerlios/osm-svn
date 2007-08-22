/*
 * @(#)EmptyValidator.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 22/08/2001
 */

package net.osm.shell.control.field;

/**
 * Tests for equality against a supplied value.
 */

public class EqualValidator implements Validator
{

    private boolean trace = false;
    private Object object;
    private boolean mode;

    private NotEmptyValidator notEmpty = new NotEmptyValidator();

    public EqualValidator( Object object, boolean mode )
    {
        this.mode = mode;
        setBase( object );
    }

   /**
    * Set the base reference object against which the equality test
    * will be made.
    */
    public void setBase( Object object )
    {
        this.object = object;
    }

   /**
    * This validator
    * returns true the supplied source argument is equal to the 
    * constructor supplied object.
    */
    public boolean verify( Object source )
    {
	  if( trace )	
	  {
            System.out.println("EqualValidator");
            System.out.println("\t object: '" + object + "'");
            System.out.println("\t source: '" + source + "'");
            System.out.println("\t not empty: " + notEmpty.verify( source ) );
            System.out.println("\t equals: " + object.equals( source ) );
            System.out.println("\t mode: " + mode );
            System.out.println("\t result: " + doVerify( source ) );
            System.out.println("\n");
        }
        return doVerify( source );
    }

    public boolean doVerify( Object source )
    {
        if( !notEmpty.verify( source ) ) return false;
	  if( mode )
	  {
            return object.equals( source );
        }
        else
	  {
            return !object.equals( source );
        }
    }

}
