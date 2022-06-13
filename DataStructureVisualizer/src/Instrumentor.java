import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.Statement;
import exceptions.InvalidClassNameException;
import exceptions.InvalidFieldName;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

// Add the analyzer class to the project under analysis

// Now:
// public members: look at the entire code base
// private members: instrument only the class where the data structure

// Later:
// static: for static data structures make the analyzer instance static
// local variables:

public class Instrumentor {
    private List<CompilationUnit> compilationUnits;
    private String className;
    private String fieldName;

    public Instrumentor(String className, String fieldName, List<CompilationUnit> cus) {
        compilationUnits = cus;
        this.className = className;
        this.fieldName = fieldName;
    }

    private void instrumentProject(List<ClassOrInterfaceDeclaration> classes, FieldDeclaration field) {
        classes.forEach(classDec -> instrumentClass(classDec, field));
    }

    private void instrumentClass(ClassOrInterfaceDeclaration classDec, FieldDeclaration field) {
        System.out.println("Instrumenting class: " + classDec.getNameAsString());
        List<MethodDeclaration> methods = classDec.findAll(MethodDeclaration.class);
        methods.forEach(method -> instrumentMethod(method, field));
    }

    private void instrumentMethod(MethodDeclaration method, FieldDeclaration field) {
        System.out.println("Instrumenting method: " + method.getNameAsString());
        List<Class> modifyingMethods = getModifyingMethods(field.getVariable(0));
        List<Expression> expressions = new ArrayList<>();
        // for each method - find all modifying statements
        // injection - analyze
        modifyingMethods.forEach(modifyingMethod -> expressions.addAll(method.findAll(modifyingMethod)));
        expressions.forEach(this::injectAnalyzer);
        // for each field assignment
        // injection - set the instance
        method.findAll(AssignExpr.class).forEach(this::injectSetInstance);
    }

    private void injectAnalyzer(Expression expression) {
        // TODO: implement
    }

    private void injectSetInstance(Expression expression) {
        // TODO: implement
    }

    /**
     * Supports Lists and Map for now.
     * */
    private List<Class> getModifyingMethods(VariableDeclarator variable) {
        // look at the type
        // if it is List -> return [add, remove, ...]
        // if Map return [...]
        // TODO: Tarek
        return Arrays.stream(new Class[] { MethodCallExpr.class }).toList();
    }

    /**
     * Throws if the field is not found and if the field is of an unsupported type
     * Throw for static fields
     * Determine which case and call the proper function
     * */
    public void instrument() {
        System.out.println("Stared instrumenting the code...");
        // Find the class
        List<ClassOrInterfaceDeclaration> classes = getNodes(compilationUnits, ClassOrInterfaceDeclaration.class);
        ClassOrInterfaceDeclaration classUnderAnalysis = null;
        for (ClassOrInterfaceDeclaration c : classes) {
            if (c.getName().toString().equals(this.className)) {
                classUnderAnalysis = c;
            }
        }
        if (classUnderAnalysis == null) {
            throw new InvalidClassNameException();
        }
        // Find the member
        List<VariableDeclarator> variables = classUnderAnalysis.findAll(VariableDeclarator.class);
        variables = variables.stream().filter(v -> v.getName().toString().equals(fieldName)).toList();
        if (variables.size() != 1) {
            throw new InvalidFieldName();
        }
        VariableDeclarator variable = variables.get(0);
        if (!(variable.getParentNode().get() instanceof FieldDeclaration)) {
            throw new InvalidFieldName();
        }
        FieldDeclaration field = (FieldDeclaration) variable.getParentNode().get();

        instrumentProject(classes, field);
        System.out.println("Finished instrumenting the code!");
    }

    private static List getNodes(List<CompilationUnit> ast, Class nodeClass) {
        List res = new LinkedList<>();
        ast.forEach(cu -> res.addAll(cu.findAll(nodeClass)));
        return res;
    }

    /**
     *  Add the analyzer class to the project under analysis
     *  Run the instrumented code or save it to disk, so it can be run manually
     * */
    public void runDynamicAnalysis() {
        // TODO: TBD
    }
}
