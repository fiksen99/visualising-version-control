package com.imperial.fiksen.codesimilarity.handlers;

import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class AllNodeVisitor extends ASTVisitor {
	
	Stack<ASTNodeWithChildren> currentRootStack = new Stack<ASTNodeWithChildren>();
	ASTNodeWithChildren root;
	
	public void preVisit(ASTNode node) {
		ASTNodeWithChildren nodeWC = new ASTNodeWithChildren(node);
		if (!currentRootStack.isEmpty()) {
			ASTNodeWithChildren parent = currentRootStack.peek();
			parent.addChild(nodeWC);
		} else {
			root = nodeWC;
		}
		currentRootStack.push(nodeWC);
	}
	
	public void postVisit(ASTNode node) {
		currentRootStack.pop();
	}
	
	public ASTNodeWithChildren getRoot() {
		return root;
	}
	
}
