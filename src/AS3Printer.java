
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
  String lastClass = "";

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

  public String hashName(String base, String ext) {
    return base + "$" + Integer.toHexString(ext.hashCode());
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
    //System.out.println("visit method " + m.getSignature());

    visitCtNamedElement(m);
    try {
      if (m.getReference().getOverridingExecutable() != null)
	// most of the time this override will complain
        if (! m.getSimpleName().equals("toString"))
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

    CtClass<?> klass = (CtClass<?>) c.getDeclaringType();
    String className = klass.getSimpleName();
    if (klass.getConstructors().size() > 1) {
	if (! lastClass.equals(className)) {
	    lastClass = className;

	    writeln();
	    write("// Constructor");
	    writeln();
	    write("public function " + className + "(f:Function, ...args) {");
	    write(" f(args);");
	    write(" }");
            writeln();
            writeln();
	}

	className = hashName(className, c.getSignature());
    }

    visitCtNamedElement(c);

    write("function ");
    writeGenericsParameter(c.getFormalTypeParameters());
    write(className);
    write("(");
    if (c.getParameters().size() > 0) {
      for (CtParameter<?> p : c.getParameters()) {
	visitCtParameter(p);
	write(", ");
      }
      removeLastChar();
    }
    write(") ");
/*
    if (c.getThrownTypes() != null && c.getThrownTypes().size() > 0) {
      write("throws ");
      for (CtTypeReference ref : c.getThrownTypes()) {
	      scan(ref);
	      write(", ");
      }
      removeLastChar();
      write(" ");
    }
*/
    scan(c.getBody());
  }

  public AS3Printer writeModifiers(CtModifiable m) {

    boolean isStatic = m.getModifiers().contains(ModifierKind.STATIC);

    for (ModifierKind mod : m.getModifiers()) {
      String smod = mod.toString().toLowerCase();
      if ("abstract".equals(smod)
      || "synchronized".equals(smod)
      || ("final".equals(smod)
	  && ( !(m instanceof CtClass 
		|| m instanceof CtField
		|| m instanceof CtLocalVariable)
             || !(m instanceof CtMethod && isStatic))))
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

  private String overloadedConstructor(CtNewClass<?> newClass) {
    CtConstructor<?> ctor = 
      (CtConstructor<?>) newClass.getExecutable().getDeclaration();
    if (ctor != null) {
      CtClass<?> klass = (CtClass<?>) ctor.getDeclaringType();
      if (klass.getConstructors().size() > 1)
        return hashName(klass.getSimpleName(), ctor.getSignature());
    }

    return null;
  }

  public <T> void visitCtNewClass(CtNewClass<T> newClass) {
    enterCtStatement(newClass);
    enterCtExpression(newClass);

    if (newClass.getTarget() != null)
	    scan(newClass.getTarget()).write(".");

    if (newClass.getAnonymousClass() != null) {
	    write("new ");
	    if (newClass.getAnonymousClass().getSuperclass() != null) {
		    scan(newClass.getAnonymousClass().getSuperclass());
	    } else if (newClass.getAnonymousClass().getSuperInterfaces().size() > 0) {
		    for (CtTypeReference ref : newClass.getAnonymousClass()
				    .getSuperInterfaces()) {
			    scan(ref);
		    }
	    }
	    write("(");
	    for (CtExpression<?> exp : newClass.getArguments()) {
		    scan(exp);
		    write(", ");
	    }
	    if (newClass.getArguments().size() > 0)
		    removeLastChar();
	    write(")");
	    scan(newClass.getAnonymousClass());
    } else {
	    write("new ").scan(newClass.getType());

	    if (newClass.getExecutable() != null
		    && newClass.getExecutable().getActualTypeArguments() != null) {
		    writeGenericsParameter(newClass.getExecutable()
				    .getActualTypeArguments());
	    }
	    write("(");
	    String ct = overloadedConstructor(newClass);
	    boolean remove = false;

	    if (ct != null) {
		write(ct);
		if (newClass.getArguments().size() > 0)
		    write(", ");
	    }

	    for (CtCodeElement e : newClass.getArguments()) {
		    scan(e);
		    write(", ");
		    remove = true;
	    }
	    if (remove)
		    removeLastChar();

	    write(")");
    }
    exitCtExpression(newClass);
  }

  public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {
    /*
    scan(reference.getComponentType());
    if (!skipArray)
      write("[]");
    */

    CtTypeReference cType = reference.getComponentType();
    Class cClass =  null;
    try {
       cClass = cType.getActualClass();
    } catch(Exception e) {
       System.out.println("  " + e.getMessage());
    }

    if (cClass != null 
       && (cClass.equals(Byte.class) || cClass.equals(byte.class))) {
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
    Class cClass = null;
    try {
       cClass = cType.getActualClass();
    } catch(Exception e) {
       System.out.println("  " + e.getMessage());
    }

    if (cClass != null 
       && (cClass.equals(Byte.class) || cClass.equals(byte.class))) {
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
	write(", ");
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

    if (ref == null) {
      write("ByteArray(");
      if (newArray.getDimensionExpressions().size() > 0)
        scan(newArray.getDimensionExpressions().get(0));
      write(")");

      if (newArray.getElements().size() > 0) {
        write(" /* ");
        for (CtExpression e : newArray.getElements()) {
	  scan(e);
	  write(", ");
        }
        if (newArray.getElements().size() > 0)
	  removeLastChar();
        write(" */");
      }
    } else {
      if (newArray.getDimensionExpressions().size() > 0
      && newArray.getElements().size() == 0) {
        write("jas.JAS.newByteArray(");
        scan(newArray.getDimensionExpressions().get(0));
        write(")");
      }
      else if (newArray.getElements().size() > 0) {
        write("jas.JAS.initByteArray(");
        for (CtExpression e : newArray.getElements()) {
	  scan(e);
	  write(", ");
        }
        if (newArray.getElements().size() > 0)
	  removeLastChar();
        write(")");
      } else
        write("new ByteArray()");
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

  public <T> void visitCtInvocation(CtInvocation<T> invocation) {
    enterCtStatement(invocation);
    enterCtExpression(invocation);

//System.out.println("visit Invocation " + invocation.getExecutable().getSimpleName());
    if (invocation.getExecutable().getSimpleName().equals("<init>")) {
	// It's a constructor (super or this)
	CtType<?> parentType = invocation.getParent(CtType.class);
	if ((parentType != null)
	    && (parentType.getQualifiedName() != null)
	    && parentType.getQualifiedName().equals(
		invocation.getExecutable().getDeclaringType()
					    .getQualifiedName())) {
	    write("this");
	} else {
	    write("super");
	}
    } else {
	// It's a method invocation
	if (invocation.getExecutable().isStatic()) {
		String replaced = ReplaceUtil.replaceMethodInvocation(invocation);
		if (null != replaced)
			write(replaced);
		else {
		    CtTypeReference<?> type = invocation.getExecutable()
			.getDeclaringType();


		    //if (isHiddenByField(invocation.getParent(CtType.class), type)) {
		    //	importsContext.imports.remove(type.getSimpleName());
		    //}
		    //context.ignoreGenerics = true;
		    scan(type);
		    //context.ignoreGenerics = false;
		    write(".");
	            write(invocation.getExecutable().getSimpleName());
		}
	} else if (invocation.getTarget() != null) {
		//context.enterTarget();
		scan(invocation.getTarget());
		//context.exitTarget();

		String replaced = ReplaceUtil.replaceMethodInvocation(invocation);
		if (null != replaced) {
			if (! replaced.startsWith(" "))
				write(".");
			write(replaced);
		} else {
			write(".");
	        	write(invocation.getExecutable().getSimpleName());
		}
	} else
		write(invocation.getExecutable().getSimpleName());
    }
    write("(");
    boolean remove = false;
    for (CtExpression<?> e : invocation.getArguments()) {
	scan(e);
	write(", ");
	remove = true;
    }
    if (remove) {
	removeLastChar();
    }
    write(")");
    exitCtExpression(invocation);
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
   */
  public AS3Printer writeOperator(BinaryOperatorKind o) {
    if (o == BinaryOperatorKind.INSTANCEOF) 
      write("is");
    else
      super.writeOperator(o);
    return this;
  }

  public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
    enterCtExpression(operator);
    boolean paren = operator.getParent() instanceof CtBinaryOperator
		    || operator.getParent() instanceof CtUnaryOperator;
    if (paren)
      write("(");
   
/*
    if (operator.getKind() != BinaryOperatorKind.INSTANCEOF) {
*/
      scan(operator.getLeftHandOperand());
      write(" ").writeOperator(operator.getKind()).write(" ");
      scan(operator.getRightHandOperand());
/*
    } else {
      write("typeof(");
      scan(operator.getLeftHandOperand());
      write(") == \"");
      scan(operator.getRightHandOperand());
      write("\"");
    }
*/
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
    write("jas.JAS.assert(");
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
      write(", ");
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
