
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.reference.*;

public class NewArrayProcessor extends AbstractProcessor<CtNewArray> {

    public void init()
    {
        super.init();
    }

    public void process(CtNewArray a) {

System.out.println("processing NewArray...");
        
      CtArrayTypeReference at = (CtArrayTypeReference) a.getType();
      CtTypeReference t = at.getComponentType();

      if (t.getActualClass().equals(Byte.class)
	|| t.getActualClass().equals(Byte.TYPE)) {
	System.out.println("...ByteArray");
      } else {
	System.out.println("..." + t.getActualClass().getName());
      }
    }
}
