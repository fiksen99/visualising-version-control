package com.imperial.fiksen.codesimilarity.compare;

import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.widgets.Composite;

import com.imperial.fiksen.codesimilarity.parseTreeKernel.ParseTreeKernelSimilarityAnalyser;
import com.imperial.fiksen.codesimilarity.treemanipulation.ASTNodeWithChildren;
import com.imperial.fiksen.codesimilarity.treemanipulation.AllNodeVisitor;

public class ParseTreeMergeViewer extends ContentMergeViewer {

	
	protected ParseTreeMergeViewer(int style, ResourceBundle bundle,
			CompareConfiguration cc) {
		super(style, bundle, cc);
	}

	class JavaCodeComparator implements IRangeComparator {
		
		private static final double THRESHOLD = 0.99;
		private ASTNodeWithChildren sourceTree;
		private int sizeTree;

		public JavaCodeComparator(String line) {
			char[] source = line.toCharArray();
			ASTParser parser = ASTParser.newParser(AST.JLS4);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
			parser.setSource(source);
			Map options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
			parser.setCompilerOptions(options);
			CompilationUnit result = (CompilationUnit) parser.createAST(null);
			AllNodeVisitor visitor = new AllNodeVisitor();
			result.accept(visitor);
			this.sourceTree = visitor.getRoot();
			sizeTree = 0;
			for(ASTNodeWithChildren node: sourceTree) {
				sizeTree++;
			}
		}

		@Override
		public int getRangeCount() {
			return sizeTree;
		}

		//return true if they're dissimilar as then we don't want them highlight/linked
		@Override
		public boolean rangesEqual(int thisIndex, IRangeComparator other,
				int otherIndex) {
			if(!(other instanceof JavaCodeComparator)) {
				return true;
			}
			JavaCodeComparator toCompare = (JavaCodeComparator) other;
			ASTNodeWithChildren thisNode = sourceTree;
			Iterator<ASTNodeWithChildren> iter = thisNode.iterator();
			for(int i = 0; i < thisIndex; i++) {
				if(iter.hasNext()) {
					thisNode = iter.next();
				} else {
					return true;
				}
			}
			ASTNodeWithChildren otherNode = toCompare.sourceTree;
			iter = otherNode.iterator();
			for(int i = 0; i < otherIndex; i++) {
				if(iter.hasNext()) {
					otherNode = iter.next();
				} else {
					return true;
				}
			}
			
			double comp = ParseTreeKernelSimilarityAnalyser.calculateK(thisNode, otherNode);
			double same1 = ParseTreeKernelSimilarityAnalyser.calculateK(thisNode, thisNode);
			double same2 = ParseTreeKernelSimilarityAnalyser.calculateK(otherNode, otherNode);
			double similarity = ParseTreeKernelSimilarityAnalyser.normaliseSimilarity(comp, same1, same2);
			
			return similarity < THRESHOLD;
		}

		@Override
		public boolean skipRangeComparison(int length, int maxLength,
				IRangeComparator other) {
			return false;
		}

	}


	@Override
	protected void createControls(Composite composite) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleResizeAncestor(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleResizeLeftRight(int x, int y, int leftWidth,
			int centerWidth, int rightWidth, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateContent(Object ancestor, Object left, Object right) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void copy(boolean leftToRight) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected byte[] getContents(boolean left) {
		// TODO Auto-generated method stub
		return null;
	}
}
