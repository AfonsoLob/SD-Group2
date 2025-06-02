package serverSide.main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import serverSide.interfaces.Register.IRegister;
import serverSide.sharedRegions.RegisterRemoteObject;


public class RegisterRemoteObjectServer {
    
    // Static references for cleanup
    private static IRegister regEngineStub = null;
    private static RegisterRemoteObject regEngine = null;
    private static Registry registry = null;
    private static volatile boolean shutdownRequested = false;
    private static final String NAME_ENTRY = "RegisterService";
    
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
      System.out.println("RegisterRemoteObjectServer starting...");

      // Add shutdown hook for proper cleanup
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          System.out.println("RegisterRemoteObjectServer: Shutdown hook triggered, performing cleanup...");
          performShutdown();
      }));

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

     /* Security manager is no longer needed in modern Java versions */
      
      System.out.println ("Security manager setup skipped (deprecated in modern Java)!");

     /* instantiate a registration remote object and generate a stub for it */

      regEngine = new RegisterRemoteObject (rmiRegHostName, rmiRegPortNumb);  // object that enables the registration
                                                                                                   // of other remote objects

      try
      { regEngineStub = (IRegister) UnicastRemoteObject.exportObject (regEngine, portNumb);
      }
      catch (RemoteException e)
      { System.out.println ("RegisterRemoteObject stub generation exception: " + e.getMessage ());
        System.exit (1);
      }
      System.out.println ("Stub was generated!");

     /* register it with the local registry service */

      try
      { 
        // Create the RMI registry on the specified port
        registry = LocateRegistry.createRegistry (rmiRegPortNumb);
        System.out.println ("RMI registry was created on port " + rmiRegPortNumb + "!");
      }
      catch (RemoteException e)
      { 
        // If creation fails, try to get existing registry
        System.out.println ("Failed to create registry, trying to connect to existing one...");
        try 
        {
          registry = LocateRegistry.getRegistry (rmiRegHostName, rmiRegPortNumb);
          System.out.println ("Connected to existing RMI registry!");
        }
        catch (RemoteException e2)
        {
          System.out.println ("RMI registry creation/connection exception: " + e2.getMessage ());
          System.exit (1);
        }
      }

      try
      { registry.rebind (NAME_ENTRY, regEngineStub);
      }
      catch (RemoteException e)
      { System.out.println ("RegisterRemoteObject remote exception on registration: " + e.getMessage ());
        System.exit (1);
      }
      System.out.println ("RegisterRemoteObject object was registered!");
      
      // Keep server running until shutdown is requested
      System.out.println("RegisterRemoteObjectServer: Service is now running and waiting for requests...");
      while (!shutdownRequested) {
          try {
              Thread.sleep(1000); // Check every second
          } catch (InterruptedException e) {
              System.out.println("RegisterRemoteObjectServer: Interrupted, shutting down...");
              Thread.currentThread().interrupt();
              break;
          }
      }
      
      System.out.println("RegisterRemoteObjectServer: Main thread exiting.");
   }
   
   /**
    * Perform clean shutdown of the RegisterRemoteObject service
    */
   private static void performShutdown() {
       shutdownRequested = true;
       
       try {
           if (registry != null && regEngineStub != null) {
               System.out.println("RegisterRemoteObjectServer: Unbinding " + NAME_ENTRY + "...");
               try {
                   registry.unbind(NAME_ENTRY);
                   System.out.println("RegisterRemoteObjectServer: Successfully unbound " + NAME_ENTRY);
               } catch (Exception e) {
                   System.err.println("RegisterRemoteObjectServer: Error unbinding service: " + e.getMessage());
               }
               
               System.out.println("RegisterRemoteObjectServer: Unexporting RegisterRemoteObject...");
               try {
                   UnicastRemoteObject.unexportObject(regEngine, true);
                   System.out.println("RegisterRemoteObjectServer: Successfully unexported RegisterRemoteObject");
               } catch (Exception e) {
                   System.err.println("RegisterRemoteObjectServer: Error unexporting object: " + e.getMessage());
               }
           }
       } catch (Exception e) {
           System.err.println("RegisterRemoteObjectServer: Error during shutdown: " + e.getMessage());
       }
       
       System.out.println("RegisterRemoteObjectServer: Shutdown complete.");
   }
}
