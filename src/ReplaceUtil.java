
import spoon.processing.Environment;
import spoon.reflect.Factory;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import java.util.*;

public class ReplaceUtil
{
  static final Map<Class, String> typesMap = new HashMap();

  static {
    typesMap.put(boolean.class,             "Boolean");
    typesMap.put(double.class,              "Number");
    typesMap.put(float.class,               "Number");
    typesMap.put(java.lang.String.class,    "String");
    typesMap.put(java.lang.Object.class,    "Object");
    typesMap.put(java.lang.Throwable.class, "Error");
    typesMap.put(java.util.Date.class,      "Date");
    typesMap.put(java.util.Map.class,       "Object");
  }

  static CtTypeReference replaceType(Environment env, CtTypeReference t)
  {
    return replaceType(env.getFactory(), t);
  }


  static CtTypeReference replaceType(Factory f, CtTypeReference t)
  {
    if (t != null) {
      Class c = t.getActualClass();
      String asName = typesMap.get(c);
      if (asName != null)
	t = f.Type().createReference(asName);
      else {
        for (Class jc : typesMap.keySet())
	  if (t.isSubtypeOf(f.Type().createReference(jc))) {
	    t = f.Type().createReference(typesMap.get(jc));
            break;
          }
      }
    }

    return t;
  }

  static boolean replace(CtElement e, String replacement)
  {
    if (null == e) {
//System.out.println("replace element is null");
	return false;
    }
    SourcePosition pos = e.getPosition();
    if (null == pos) {
	System.out.println("replace position is null");
	return false;
    }

    int start = pos.getSourceStart();
    int length = pos.getSourceEnd() - pos.getSourceStart() + 1;

    SourceCodeFragment fragment = 
	new SourceCodeFragment(start, replacement, length);

    pos.getCompilationUnit().addSourceCodeFragment(fragment); 
//  System.out.println("replaced > " + replacement);
    return true;
  }
}
