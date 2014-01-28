/*
 * Created on Feb 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import javax.servlet.ServletContext;
import com.moedamas.busdriver.*;

/**
 * @author cemartins
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestBusDriver extends busService{

	/**
	 * @param context
	 */
	public TestBusDriver(ServletContext context) {
		super(context);

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.busService#goService()
	 */
	public void goService(busDriverRequest request) throws busException {
		
		
		request.addValue("myattribute","Olá Mundo!!!");
		request.addValue("myattribute", "olá outra vez");
		
		int Step = super.getStep(request);
		switch(Step) {
		case 1: throw new busServiceException("Teste de um busService Exception");
		case 2: throw new busException("Teste de um busException");
		}

	}
}
