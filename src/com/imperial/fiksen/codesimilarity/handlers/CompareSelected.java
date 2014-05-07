package com.imperial.fiksen.codesimilarity.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import parseTreeKernel.ParseTreeKernelSimilarityAnalyser;
import comparison.SimilarityAnalyser;
import comparison.TreeBuilder;
import belkhoucheComparison.SyntacticTreeBuilder;

public class CompareSelected extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//use a set so projects only appear once. linked to retain order for prettiness
		Set<IProject> selectedProjects = new LinkedHashSet<IProject>();
	    ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
	            .getActivePage().getSelection();
        if (selection != null & selection instanceof IStructuredSelection) {
          IStructuredSelection strucSelection = (IStructuredSelection) selection;
          Iterator x = strucSelection.iterator();
          for (Iterator<Object> iterator = strucSelection.iterator(); iterator
              .hasNext();) {
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

	private void createAST(IPackageFragment mypackage) throws JavaModelException {
		TreeBuilder builder = new SyntacticTreeBuilder();
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			printCompilationUnitDetails(unit);
			CompilationUnit parse = parse(unit);
			MethodVisitor visitor = new MethodVisitor();
			parse.accept(visitor);
			//builder.doStuff(parse);
//			for (MethodDeclaration method : visitor.getMethods()) {
//				if(method.getName().toString().equals("testMethodBool")) {
//					System.out.println("HERE!");
//					builder.createTreeFromMethods(method);
//				}
//				//System.out.println(method.toString());
//				System.out.println(method.getBody().toString());
//			}
		}
	}

	private CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}


    private void printCompilationUnitDetails(ICompilationUnit unit)
        throws JavaModelException {
      System.out.println("Source file " + unit.getElementName());
      Document doc = new Document(unit.getSource());
      System.out.println("Has number of lines: " + doc.getNumberOfLines());
    }
}
