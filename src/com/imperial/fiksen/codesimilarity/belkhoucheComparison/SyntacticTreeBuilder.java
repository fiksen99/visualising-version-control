package com.imperial.fiksen.codesimilarity.belkhoucheComparison;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

public class SyntacticTreeBuilder {
	
	List<RegionTreeNode> regionTreeNodes;
	

	public void createTreeFromMethods(MethodDeclaration method) {
		@SuppressWarnings("unchecked")
		List<Object> stmts = method.getBody().statements();
		System.out.println("creating tree");
		regionTreeNodes = new ArrayList<RegionTreeNode>();
		buildRegions(stmts, regionTreeNodes);
	}
	
	@SuppressWarnings("unchecked")
	private void buildRegions(List<Object> stmts, List<RegionTreeNode> thisLevel) {
		boolean newRegion = false;
		RegionTreeNode region = createNewRegion(thisLevel);
		for (Object stmt : stmts) {
			if (newRegion) {
				region = createNewRegion(thisLevel);
			}
			if (stmt instanceof WhileStatement) {
				region = createNewRegion(thisLevel);
				Block child = (Block) ((WhileStatement) stmt).getBody();
				buildRegions(child.statements(), region.getChildren());
				newRegion = true;
				stmt = ((WhileStatement) stmt).getExpression();
			}
			region.addStatement(stmt);
		}
	}
	
	private RegionTreeNode createNewRegion(List<RegionTreeNode> level) {
		RegionTreeNode region = new RegionTreeNode();
		level.add(region);
		return region;
	}
	
	private class RegionTreeNode {
		private List<RegionTreeNode> children;
		private List<Object> statements;
		private String type;
		
		public RegionTreeNode() {
			children = new ArrayList<RegionTreeNode>();
			statements = new ArrayList<Object>();
			type = null;
		}
		
		public void addStatement(Object stmt) {
			statements.add(stmt);
		}
		
		public List<RegionTreeNode> getChildren() {
			return children;
		}
		
	}

}
