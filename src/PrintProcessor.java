
import spoon.processing.AbstractProcessor;
import spoon.processing.TraversalStrategy;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.*;
import spoon.reflect.code.*;
import spoon.support.*;

import java.io.*;
import java.util.*;

/**
 * This processor replaces variable (local variables, parameters and fields)
 * declarations with a J2ME compliant equivalent declaration.
 */
public class PrintProcessor extends AbstractProcessor<CtClass> {

    public void process(CtClass c) {
        
	AS3Printer printer = new AS3Printer(getEnvironment());
	String packageName = c.getPackage().getQualifiedName();

	if (packageName.equals(CtPackage.TOP_LEVEL_PACKAGE_NAME))
	  packageName = ""; // no package name
	else
	  packageName += " ";

	printer.write("package " + packageName + "{");
	printer.incTab().writeln();
	printer.calculate(c.getPosition().getCompilationUnit().getDeclaredTypes());
	printer.decTab().writeln().write("}");

	String classname = c.getQualifiedName();
	System.out.println(classname + "...");
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
