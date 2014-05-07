package com.imperial.fiksen.codesimilarity.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
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
	    ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
	            .getActivePage().getSelection();
        if (selection != null & selection instanceof IStructuredSelection) {
          IStructuredSelection strucSelection = (IStructuredSelection) selection;
          Iterator x = strucSelection.iterator();
          for (Iterator<Object> iterator = strucSelection.iterator(); iterator
              .hasNext();) {
            ASTNode element = (ASTNode) iterator.next();
            System.out.println(element.structuralPropertiesForType().toString());
            if( element instanceof CompilationUnit ) {
            	//((CompilationUnit) element).
            }
          }
        }
        return null;
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		IWorkspaceRoot root = workspace.getRoot();
//		IProject[] projects = root.getProjects();
//		SimilarityAnalyser analyser = new ParseTreeKernelSimilarityAnalyser();
//		analyser.analyse(projects);
//		return null;
	}

	private void analyseMethods(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				createAST(mypackage);
			}
		}
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
