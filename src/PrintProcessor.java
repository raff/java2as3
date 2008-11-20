
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.*;

import java.io.*;
import java.util.*;

/**
 * This processor replaces variable (local variables, parameters and fields)
 * declarations with an AS3 compliant equivalent declaration.
 */
public class PrintProcessor extends AbstractProcessor<CtSimpleType<?>> {

    public void process(CtSimpleType<?> c) {
        if (!c.isTopLevel())
	  return;

	String classname = c.getQualifiedName();

	if (c instanceof CtAnnotationType) {
	  System.out.println("skipping annotation " + classname + "...");
	  return;
	} else {
	  System.out.println(classname + "...");
	}

	String packageName = c.getPackage().getQualifiedName();
	if (packageName.equals(CtPackage.TOP_LEVEL_PACKAGE_NAME))
	  packageName = ""; // no package name

	if (packageName.length() > 0)
	  packageName += " ";

	AS3Printer printer = new AS3Printer(getEnvironment());
	printer.write("package " + packageName + "{");
	printer.incTab().writeln();

        List<CtSimpleType<?>> singleClass = new ArrayList<CtSimpleType<?>>();
	singleClass.add(c);

	printer.calculate(c.getPosition().getCompilationUnit(), singleClass);
	printer.decTab().writeln().write("}");

	try {
	  FileWriter fw = new FileWriter(classfile(classname));
	  fw.write(printer.toString());
	  fw.flush();
	  fw.close();
        } catch(Exception e) {
	  System.out.println("error writing: " + e.getMessage());
        }
    }

    private static final String outputFolder = "as3";
    private static final String extension = ".as";

    private File classfile(String className)
    {
	className = outputFolder 
		+ "/" + className.replaceAll("\\.", "/") 
		+ extension;
	File f = new File(className);
	f.getParentFile().mkdirs();
	return f;
    }
}
