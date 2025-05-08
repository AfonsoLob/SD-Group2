package serverSide.entities;


import example.commInfra.ServerCom.ServerComHandler;
import serverSide.interfaces.Logger.ILogger_all;


public class PLoggerProxy extends Thread {

    private static int nProxy = 0;
    private ServerComHandler sconi;
    private ILogger_all loggerInter;

    private PLoggerProxy(ServerComHandler sconi, ILogger_all loggerInter) {
        super("PLoggerProxy_" + PLoggerProxy.getProxyId());
        this.sconi = sconi;
        this.loggerInter = loggerInter;
    }

    public static PLoggerProxy getInstanceLoggerProxy(ServerComHandler sconi, ILogger_all loggerInter) {
        return new PLoggerProxy(sconi, loggerInter);
    }

    private static int getProxyId() {
        Class<?> cl = null;
        int proxyId;

        try {
            cl = Class.forName("serverSide.entities.PLoggerProxy");
        } catch (ClassNotFoundException e) {
            System.err.println("Data type PLoggerProxy was not found!");
            e.printStackTrace();
            System.exit(1);
        }
        synchronized (cl) {
            proxyId = nProxy;
            nProxy += 1;
        }
        return proxyId;
    }

    
}
