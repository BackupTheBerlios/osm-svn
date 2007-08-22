package net.osm.hub.resource;

import net.osm.realm.AccessPolicyBase;
import net.osm.realm.AccessTable;
import net.osm.realm.AccessTableBase;
import net.osm.realm.AccessDescriptorBase;
import net.osm.realm.AccessDescriptor;

/**
 * A table of access descriptions for a particular role.
 */
public class StructuredPushSupplierAccessPolicy extends AccessPolicyBase
{

    //=======================================================
    // static
    //=======================================================

    private static AccessTableBase ownerTable;
    private static AccessTableBase userTable;
    private static AccessTable[] defaultTables;

    static
    {
        ownerTable = new AccessTableBase( "owner", true, new AccessDescriptor[0]);
        userTable = new AccessTableBase( "user", true, new AccessDescriptor[0]);
        defaultTables = new AccessTable[]{ userTable, ownerTable };
    }

    //=======================================================
    // constructors
    //=======================================================

   /**
    * Default constructor used during internalization.
    */
    public StructuredPushSupplierAccessPolicy()
    {
        super( defaultTables );
    }

}

