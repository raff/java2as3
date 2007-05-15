
import spoon.processing.AbstractProcessor;
import spoon.processing.TraversalStrategy;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.*;
import spoon.reflect.code.*;
import spoon.support.*;

/**
 * This processor replaces variable (local variables, parameters and fields)
 * declarations with a J2ME compliant equivalent declaration.
 */
public class PrintProcessor extends AbstractProcessor<CtClass> {

    public void process(CtClass c) {
        
	AS3Printer printer = new AS3Printer(getEnvironment());
	String packageName = c.getPackage().getQualifiedName();

	printer.write("package " + packageName + " {");
	printer.incTab().writeln();

	printer.scan(c);

	printer.decTab().writeln();
	printer.write("}");

	System.out.println(printer.toString());

    }
}
