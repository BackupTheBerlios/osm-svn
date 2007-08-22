/**
 */

package net.osm.ins;

import java.util.Vector;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CosNaming.NamingContextExtPackage.*;

import net.osm.ins.pss.NCStorageHome;
import net.osm.ins.pss.LOStorageHome;
import net.osm.ins.pss.POStorageHome;
import net.osm.ins.pss.*;


/**
 */
public class NamingContextServant extends NamingContextExtPOA implements LogEnabled, Contextualizable
{ 

    //===============================================  
    // State members 
    //===============================================  

    private Logger log;
    private ORB orb;
    
   /**
    * the reference to the NamingContext factory to create and find persistent NamingContext
    */
    private NCStorageHome namingContextStorageHome;
    
   /**
    * the reference to the NamingObject factory to create and find persistent NamingObject
    */
    private LOStorageHome namingObjectStorageHome;
    
   /**
    * The reference to the POStorage factory to create and find 
    * persistent POStorage instance
    */
    private POStorageHome proxyObjectStorageHome;
            
   /**
    * Root of the naming context tree.
    */
    public NCStorage nc_root;
    

    //===============================================  
    // Constructors
    //===============================================  

   /**
    * Constructor
    * @param orb
    * @param context - the PSS storage home for NCStorage storage types
    * @param local - the PSS storage home for local LOStorage storage types
    * @param proxy - the PSS storage home for remote POStorage storage types
    */
    protected NamingContextServant( ORB orb, NCStorageHome context, LOStorageHome local, POStorageHome proxy)
    {
        this.orb = orb;
        this.namingContextStorageHome = context;
        this.namingObjectStorageHome = local;
        this.proxyObjectStorageHome = proxy;
    }

    // ===========================================================
    // Loggable implementation
    // ===========================================================
    
   /**
    * Sets the log channel for this instance.
    */
    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

   /**
    * Returns the log channel for this instance.
    * @return Logger the logging channel
    */
    public Logger getLogger()
    {
        return log;
    }

    // ===========================================================
    // Contextualizable implementation
    // ===========================================================

   /**
    * Provide runtime context.
    */

