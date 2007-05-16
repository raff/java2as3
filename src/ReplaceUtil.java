
import spoon.processing.Environment;
import spoon.reflect.Factory;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import java.util.*;

public class ReplaceUtil
{
  static final Map<Class, String> typesMap = new HashMap();

  static {
	/**
	 * Primitive types
	 */
    typesMap.put(boolean.class,             "Boolean");
    typesMap.put(double.class,              "Number");
    typesMap.put(float.class,               "Number");
    typesMap.put(long.class,                "Number");
    typesMap.put(short.class,               "int");
    typesMap.put(char.class,                "String");
	/**
	 * Basic types
	 */
    typesMap.put(java.lang.Boolean.class,   "Boolean");
    typesMap.put(java.lang.Character.class, "String");
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
    typesMap.put(java.util.Date.class,      "Date");
    typesMap.put(java.util.Map.class,       "Object");
    typesMap.put(java.util.HashMap.class,   "Object");
    typesMap.put(java.util.Hashtable.class, "Object");
  }

  static CtTypeReference replaceType(Environment env, CtTypeReference t)
  {
    return replaceType(env.getFactory(), t);
  }

  static CtTypeReference replaceType(Factory f, CtTypeReference t)
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
	t = f.Type().createReference(asName);
    }

    return t;
  }

	/**
	 * Uses Fragment api to replace code
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
