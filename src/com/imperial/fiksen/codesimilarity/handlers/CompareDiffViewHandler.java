package com.imperial.fiksen.codesimilarity.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.imperial.fiksen.codesimilarity.compare.SimilarityCompareEditorInput;

public class CompareDiffViewHandler extends AbstractHandler {
	private static final int NUM_COMPARABLE_FILES = 2;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IFile left = null;
		IFile right = null;
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
	    ISelection selection = page.getSelection();
        if (selection != null & selection instanceof IStructuredSelection) {
          IStructuredSelection strucSelection = (IStructuredSelection) selection;
          Iterator<Object> iterator = strucSelection.iterator();
          int selectedFiles = 0;
          while(iterator.hasNext()) {
            Object element = iterator.next();
            //if the selected item is a java file
            if(element instanceof ICompilationUnit) {
            	
            	ICompilationUnit i = (ICompilationUnit) element;
            	IFile file = (IFile) i.getResource();
            	if( left == null ) {
            		left = file;
            	} else {
            		right = file;
            	}
            	selectedFiles++;
            }
          }
          if(selectedFiles != NUM_COMPARABLE_FILES ) {
        	  throw new ExecutionException("Must select exactly " + NUM_COMPARABLE_FILES + " java files");
          }
        }		

		ITypedElement leftTypedElem = SaveableCompareEditorInput.createFileElement(left);
		ITypedElement rightTypedElem = SaveableCompareEditorInput.createFileElement(right);

		ResourceNode leftResourceNode = new ResourceNode(left);
		ResourceNode rightResourceNode = new ResourceNode(right); 
		
		CompareEditorInput in = new SimilarityCompareEditorInput(leftTypedElem, rightTypedElem, page);
		CompareUI.openCompareEditor(in);
		return null;
	}
}
