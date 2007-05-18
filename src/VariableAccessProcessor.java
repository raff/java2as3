
import spoon.processing.AbstractProcessor;
import spoon.processing.TraversalStrategy;
import spoon.reflect.code.*;
import java.util.*;

public class VariableAccessProcessor extends AbstractProcessor<CtVariableAccess> {
    private Set<String> processed = new HashSet();

    public void init()
    {
        super.init();
    }

    public void process(CtVariableAccess v) {
        
      String name = v.getVariable().getSimpleName();
      if (processed.contains(name))
        v.getVariable().setSimpleName("jas$" + name);
      else if (ReplaceUtil.isReserved(name)) {
        v.getVariable().getDeclaration().setSimpleName("jas$" + name);
        processed.add(name);
      }
    }
}
