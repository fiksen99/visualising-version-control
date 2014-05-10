package com.imperial.fiksen.codesimilarity.handlers;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.imperial.fiksen.codesimilarity.analysers.SimilarityAnalyser;
import com.imperial.fiksen.codesimilarity.parseTreeKernel.ParseTreeKernelSimilarityAnalyser;

public class CompareSelected extends AbstractHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//use a set so projects only appear once. linked to retain order for prettiness
		Set<IProject> selectedProjects = new LinkedHashSet<IProject>();
	    ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
	            .getActivePage().getSelection();
        if (selection != null & selection instanceof IStructuredSelection) {
          IStructuredSelection strucSelection = (IStructuredSelection) selection;
          Iterator<Object> iterator = strucSelection.iterator();
          while(iterator.hasNext()) {
            Object element = iterator.next();
            //if the selected item is part of a java project
            if(element instanceof IJavaElement) {
            	IJavaElement i = (IJavaElement) element;
            	IProject project = i.getJavaProject().getProject();
            	selectedProjects.add(project);
            }
          }
        }
        IProject[] projectsArr = selectedProjects.toArray(new IProject[selectedProjects.size()]);
        SimilarityAnalyser analyser = new ParseTreeKernelSimilarityAnalyser();
        analyser.analyse(projectsArr);
        return null;
	}
}