    public void contextualize( Context context )
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("contextualize");
	  try
	  {
		nc_root = ((INSContext)context).getRootNamingContextStore();
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException( "failed to resolve INS context" );
        }
    }


    //===============================================  
    // Naming Context Implementation
    //===============================================  

   /**
    * Creates a binding of a name and an object in the naming
    * context. Naming contexts that are bound using bind do not
    * participate in name resolution when compound names are passed to
    * be resolved.
    *
    * @param n    The compound name for the object to bind
    * @param    obj    The object to bind
    *
    * @exception    NotFound        Indicates the name does not identify a binding.
    * @exception CannotProceed Indicates that the implementation has
    *                                given up for some reason. The client, however, may
    *                                be able to continue the operation at the returned
    *                                naming context.
    * @exception Indicates the name is invalid. (A name of length 0 is
    *                                invalid; implementations may place other
    *                                restrictions on names.)
    * @exception AlreadyBound Indicates an object is already bound to
    *                                the specified name. Only one object can be bound
    *                                to a particular name in a context. The bind and
    *                                the bind_context operations raise the AlreadyBound
    *                                exception if the name is bound in the context; the
    *                                rebind and rebind_context operations unbind the
    *                                name and rebind the name to the object passed as
    *                                an argument.
    */
    public void bind(NameComponent[] n, org.omg.CORBA.Object obj)
    throws NotFound, CannotProceed, InvalidName, AlreadyBound 
    { 
        
        if ( n.length == 0 ) throw new InvalidName();
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("\nBind an object ( " + n[n.length-1].id + " )");
        
        
        // get the complete component name of the object
        NameComponent[] componentName = getNameComponent( n );
        String componentName_str = org.openorb.util.NamingUtils.to_string ( componentName );
                
        if( getLogger().isDebugEnabled() ) getLogger().debug("name :" + componentName_str);
        
        try 
        {
            // make sure the object does not exist
            LOStorage no = this.namingObjectStorageHome.find_by_componentName( componentName_str );
            if( getLogger().isDebugEnabled() ) getLogger().debug(" NamingObject already bound " + componentName_str );
            throw new AlreadyBound();
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
	  {
            // create the persistent entry
            LOStorage no = ( ( LOStorageHome ) this.namingObjectStorageHome ).create( componentName_str, obj );
            addNamingObject ( componentName );
        }
    }

    
   /**
    * Creates a binding of a name and an object in the naming context
    * even if the name is already bound in the context. Naming contexts
    * that are bound using rebind do not participate in name resolution
    * when compound names are passed to be resolved.
     *
    * @param    n    The compound name for the object to rebind
    * @param    obj    The object to rebind
    *
    * @exception    NotFound        Indicates the name does not identify a binding.
    * @exception CannotProceed Indicates that the implementation has
    *                                given up for some reason. The client, however, may
    *                                be able to continue the operation at the returned
    *                                naming context.
    * @exception InvalidName Indicates the name is invalid. (A name of length 0 is
    *                                invalid; implementations may place other
    *                                restrictions on names.)
    */
    public void rebind(NameComponent[] n, org.omg.CORBA.Object obj)
    throws NotFound, CannotProceed, InvalidName { 
        
        if ( n.length == 0 ) throw new InvalidName();
                    
        if( getLogger().isDebugEnabled() ) getLogger().debug("\nRebind an object ( " + n[n.length-1].id + " )");
        
        // get the complete componentName of the object to bind
        NameComponent[] componentName = getNameComponent( n );
        String componentName_str = org.openorb.util.NamingUtils.to_string ( componentName );
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("name: " + componentName_str);
        try { 
            
            // search whether the persistent naming object already exists
            LOStorage no = this.namingObjectStorageHome.find_by_componentName( componentName_str );
            if( getLogger().isDebugEnabled() ) getLogger().debug(" NamingObject already exist " + componentName_str );
            no.namingObj( obj );
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
        { 
            // create the persistent entry
            LOStorage namingObject = this.namingObjectStorageHome.create( componentName_str, obj );
            addNamingObject ( componentName );
        }
    }

    
   /**
    * Names an object that is a naming context. Naming contexts that
    * are bound using bind_context() participate in name resolution
    * when compound names are passed to be resolved.
    *
    * @param    n    The compound name for the naming context to bind
    * @param    obj    The naming context to bind    
    *
    * @exception    NotFound        Indicates the name does not identify a binding.
    * @exception CannotProceed Indicates that the implementation has
    *                                given up for some reason. The client, however, may
    *                                be able to continue the operation at the returned
    *                                naming context.
    * @exception Indicates the name is invalid. (A name of length 0 is
    *                                invalid; implementations may place other
    *                                restrictions on names.)
    * @exception AlreadyBound Indicates an object is already bound to
    *                                the specified name. Only one object can be bound
    *                                to a particular name in a context. The bind and
    *                                the bind_context operations raise the AlreadyBound
    *                                exception if the name is bound in the context; the
    *                                rebind and rebind_context operations unbind the
    *                                name and rebind the name to the object passed as
    *                                an argument.
    */
    public void bind_context(NameComponent[] n, NamingContext nc)
    throws NotFound, CannotProceed, InvalidName, AlreadyBound 
    {
        
        if ( n.length == 0 ) throw new InvalidName();
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("Bind a context ( " + n[n.length-1].id + " )");
        
        boolean is_local = ( ( org.omg.CORBA.portable.ObjectImpl ) nc )._is_local();
        if( getLogger().isDebugEnabled() ) getLogger().debug("is local : " + is_local );
                
        // get the complete component name of the Naming context to bind
        NameComponent[] componentName = getNameComponent( n );
        String componentName_str = org.openorb.util.NamingUtils.to_string ( componentName );
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("componentName : " + componentName_str );
                        
        if (is_local)
        {
            try
            {
                // search whether the Naming context already exists
                NCStorage namingContext = this.namingContextStorageHome.find_by_componentName( componentName_str );
                if( getLogger().isDebugEnabled() ) getLogger().debug (" NamingContext already exist " + componentName_str );
                throw new AlreadyBound();      
            }
            catch ( org.omg.CosPersistentState.NotFound e )
            {
                try 
		    {
                    // set the nameComponent to the namingContext
                    byte[] id = _poa().reference_to_id( nc );
                    NCStorage namingContext = getNamingContext( id );
                    namingContext.componentName ( componentName_str );
                    
                    // add the naming context to its parent
                    addNamingContext ( componentName );
                }
                catch ( org.omg.PortableServer.POAPackage.WrongAdapter wrongAdapter ) 
                { 
                    if( getLogger().isErrorEnabled() ) getLogger().error("wrong adapter", wrongAdapter );
                }
                catch ( org.omg.PortableServer.POAPackage.WrongPolicy wrongPolicy ) 
                {
                    if( getLogger().isErrorEnabled() ) getLogger().error("wrong policy", wrongPolicy );
                }
            }
        }
        else 
	  { 
            try 
		{ 
                // search whether the POStorage already exists
                POStorage no = this.proxyObjectStorageHome.find_by_componentName( componentName_str );
                if( getLogger().isDebugEnabled() ) getLogger().debug(" ProxyNamingContext already exist : " + componentName_str );
                throw new AlreadyBound();
            }
            catch( org.omg.CosPersistentState.NotFound notFound ) 
		{ 
                // create the ProxyNamingContext
                this.proxyObjectStorageHome.create( componentName_str , nc );
                // add the Proxy Naming Context to its parent.
                addProxyNamingContext ( componentName );
            }
        }
    }

   /**
    * Creates a binding of a name and a naming context in the naming
    * context even if the name is already bound in the context. Naming
    * contexts that are bound using rebind_context() participate in
    * name resolution when compound names are passed to be resolved.
    *
    * @param    n    The compound name for the naming context to rebind
    * @param    obj    The naming context to rebind         
    *
    * @exception    NotFound        Indicates the name does not identify a binding.
    * @exception CannotProceed Indicates that the implementation has
    *                                given up for some reason. The client, however, may
    *                                be able to continue the operation at the returned
    *                                naming context.
    * @exception InvalidName Indicates the name is invalid. (A name of
    *                                length 0 is invalid; implementations may place
    *                                other restrictions on names.)
    * @exception CannotProceed
    */
    public void rebind_context(NameComponent[] n, NamingContext nc)
    throws NotFound, CannotProceed, InvalidName
    {
        if ( n.length == 0 ) throw new InvalidName();
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("Rebind a context ( " + n[n.length-1].id + " )");
        
        // get the complete componentName of the NamingContext to rebind
        NameComponent[] componentName = getNameComponent( n );
        String componentName_str = org.openorb.util.NamingUtils.to_string ( componentName );
        
        boolean is_local = ( ( org.omg.CORBA.portable.ObjectImpl ) nc )._is_local();
        
        if (is_local) { 
            
            try 
		{ 
                // search whether the Naming context already exists
                NCStorage namingContext = this.namingContextStorageHome.find_by_componentName( componentName_str );
                if( getLogger().isDebugEnabled() ) getLogger().debug (" NamingContext already exist " + componentName_str );
                // if a naming context already exist, it must be unbound.
                unbind(n);                
            }
            catch ( org.omg.CosPersistentState.NotFound ex )
            {
		}
            
            try 
		{
                // set the nameComponent to the namingContext
                byte[] id = _poa().reference_to_id( nc );
                NCStorage namingContext = getNamingContext( id );
                // case of a rebind of a Naming Context that is already bound somewhere else.
                if ( !namingContext.componentName().equals("") )
                    throw new CannotProceed( nc, n);
                namingContext.componentName ( componentName_str );
                addNamingContext ( componentName );
            }
            catch ( org.omg.PortableServer.POAPackage.WrongAdapter wrongAdapter ) 
		{
                if( getLogger().isErrorEnabled() ) getLogger().error("wrong adapter", wrongAdapter );
            }
            catch ( org.omg.PortableServer.POAPackage.WrongPolicy wrongPolicy ) 
		{
                if( getLogger().isErrorEnabled() ) getLogger().error("wrong policy", wrongPolicy );
            }
        }
        else 
	  { 
            
            // create the Proxy naming context
            this.proxyObjectStorageHome.create( componentName_str , nc );
            // add the Proxy Naming Context to its parent.
            addProxyNamingContext ( componentName );
        }
    }

   /**
    * Names can have multiple components; therefore, name resolution
    * can traverse multiple contexts.
    *
    * @param    n    The compound name for the object to resolve
    *
    * @exception    NotFound        Indicates the name does not identify a binding.
    * @exception CannotProceed Indicates that the implementation has
    *                                given up for some reason. The client, however, may
    *                                be able to continue the operation at the returned
    *                                naming context.
    * @exception InvalidName Indicates the name is invalid. (A name of
    *                                length 0 is invalid; implementations may place
    *                                other restrictions on names.)
    */
    public org.omg.CORBA.Object resolve(NameComponent[] n)
    throws NotFound, CannotProceed, InvalidName 
    { 
        if ( n.length == 0 ) throw new InvalidName();
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("Resolve an object ( " + n[n.length-1].id + " )");
        
        // get the complete componentName of the persistent naming object to resolve
        NameComponent[] componentName = getNameComponent( n );
        String componentName_str = org.openorb.util.NamingUtils.to_string ( componentName );
        if( getLogger().isDebugEnabled() ) getLogger().debug("componentName : " + componentName_str );
                
        try 
        {
            LOStorage no = this.namingObjectStorageHome.find_by_componentName( componentName_str );
            return no.namingObj();
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
        {
            try 
            {
                NCStorage nc = this.namingContextStorageHome.find_by_componentName( componentName_str );
                return createReference(nc);
            }
            catch ( org.omg.CosPersistentState.NotFound ex ) 
            {
                try 
                {
                    POStorage pnc = this.proxyObjectStorageHome.find_by_componentName ( componentName_str );
                    return pnc.ctx();
                }
                catch ( org.omg.CosPersistentState.NotFound nf ) 
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(" Object not found " + componentName_str );
                    throw new NotFound(NotFoundReason.missing_node, n);
                }
            }
        }
        
    }

   /**
    * The unbind operation removes a name binding from a context.
    *
    * @param n The compound name for the node to unbind ( an object or
    * a naming context )
    *
    * @exception    NotFound        Indicates the name does not identify a binding.
    * @exception CannotProceed Indicates that the implementation has
    *                                given up for some reason. The client, however, may
    *                                be able to continue the operation at the returned
    *                                naming context.
    * @exception InvalidName Indicates the name is invalid. (A name of
    *                                length 0 is invalid; implementations may place
    *                                other restrictions on names.)
    */
    public void unbind(NameComponent[] n)
    throws NotFound, CannotProceed, InvalidName
    {
        if ( n.length == 0 ) throw new InvalidName();
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("Unbind ( " + n[n.length-1].id + " )");
        
        // get the complete component Name of the naming object to unbind
        NameComponent[] componentName = getNameComponent( n );
        String componentName_str = org.openorb.util.NamingUtils.to_string ( componentName );
        if( getLogger().isDebugEnabled() ) getLogger().debug("componentName_str : " + componentName_str );
                
        try 
        { 
            
            // find if the node to unbind is a NamingContext
            NCStorage nc  = this.namingContextStorageHome.find_by_componentName( componentName_str );
            if( getLogger().isDebugEnabled() ) getLogger().debug("unbind the namingContext : " + componentName_str );
                        
            // remove the naming context from the children list of its parent
            removeNamingContext ( componentName );
            nc.componentName("");
        }
        catch ( org.omg.CosPersistentState.NotFound ex ) 
        {
            
            try 
		{
                // find if the node to unbind is a ProxyNamingContext
                POStorage pnc = this.proxyObjectStorageHome.find_by_componentName( componentName_str );
                if( getLogger().isDebugEnabled() ) getLogger().debug("unbind the ProxyNamingContext : " + componentName_str );
                                
                // remove the proxy naming context from the children list of its parent
                removeProxyNamingContext( componentName );
                pnc.destroy_object();
                                
            }
            catch ( org.omg.CosPersistentState.NotFound e ) 
		{
                
                try 
		    {
                    // find if the node to unbind is a naming object
                    LOStorage no = this.namingObjectStorageHome.find_by_componentName( componentName_str );
                    if( getLogger().isDebugEnabled() ) getLogger().debug("unbind the naming object : " + componentName_str);
                                    
                    // remove the naming object from the children list of its parent
                    removeNamingObject( componentName );
                    no.destroy_object();
                }
                catch ( org.omg.CosPersistentState.NotFound notFound ) 
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug("node not found : " + componentName_str );
                    throw new NotFound( NotFoundReason.not_context, n);
                }
            }
        }
    }

   /**
    * This operation returns a naming context implemented by the same
    * naming server as the context on which the operation was
    * invoked. The new context is not bound to any name.
    *
    * @return A new naming context ( this new naming context must be
    * binded )
    */
    public NamingContext new_context()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("Create a new context");
        
        NCStorageRef[] nc_children = new NCStorageRef[0];
        LOStorageRef[] no_children = new LOStorageRef[0];
        POStorageRef[] pnc_children = new POStorageRef[0];
        
        NCStorage newNCStorage = 
		this.namingContextStorageHome.create( 
			"", nc_children, no_children, pnc_children);
        return createReference( newNCStorage );
    }

   /**
    * This operation creates a new context and binds it to the name
    * supplied as an argument. The newly-created context is implemented
    * by the same naming server as the context in which it was bound
    * (that is, the naming server that implements the context denoted
    * by the name argument excluding the last component).
    *
    * @param    n    The compound name for the naming context to create and to bind.
    *
    * @exception    NotFound        Indicates the name does not identify a binding.
    * @exception CannotProceed Indicates that the implementation has
    *                                given up for some reason. The client, however, may
    *                                be able to continue the operation at the returned
    *                                naming context.
    * @exception InvalidName Indicates the name is invalid. (A name of
    *                                length 0 is invalid; implementations may place
    *                                other restrictions on names.)
    * @exception AlreadyBound Indicates an object is already bound to
    *                                the specified name. Only one object can be bound
    *                                to a particular name in a context.
    */
    public NamingContext bind_new_context(NameComponent[] n)
    throws NotFound, AlreadyBound, CannotProceed, InvalidName 
    { 
        
        if ( n.length == 0 ) throw new InvalidName();
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("bind_new_context : " + org.openorb.util.NamingUtils.to_string( n ) );
        
        NamingContext nc = new_context();
        bind_context(n,nc);

        return nc;
    }

   /**
    * The destroy operation deletes a naming context.
    *
    * @exception NotEmpty If the naming context contains bindings, the
    * NotEmpty exception is raised.
    */
    public void destroy()
        throws NotEmpty
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("Destroy a NamingContext" );
        
        NCStorage nc = null;

        try 
	  { 
            
            // Case of a destroy action on the root naming context
            nc = getNamingContext();
            if ( nc.equals ( nc_root ) ) 
	      {
                throw new org.omg.CORBA.NO_PERMISSION();
            }
            
            // if the Naming context is local
            NCStorageRef[] nc_children = nc.nc_children();
            LOStorageRef[] no_children = nc.no_children();
            POStorageRef[] pnc_children = nc.pnc_children();
        
            if ( ( nc_children.length == 0 ) && ( no_children.length == 0 ) && ( pnc_children.length == 0 )) 
            { 
                
                // if the Naming Context is not empty
                String componentName_str = nc.componentName();
                if( getLogger().isDebugEnabled() ) getLogger().debug("componentName : " + componentName_str);
            
                NameComponent[] componentName = null;
                try 
		    {
                    componentName = org.openorb.util.NamingUtils.to_name(componentName_str);
                }
                catch ( InvalidName in )
                { 
			  if( getLogger().isDebugEnabled() ) getLogger().debug("Invalid Name" ); 
			  return; 
		    }
                
                // remove the reference of the Naming context from the list of children of the parent
                removeNamingContext ( componentName );
                
                // destroy the persistent object
                nc.destroy_object();
            }
            else
            { 
                throw new NotEmpty();
            }
        }
        catch ( NotFound e ) 
        { 
            
            // if the Naming context to destroy is a Proxy Naming Context
            POStorage pnc = null;
            try { 
                pnc = getProxyNamingContext();
                // A Proxy Naming Context has no child ( It reprensents a distant Naming Context )
                pnc.destroy_object();
                
                // Report event ...
                String componentName_str = nc.componentName();
                if( getLogger().isDebugEnabled() ) getLogger().debug("componentName : " + componentName_str );
                
                NameComponent[] componentName = null;
                try 
		    {
                    componentName = org.openorb.util.NamingUtils.to_name(componentName_str);
                }
                catch ( InvalidName in )
                { 
		        if( getLogger().isDebugEnabled() ) getLogger().debug( "Invalid Name" ); 
                    return; 
		    }
            }
            catch ( NotFound ex )
            {
                if( getLogger().isDebugEnabled() ) getLogger().debug(" Binding not found ! " );
                return; 
            }
        }
    }

   /**
    * The list operation allows a client to iterate through a set of
    * bindings in a naming context.
    *
    * @param    how_many    Maximum number of elements into the binding list.
    * @param bl This parameter returns a list that contains all node of
    *             the naming context
    * @param bi This parameter returns a binding iterator to iterate in
    *           the list.
    *
    * @return The list operation returns at most the requested number
    *                of bindings in BindingList bl. If the naming context
    *                contains additional bindings, the list operation returns a
    *                BindingIterator with the additional bindings. If the
    *                naming context does not contain additional bindings, the
    *                binding iterator is a nil object reference.
    */
    public void list(int how_many, BindingListHolder bl, BindingIteratorHolder bi) { 
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("List all objects");
        
        int max = 0;
        NCStorage nc = null;
        try 
	  {
             nc = getNamingContext();
        }
        catch ( NotFound e) 
        {
		if( getLogger().isErrorEnabled() ) getLogger().error("naming context not found", e );
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
            
        if( getLogger().isDebugEnabled() ) getLogger().debug("nc : " + nc.componentName() );
        
        LOStorageRef[] no_children = nc.no_children();
        NCStorageRef[] nc_children = nc.nc_children();
        POStorageRef[] pnc_children = nc.pnc_children();
        
        int bindings_size = no_children.length + nc_children.length + pnc_children.length;
        if( getLogger().isDebugEnabled() ) getLogger().debug("bindings_size " + bindings_size );
        
        Binding[] bindings = new Binding[ bindings_size ];
        
        for ( int i =0 ; i < no_children.length ; i++ ) 
	  { 
            
            LOStorageRef child = no_children[i];
            
            LOStorage namingObject = ( LOStorage ) child.deref();
            
            bindings[i] = getBinding ( namingObject );
        }
        
        for ( int i=0; i < nc_children.length; i++ ) 
        { 
            NCStorage namingContext = ( NCStorage ) nc_children[i].deref();
            bindings[i + no_children.length] = getBinding( namingContext );
        }
        
        for ( int i=0; i < pnc_children.length; i++ ) 
        { 
            POStorage proxyNamingContext = ( POStorage ) pnc_children[ i ].deref();
            bindings[i - pnc_children.length] = getBinding ( proxyNamingContext ); 
        }
        
        if ( bindings_size < how_many ) 
	  {
            max = bindings_size;
        }
	  else
        {
            max = how_many;
        }

        if( getLogger().isDebugEnabled() ) getLogger().debug("max : " + max);
        bl.value = new Binding[max];
                
        for ( int k=0 ; k < max ; k++ ) 
        { 
            bl.value[k] = bindings[k];
        }
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("how_many : " + how_many);
        if( getLogger().isDebugEnabled() ) getLogger().debug("bindings_size : " + bindings_size );
	  Logger child = getLogger().getChildLogger("ITERATOR");
        if ( how_many < bindings_size ) 
	  { 
            Vector next = new Vector();
            
            for ( int i=how_many; i < bindings_size; i++ ) { 
                next.addElement( bindings[i] );
            }           
            BindingIteratorServant b = new BindingIteratorServant( child, this.orb, next );
            bi.value = b._this();
        }
        else 
	  {
            bi.value = new BindingIteratorServant ( child, this.orb, new Vector() )._this();
        }
    }
    
   /**
    * This operation accepts Name and returns a stringified name.
    *
    * @param     n    the name to stringified.
    * @exception InvalidName This exception is raised if the name is
    * invalid.
    */
    public String to_string(NameComponent[] n)
    throws InvalidName 
    { 
        return org.openorb.util.NamingUtils.to_string ( n );
    }
    
   /**
    * This operation accepts a stringified name and returns a Name.         
    *
    * @param     sn    the stringified name to transform to a name.
    * @exception InvalideName This exception is raised if the
    *                                 stringified name is syntactically malformed or
    *                                 violates an implementation limit.
    */
    public NameComponent[] to_name(String sn) throws InvalidName
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("to_name " + sn);
        return org.openorb.util.NamingUtils.to_name( sn );
    }


   /**
    * This operation takes an URL address and performs any escapes
    * necessary on the stringified name and returns a fully formed URL
    * string.
    *
    * @param     addr    the address ( for example myhost.xyz.com )
    * @param     sn        the stringified name to add to the URL
    * @return     the URL string format.
    *
    * @exception InvalidAddress This exception is raises if a address
    *                is invalid ( it means that the address does not
    *                respect the address format ).
    * @exception InvalidName This exception is raised if the
    *                                 stringified name is syntactically malformed or
    *                                 violates an implementation limit.
    */
    public String to_url(String addr, String sn) throws InvalidAddress, InvalidName
    {                
        if( getLogger().isDebugEnabled() ) getLogger().debug("to_url");
        return org.openorb.util.NamingUtils.to_url( addr, sn );
    }

   /**
    * This is a convenience operation that performs a resolve in the
    * same manner as NamingContext::resolve.  It accepts a stringified
    * name as an argument instead of a Name.
    *
    * @param     n    the stringified name of the object ( or naming context ) to resolve
    * @return     the resolved object.
    *
    * @exception     NotFound        Indicates the name does not identify a binding.
    * @exception     CannotProceed    Indicates that the implementation has given up for some reason. The
    *                                 client, however, may be able to continue the operation at the returned
    *                                 naming context.
    * @exception     InvalidName        Indicates the name is invalid. (A name of length 0 is invalid;
    *                                 implementations may place other restrictions on names.)
    */
    public org.omg.CORBA.Object resolve_str(String n) throws NotFound, CannotProceed, InvalidName
    {                        
        NameComponent [] name = org.openorb.util.NamingUtils.to_name( n );
        return resolve( name );
    }

    
   /**
    * This function adapt a Name for a stringified form
    */
    private String adaptName( String n )
    {
        String str = "";
        
        for ( int i=0; i<n.length(); i++ )
        {
            if ( n.charAt(i) == '/' )
                str = str + "\\/";
            else
            if ( n.charAt(i) == '.' )
                str = str + "\\.";
            else
                str = str + n.charAt(i);
        }        
        
        return str;
    }
    
   /**
    * This function removes extra information from string
    */
    private String removeExtra( String n )
    {
        String str = "";
        
        for ( int i=0; i<n.length(); i++ )
        {
            if ( n.charAt(i) == '\\' )
                continue;
            else
                str = str + n.charAt(i);
        }        
        
        return str;
    }
    
   /**
    * This function checks a address format.
    */
    private String checkFormat( String addr ) throws InvalidAddress
    {        
        String ad;
        int index;
        java.util.Vector list = new java.util.Vector();
        
        try
        {
            // Get all path of the Addree
            int old = 0;
            while ( true )
            {            
                index = addr.indexOf(",", old);
                
                if ( index == -1 )
                {                            
                    list.addElement( addr.substring( old ) );
                    
                    break;
                }
                else
                {            
                    list.addElement( addr.substring( old, index ) );
                    
                    old = index + 1;
                }
            }
            
            // Now, check addresses to get the object
            for ( int i=0; i<list.size(); i++ )
            {
                ad = ( String ) list.elementAt(i);
                
                index = addr.indexOf("@");
                
                // Is there a version data ?
                if ( index != -1 )            
                {                                        
                    index = ad.substring(0, index).indexOf(".");
                    
                    if ( index == -1 )
                        throw new InvalidAddress();
                }                                
            }
        }
        catch ( Exception ex )
        {
            throw new InvalidAddress();
        }
        
        return addr;
    }        
    
   /**
    * This function adapts a stringified named to a URL name.
    */
    private String adaptToURL( String sn )
        throws InvalidName
    {
        // To complete in future...
        return sn;
    }
    
   /**
    * Return the Name of this NamingContext
    */
    private NameComponent [] getNameComponent( NameComponent[] n) throws NotFound { 
        
        NCStorage p_namingContext = getNamingContext();
        
        try
        {
            NameComponent [] currentName = org.openorb.util.NamingUtils.to_name(p_namingContext.componentName());
            
            NameComponent [] nameComponent = new NameComponent[n.length + currentName.length ];
            for ( int k=0; k < currentName.length; k++) 
		{ 
                nameComponent[k] = currentName[k];
            }
            for ( int k=0; k < n.length ; k++ ) 
		{ 
                nameComponent[currentName.length + k] = n[k];
            }
        
            return nameComponent;
        }
        catch ( InvalidName e ) 
	  { 
            e.printStackTrace();
            return null;
        }
    }
    
    
   /**
    * return the persistent naming context that correspond to this current naming context
    */
    private NCStorage getNamingContext() throws NotFound 
    {
        return getNamingContext ( this._object_id() );
    }
    
   /**
    * return the persistent naming context by pid
    * @param pid the pid of the naming context
    */
    private NCStorage getNamingContext( byte[] pid ) throws NotFound 
    { 
        if( getLogger().isDebugEnabled() ) getLogger().debug("getNamingContext");
        try 
        { 
            return ( NCStorage ) this.namingContextStorageHome.get_catalog().find_by_pid ( pid );
        }
        catch ( org.omg.CosPersistentState.NotFound nf ) 
        { 
            throw new NotFound( );
        }
    }
    
   /**
    * return the persistent proxy naming context 
    */
    private POStorage getProxyNamingContext() throws NotFound {
         
        byte[] nc_id = this._object_id();
        try
        {
            return ( POStorage ) this.proxyObjectStorageHome.get_catalog().find_by_pid ( nc_id );
        }
        catch ( org.omg.CosPersistentState.NotFound nf ) { 
            throw new NotFound();
        }
    }
    
   /**
    * Add a naming context to the list of children that is stored in the parent object
    * @param the componentName of the object
    */
    private void addNamingContext ( org.omg.CosNaming.NameComponent[] componentName ) { 
        
        NCStorage parent = null;
        
        try 
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("addNamingContext : " + org.openorb.util.NamingUtils.to_string( componentName ));
            // get the parent of the NamingContext
            NameComponent[] parentName = new NameComponent[ componentName.length - 1];
            for ( int i=0; i < parentName.length; i++ ) 
            { 
                parentName[i] = componentName[i];
            }
        
            if( getLogger().isDebugEnabled() ) getLogger().debug("parent nameComponent : " + org.openorb.util.NamingUtils.to_string( parentName ));
            parent = this.namingContextStorageHome.find_by_componentName ( org.openorb.util.NamingUtils.to_string( parentName ) );
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
	  { 
            if( getLogger().isErrorEnabled() ) getLogger().error("naming context not found", e );
            return;
        }
        catch ( InvalidName e ) 
        { 
            if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered while adding a naming context", e );
        }
        
        try 
        { 
            
            // get the reference of the naming context to add
            NCStorageRef namingContextStorageRef = 
			this.namingContextStorageHome.find_ref_by_componentName( 
				org.openorb.util.NamingUtils.to_string ( componentName ) );
                
            // store the new children list to the parent
                
            NCStorageRef[] children = parent.nc_children();
            if( getLogger().isDebugEnabled() ) getLogger().debug("parent children : " + children.length);
                
            NCStorageRef[] update_children = new NCStorageRef[ children.length + 1 ];
    
            for ( int k=0 ; k < update_children.length - 1 ; k ++ ) 
	      { 
                update_children[k] = children[k];
            }
            update_children[update_children.length -1] = namingContextStorageRef;
            
            parent.nc_children(update_children);
        }
        catch ( InvalidName e ) 
	  {
		if( getLogger().isErrorEnabled() ) getLogger().error("unexpected exception while adding a naming context", e); 
        }
    }
    
   /**
    * Add a naming object to the list of children that is stored in the parent object
    * @param the componentName of the object
    */
    private void addNamingObject ( NameComponent[] componentName ) { 
        
        NCStorage parent = null;
        
        try {
            
            if( getLogger().isDebugEnabled() ) getLogger().debug(" addNamingObject " + org.openorb.util.NamingUtils.to_string(componentName));
        
            // get the parent of the object
            NameComponent[] parentName = new NameComponent[ componentName.length - 1];
            for ( int i=0; i < parentName.length; i++ ) 
		{ 
                parentName[i] = componentName[i];
            }
        
            String parentName_str = org.openorb.util.NamingUtils.to_string( parentName );
            if( getLogger().isDebugEnabled() ) getLogger().debug("parentName : " + parentName_str );
                            
            parent = this.namingContextStorageHome.find_by_componentName (parentName_str );
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("unexpected exception while adding a naming object", e); 
        }
        catch ( InvalidName e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name while adding a naming object", e ); 
        }
        
        try
        {
            // get the ref of the object
            LOStorageRef objectRef = 
			this.namingObjectStorageHome.find_ref_by_componentName( 
				org.openorb.util.NamingUtils.to_string(componentName) );
        
            // store the new children list to the parent
            LOStorageRef[] children = parent.no_children();
            LOStorageRef[] update_children = new LOStorageRef[ children.length + 1 ];
            for ( int k=0 ; k < update_children.length - 1 ; k ++ ) 
		{ 
                update_children[k] = children[k];
            }
            update_children[update_children.length -1] = objectRef;
            parent.no_children( update_children );
        }
        catch ( InvalidName e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name while adding a naming object", e); 
        }
    }
    
   /**
    * Add the proxy naming context to the list of children that is stored in the parent object
    * @param the componentName of the object
    */
    private void addProxyNamingContext ( NameComponent[] componentName ) { 
        
        // get the parent of the NamingContext
        NameComponent[] parentName = new NameComponent[ componentName.length - 1];
        for ( int i=0; i < parentName.length; i++ ) 
	  { 
            parentName[i] = componentName[i];
        }
        NCStorage parent = null;
        try 
	  { 
            parent = this.namingContextStorageHome.find_by_componentName ( org.openorb.util.NamingUtils.to_string( parentName ));
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
	  { 
            if( getLogger().isErrorEnabled() ) getLogger().error("unexpected error while adding a proxy naming context", e);
        }
        catch ( InvalidName e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name while adding a proxy naming context", e); 
        }
        
        try
        {
            // get the reference of the proxy naming context to add
            POStorageRef proxyNCStorageRef = 
			this.proxyObjectStorageHome.find_ref_by_componentName( 
				org.openorb.util.NamingUtils.to_string(componentName ));
        
            // store the new children list to the parent
        
            POStorageRef[] children = parent.pnc_children();
            POStorageRef[] update_children = new POStorageRef[ children.length + 1 ];
            for ( int k=0 ; k < update_children.length - 1 ; k ++ ) { 
                update_children[k] = children[k];
            }
            update_children[update_children.length -1] = proxyNCStorageRef;
            parent.pnc_children ( update_children );
        }
        catch ( InvalidName e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name while adding a proxy naming context", e); 
        }
    }
    
   /**
    * Remove the naming object from the list of children that is stored in the parent object
    * @param the componentName of the object
    */
    private void removeNamingObject ( NameComponent[] componentName ) { 
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("removeNamingObject");
        // get the parent of the object
        NameComponent[] parentName = new NameComponent[ componentName.length - 1];
        for ( int i=0; i < parentName.length; i++ ) 
	  { 
            parentName[i] = componentName[i];
        }
        NCStorage parent = null;
        try 
	  {
            parent = this.namingContextStorageHome.find_by_componentName ( org.openorb.util.NamingUtils.to_string( parentName ) );
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("unexpected exception while removing a naming object", e); 
        }
        catch ( InvalidName e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered while removing a naming object", e); 
        }
        
        try 
	  { 
            
            // get the ref of the object
        
            LOStorageRef objectRef = 
			this.namingObjectStorageHome.find_ref_by_componentName( 
				org.openorb.util.NamingUtils.to_string ( componentName ) );
            String objectRefName = ( ( LOStorage ) objectRef.deref() ).componentName();
        
            // remove the objectRef from the list of the children of the naming context
            LOStorageRef[] children = parent.no_children();
            LOStorageRef[] update_children = new LOStorageRef[ children.length - 1 ];
            int index = children.length - 1;
            for ( int k=0; k < children.length -1 ; k++ ) 
		{ 
                String childrenName = ( ( LOStorage ) children[k].deref() ).componentName();
                if ( childrenName.equals( objectRefName ) ) 
		    { 
                    index = k;
                    break;
                }
                else 
		    {
                    update_children[k] = children[k];
                }
            }
        
            for ( int i = index + 1; i < children.length ; i++ ) 
		{ 
                update_children[i - 1] = children[i];
            }
            
            parent.no_children ( update_children );
        }
        catch ( InvalidName e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered while removing a naming object", e ); 
        }
    }
    
   /**
    * Remove the naming context from the list of children that is stored in the parent object
    * @param the componentName of the object
    */
    private void removeNamingContext ( NameComponent[] componentName ) 
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("removeNamingContext");
        // get the parent of the object
        NameComponent[] parentName = new NameComponent[ componentName.length - 1];
        for ( int i=0; i < parentName.length; i++ ) 
	  { 
            parentName[i] = componentName[i];
        }
        NCStorage parent = null;
        try 
	  {
            parent = this.namingContextStorageHome.find_by_componentName ( org.openorb.util.NamingUtils.to_string( parentName ) );
            
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
        { 
		if( getLogger().isErrorEnabled() ) getLogger().error("unexpected not-found exception while removing a naming context", e ); 
        }
        catch ( InvalidName e ) 
        { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered while removing a naming context", e ); 
        }
        
        try 
        {
            
            // get the ref of the object
            
            NCStorageRef objectRef = 
			this.namingContextStorageHome.find_ref_by_componentName( 
				org.openorb.util.NamingUtils.to_string( componentName ) );
            String objectRefName = ( ( NCStorage )objectRef.deref()).componentName();
            if( getLogger().isDebugEnabled() ) getLogger().debug("find ref  " + objectRefName);
            // remove the objectRef from the list of the children of the naming context
        
            NCStorageRef[] children = parent.nc_children();
        
            NCStorageRef[] update_children = new NCStorageRef[ children.length - 1 ];
            int index = children.length - 1;
            
            for ( int k=0; k < children.length - 1 ; k++ ) 
		{ 
                String childrenName = ( (NCStorage )children[k].deref() ).componentName();
                if( getLogger().isDebugEnabled() ) getLogger().debug ("children " + k + " " + childrenName);
                if ( childrenName.equals( objectRefName ) ) 
                { 
                    index = k;
                    break;
                }
                else 
                {
                    update_children[k] = children[k];
                }
            }
            
            if( getLogger().isDebugEnabled() ) getLogger().debug("index : " + index );
            for ( int i = index + 1; i < children.length ; i++ ) 
            { 
                update_children[i - 1] = children[i];
            }
            
            parent.nc_children ( update_children );
        }
        catch ( InvalidName e ) 
        { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered while removing a naming context", e ); 
        }
    }
    
   /**
    * Remove the proxy naming context from the list of children that is stored in the parent object
    * @param the componentName of the object
    */
    private void removeProxyNamingContext ( NameComponent[] componentName ) 
    { 
        // get the parent of the object
        NameComponent[] parentName = new NameComponent[ componentName.length - 1];
        for ( int i=0; i < parentName.length; i++ ) 
	  { 
            parentName[i] = componentName[i];
        }
        NCStorage parent = null;
        try 
	  {
            parent = this.namingContextStorageHome.find_by_componentName ( org.openorb.util.NamingUtils.to_string (parentName) );
        }
        catch ( org.omg.CosPersistentState.NotFound e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("unexpected error while removing a proxy naming context", e); 
        }
        catch ( InvalidName e ) 
	  { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered while removing a proxy naming context", e); 
        }
        
        try
        {
            // get the ref of the object
        
            POStorageRef objectRef = 
			this.proxyObjectStorageHome.find_ref_by_componentName( 
				org.openorb.util.NamingUtils.to_string(componentName) );
            String objectRefName = ( (POStorage) objectRef.deref() ).componentName();
        
            // remove the objectRef from the list of the children of the naming context
        
            POStorageRef[] children = parent.pnc_children();
        
            POStorageRef[] update_children = new POStorageRef[ children.length - 1 ];
            int index = children.length - 1;
            for ( int k=0; k < children.length - 1 ; k++ ) 
		{ 
                String childrenName = ( (POStorage) children[k].deref()).componentName();
                if ( childrenName.equals( objectRefName ) ) 
		    { 
                    index = k;
                    break;
                }
                else 
		    {
                    update_children[k] = children[k];
                }
            }
        
            for ( int i = index + 1; i < children.length ; i++ ) 
		{ 
                update_children[i - 1] = children[i];
            }
            
            parent.pnc_children ( update_children );
        }
        catch ( InvalidName e ) 
	  {
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered while removing a proxy naming context", e); 
        }
    }
    
   /**
    * Create the naming context Corba reference from a persistent naming context 
    *   The pid of the Corba object correspond to the pid of the persistent object
    * @param the persistent naming context
    * @return the naming context
    */
    private NamingContext createReference( NCStorage nc ) 
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("createReference");
        byte[] nc_pid = nc.get_pid();
        org.omg.CORBA.Object ncObj = null;
        ncObj = _poa().create_reference_with_id(nc_pid, org.omg.CosNaming.NamingContextHelper.id());
        return NamingContextHelper.narrow(ncObj);
    }
    
   /**
    * Create a binding object from a persistent naming object, specifying the binding_name and the binding_type
    * @param no the naming object
    * @return the binding object
    */
    private Binding getBinding ( LOStorage no ) { 
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("getBinding namingObject");
        Binding binding = new Binding();
        
        try
        {
            org.omg.CosNaming.NameComponent[] name = new org.omg.CosNaming.NameComponent[1];
            org.omg.CosNaming.NameComponent[] componentName = org.openorb.util.NamingUtils.to_name ( no.componentName() );
            name[0] = componentName[componentName.length -1  ];
            binding.binding_name = name;
            binding.binding_type = org.omg.CosNaming.BindingType.nobject;
        }
        catch ( InvalidName e )
        {
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered in getBinding", e ); 
        }
        
        return binding;
        
    }
    
   /**
    * Create a binding object from a persistent naming context, specifying the binding_name and the binding_type
    * @param no the naming context
    * @return the binding object
    */
    private Binding getBinding ( NCStorage nc ) {
        
        if( getLogger().isDebugEnabled() ) getLogger().debug("getBinding namingContext");
        Binding binding = new Binding();
        
        try 
	  {
            NameComponent[] name = new NameComponent[1];
            if( getLogger().isDebugEnabled() ) getLogger().debug("componentName : " + nc.componentName());
            NameComponent[] componentName = org.openorb.util.NamingUtils.to_name ( nc.componentName() );
            name[0] = componentName[componentName.length -1  ];
            binding.binding_name = name;
            binding.binding_type = BindingType.ncontext;
        }
        catch ( InvalidName e ) 
        { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered in getBinding", e); 
        }
        
        return binding;
    }
    
   /**
    * Create a binding object from a persistent proxy naming context, specifying the binding_name and the binding_type
    * @param no the proxy naming context
    * @return the binding object
    */
    private Binding getBinding ( POStorage pnc ) {
        
        Binding binding = new Binding();
        
        try 
        {
            NameComponent[] name = new NameComponent[1];
            NameComponent[] componentName = org.openorb.util.NamingUtils.to_name ( pnc.componentName() );
            name[0] = componentName[componentName.length -1  ];
            binding.binding_name = name;
            binding.binding_type = BindingType.ncontext;
        }
        catch ( InvalidName e ) 
        { 
		if( getLogger().isErrorEnabled() ) getLogger().error("invalid name encountered in getBinding", e); 
        }
        
        return binding;
    }

   /**
    * return the path of the naming context. 
    * The Name that is stored in persistence contains the root name "NameService"
    * The returned path correspond to that NameComponent without the root.
    * @param the componentName including the root node
    * @return the componentName without the root node
    */
    private NameComponent[] getPath ( NameComponent[] componentName ) {
        
        NameComponent[] path = new NameComponent[ componentName.length - 1];
        for ( int k=1; k < componentName.length ; k++ ) { 
            path[k -1] = componentName[k];
        }    
        return path;
    }
}
