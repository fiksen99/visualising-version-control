package com.imperial.fiksen.codesimilarity.analysers;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

public abstract class SimilarityAnalyser {
	
	public static final String RESULTS_SEPARATOR = "/";

	protected static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	
	public abstract void analyse(IProject[] projects);

	protected ASTNode parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return parser.createAST(null);
	}

}
