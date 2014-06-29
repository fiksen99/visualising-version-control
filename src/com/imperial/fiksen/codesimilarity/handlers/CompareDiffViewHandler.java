package com.imperial.fiksen.codesimilarity.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
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

@SuppressWarnings("restriction")
public class CompareDiffViewHandler extends AbstractHandler {
	private static final int NUM_COMPARABLE_FILES = 2;

	public Object execute(ExecutionEvent event) {
		List<IFile> files = new ArrayList<IFile>();
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
	    ISelection selection = page.getSelection();
        if (selection != null & selection instanceof IStructuredSelection) {
          IStructuredSelection strucSelection = (IStructuredSelection) selection;
          Iterator<Object> iterator = strucSelection.iterator();
          int selectedFiles = 0;
          while(iterator.hasNext()) {
            Object element = iterator.next();
            //if the selected item is a java file
            IFile file = null;
            if(element instanceof ICompilationUnit) {
            	ICompilationUnit i = (ICompilationUnit) element;
            	file = (IFile) i.getResource();
            	files.add(file);
            }
            
          }
        }		
        IFile[] filesArr = files.toArray(new IFile[0]);

        for(int i = 0; i < filesArr.length; i++) {
        	for(int j = i+1; j < filesArr.length; j++) {
            	ITypedElement leftTypedElem = SaveableCompareEditorInput.createFileElement(filesArr[i]);
        		ITypedElement rightTypedElem = SaveableCompareEditorInput.createFileElement(filesArr[j]);

        		CompareConfiguration config = new CompareConfiguration();
        		CompareUIPlugin.getDefault().getPreferenceStore().setValue(ComparePreferencePage.OPEN_STRUCTURE_COMPARE, Boolean.FALSE);
        		CompareEditorInput in = new SimilarityCompareEditorInput(leftTypedElem, rightTypedElem, config, page);
        		CompareUI.openCompareEditor(in);
        	}
        }
		return null;
	}
}
