
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
  Stack<CtExpression> parenthesedExpression = new Stack<CtExpression>();
  boolean skipArray = false;

  AS3Printer(Environment env) {
    super(env);
    this.env = env;
  }

  public AS3Printer scan(CtTypeReference t) {
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
    if (! (localVariable.getParent() instanceof CtCatch)) {
      writeModifiers(localVariable);
      if (localVariable.hasModifier(ModifierKind.FINAL))
        write("const ");
      else
        write("var ");
    }
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
    if (f.hasModifier(ModifierKind.FINAL))
      write("const ");
    else
      write("var ");
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
/*
    if (c.getThrownTypes() != null && c.getThrownTypes().size() > 0) {
      write("throws ");
      for (CtTypeReference ref : c.getThrownTypes()) {
	      scan(ref);
	      write(" , ");
      }
      removeLastChar();
      write(" ");
    }
*/
    scan(c.getBody());
  }

  public AS3Printer writeModifiers(CtModifiable m) {
    for (ModifierKind mod : m.getModifiers()) {
      String smod = mod.toString().toLowerCase();
      if (m instanceof CtMethod || m instanceof CtClass
      || ! "final".equals(smod))
         write(smod + " ");
    }
    return this;
  }

  private boolean shouldSetBracket(CtExpression<?> e) {
    if (e.getTypeCasts().size() != 0)
      return true;
    if (e.getParent() instanceof CtBinaryOperator
    || e.getParent() instanceof CtUnaryOperator)
      return e instanceof CtTargetedExpression
	|| e instanceof CtAssignment || e instanceof CtConditional
	|| e instanceof CtUnaryOperator;
    if (e.getParent() instanceof CtTargetedExpression)
      return e instanceof CtBinaryOperator || e instanceof CtAssignment
	|| e instanceof CtConditional;

    return false;
  }

  /**
   * Enters an expression.
   */
  protected void enterCtExpression(CtExpression<?> e) {
    /*
    if (e.getPosition() != null) {
      lineNumberMapping.put(line, e.getPosition().getLine());
    }
    */
    if (shouldSetBracket(e)) {
      parenthesedExpression.push(e);
    }

    if (!e.getTypeCasts().isEmpty()) {
      for (CtTypeReference r : e.getTypeCasts()) {
	write("(");
      }
    }
  }

  /**
   * Exits an expression.
   */
  protected void exitCtExpression(CtExpression<?> e) {
    while (parenthesedExpression.size() > 0
      && e.equals(parenthesedExpression.peek())) {
      parenthesedExpression.pop();
      for (CtTypeReference r : e.getTypeCasts()) {
        write(" as ");
        scan(r);
        write(")");
      }
    }
  }

  public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {
    /*
    scan(reference.getComponentType());
    if (!skipArray)
      write("[]");
    */

    write("Array");
    if (!skipArray) {
      write(" /* ");
      scan(reference.getComponentType());
      write(" */");
    }
  }

  @SuppressWarnings("unchecked")
  public <T> void visitCtNewArray(CtNewArray<T> newArray) {
    enterCtExpression(newArray);

    CtTypeReference<?> ref = newArray.getType();

    if (ref != null)
      write("new ");

    skipArray = true;
    scan(ref);
    skipArray = false;
    if (newArray.getDimensionExpressions().size() != 0) {
      for (int i = 0; ref instanceof CtArrayTypeReference; i++) {
	write("(");
	if (newArray.getDimensionExpressions().size() > i) {
	  scan(newArray.getDimensionExpressions().get(i));
	}
	write(")");
	ref = ((CtArrayTypeReference) ref).getComponentType();
      }
    }
    
    else {
      write("( ");
      for (CtExpression e : newArray.getElements()) {
	scan(e);
	write(" , ");
      }
      if (newArray.getElements().size() > 1) // this was > 0, but I want to
	removeLastChar();		     // throw an error for size==1
      write(" )");
    }
    exitCtExpression(newArray);
  }

  public AS3Printer writeExtendsClause(CtClass<?> c) {
    if (c.getSuperclass() != null) {
      write(" extends ");
      scan(c.getSuperclass());
    }
    return this;
  }

  public AS3Printer writeThrowsClause(CtExecutable<?> e) {
	return this;
  }
}
