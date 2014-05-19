package com.imperial.fiksen.codesimilarity.handlers;

import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.imperial.fiksen.codesimilarity.analysers.SimilarityAnalyser;
import com.imperial.fiksen.codesimilarity.parseTreeKernel.ParseTreeKernelSimilarityAnalyser;
import com.imperial.fiksen.codesimilarity.utils.ResultsPrinter;

public class GetInfo extends AbstractHandler {
	
	SimilarityAnalyser analyser = new ParseTreeKernelSimilarityAnalyser();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("analyseAll") {
			  @Override
			  protected IStatus run(IProgressMonitor monitor) {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();
					IProject[] projects = root.getProjects();
					//TODO: dependency injection of analyser
					if(analyser.getScores()==null) {
						analyser.analyse(projects);
					} else {
						double[][] scores = analyser.getScores();
						List<String> orderedProjects = analyser.getOrderedProjects();
						Set<Integer> toIgnore = analyser.getToIgnore();
						ResultsPrinter.print(scores, orderedProjects, toIgnore);
					}
					return Status.OK_STATUS;
			  }
				  
			};

		job.schedule(); 
		return null;
	}

}
