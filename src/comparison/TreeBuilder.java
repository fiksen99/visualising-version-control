package comparison;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public abstract class TreeBuilder {
	
	public TreeBuilder() {
		// TODO Auto-generated constructor stub
	}

	public abstract void createTreeFromMethods(MethodDeclaration method);

	public abstract void doStuff(CompilationUnit parse);
}
