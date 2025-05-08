package serverSide.entities;

import commInfra.Message;
import commInfra.MessageException;
// import commInfra.MessageException;
import commInfra.ServerCom;
import serverSide.sharedRegions.PollingStationInterface;

// The Proxy serves as a service provider agent in a client-server architecture for a barber shop simulation. Here are its key functions:
//      Thread Management
//      State Management
//      Communication Handling

public class PMPollingStationProxy extends Thread{
    /**
   *  Number of instantiayed threads.
   */
   private static int nProxy = 0;

  /**
   *  Communication channel.
   */

   private ServerCom sconi;

  /**
   *  Interface to the Polling Station Shop.
   */

   private PollingStationInterface pStationInter;

  /**
   *  Voter identification.
   */

   private int voterId;

  /**
   *  Voter state.
   */

   private int voterState;

  /**
   *  Clerk identification.
   */

   // private int clerkId;

  /**
   *  Clerk state.
   */

   // private int clerkState;

  /**
   *  Instantiation of a client proxy.
   *
   *     @param sconi communication channel
   *     @param pStationInter interface to the barber shop
   */

   private PMPollingStationProxy (ServerCom sconi, PollingStationInterface pStationInter)
   {
      super ("PollingStationProxy_" + PMPollingStationProxy.getProxyId ());
      this.sconi = sconi;
      this.pStationInter = pStationInter;
   }


   public PMPollingStationProxy getInstance (ServerCom sconi, PollingStationInterface pStationInter)
   {
      PMPollingStationProxy proxy = new PMPollingStationProxy (sconi, pStationInter);
      return proxy;
   }

  /**
   *  Generation of the instantiation identifier.
   *
   *     @return instantiation identifier
   */

   private static int getProxyId ()
   {
      Class<?> cl = null;                                            // representation of the BarberShopClientProxy object in JVM
      int proxyId;                                                   // instantiation identifier

      try
      { cl = Class.forName ("serverSide.entities.MPollingStationProxy");
      }
      catch (ClassNotFoundException e)
      { GenericIO.writelnString ("Data type MPollingStationProxy was not found!");
        e.printStackTrace ();
        System.exit (1);
      }
      synchronized (cl)
      { proxyId = nProxy;
        nProxy += 1;
      }
      return proxyId;
   }


  /**
   *   Set voter id.
   *
   *     @param id voter id
   */

   public void setVoterId (int id)
   {
      voterId = id;
   }

  /**
   *   Get voter id.
   *
   *     @return voter id
   */

   public int getVoterId ()
   {
      return voterId;
   }

  /**
   *   Set voter state.
   *
   *     @param state new voter state
   */

   public void setVoterState (int state)
   {
      voterState = state;
   }

  /**
   *   Get voter state.
   *
   *     @return voter state
   */

   public int getVoterState ()
   {
      return voterState;
   }

  /**
   *  Life cycle of the service provider agent.
   */

   @Override
   public void run ()
   {
      Message inMessage = null,                                      // service request
              outMessage = null;                                     // service reply

      
     /* service providing */ // versão ServerCom nossa, em caso de erro mudar para a versão do prof
      sconi.start();
      ServerCom.ServerComHandler handler = sconi.accept();
      inMessage = handler.readMessage();                                // get service request
      // inMessage = (Message) sconi.readObject ();                     // get service request
      try
      {
         outMessage = pStationInter.processAndReply (inMessage);         // process it
      }
      catch (MessageException e)
      {
         System.out.println("Message exception on serverSide: " + e.getMessage());
         e.printStackTrace ();
         // outMessage = new Message (Message.ERROR, "Message exception on serverSide: " + e.getMessage()); // send error message
         System.exit (1);
      }
      handler.writeMessage(outMessage);                                // send service reply
      handler.close ();                                                // close the communication channel
   }
}
