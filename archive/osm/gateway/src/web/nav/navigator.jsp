
<%@ page errorPage="../exception.jsp" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%@ page import="net.osm.adapter.ServiceAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%@ page import="net.osm.session.user.PrincipalAdapter" %>
<%@ page import="net.osm.session.resource.AbstractResourceAdapter" %>
<%@ page import="net.osm.session.task.TaskAdapter" %>
<%@ page import="net.osm.gateway.util.ChooserUtil" %>
<%! HomeAdapter m_gateway; %>
<%! PrincipalAdapter m_principal; %>
<%! String m_services_string; %>
<%! String m_desktop_string; %>
<%! String m_tasks_string; %>
<%
    m_gateway = (HomeAdapter) application.getAttribute("net.osm.session");
    if( m_gateway != null )
    {

        //
        // generate the list of gateway services
        //

        StringBuffer buffer = new StringBuffer();
        try
        {
            String[] names = m_gateway.getNames();
            for( int i=0; i<names.length; i++ )
            {
                ServiceAdapter adapter = (ServiceAdapter) m_gateway.lookup( names[i] );
                String service = ChooserUtil.getChooserJavacript( 
                  "services", m_gateway.lookup( names[i] ) );
                buffer.append( "\n" + service );
            }
        }
        catch( Throwable e )
        {
            System.out.println("Internal error while preparing services breakdown.");
            e.printStackTrace();
        }
        finally
        {
            m_services_string = buffer.toString();
        }

        //
        // generate the list of workspaces in the desktop
        //

        buffer = new StringBuffer();
        try
        {
            m_principal = m_gateway.resolve_user( true );
            Iterator iterator = m_principal.getDesktop().getContained();
            while( iterator.hasNext() )
            {
                AbstractResourceAdapter adapter = (AbstractResourceAdapter) iterator.next();
                String name = adapter.getName();
                if( name.indexOf("'") > -1 )
                {
                    name = name.substring(0,name.indexOf("'"))
                      + "\\"
                      + name.substring( name.indexOf("'"), name.length() );
                    System.out.println(
                      "WARNING: the resource name contains a javascript quote "
                      + " character which may raise a javascript error.\n"
                      + "name: " + name );
                }
                buffer.append( 
                   "insDoc(desktop, gLnk(0, \"<span class='tree-node-text'>" 
                   + name
                   + "</span>\", \"../" + adapter.getURL() + "\", '"
                   + name + "', \"images/jmenu/jaxm_profile02.gif\", \"" + name + "\"));\n\n" );
            }
        }
        catch( Throwable e )
        {
            System.out.println("Internal error while preparing desktop breakdown.");
            e.printStackTrace();
        }
        finally
        {
            m_desktop_string = buffer.toString();
        }

        //
        // generate the list of tasks owned by the principal
        //

        buffer = new StringBuffer();
        try
        {
            Iterator iterator = m_principal.getTasks();
            while( iterator.hasNext() )
            {
                AbstractResourceAdapter adapter = (AbstractResourceAdapter) iterator.next();
                String name = adapter.getName();
                if( name.indexOf("'") > -1 )
                {
                    name = name.substring(0,name.indexOf("'"))
                      + "\\"
                      + name.substring( name.indexOf("'"), name.length() );
                    System.out.println(
                      "WARNING: the resource name contains a javascript quote "
                      + " character which may raise a javascript error.\n"
                      + "name: " + name );
                }
                buffer.append( 
                   "insDoc( tasks, gLnk(0, \"<span class='tree-node-text'>" 
                   + name
                   + "</span>\", \"../" + adapter.getURL() + "\", \""
                   + name + "\", \"images/jmenu/jaxm_profile02.gif\", '" 
                   + name + "'));\n\n" );
            }
        }
        catch( Throwable e )
        {
            System.out.println("Internal error while preparing tasks breakdown.");
            e.printStackTrace();
        }
        finally
        {
            m_tasks_string = buffer.toString();
        }

    }

%>

<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML//EN">
<HTML>
<HEAD>
<TITLE>MenuTree</TITLE>
<LINK REL="stylesheet" TYPE="text/css" HREF="../css/clustmgr-style.css">
</head>
<body class="tree-body" ONRESIZE="if (document.layers) self.location.reload(true)">
<script src="js/menutree.js"> </script>
<script src="js/browserVersion.js"> </script>
<script> USETEXTLINKS = 1</script>
<script>

    foldersTree = gFld("<span class='tree-node-text'>Gateway</span>", "../welcome.jsp", 
        'Gateway', "images/jmenu/folder_16_pad.gif", "top");
	
    services = insFld(foldersTree, gFld("<span class='tree-node-text'>Services</span>", 
      "../services-welcome.jsp", 'services', 
        "images/jmenu/folder_16_pad.gif", "main"));

    <%= m_services_string %>

    principal = insFld(foldersTree, gFld("<span class='tree-node-text'>Principal</span>", 
	  "../principal", 'principal', 
        "images/jmenu/folder_16_pad.gif", "principal"));

    desktop = insFld(principal, gFld("<span class='tree-node-text'>Desktop</span>", 
        "../principal?view=desktop", 'desktop', 
        "images/jmenu/folder_16_pad.gif", "desktop"));

    <%= m_desktop_string %>

    tasks = insFld(principal, gFld("<span class='tree-node-text'>Tasks</span>", 
        "../principal?view=tasks", 'tasks', 
        "images/jmenu/folder_16_pad.gif", "tasks"));

    <%= m_tasks_string %>

</script>
<script>
initializeDocument();
</script>
</body>
</html>

