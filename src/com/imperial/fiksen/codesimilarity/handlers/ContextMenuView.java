package com.imperial.fiksen.codesimilarity.handlers;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.dialogs.ViewContentProvider;
import org.eclipse.ui.internal.dialogs.ViewLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class ContextMenuView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
	    TableViewer viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
	            | SWT.V_SCROLL);
	        viewer.setContentProvider(new ViewContentProvider());
	        //viewer.setLabelProvider(new ViewLabelProvider());
	        viewer.setInput(getViewSite());
	        // This is new code
	        // First we create a menu Manager
	        MenuManager menuManager = new MenuManager();
	        Menu menu = menuManager.createContextMenu(viewer.getTable());
	        // Set the MenuManager
	        viewer.getTable().setMenu(menu);
	        getSite().registerContextMenu(menuManager, viewer);
	        // make the selection available
	        getSite().setSelectionProvider(viewer);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
