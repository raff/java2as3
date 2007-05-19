
import spoon.processing.Environment;
import spoon.reflect.code.BinaryOperatorKind;
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
  Map<Integer, Integer> lineNumberMapping;
  int skipArray = 0;
  boolean noTypeDecl = false;

  int line = 1;
  int statics = 1;

  AS3Printer(Environment env) {
    super(env);
    this.env = env;
    this.lineNumberMapping = super.getLineNumberMapping();
  }

  public AS3Printer scan(CtTypeReference t) {
    if (t != null) {
      t = ReplaceUtil.replaceType(t);
      t.accept(this);
    }
    return this;
  }

	/**
	 * Print local variable
	 */
  public <T> AS3Printer writeLocalVariable(CtLocalVariable<T> localVariable) {
    if (!noTypeDecl && !(localVariable.getParent() instanceof CtCatch)) {
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
    //System.out.println("visit method " + m.getSimpleName());

    visitCtNamedElement(m);
    try {
      if (m.getReference().getOverridingExecutable() != null)
        write("override ");
    } catch(Exception e) {
	System.out.println("  getOverride error: " + e.getMessage());
    }

    write("function ");
    write(m.getSimpleName());
    write("(");
    writeExecutableParameters(m);
    write(")");
    write(":");
    writeGenericsParameter(m.getFormalTypeParameters());
    scan(m.getType());
    //writeThrowsClause(m);
    if (m.getBody() != null) {
      write(" ");
      visitCtBlock(m.getBody());
      if (m.getBody().getPosition() != null
      && (m.getBody().getStatements().isEmpty() 
      || !(m.getBody().getStatements().get(m.getBody().getStatements().size() - 1) instanceof CtReturn))) {
        lineNumberMapping.put(line, m.getBody().getPosition().getEndLine());
      }
    } else {
      if (! (m.getParent() instanceof CtInterface))
        write(" { throw new Error(\"abstract\") };");
      else
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
    write("function ");
    writeGenericsParameter(c.getFormalTypeParameters());
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
      if ("abstract".equals(smod)
      || "synchronized".equals(smod)
      || ("final".equals(smod)
	  && !(m instanceof CtMethod 
		|| m instanceof CtClass 
		|| m instanceof CtField
		|| m instanceof CtLocalVariable)))
        write("/*" + smod + "*/ ");
      else if ("final".equals(smod) 
	&& (m instanceof CtLocalVariable || m instanceof CtField))
	; // we are going to change var to const
      else
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
    if (e.getPosition() != null) {
      lineNumberMapping.put(line, e.getPosition().getLine());
    }
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

    CtTypeReference cType = reference.getComponentType();
    if (cType.getActualClass().equals(Byte.class)
    || cType.getActualClass().equals(byte.class)) {
       write("ByteArray");
       return; 
    }

    write("Array");
    if (skipArray <= 0) {
      if (skipArray-- == 0) write(" /* "); else write(" ");
      scan(reference.getComponentType());
      if (++skipArray == 0) write(" */");
    }
  }

  @SuppressWarnings("unchecked")
  public <T> void visitCtNewArray(CtNewArray<T> newArray) {

    CtTypeReference<?> ref = newArray.getType();
    CtTypeReference cType = ((CtArrayTypeReference) ref).getComponentType();

    if (cType.getActualClass().equals(Byte.class)
    || cType.getActualClass().equals(byte.class)) {
      visitNewByteArray(newArray, ref);
      return; 
    }

    if (newArray.getElements().size() == 1) {
        enterCtExpression(newArray);
	CtExpression e = newArray.getElements().get(0);
	write("[ ");
	scan(e);
	write(" ] as Array");
        exitCtExpression(newArray);
	return;
    }

    enterCtExpression(newArray);

    if (ref != null)
      write("new ");

    skipArray++;
    scan(ref);
    skipArray--;
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
      if (newArray.getElements().size() > 0)
	removeLastChar();
      write(" )");
    }
    exitCtExpression(newArray);
  }

  private <T> void visitNewByteArray(
	CtNewArray<T> newArray, CtTypeReference ref) 
  {
    enterCtExpression(newArray);

    if (ref != null)
      write("new ");

    write("ByteArray(");
    if (newArray.getDimensionExpressions().size() > 0)
      scan(newArray.getDimensionExpressions().get(0));
    write(")");

    if (newArray.getElements().size() > 0) {
      write(" /* ");
      for (CtExpression e : newArray.getElements()) {
	scan(e);
	write(" , ");
      }
      if (newArray.getElements().size() > 0)
	removeLastChar();
      write(" */");
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

  public <T> void visitCtLiteral(CtLiteral<T> literal) {
    if (literal.getValue() instanceof CtTypeReference) {
      enterCtExpression(literal);
      scan((CtTypeReference) literal.getValue());
      exitCtExpression(literal);
    } else if (literal.getValue() instanceof Long) {
      write(literal.getValue().toString()); // + "L");
    } else if (literal.getValue() instanceof Float) {
      write(literal.getValue().toString()); // + "F");
    } else {
      super.visitCtLiteral(literal);
    }
  }

  /**
   * Writes a binary operator.
   *
  public AS3Printer writeOperator(BinaryOperatorKind o) {
    if (o == BinaryOperatorKind.INSTANCEOF) 
      write("typeof");
    else
      super.writeOperator(o);
    return this;
  }
    */

  public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
    enterCtExpression(operator);
    boolean paren = operator.getParent() instanceof CtBinaryOperator
		    || operator.getParent() instanceof CtUnaryOperator;
    if (paren)
      write("(");
   
    if (operator.getKind() != BinaryOperatorKind.INSTANCEOF) {
      scan(operator.getLeftHandOperand());
      write(" ").writeOperator(operator.getKind()).write(" ");
      scan(operator.getRightHandOperand());
    } else {
      write("typeof(");
      scan(operator.getLeftHandOperand());
      write(") == \"");
      scan(operator.getRightHandOperand());
      write("\"");
    }

    if (paren)
      write(")");
    exitCtExpression(operator);
  }

  public AS3Printer writeHeader(List<CtSimpleType<?>> types) {
    if (!types.isEmpty()) {
      CtPackage pack = types.get(0).getPackage();
      //scan(pack).writeln().writeln();
      for (CtTypeReference<?> ref : getImports()) {
	// ignore non-top-level type
	if (ref.getPackage() != null) {
	  // ignore java.lang package
	  if (!ref.getPackage().getSimpleName().equals("java.lang"))
	    // ignore type in same package
	    if (!ref.getPackage().getSimpleName().equals(
	      pack.getQualifiedName())) {
	      write("import " + ref.getQualifiedName() + ";").writeln();
	    }
	}
      }
      writeln();
    }
    return this;
  }

  public <T> void visitCtAssert(CtAssert<T> asserted) {
    enterCtStatement(asserted);
    write("JAS.assert(");
    scan(asserted.getAssertExpression());
    if (asserted.getExpression() != null) {
	write(", ");
	scan(asserted.getExpression());
    }
    write(")");
  }
/*
  public <T> void visitCtVariableAccess(CtVariableAccess<T> variableAccess) {
    enterCtExpression(variableAccess);
    write("$").write(variableAccess.getVariable().getSimpleName());
    exitCtExpression(variableAccess);
  }
*/

  public void visitCtFor(CtFor forLoop) {
    enterCtStatement(forLoop);
    write("for (");
    List<CtStatement> st = forLoop.getForInit();
    if (st.size() > 0) {
      scan(st.get(0));
    }
    if (st.size() > 1) {
      noTypeDecl = true;
      for (int i = 1; i < st.size(); i++) {
	write(", ");
	scan(st.get(i));
      }
      noTypeDecl = false;
    }
    write(" ; ");
    scan(forLoop.getExpression());
    write(" ; ");
    for (CtStatement s : forLoop.getForUpdate()) {
      scan(s);
      write(" , ");
    }
    if (forLoop.getForUpdate().size() > 0)
      removeLastChar();
    write(")");
    if (forLoop.getBody() instanceof CtBlock) {
      write(" ");
      scan(forLoop.getBody());
    } else {
      incTab().writeln();
      writeStatement(forLoop.getBody());
      decTab();
    }
  }

  public void visitCtForEach(CtForEach foreach) {
    enterCtStatement(foreach);
    write("for each (");
    scan(foreach.getVariable());
    write(" in ");
    scan(foreach.getExpression());
    write(")");

    if (foreach.getBody() instanceof CtBlock) {
      write(" ");
      scan(foreach.getBody());
    } else {
      incTab().writeln();
      writeStatement(foreach.getBody());
      decTab();
    }
  }

	/**
	 * Writes a statement.
	 */
  protected void writeStatement(CtStatement e) {
    scan(e);
    if (!((e instanceof CtBlock) || (e instanceof CtIf)
      || (e instanceof CtFor) || (e instanceof CtForEach)
      || (e instanceof CtWhile) || (e instanceof CtTry)
      || (e instanceof CtSwitch) || (e instanceof CtSynchronized)))
      write(";");
  }

    /**
     * Writes a generics parameter.
     */
  public AS3Printer writeGenericsParameter(
    Collection<CtTypeReference<?>> params) {
    if (params == null)
      return this;
    if ( params.size() > 0) {
      write("/*");
      //context.ignoreImport = true;
      for (CtTypeReference param : params) {
    scan(param);
    write(", ");
      }
      //context.ignoreImport = false;
      removeLastChar();
      write("*/ ");
    }
    return this;
  }

  public void visitCtAnonymousExecutable(CtAnonymousExecutable impl) {
    writeAnnotations(impl);
    writeModifiers(impl);
    write("private function jas$static" + statics + "():* ");
    impl.getBody().accept(this);
    writeln();
    write("static private const _jas$static" + statics 
	+ ":* = jas$static" + statics + "();");
    statics++;
  }

  public void visitCtSynchronized(CtSynchronized synchro) {
    enterCtStatement(synchro);
    write("/* synchronized ");
    if (synchro.getExpression() != null) {
      removeLastChar();
      write("(");
      scan(synchro.getExpression());
      write(") ");
    }
    write("*/ ");
    scan(synchro.getBlock());
  }
}
