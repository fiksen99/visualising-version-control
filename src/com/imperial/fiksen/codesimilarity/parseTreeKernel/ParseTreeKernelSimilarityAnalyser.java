package com.imperial.fiksen.codesimilarity.parseTreeKernel;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

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
	
	public int notify;
	
	private double min = 1.0;
	boolean recalculate;
	
	private Set<String> filesToIgnore;
	private boolean hasSkeleton;
	protected int completed;
	
	public ParseTreeKernelSimilarityAnalyser() {
		super();
		recalculate = true;
	}
	
	public void executeComparison(IProject[] projects) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        Future<?>[] toComplete = new Future<?>[NUM_THREADS];
        for(int i = 0; i < NUM_THREADS; i++) {
        	toComplete[i] = executor.submit(new Compute(i, projects.clone(), scores));
        }
        for(int i = 0; i < NUM_THREADS; i++) {
        	try {
				toComplete[i].get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        executor.shutdown();
	}

	@Override
	public void analyse(IProject[] projects) {
		recalculate = true;
		if(recalculate) {
			setUp(projects);
			executeComparison(projects);
			normaliseAllScores();
			System.out.println("100% complete!");
		}
		try {
			print(new PrintStream(PATH_TO_RESOURCES + SAVE_FILE));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.analyse(projects);
	}

	private class Compute implements Runnable {
		
		private double[][] scores;
		private IProject[] projects;
		private int threadNum;

		public Compute(int fraction, IProject[] projects, double[][] scores) {
			this.threadNum = fraction+1;
			this.scores = scores;
			this.projects = projects;
		}

		public void run() {
			System.out.println("running task " + threadNum);
			computeScores();
			System.out.println("thread " + threadNum + " complete");
		}
		
		private void computeScores() {
			double[] skeletonNormalise = new double[total];
			int projectNum = 0;
			for (IProject project1 : projects) {
				if(projectNum % NUM_THREADS == this.threadNum-1)  {
					String project1Name = project1.getName();
					boolean p1IsSkeleton = project1Name.endsWith(SKELETON_PROJECT);
					for (IProject project2 : projects) {
						try {
							if (project1.isNatureEnabled(JDT_NATURE)
									&& project2.isNatureEnabled(JDT_NATURE)) {
								String project2Name = project2.getName();
								double sim = normaliseSimilarity(project1, project2);
								int index1 = orderedProjects.lastIndexOf(project1Name);
								int index2 = orderedProjects.lastIndexOf(project2Name);
								synchronized(scores) {
									scores[index1][index2] = sim;
								}
								if(hasSkeleton && p1IsSkeleton) {
									if(sim == 1.0) {
										toIgnore.add(index2);
									}
									skeletonNormalise[index2] = sim;
								} 
								synchronized(filesToIgnore) {
								++completed;
								}
								if((completed)%(4*NUM_THREADS) == 0) {
									System.out.println((completed*100)/(total*total) + "% complete");
								}
							}
						} catch (CoreException e) {
							e.printStackTrace();
							return;
						}
					}
				}
				projectNum++;
			}
		}
		
	}

	private void setUp(IProject[] projects) {
		total = 0;
		pairedScores = new ConcurrentHashMap<String, Double>();
		filesToIgnore = new HashSet<String>();
		hasSkeleton = false;
		orderedProjects = new LinkedList<String>();
		completed = 0;
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
				e.printStackTrace();
				return;
			}
		}
		toIgnore = Collections.synchronizedSet(new HashSet<Integer>());		
		scores = new double [total][total];
		
	}

	private void normaliseAllScores() {
		double ePowMin = Math.pow(Math.E, min);
		for(int i = 0; i < scores.length; i++) {
			for(int j = 0; j < scores[i].length; j++) {
				//scores[i][j] = 1.0-(Math.pow(Math.E, scores[i][j])-ePowMin)/(Math.E-ePowMin);
				//scores[i][j] = (scores[i][j]-min)/(1-min);
				//scores[i][j] = 1.0-(scores[i][j]-(skeletonNormalise[i]+skeletonNormalise[j])/2.0);
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
			if (package1.getKind() == IPackageFragmentRoot.K_SOURCE) {
				double maxSim = 0.0;
				for (IPackageFragment package2 : packages2) {
					if(package2.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit unit1 : package1.getCompilationUnits()) {
							for (ICompilationUnit unit2 : package2.getCompilationUnits()) {
//								if(!unit1.getElementName().equals("IOUtil.java") && !unit2.getElementName().equals("IOUtil.java")) {
								if ((unit1.getElementName().equals("RecursionLibrary.java")
										||  unit1.getElementName().equals("LoopArraysLibrary.java"))
										&&(unit2.getElementName().equals("RecursionLibrary.java")
												||  unit2.getElementName().equals("LoopArraysLibrary.java"))) {
									AllNodeVisitor visitor1 = new AllNodeVisitor();
									AllNodeVisitor visitor2 = new AllNodeVisitor();
									ASTNode parse = parse(unit1);
									parse.accept(visitor1);
									parse = parse(unit2);
									parse.accept(visitor2);
									maxSim = Math.max(maxSim, calculateK(visitor1.getRoot(), visitor2.getRoot()));
								}
							}
						}
					}
				}
				k += maxSim;
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
			//needed? ensures reflexive
			if(children1.size() > children2.size()) {
				List<ASTNodeWithChildren> temp = children1;
				children1 = children2;
				children2 = temp;
			}
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
