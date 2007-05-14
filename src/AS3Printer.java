
import spoon.processing.Environment;
import spoon.reflect.*;
import spoon.reflect.code.*;
import spoon.reflect.visitor.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;

import java.util.*;

/**
 * A visitor for generating AS3 code from the program compile-time metamodel.
 */
public class AS3Printer extends DefaultJavaPrettyPrinter {

  private Environment env;

  AS3Printer(Environment env) {
    super(env);
    this.env = env;
  }

  public DefaultJavaPrettyPrinter scan(CtTypeReference t) {
    if (t != null) {
      t = ReplaceUtil.replaceType(env, t);
      t.accept(this);
    }
    return this;
  }

	/**
	 * Print local variable
	 */
  public <T> AS3Printer writeLocalVariable(CtLocalVariable<T> localVariable) {
    writeModifiers(localVariable);
    write(" var ");
    write(localVariable.getSimpleName());
    write(":");
    scan(localVariable.getType());
    if (localVariable.getDefaultExpression() != null) {
      write(" = ");
      scan(localVariable.getDefaultExpression());
    }
    return this;
  }

	/**
	 * Print method definition
	 */
  public <T> void visitCtMethod(CtMethod<T> m) {
    visitCtNamedElement(m);
    writeGenericsParameter(m.getFormalTypeParameters());
    write("function ");
    write(m.getSimpleName());
    write("(");
    writeExecutableParameters(m);
    write(")");
    write(":");
    scan(m.getType());
    //writeThrowsClause(m);
    if (m.getBody() != null) {
      write(" ");
      visitCtBlock(m.getBody());
      if (m.getBody().getPosition() != null
      && (m.getBody().getStatements().isEmpty() 
      || !(m.getBody().getStatements().get(m.getBody().getStatements().size() - 1) instanceof CtReturn))) {
        //lineNumberMapping.put(line, m.getBody().getPosition().getEndLine());
      }
    } else {
      write(";");
    }
  }

	/**
	 * Print method/constructor parameter
	 */
  public <T> void visitCtParameter(CtParameter<T> parameter) {
    //writeAnnotations(parameter);
    writeModifiers(parameter);
    if (parameter.isVarArgs()) {
      //scan(((CtArrayTypeReference<T>) parameter.getType()).getComponentType());
      write("...");
      write(parameter.getSimpleName());
    } else {
      write(parameter.getSimpleName());
      write(":");
      scan(parameter.getType());
    }
  }

  public <T> void visitCtField(CtField<T> f) {
    visitCtNamedElement(f);
    write(f.getSimpleName());
    write(":");
    scan(f.getType());

    if (f.getParent() == null
    || !CtAnnotationType.class.isAssignableFrom(f.getParent().getClass())) {
      if (f.getDefaultExpression() != null) {
	write(" = ");
	scan(f.getDefaultExpression());
      }
    } else {
      write("()");
      if (f.getDefaultExpression() != null) {
	write(" default ");
	scan(f.getDefaultExpression());
      }
    }
    write(";");
  }

  public <T> void visitCtConstructor(CtConstructor<T> c) {
    visitCtNamedElement(c);
    writeGenericsParameter(c.getFormalTypeParameters());
    write("function ");
    write(c.getDeclaringType().getSimpleName());
    write("(");
    if (c.getParameters().size() > 0) {
      for (CtParameter<?> p : c.getParameters()) {
	visitCtParameter(p);
	write(" ,");
      }
      removeLastChar();
    }
    write(") ");
    if (c.getThrownTypes() != null && c.getThrownTypes().size() > 0) {
      write("throws ");
      for (CtTypeReference ref : c.getThrownTypes()) {
	      scan(ref);
	      write(" , ");
      }
      removeLastChar();
      write(" ");
    }
    scan(c.getBody());
  }
}
