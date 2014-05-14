package com.imperial.fiksen.codesimilarity.parseTreeKernel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

import com.imperial.fiksen.codesimilarity.analysers.SimilarityAnalyser;
import com.imperial.fiksen.codesimilarity.treemanipulation.ASTNodeWithChildren;
import com.imperial.fiksen.codesimilarity.treemanipulation.AllNodeVisitor;

public class ParseTreeKernelSimilarityAnalyser extends SimilarityAnalyser {
	
	private static final double DECAY_FACTOR = 0.9;
	private static final int THRESHOLD_DEPTH = 4;
	
	private Map<String, Double> pairedScores;
	private double[][] scores;
	
	private int total;
	
	
	public ParseTreeKernelSimilarityAnalyser() {
		total = 0;
		pairedScores = new HashMap<String, Double>();
	}

	@Override
	public void analyse(IProject[] projects) {
		List<String> orderedProjects = new LinkedList<String>();
		for (int i = 0 ; i < projects.length ; i++) {
			IProject project = projects[i];
			try {
				if (project.isNatureEnabled(JDT_NATURE)) {
					orderedProjects.add(project.getName());
					total++;
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		scores = new double [total][total];
		for (IProject project1 : projects) {
			for (IProject project2 : projects) {
				try {
					if (project1.isNatureEnabled(JDT_NATURE)
							&& project2.isNatureEnabled(JDT_NATURE)) {
						double sim = normaliseSimilarity(project1, project2);
						int index1 = orderedProjects.lastIndexOf(project1.getName());
						int index2 = orderedProjects.lastIndexOf(project2.getName());
						scores[index1][index2] = sim;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		com.imperial.fiksen.codesimilarity.utils.ResultsPrinter.printScore(scores, orderedProjects);
	}

	private double normaliseSimilarity(IProject project1, IProject project2) throws JavaModelException {
		double k = getScore(project1, project2);
		double p1Score = getScore(project1);
		double p2Score = getScore(project2);
		return k/Math.sqrt(p1Score*p2Score);
	}
	
	private double getScore(IProject project) throws JavaModelException {
		return getScore(project, project);
	}

	private double getScore(IProject project1, IProject project2) throws JavaModelException {
		String name1 = project1.getName();
		String name2 = project2.getName();
		String combined = null;
		if(name2.compareTo(name1) <= 0) {
			combined = name1 + SimilarityAnalyser.RESULTS_SEPARATOR + name2;
		} else {
			combined = name2 + SimilarityAnalyser.RESULTS_SEPARATOR + name1;
		}
		Double score = this.pairedScores.get(combined);
		if(score == null) {
			score = compareProjects(project1, project2);
			this.pairedScores.put(combined, score);
		}
		return score;
	}
	
	private double compareProjects(IProject project1, IProject project2) throws JavaModelException {
		IPackageFragment[] packages1 = JavaCore.create(project1).getPackageFragments();
		IPackageFragment[] packages2 = JavaCore.create(project2).getPackageFragments();
		return comparePackages(packages1, packages2);
	}

	private double comparePackages(IPackageFragment[] packages1,
			IPackageFragment[] packages2) throws JavaModelException {
		double k = 0;
		for (IPackageFragment package1 : packages1) {
			for (IPackageFragment package2 : packages2) {
				if (package1.getKind() == IPackageFragmentRoot.K_SOURCE
						&& package2.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit1 : package1.getCompilationUnits()) {
						for (ICompilationUnit unit2 : package2.getCompilationUnits()) {
							if (unit1.getElementName().equals(unit2.getElementName())) {
								AllNodeVisitor visitor1 = new AllNodeVisitor();
								AllNodeVisitor visitor2 = new AllNodeVisitor();
								ASTNode parse = parse(unit1);
								parse.accept(visitor1);
								parse = parse(unit2);
								parse.accept(visitor2);
								k += calculateK(visitor1.getRoot(), visitor2.getRoot());
							}
						}
					}
				}
			}
		}
		return k;
		
	}

	private double calculateK(ASTNodeWithChildren root1, ASTNodeWithChildren root2) {
		double k = 0;
		for(ASTNodeWithChildren node1 : root1) {
			for(ASTNodeWithChildren node2 : root2) {
				k += c(node1, node2, 1);
			}
		}
		return k;
	}

	private double c(ASTNodeWithChildren node1, ASTNodeWithChildren node2, int depth) {
		if(!node1.getNode().getClass().equals(node2.getNode().getClass())) {
			//n1 and n2 are different
			return 0;
		} else {
			double prod = DECAY_FACTOR;
			List<ASTNodeWithChildren> children1 = node1.getChildren();
			List<ASTNodeWithChildren> children2 = node2.getChildren();
			if(depth < THRESHOLD_DEPTH) {
				for(int i = 0; i < children1.size(); i++) {
					double max = 0;
					ASTNodeWithChildren childNode1 = children1.get(i);
					for(int j = 0; j < children2.size(); j++) {
						max = Math.max(c(childNode1, children2.get(j), depth+1), max);
					}
					prod *= 1 + max;
				}
			}
			return prod;
		}
	}

}
