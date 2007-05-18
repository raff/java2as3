
import spoon.processing.AbstractProcessor;
import spoon.processing.TraversalStrategy;
import spoon.reflect.code.*;
import java.util.*;

public class VariableAccessProcessor extends AbstractProcessor<CtVariableAccess> {
    public void init()
    {
        super.init();
    }

    public void process(CtVariableAccess v) {
        
      String name = v.getVariable().getSimpleName();
      if (ReplaceUtil.isReserved(name))
        v.getVariable().setSimpleName("jas$" + name);
    }
}
