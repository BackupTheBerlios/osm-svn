/*
 * @(#)Installer.java
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

import org.omg.CORBA.ORB;
import net.osm.discovery.DescriptionHelper;
import net.osm.discovery.DisclosureHelper;
import net.osm.discovery.DisclosurePolicyHelper;
import net.osm.discovery.FeatureHelper;
import net.osm.discovery.IdentifierHelper;
import net.osm.discovery.KeyHelper;
import net.osm.discovery.ChainHelper;
import net.osm.discovery.ReceiptHelper;
import net.osm.discovery.ScoreHelper;
import net.osm.discovery.SelectionHelper;
import net.osm.discovery.SelectionSetHelper;
import net.osm.discovery.URIHelper;
import net.osm.discovery.UtcTHelper;
import net.osm.discovery.CompositeFilterHelper;
import net.osm.discovery.ContentFilterHelper;
import net.osm.discovery.ScalarFilterHelper;

/**
 * Register value implememtations for the net.osm.discovery services.
 */

public class PortalSingleton
{
    public static void init( ORB orb ) 
    {
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			CompositeFilterHelper.id(), new DefaultCompositeFilter());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			ContentFilterHelper.id(), new DefaultContentFilter());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			ScalarFilterHelper.id(), new DefaultScalarFilter());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			ChainHelper.id(), new ChainBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			ScoreHelper.id(), new ScoreBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			DisclosurePolicyHelper.id(), new DisclosurePolicyBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			IdentifierHelper.id(), new IdentifierBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			KeyHelper.id(), new KeyBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			FeatureHelper.id(), new FeatureBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			DescriptionHelper.id(), new DescriptionBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			ReceiptHelper.id(), new ReceiptBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			SelectionHelper.id(), new SelectionBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			UtcTHelper.id(), new UtcTBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			SelectionSetHelper.id(), new SelectionSetBase());
	 ((org.omg.CORBA_2_3.ORB) orb).register_value_factory( 
			URIHelper.id(), new URIBase());
    }
}
