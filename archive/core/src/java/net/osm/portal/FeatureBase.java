/*
 * @(#)FeatureBase.java
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
import net.osm.discovery.Feature;
import net.osm.discovery.Entry;
import net.osm.discovery.Content;
import net.osm.discovery.Score;

public class FeatureBase extends Feature implements ValueFactory {
    
    // constructor

    public FeatureBase( ){}

    public FeatureBase(String name, String value ){
	  this.name = name;
	  this.value = value;
    }

    // operations from ValueFactory

    public java.io.Serializable read_value( InputStream is ){
	  return is.read_value( new FeatureBase() );
    }

    // operations from Entry

    public String name() {
	  return this.name;
    }

    // utilities

    public String toString() {
	  return "FeatureBase[" + this.name + ";" + this.value + "]";
    }

} // FeatureBase
