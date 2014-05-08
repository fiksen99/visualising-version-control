package com.imperial.fiksen.codesimilarity.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import parseTreeKernel.ParseTreeKernelSimilarityAnalyser;
import comparison.SimilarityAnalyser;

public class GetInfo extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		SimilarityAnalyser analyser = new ParseTreeKernelSimilarityAnalyser();
		analyser.analyse(projects);
		return null;
	}

}
