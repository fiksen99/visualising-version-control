package com.imperial.fiksen.codesimilarity.parseTreeKernel;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.*;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.IDocument;

import com.imperial.fiksen.codesimilarity.analysers.SimilarityAnalyser;
import com.imperial.fiksen.codesimilarity.treemanipulation.ASTNodeWithChildren;
import com.imperial.fiksen.codesimilarity.treemanipulation.AllNodeVisitor;
import com.imperial.fiksen.codesimilarity.utils.OrangeUtils;

public class ParseTreeKernelSimilarityAnalyser extends SimilarityAnalyser {
	
	private static final double DECAY_FACTOR = 0.2;
	private static final int THRESHOLD_DEPTH = 3;
	
	private Map<String, Double> pairedScores;
	
	private int total;
	
	public int notify;
	
	private double min = 1.0;
	boolean recalculate;
	
	private Set<String> filesToCheck;
	private boolean hasSkeleton;
	protected int completed;
	
	double[] skeletonNormalise;
	private boolean filterFiles;
	
	public ParseTreeKernelSimilarityAnalyser() {
		super();
		recalculate = true;
	}

	@Override
	public void analyse(IProject[] projects, Set<String> checkFiles) {
		recalculate = true;
		if(recalculate) {
			setUp(projects, checkFiles);
			executeComparison(projects);
			System.out.println(scores[0][0]);
			normaliseAllScores();
			System.out.println("100% complete!");
		}
		try {
			print(new PrintStream(OrangeUtils.PATH_TO_RESOURCES + OrangeUtils.SAVE_FILE));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.analyse(projects, checkFiles);
	}

	private void setUp(IProject[] projects, Set<String> checkFiles) {
		total = 0;
		pairedScores = new ConcurrentHashMap<String, Double>();
		filesToCheck = checkFiles;
		filterFiles = !filesToCheck.isEmpty();
		hasSkeleton = false;
		projectsToIgnore = null;
		orderedProjects = new LinkedList<String>();
		completed = 0;
		for (int i = 0 ; i < projects.length ; i++) {
			IProject project = projects[i];
			try {
				if (project.isNatureEnabled(JDT_NATURE)) {
					String projectName = project.getName();
					if(projectName.endsWith(SKELETON_PROJECT)) {
						hasSkeleton = true;
						projectsToIgnore = Collections.synchronizedSet(new HashSet<Integer>());	
					}
					orderedProjects.add(projectName);
					total++;
				}
			} catch (CoreException e) {
				e.printStackTrace();
				return;
			}
		}	
		scores = new double [total][total];
		
	}
	
	public void executeComparison(IProject[] projects) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        Future<?>[] toComplete = new Future<?>[NUM_THREADS];
        long time = System.currentTimeMillis();
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
        System.out.println(System.currentTimeMillis() - time);
        executor.shutdown();
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
			skeletonNormalise = new double[total];
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
								double sim = calculateNormalised(project1, project2);
								int index1 = orderedProjects.lastIndexOf(project1Name);
								int index2 = orderedProjects.lastIndexOf(project2Name);
								synchronized(scores) {
									scores[index1][index2] = sim;
									min = Math.min(min, sim);
								}
								if(hasSkeleton && p1IsSkeleton) {
									if(sim == 1.0) {
										projectsToIgnore.add(index2);
									}
									skeletonNormalise[index2] = sim;
								} 
								synchronized(filesToCheck) {
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

	private void normaliseAllScores() {
		double ePowMin = Math.pow(Math.E, min);
		for(int i = 0; i < scores.length; i++) {
			for(int j = 0; j < scores[i].length; j++) {
//				scores[i][j] = 1.0-(Math.pow(Math.E, scores[i][j])-ePowMin)/(Math.E-ePowMin);
//				System.out.println("________________");
//				System.out.println(scores[i][j]);
//				scores[i][j] = 1.0-(scores[i][j]-min)/(1-min);
//				System.out.println(scores[i][j]);
//				scores[i][j] = 1.0-(scores[i][j]-(skeletonNormalise[i]+skeletonNormalise[j])/2.0);
				scores[i][j] = 1.0-scores[i][j];
			}
		}
	}

	private double calculateNormalised(IProject project1, IProject project2) throws JavaModelException {
		double k = getScore(project1, project2);
		double p1Score = getScore(project1);
		double p2Score = getScore(project2);
		return normaliseSimilarity(k, p1Score, p2Score);
	}

	public static double normaliseSimilarity(double comp, double selfComp1, double selfComp2){
		//System.out.println(comp+"/sqrt("+selfComp1+"*"+selfComp2+")");
		return comp/Math.sqrt(selfComp1*selfComp2);
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
		List<ICompilationUnit> files1 = getCompilationUnits(project1);
		List<ICompilationUnit> files2 = getCompilationUnits(project2);
		return comparePackages(files1, files2);
	}

	private List<ICompilationUnit> getCompilationUnits(IProject project) throws JavaModelException {
		List<ICompilationUnit> files = new LinkedList<ICompilationUnit>();
		for(IPackageFragment pkg: JavaCore.create(project).getPackageFragments()) {
			if(pkg.getKind() == IPackageFragmentRoot.K_SOURCE) {
				for(ICompilationUnit file: pkg.getCompilationUnits()) {
					files.add(file);
				}
			}
		}
		return files;
	}

	private double comparePackages(List<ICompilationUnit> files1,
			List<ICompilationUnit> files2) throws JavaModelException {
		double k = DECAY_FACTOR;
		if(files1.size() > files2.size()) {
			List<ICompilationUnit> temp = files1;
			files1 = files2;
			files2 = temp;
		}
		for(ICompilationUnit unit1: files1) {
			double maxSim = 0;
			for(ICompilationUnit unit2: files2) {
				if(!filterFiles ||
						(filesToCheck.contains(unit1.getElementName())
						&&  filesToCheck.contains(unit2.getElementName()))) {
				AllNodeVisitor visitor1 = new AllNodeVisitor();
				ASTNode parsed = parse(unit1);
				parsed.accept(visitor1);
				AllNodeVisitor visitor2 = new AllNodeVisitor();
				parsed = parse(unit2);
				parsed.accept(visitor2);
				maxSim = Math.max(maxSim, calculateK(visitor1.getRoot(), visitor2.getRoot()));
				}
			}
			k *= 1+maxSim;
		}
		return k;
		
	}

	public static double calculateK(final ASTNodeWithChildren root1, ASTNodeWithChildren root2) {
		double k = 0;
		for(ASTNodeWithChildren node1 : root1) {
			for(ASTNodeWithChildren node2 : root2) {
				double cVal = c(node1, node2, 1); 
				k += cVal;
			}
		}
		return k;
	}

	private static double c(ASTNodeWithChildren node1, ASTNodeWithChildren node2, int depth) {
		if(!node1.getNode().getClass().equals(node2.getNode().getClass())) {
			//n1 and n2 are different
			return 0;
		} else {
			double prod = DECAY_FACTOR;
			List<ASTNodeWithChildren> children1 = node1.getChildren();
			List<ASTNodeWithChildren> children2 = node2.getChildren();
			//needed? ensures reflexive
			if(children1.size() > children2.size()
					|| (children1.size() == children2.size() && isHigherPriority(children1, children2))) {
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

	private static boolean isHigherPriority(
			List<ASTNodeWithChildren> first,
			List<ASTNodeWithChildren> second) {
		PriorityQueue<Integer> q1 = new PriorityQueue<Integer>();
		PriorityQueue<Integer> q2 = new PriorityQueue<Integer>();
		for(ASTNodeWithChildren child:first) {
			q1.add(child.getNode().getNodeType());
		}
		for(ASTNodeWithChildren child:second) {
			q2.add(child.getNode().getNodeType());
		}
		Integer node1Type;
		while((node1Type = q1.poll()) != null) {
			Integer node2Type = q2.poll();
			if(node1Type < node2Type) {
				return true;
			} else if(node2Type < node1Type) {
				return false;
			}
		}
		//they have the same children so doesn't matter
		return true;
	}

	public RangeDifference[] compare(IDocument leftDoc, IDocument rightDoc) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(leftDoc.get().toCharArray());
		CompilationUnit leftUnit = (CompilationUnit) parser.createAST(null);
		AllNodeVisitor visitor1 = new AllNodeVisitor();
		leftUnit.accept(visitor1);
		parser.setSource(rightDoc.get().toCharArray());
		CompilationUnit rightUnit = (CompilationUnit) parser.createAST(null);
		AllNodeVisitor visitor2 = new AllNodeVisitor();
		rightUnit.accept(visitor2);
		return detailedCalculateK(visitor1.getRoot(), visitor2.getRoot(), leftUnit, rightUnit);
	}

	private RangeDifference[] detailedCalculateK(ASTNodeWithChildren root1,
			ASTNodeWithChildren root2, CompilationUnit leftUnit, CompilationUnit rightUnit) {
		double maxVal = 0;
		ASTNode selectedNode1 = null;
		ASTNode selectedNode2 = null;
		for(ASTNodeWithChildren node1 : root1) {
			if(ASTNode.nodeClassForType(node1.getNode().getNodeType()).getSimpleName().equals("MethodDeclaration")) {
				for(ASTNodeWithChildren node2 : root2) {
					if(ASTNode.nodeClassForType(node2.getNode().getNodeType()).getSimpleName().equals("MethodDeclaration")) {
						double cVal = c(node1, node2, 1);
						double normalisedVal = cVal/(Math.pow((c(node1, node1, 1)*c(node2,node2,1)), 1));
						if(normalisedVal > maxVal) {
							maxVal = normalisedVal;
							selectedNode1 = node1.getNode();
							selectedNode2 = node2.getNode();
						}
					}
				}
			}
		}
		if(selectedNode1 != null && selectedNode2 != null ) {
			RangeDifference[] ret = new RangeDifference[3];
			RangeDifference rd1 = new ParseTreeRangeDifference(RangeDifference.NOCHANGE, 
					0, getStartLine(selectedNode2, rightUnit)-1, 
					0, getStartLine(selectedNode1, leftUnit)-1,
					-1, -1); 
			RangeDifference rd2 = new ParseTreeRangeDifference(RangeDifference.CHANGE, 
					getStartLine(selectedNode2, rightUnit), 
					getEndLine(selectedNode2, rightUnit)-getStartLine(selectedNode2, rightUnit), 
					getStartLine(selectedNode1, leftUnit), 
					getEndLine(selectedNode1, leftUnit)-getStartLine(selectedNode1, leftUnit),
					-1, -1); 
			RangeDifference rd3 = new ParseTreeRangeDifference(RangeDifference.NOCHANGE,
					getEndLine(selectedNode2, rightUnit), 1000,
					getEndLine(selectedNode1, leftUnit), 1000,
					-1, -1);
			ret[0] = rd1;
			ret[1] = rd2;
			ret[2] = rd3;
			return ret;
		} else{
			RangeDifference[] ret = {new ParseTreeRangeDifference(RangeDifference.NOCHANGE, 
					0, rightUnit.getExtendedLength(rightUnit), 
					0, leftUnit.getExtendedLength(leftUnit), 
					-1, -1)};
			return ret;
		}
	}
	
	private int getStartLine(ASTNode node, CompilationUnit unit) {
		if(node == null) {
			return 0;
		}
		return unit.getLineNumber(node.getStartPosition())-1;
	}
	
	private int getEndLine(ASTNode node, CompilationUnit unit) {
		if(node == null) {
			return 0;
		}
		return unit.getLineNumber(node.getStartPosition()+node.getLength());
	}

	public class ParseTreeRangeDifference extends RangeDifference{

		protected ParseTreeRangeDifference(int kind, int rightStart,
				int rightLength, int leftStart, int leftLength,
				int ancestorStart, int ancestorLength) {
			super(kind, rightStart, rightLength, leftStart, leftLength, ancestorStart,
					ancestorLength);
		}
		
	}
}
