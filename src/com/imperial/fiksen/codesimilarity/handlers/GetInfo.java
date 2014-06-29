package com.imperial.fiksen.codesimilarity.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import com.imperial.fiksen.codesimilarity.analysers.SimilarityAnalyser;
import com.imperial.fiksen.codesimilarity.parseTreeKernel.ParseTreeKernelSimilarityAnalyser;

public class GetInfo extends AbstractHandler {
	
	SimilarityAnalyser analyser = new ParseTreeKernelSimilarityAnalyser();

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Job job = new Job("analyseAll") {
			  @Override
			  protected IStatus run(IProgressMonitor monitor) {
				  
					Set<String> checkFiles = new HashSet<String>();
					IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
				    ISelection selection = page.getSelection();
			        if (selection != null & selection instanceof IStructuredSelection) {
			          IStructuredSelection strucSelection = (IStructuredSelection) selection;
			          Iterator<Object> iterator = strucSelection.iterator();
			          while(iterator.hasNext()) {
			            Object element = iterator.next();
			            //if the selected item is a java file
			            IFile file = null;
			            if(element instanceof ICompilationUnit) {
			            	ICompilationUnit i = (ICompilationUnit) element;
			            	file = (IFile) i.getResource();
			            	checkFiles.add(file.getName());
			            }
			            
			          }
			        }		
			        
			        
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();
					IProject[] projects = root.getProjects();
					//TODO: dependency injection of analyser
					analyser.analyse(projects, checkFiles);
					return Status.OK_STATUS;
			  }
				  
			};

		job.schedule(); 
		return null;
	}

}
