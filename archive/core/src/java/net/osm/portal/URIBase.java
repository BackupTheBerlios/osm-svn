/*
 * @(#)URIBase.java
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

import java.io.Serializable;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.URI;
import net.osm.discovery.Score;

/**
ValueFactory for URI 
*/

public class URIBase extends URI implements ValueFactory
{

    // constructors

    public URIBase( ){}

    public URIBase(String value ){
	  this.value = value;
    }

    // operations from ValueFactory

    public Serializable read_value( InputStream is ){
	  return is.read_value( new URIBase() );
    }

    // utilities

    public String toString() {
	  return this.value;
    }


} // URIBase
