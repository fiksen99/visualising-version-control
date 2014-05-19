package com.imperial.fiksen.codesimilarity.analysers;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

public abstract class SimilarityAnalyser {
	
	public static final String RESULTS_SEPARATOR = "/";

	protected static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";

	protected double[][] scores;
	
	protected static final String SKELETON_PROJECT = "skeleton_";
	

	protected List<String> orderedProjects;
	
	protected Set<Integer> toIgnore;
	
	public abstract void analyse(IProject[] projects);

	protected ASTNode parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return parser.createAST(null);
	}

	public double[][] getScores() {
		return scores;
	}

	public List<String> getOrderedProjects() {
		return this.orderedProjects;
	}
	
	public Set<Integer> getToIgnore() {
		return toIgnore;
	}

}
