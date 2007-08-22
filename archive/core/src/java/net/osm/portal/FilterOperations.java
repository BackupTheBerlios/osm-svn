
/*
 * @(#)FilterOperations.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.portal;


/**
FilterOperations interface.
*/

public interface FilterOperations
{
    public String name();

    public ScoreBase measure( Object source );

    public boolean equivilent( int n, Object source );

} // FilterOperations
