/*
 * @(#)UtcTBase.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @version 1.0 29/07/2000
 */


package net.osm.portal;

import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import net.osm.discovery.UtcT;

import net.osm.time.TimeUtils;


/**
ValueFactory for Discovery.UtcT 
*/

public class UtcTBase extends UtcT implements ValueFactory
{

    // constructors

    public UtcTBase( ){}

    public UtcTBase( org.omg.TimeBase.UtcT time ){
	  this.value = time;
    }

    public UtcTBase( Date date ){
	  this.value = TimeUtils.dateToUtc( date );
    }

    // operations from value factory

    public Serializable read_value( InputStream is ){
	  return is.read_value( new UtcTBase() );
    }

    // from UtcT

    public Date date() {
	  return convertUtcTToDate( this );
    }

    // utilities

    public String toString() {
	  return DateFormat.getDateInstance().format( this.date() );
    }

    public static Date convertUtcTToDate( UtcT utct ) {
	  return TimeUtils.convertToDate( utct.value );
    }

    public static UtcT convertDateToUtcT( Date date ) {
	  return new UtcTBase( TimeUtils.dateToUtc( date ));
    }

    public boolean equals( Object obj ) {
	  try{
		Long j = new Long( this.value.time );
		Long k = new Long( ((UtcT)obj).value.time );
		return j.equals( k );
	  } catch (Exception e) {
		return false;
	  }
    }

    public static UtcT now( ){
	   return convertDateToUtcT( new Date() );
    }


} // KeyBase
