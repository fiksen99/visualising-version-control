package com.imperial.fiksen.codesimilarity.handlers;

import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class AllVisitor extends ASTVisitor {
	
	Stack<ASTNodeWithChildren> currentRootStack = new Stack<ASTNodeWithChildren>();
	ASTNodeWithChildren root;
	
	public void preVisit(ASTNode node) {
		ASTNodeWithChildren nodeWC = new ASTNodeWithChildren(node);
		if (!currentRootStack.isEmpty()) {
			ASTNodeWithChildren root = currentRootStack.peek();
			root.addChild(nodeWC);
		} else {
			root = nodeWC;
		}
		System.out.println("node type: " + node.getClass().getName());
//		System.out.println("visited " + node.toString());
		currentRootStack.push(nodeWC);
	}
	
	public void postVisit(ASTNode node) {
		System.out.println("node type: " + node.getClass().getName());
		currentRootStack.pop();
	}
	
	public ASTNodeWithChildren getRoot() {
		return root;
	}
	
}
