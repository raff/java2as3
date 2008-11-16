
import spoon.processing.Environment;
import spoon.reflect.Factory;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.code.*;
import spoon.reflect.cu.*;
import java.util.*;

public class ReplaceUtil
{
  static final Map<Class, String> typesMap = new HashMap();
  static final Map<String, String> methodsMap = new HashMap();
  static final Set<String> reservedWords = new HashSet();

  static {
	/**
	 * Primitive types
	 */
    typesMap.put(boolean.class,             "Boolean");
    typesMap.put(double.class,              "Number");
    typesMap.put(float.class,               "Number");
    typesMap.put(long.class,                "Number");
    typesMap.put(byte.class,                "int");
    typesMap.put(short.class,               "int");
    typesMap.put(char.class,                "String");
	/**
	 * Basic types
	 */
    typesMap.put(java.lang.Boolean.class,   "Boolean");
    typesMap.put(java.lang.Character.class, "String");
    typesMap.put(java.lang.Byte.class,      "int");
    typesMap.put(java.lang.Short.class,     "int");
    typesMap.put(java.lang.Integer.class,   "int");
    typesMap.put(java.lang.Long.class,      "Number");
    typesMap.put(java.lang.Float.class,     "Number");
    typesMap.put(java.lang.Double.class,    "Number");
    typesMap.put(java.lang.String.class,    "String");
    typesMap.put(java.lang.Object.class,    "Object");
	/**
	 * Complex types
	 */
    typesMap.put(java.lang.Throwable.class, "Error");
    typesMap.put(java.lang.Error.class,     "Error");
    typesMap.put(java.lang.Exception.class, "Error");
    typesMap.put(java.lang.RuntimeException.class, "Error");
    typesMap.put(java.io.IOException.class, "IOError");
    typesMap.put(java.util.Date.class,      "Date");
    typesMap.put(java.util.Map.class,       "Object");
    typesMap.put(java.util.HashMap.class,   "Object");
    typesMap.put(java.util.Hashtable.class, "Object");
    typesMap.put(java.util.ArrayList.class, "Array");

    typesMap.put(java.lang.StringBuffer.class,  "String");
    typesMap.put(java.lang.StringBuilder.class, "String");

	/**
	 * Reserved words
	 */
    reservedWords.add("as");
    reservedWords.add("in");
    reservedWords.add("is");
    reservedWords.add("each");
    reservedWords.add("with");
    reservedWords.add("get");
    reservedWords.add("set");
    reservedWords.add("var");
    reservedWords.add("label");
    reservedWords.add("dynamic");
    reservedWords.add("internal");
    reservedWords.add("native");
    reservedWords.add("override");
    reservedWords.add("const");
    reservedWords.add("function");
    reservedWords.add("interface");
    reservedWords.add("namespace");
    reservedWords.add("include");
    reservedWords.add("use");
    reservedWords.add("NaN");
    reservedWords.add("Infinity");
    reservedWords.add("undefined");
    reservedWords.add("Null");

	/**
	 * Methods
	 */
    methodsMap.put("java.lang.Integer.parseInt",   "parseInt");
    methodsMap.put("java.lang.Long.parseLong",     "parseInt");
    methodsMap.put("java.lang.Float.parseFloat",   "parseFloat");
    methodsMap.put("java.lang.Double.parseDouble", "parseFloat");
    methodsMap.put("java.lang.Float.isNaN",        "isNaN");
    methodsMap.put("java.lang.Double.isNaN",       "isNaN");
    methodsMap.put("java.lang.System.currentTimeMillis", "(new Date()).getTime");
    methodsMap.put("java.lang.StringBuffer.append", " += ");
    methodsMap.put("java.lang.StringBuilder.append", " += ");
    methodsMap.put("java.lang.String.equals", " == ");
    methodsMap.put("java.util.ArrayList.add", "push");
  }

  static CtTypeReference replaceType(CtTypeReference t)
  {
    if (t != null) {
      String asName = null;

      try {
 	Class c = t.getActualClass();
        asName = typesMap.get(c);
      } catch(Exception e) {
	// follow through
      }

      if (asName != null)
	t = t.getFactory().Type().createReference(asName);
    }

    return t;
  }

  static String replaceMethodInvocation(CtInvocation invocation)
  {
	String fullName = 
		invocation.getExecutable().getDeclaringType().toString() 
		    + "." + invocation.getExecutable().getSimpleName();

	return methodsMap.get(fullName);
  }

  static boolean isReserved(String name)
  {
    return reservedWords.contains(name);
  }

	/**
	 * Uses Fragment API to replace code
	 */
  static boolean replace(CtElement e, String replacement)
  {
    SourcePosition pos = e.getPosition();

    int start = pos.getSourceStart();
    int length = pos.getSourceEnd() - pos.getSourceStart() + 1;

    SourceCodeFragment fragment = 
	new SourceCodeFragment(start, replacement, length);

    pos.getCompilationUnit().addSourceCodeFragment(fragment); 
    return true;
  }
}
