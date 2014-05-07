package parseTreeKernel;

import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.imperial.fiksen.codesimilarity.handlers.ASTNodeWithChildren;
import com.imperial.fiksen.codesimilarity.handlers.AllNodeVisitor;
import com.imperial.fiksen.codesimilarity.handlers.MethodVisitor;

import comparison.SimilarityAnalyser;

public class ParseTreeKernelSimilarityAnalyser extends SimilarityAnalyser {
	
	private static final double DECAY_FACTOR = 0.9;
	private static final int THRESHOLD_DEPTH = 4;
	
	private Map<String, Double> pairedScores;
	private String[] projectRef;
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
						//System.out.println("in projects " + project1.getName() + ", " + project2.getName());
						double sim = normaliseSimilarity(project1, project2);
						int index1 = orderedProjects.lastIndexOf(project1.getName());
						int index2 = orderedProjects.lastIndexOf(project2.getName());
						scores[index1][index2] = sim;
						//System.out.println("have sim value: " + sim); 
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		utils.ResultsPrinter.printScore(scores, orderedProjects);
	}

	private double normaliseSimilarity(IProject project1, IProject project2) throws JavaModelException {
		double k = getScore(project1, project2);
		double p1Score = getScore(project1);
		double p2Score = getScore(project2);
//		System.out.println("k: " + k);
//		System.out.println(project1.getName() + ": " + p1Score);
//		System.out.println(project2.getName() + ": " + p2Score);
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
			score = compareTrees(project1, project2);
			this.pairedScores.put(combined, score);
		}
		return score;
	}
	
	private double compareTrees(IProject project1, IProject project2) throws JavaModelException {
		double k = 0;
		IPackageFragment[] packages1 = JavaCore.create(project1).getPackageFragments();
		IPackageFragment[] packages2 = JavaCore.create(project2).getPackageFragments();
		for (IPackageFragment package1 : packages1) {
			for (IPackageFragment package2 : packages2) {
				if (package1.getKind() == IPackageFragmentRoot.K_SOURCE
						&& package2.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit1 : package1.getCompilationUnits()) {
						for (ICompilationUnit unit2 : package2.getCompilationUnits()) {
							if (unit1.getElementName().equals(unit2.getElementName())) {
								AllNodeVisitor visitor1 = new AllNodeVisitor();
								AllNodeVisitor visitor2 = new AllNodeVisitor();
//								System.out.println(project1.toString() +"\n" + project2.toString());
//								System.out.println(unit1.getElementName());
//								System.out.println(unit2.getElementName());
								CompilationUnit parse = parse(unit1);
								parse.accept(visitor1);
								parse = parse(unit2);
								parse.accept(visitor2);
								k += calculateK(visitor1.getRoot(), visitor2.getRoot());
								//System.out.println("loop iteration");
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
//		root1.printTree();
		for(ASTNodeWithChildren node1 : root1) {
			for(ASTNodeWithChildren node2 : root2) {
				k += c(node1, node2, 0);
			}
		}
		return k;
	}

	
	//TODO: broken something here, different values for t1 t2 vs t2 t1
	private double c(ASTNodeWithChildren node1, ASTNodeWithChildren node2, int depth) {
		if(node1.getNode().getClass().equals(node2.getNode().getClass())) {
			//n1 and n2 are different
			return 0;
		} else {
			double prod = DECAY_FACTOR;
			List<ASTNodeWithChildren> children1 = node1.getChildren();
			List<ASTNodeWithChildren> children2 = node2.getChildren();
			if(depth < THRESHOLD_DEPTH) {
				for(int i = 0; i < children1.size(); i++) {
					double mean = 0;
					for(int j = 0; j < children2.size(); j++) {
						mean += c(children1.get(i), children2.get(j), depth+1);
					}
					if(children2.size() > 0) {
						mean /= children2.size();
					}
					prod *= 1 + mean;
				}
			}
			return prod;
		}
	}

}
