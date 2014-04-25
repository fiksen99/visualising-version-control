package com.imperial.fiksen.codesimilarity.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;


public class ASTNodeWithChildren implements Iterable<ASTNodeWithChildren>{
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
			boolean[] used = new boolean[children2.size()]; 
			boolean matched = false;
			for(int j = 0; j < children2.size(); j++) {
				if(used[i]) continue;
				ASTNodeWithChildren child2 = children2.get(i);
				if(child1.getNode().getClass().equals(child2.getNode().getClass())
						&& !areTreesDifferent(child1, child2)) {
					matched = true;
					used[j] = true;
					break;
				}
			}
			if(!matched) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<ASTNodeWithChildren> iterator() {
		return new ASTNWCIterator(this);
	}
	
	//depth first search iterator (in-order)
	public class ASTNWCIterator implements Iterator<ASTNodeWithChildren>{
		
		private LinkedList<ASTNodeWithChildren> nodes;
		
		protected ASTNWCIterator(ASTNodeWithChildren root) {
			nodes = new LinkedList<ASTNodeWithChildren>();
			addChildren(root);
		}
		
		private void addChildren(ASTNodeWithChildren root) {
			nodes.push(root);
			for(ASTNodeWithChildren child : root.getChildren()) {
				addChildren(child);
			}
		}

		@Override
		public boolean hasNext() {
			return !nodes.isEmpty();
		}

		@Override
		public ASTNodeWithChildren next() {
			if(!hasNext()) throw new NoSuchElementException();
			return nodes.removeFirst();
			
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}
