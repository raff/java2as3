
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.*;

public class VariableProcessor extends AbstractProcessor<CtVariable> {

    public void init()
    {
        super.init();
    }

    public void process(CtVariable v) {
        
      ReplaceUtil.replaceName(v);
    }
}
