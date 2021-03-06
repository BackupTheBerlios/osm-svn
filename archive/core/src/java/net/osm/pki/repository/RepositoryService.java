/*
 * @(#)RepositoryService.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 12/03/2001
 */


package net.osm.pki.repository;

import org.omg.CORBA.ORB;
import org.omg.PKIRepository.Repository;


/**
 * The RepositoryService is an interface facilitating access to a PKI repository.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface RepositoryService 
{

   /**
    * Returns the realm authenticator.
    * @return Repository the PKI repository service
    */
    public Repository getRepository( );
    
}



