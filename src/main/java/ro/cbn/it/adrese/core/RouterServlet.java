package ro.cbn.it.adrese.core;


import ro.cbn.it.framework.annotation.UrlPattern;
import ro.cbn.it.framework.request.AlterableRequest;
import ro.cbn.it.framework.servlet.AbstractIController;
import ro.cbn.it.framework.servlet.IControllerImpl;
import ro.cbn.it.framework.servlet.ServletRoutingUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@UrlPattern("/")
public class RouterServlet extends IControllerImpl {

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        AlterableRequest alterableRequest = new AlterableRequest(req);
        Object controller = ServletRoutingUtils.getController(alterableRequest);
        if (controller instanceof AbstractIController) {
            ((AbstractIController) controller).run(alterableRequest, resp, null);
        }
    }
	
}
