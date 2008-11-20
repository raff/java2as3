
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;

public class InvocationProcessor extends AbstractProcessor<CtInvocation<?>> {

    public void init()
    {
        super.init();
    }

    public void process(CtInvocation<?> v) {
        
	System.out.printf("invoke %s %s\n" 
		, v.getExecutable().getDeclaringType().getSimpleName(), v.getExecutable().getSimpleName());
/*
try {
	System.out.println("invoke " + v.getExecutable().getActualMethod());
} catch(Exception e) {
}
*/

/*
      String name = v.getSimpleName();
      if (ReplaceUtil.isReserved(name))
        v.setSimpleName("jas$" + name);
*/
    }
}
