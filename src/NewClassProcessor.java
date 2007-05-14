
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.reference.*;

public class NewClassProcessor extends AbstractProcessor<CtNewClass> {

    public void init()
    {
        super.init();
    }

    public void process(CtNewClass c) {
        
      CtTypeReference t = ReplaceUtil.replaceType(getFactory(), c.getType());
      c.setType(t);
    }
}
