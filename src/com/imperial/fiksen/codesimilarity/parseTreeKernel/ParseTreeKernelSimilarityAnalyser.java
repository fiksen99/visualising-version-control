package com.imperial.fiksen.codesimilarity.parseTreeKernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private static final double DECAY_FACTOR = 0.15;
	private static final int THRESHOLD_DEPTH = 3;
	
	private Map<String, Double> pairedScores;
	
	private int total;
	
	private double min = 1.0;
	
	public ParseTreeKernelSimilarityAnalyser() {
		total = 0;
		pairedScores = new HashMap<String, Double>();
	}

	@Override
	public void analyse(IProject[] projects) {
		boolean hasSkeleton = false;
		orderedProjects = new LinkedList<String>();
		for (int i = 0 ; i < projects.length ; i++) {
			IProject project = projects[i];
			try {
				if (project.isNatureEnabled(JDT_NATURE)) {
					String projectName = project.getName();
					if(projectName.endsWith(SKELETON_PROJECT)) {
						hasSkeleton = true;
					}
					orderedProjects.add(projectName);
					total++;
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		scores = new double [total][total];
		double i = 0;
		int notify = 5;
		toIgnore = new HashSet<Integer>();
		for (IProject project1 : projects) {
			String project1Name = project1.getName();
			boolean isSkeleton = project1Name.endsWith(SKELETON_PROJECT);
			for (IProject project2 : projects) {
				try {
					if (project1.isNatureEnabled(JDT_NATURE)
							&& project2.isNatureEnabled(JDT_NATURE)) {
						String project2Name = project2.getName();
						double sim = normaliseSimilarity(project1, project2);
						int index1 = orderedProjects.lastIndexOf(project1Name);
						int index2 = orderedProjects.lastIndexOf(project2Name);
						scores[index1][index2] = sim;
						if(hasSkeleton && isSkeleton && sim == 1.0) {
							toIgnore.add(index2);
						}
						min = Math.min(min, sim);
						i+=1.0;
						if(i/(total*total)*100 > notify) {
							System.out.println(notify + "% complete");
							notify = Math.max(notify+5, (int)Math.floor(i/(total*total)*100));
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		normaliseAllScores();
		com.imperial.fiksen.codesimilarity.utils.ResultsPrinter.print(scores, orderedProjects, toIgnore);
	}

	private int updateProgress(double i, int notify) {
		if(i/(total*total)*100 > notify) {
			System.out.println(notify + "% complete");
			return Math.max(notify+5, (int)Math.floor(i/(total*total)*100));
		}		
		return notify;
	}

	private void normaliseAllScores() {
		double ePowMin = Math.pow(Math.E, min);
		for(int i = 0; i < scores.length; i++) {
			for(int j = 0; j < scores[i].length; j++) {
				//scores[i][j] = 1.0-(Math.pow(Math.E, scores[i][j])-ePowMin)/(Math.E-ePowMin);
				//scores[i][j] = (scores[i][j]-min)/(1-min);
				scores[i][j] = 1.0-scores[i][j];
			}
		}
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
							if (unit1.getElementName().equals(unit2.getElementName()) && unit1.getElementName().equals("RecursionLibrary.java")) {
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
