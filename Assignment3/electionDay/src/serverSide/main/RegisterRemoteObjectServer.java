package serverSide.main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.AlreadyBoundException;

import interfaces.Register.Register;
import serverSide.sharedRegions.RegisterRemoteObject;

public class RegisterRemoteObjectServer {
    public static final String RMI_REGISTRY_HOSTNAME = "localhost";
    public static final int RMI_REGISTRY_PORT = 22350;
    public static final String REGISTER_SERVICE_NAME = "RegisterService";

    /**
     *  Main method.
     *
     *    @param args runtime arguments
     *        args[0] - port number for listening to service requests
     *        args[1] - name of the platform where is located the RMI registering service
     *        args[2] - port nunber where the registering service is listening to service requests
     */

    public static void main(String[] args)
    {
        int portNumb = -1;                                             // port number for listening to service requests
        String rmiRegHostName;                                         // name of the platform where is located the RMI registering service
        int rmiRegPortNumb = -1;                                       // port number where the registering service is listening to service requests

        if (args.length != 3)
        { System.out.println ("Wrong number of parameters!");
          System.exit (1);
        }
        try
        { portNumb = Integer.parseInt (args[0]);
        }
        catch (NumberFormatException e)
        { System.out.println ("args[0] is not a number!");
          System.exit (1);
        }
        if ((portNumb < 4000) || (portNumb >= 65536))
        { System.out.println ("args[0] is not a valid port number!");
          System.exit (1);
        }
        rmiRegHostName = args[1];
        try
        { rmiRegPortNumb = Integer.parseInt (args[2]);
        }
        catch (NumberFormatException e)
        { System.out.println ("args[2] is not a number!");
          System.exit (1);
        }
        if ((rmiRegPortNumb < 4000) || (rmiRegPortNumb >= 65536))
        { System.out.println ("args[2] is not a valid port number!");
          System.exit (1);
        }

        /* create and install the security manager */

        if (System.getSecurityManager () == null)
            System.setSecurityManager (new SecurityManager ());
        System.out.println ("Security manager was installed!");

        /* instantiate a registration remote object and generate a stub for it */

        RegisterRemoteObject regEngine = null;

        try {
            regEngine = new RegisterRemoteObject(rmiRegHostName, rmiRegPortNumb);
            try {
                regEngine.bind(REGISTER_SERVICE_NAME, regEngine);
                System.out.println("RegisterRemoteObjectServer: RegisterRemoteObject bound to '" + REGISTER_SERVICE_NAME + "'.");
            } catch (AlreadyBoundException e) {
                System.err.println("RegisterRemoteObjectServer: Service already bound: " + e.getMessage());
                System.exit(1);
            }
        } catch (RemoteException e) {
            System.err.println("RegisterRemoteObjectServer: Error creating or binding RegisterRemoteObject: " + e.getMessage());
            System.exit(1);
        }

        /* register it with the local registry service */

        String nameEntry = "RegisterHandler";                          // public name of the remote object that enables
                                                                       // the registration of other remote objects
        Registry registry = null;                                      // remote reference for registration in the RMI registry service

        try
        { registry = LocateRegistry.getRegistry (rmiRegHostName, rmiRegPortNumb);
        }
        catch (RemoteException e)
        { System.out.println ("RMI registry creation exception: " + e.getMessage ());
          System.exit (1);
        }
        System.out.println ("RMI registry was created!");

        try
        { registry.rebind (nameEntry, regEngine);
        }
        catch (RemoteException e)
        { System.out.println ("RegisterRemoteObject remote exception on registration: " + e.getMessage ());
          System.exit (1);
        }
        System.out.println ("RegisterRemoteObject object was registered!");
    }
}
