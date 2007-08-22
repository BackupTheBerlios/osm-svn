/*
 * @(#)Server.java
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

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Iterator;

import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.TimeBase.UtcT;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.CascadingException;
import org.apache.orb.ORB;
import org.apache.orb.util.IOR;

import org.openorb.CORBA.LoggableLocalObject;


/**
 * Resource registration and discover implementation that provides 
 * formal support for traceable service referral.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class PortalProvider extends PortalServer implements Portal
{
    
    //================================================================
    // Registry
    //================================================================
                        
    /**
     * Registration of a Description.  
     */
    public Receipt register(Artifact artifact)
        throws ExistingArtifact, RegistrationDenied, MallformedArtifact
    {
        return super.m_portal.register( artifact );
    }

    /**
     * Get the artifact based on a receipt.
     */
    public Artifact select( Receipt receipt )
        throws UnknownReceipt, MallformedReceipt, ArtifactUnavailable
    {
        return super.m_portal.select( receipt );
    }

    /**
     * Replace an existing artifact with another.
     */
    public Receipt update(Receipt receipt, Artifact artifact)
        throws RegistrationDenied, UnknownReceipt, MallformedReceipt, MallformedArtifact
    {
        return super.m_portal.update( receipt, artifact );
    }

    /**
     * Retract an existing artifact.
     */
    public Receipt retract(Receipt receipt)
        throws UnknownReceipt, MallformedReceipt, RetractionDenied
    {
        return super.m_portal.retract( receipt );
    }

    //================================================================
    // Directory
    //================================================================

    /**
     * The locate operation provides support for the location of a specific 
     * artifact based on a filter argument. 
     */
    public Artifact locate(Filter filter)
        throws NoMatch, InvalidFilter
    {
        return super.m_portal.locate( filter );
    }

    /**
     * The find operation returns a SelectionSet containing a ranked sequence of
     * Selection instances based on a filter argumement.
     */
    public SelectionSet find(Filter filter)
        throws NoMatch, InvalidFilter
    {
        return super.m_portal.find( filter );
    }

}
