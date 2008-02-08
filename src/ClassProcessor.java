
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.*;

public class ClassProcessor extends AbstractProcessor<CtClass> {

    public void init()
    {
        super.init();
    }

    public void process(CtClass c) {
        
	int n = 0;
	String last = "";

System.out.println("==== " + c.getSimpleName() + " =================");
	for (CtMethod<?> m : ((CtType<?>)c).getMethods()) {
		String name = m.getSimpleName();
		if (name.equals(last)) {
			n++;
			m.setSimpleName(name + "$" + n);
			m.getReference().setSimpleName(name + "$" + n);
		} else {
			n = 0;
			last = name;
		}
System.out.println("  " + m.getSimpleName());
	}
    }
}
