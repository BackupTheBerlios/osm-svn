
foldersTree = gFld("<span class='tree-node-text'>Gateway</span>", "../welcome.jsp", 
                'Gateway', "images/jmenu/folder_16_pad.gif", "top")
	
services = insFld(foldersTree, gFld("<span class='tree-node-text'>Services</span>", 
						"../gateway.jsp?view=services", 'services', 
                                                "images/jmenu/folder_16_pad.gif", "main"))

principal = insFld(foldersTree, gFld("<span class='tree-node-text'>Principal</span>", 
						"../gateway.jsp?view=principal", 'principal', 
                                                "images/jmenu/folder_16_pad.gif", "main"))

insDoc(principal, gLnk(0, "<span class='tree-node-text'>Profile</span>", 
				"../principal.jsp?view=profile", 'pprofile', "images/jmenu/jaxm_profile02.gif", "pprofile"))

insFld(principal, gFld("<span class='tree-node-text'>Desktop</span>", 
				"../principal.jsp?view=desktop", 'desktop', "images/jmenu/folder_16_pad.gif", "desktop"))

insFld(principal, gFld("<span class='tree-node-text'>Tasks</span>", 
				"../principal.jsp?view=tasks", 'tasks', "images/jmenu/folder_16_pad.gif", "tasks"))



