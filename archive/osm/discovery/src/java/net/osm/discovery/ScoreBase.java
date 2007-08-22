/*
 * @(#)ScoreBase.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.discovery;

import java.io.Serializable;
import java.math.BigDecimal;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.Score;


public class ScoreBase extends Score implements ValueFactory{

    public ScoreBase( ) {
        this.value = 0;
        this.base = 0;
    }

    public ScoreBase( int i, int j ) {
        this.value =  i;
        this.base = j;
    }

    // from ValueFactory

    public Serializable read_value( InputStream is ){
	  return is.read_value( new ScoreBase() );
    }

    // from net.osm.discovery.Score

    public Score score( int value, int base ) {
        this.value = this.value + value;
        this.base = this.base + base;
	  return this;
    }

    public Score increment( Score s ) {
	  if( s == null ) return this;
        return this.score( s.value, s.base );
    }

    public int ground( ) {
        return toInteger( this );
    };

    // utilities

    public static int toInteger( Score score ) {
	  double aa = new Integer( score.value ).doubleValue();
	  double bb = new Integer( score.base ).doubleValue();
	  return (new Double(((aa/bb)*100))).intValue();
    }


    public String toString( ) {
	  return "[" + this.value + ";" + this.base + "]";
    }

} // Score
