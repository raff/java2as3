
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.declaration.CtClass;

/**
 * This processor translates a Java source code into AS3 code
 * 
 * This processor does not perform any concrete translation. It just registers
 * the other processor which will be perform the real job. This processor is
 * just the entry point of the translation process.
 */
public class Java2AS3Processor extends AbstractProcessor<CtClass<?>> {
    
    public void init() {
        
        super.init();
        
        ProcessingManager pm = getEnvironment().getManager();
        pm.addProcessor( new ClassProcessor() );
        pm.addProcessor( new VariableAccessProcessor() );
        pm.addProcessor( new VariableProcessor() );
//        pm.addProcessor( new NewArrayProcessor() );
//	pm.addProcessor( new NewClassProcessor() );
//        pm.addProcessor( new InvocationProcessor() );

	// must be last
        pm.addProcessor( new PrintProcessor() );
    }

    public boolean isToBeProcessed( CtClass<?> ct ) {
        return false;
    }
    public void process( CtClass<?> ct ) {
    }
}
