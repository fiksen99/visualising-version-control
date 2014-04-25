package com.imperial.fiksen.codesimilarity.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class AllVisitor extends ASTVisitor {
	
	List<ASTNode> all = new ArrayList<ASTNode>();
	Stack<ASTNode> currentRoot = new Stack<ASTNode>();
	
	public void preVisit(ASTNode node) {
		all.add(node);
		System.out.println("node type: " + node.getClass().getName());
		System.out.println("visited " + node.toString());
	}
	
	public List<ASTNode> getNodes() {
		return all;
	}
	
	private class ASTNodeWithChildren {
		ASTNode node;
		List<ASTNodeWithChildren> children;
		
		public ASTNodeWithChildren(ASTNode node) {
			this.node = node;
			children = new ArrayList<ASTNodeWithChildren>();
		}
		
		
	}

}
