package parseTreeKernel;

import java.util.HashMap;
import java.util.Iterator;
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
import com.imperial.fiksen.codesimilarity.handlers.AllVisitor;
import com.imperial.fiksen.codesimilarity.handlers.MethodVisitor;

import comparison.SimilarityAnalyser;

public class ParseTreeKernelSimilarityAnalyser extends SimilarityAnalyser {
	
	private static final double DECAY_FACTOR = 0.9;
	private static final int THRESHOLD_DEPTH = 10;
	
	Map<IProject, Map<IProject, Map<CompilationUnit, Double>>> scores; 
	
	public ParseTreeKernelSimilarityAnalyser() {
		scores = new HashMap();
	}

	@Override
	public void analyse(IProject[] projects) {
		for (IProject project1 : projects) {
			for (IProject project2 : projects) {
				if (!project1.equals(project2)) {
					try {
						if (project1.isNatureEnabled(JDT_NATURE)
								&& project2.isNatureEnabled(JDT_NATURE)) {
							double sim = normaliseSimilarity(project1, project2);
							System.out.println("in projects " + project1.getName() + ", " + project2.getName());
							System.out.println("have sim value: " + sim); 
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private double normaliseSimilarity(IProject project1, IProject project2) throws JavaModelException {
		double kVal = compareTrees(project1, project2);
		return kVal/Math.sqrt(compareTrees(project1,project1)*compareTrees(project2,project2));
	}

	private double compareTrees(IProject project1, IProject project2) throws JavaModelException {
		// TODO Auto-generated method stub
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
								AllVisitor visitor1 = new AllVisitor();
								AllVisitor visitor2 = new AllVisitor();
//								System.out.println(project1.toString() +"\n" + project2.toString());
//								System.out.println(unit1.getElementName());
//								System.out.println(unit2.getElementName());
								CompilationUnit parse = parse(unit1);
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
				k += c(node1, node2, 0);
			}
		}
		return k;
	}

	
	//TODO: broken something here, different values for t1 t2 vs t2 t1
	private double c(ASTNodeWithChildren node1, ASTNodeWithChildren node2, int depth) {
		if(node1.getNode().getClass().equals(node2.getNode().getClass())) {
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
