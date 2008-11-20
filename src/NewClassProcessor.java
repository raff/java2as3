
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.reference.CtTypeReference;

public class NewClassProcessor extends AbstractProcessor<CtNewClass<?>> {

    public void init()
    {
        super.init();
    }

    public void process(CtNewClass<?> c) {
        
      CtTypeReference<?> t = ReplaceUtil.replaceType(c.getType());
      c.setType((CtTypeReference) t);
    }
}
