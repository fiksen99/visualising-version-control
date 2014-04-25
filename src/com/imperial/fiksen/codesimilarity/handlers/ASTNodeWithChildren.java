package com.imperial.fiksen.codesimilarity.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;


public class ASTNodeWithChildren {
	private ASTNode node;
	private List<ASTNodeWithChildren> children;
	
	public ASTNodeWithChildren(ASTNode node) {
		this.node = node;
		children = new ArrayList<ASTNodeWithChildren>();
	}
	
	public ASTNode getNode() {
		return node;
	}
	
	public List<ASTNodeWithChildren> getChildren() {
		return children;
	}
	
	public void addChild(ASTNodeWithChildren child) {
		children.add(child);
	}
	
	public static boolean areTreesDifferent(ASTNodeWithChildren t1, ASTNodeWithChildren t2) {
		List<ASTNodeWithChildren> children1 = t1.getChildren();
		List<ASTNodeWithChildren> children2 = t2.getChildren();
		if (children1.size() != children2.size()) {
			return true;
		}
		for(int i = 0; i < children1.size(); i++) {
			ASTNodeWithChildren child1 = children1.get(i);
			ASTNodeWithChildren child2 = children2.get(i);
			if(!child1.getNode().getClass().equals(child2.getNode().getClass())) {
				return true;
			}
			areTreesDifferent(child1, child2);
		}
		return false;
	}
	

}
