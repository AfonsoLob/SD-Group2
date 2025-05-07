package serverSide.entities;

import commInfra.Message;
import commInfra.MessageException;
import commInfra.ServerCom;
import serverSide.sharedRegions.BarberShopInterface;
import serverSide.sharedRegions.PollingStationInterface;

// The Proxy serves as a service provider agent in a client-server architecture for a barber shop simulation. Here are its key functions:
//      Thread Management
//      State Management
//      Communication Handling

public class MPollingStationProxy extends Thread{
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

   private int clerkId;

  /**
   *  Clerk state.
   */

   private int clerkState;

  /**
   *  Instantiation of a client proxy.
   *
   *     @param sconi communication channel
   *     @param pStationInter interface to the barber shop
   */

   public MPollingStationProxy (ServerCom sconi, PollingStationInterface pStationInter)
   {
      super ("PollingStationProxy_" + MPollingStationProxy.getProxyId ());
      this.sconi = sconi;
      this.pStationInter = pStationInter;
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
   *   Set barber id.
   *
   *     @param id barber id
   */

   public void setClerkId (int id)
   {
    clerkId = id;
   }

  /**
   *   Get barber id.
   *
   *     @return barber id
   */

   public int getClerkId ()
   {
      return clerkId;
   }

  /**
   *   Set barber state.
   *
   *     @param state new barber state
   */

   public void setClerkState (int state)
   {
      clerkState = state;
   }

  /**
   *   Get barber state.
   *
   *     @return barber state
   */

   public int getClerkState ()
   {
      return clerkState;
   }

  /**
   *   Set customer id.
   *
   *     @param id customer id
   */

   public void setVoterId (int id)
   {
      voterId = id;
   }

  /**
   *   Get customer id.
   *
   *     @return customer id
   */

   public int getVoterId ()
   {
      return voterId;
   }

  /**
   *   Set customer state.
   *
   *     @param state new customer state
   */

   public void setVoterState (int state)
   {
      voterState = state;
   }

  /**
   *   Get customer state.
   *
   *     @return customer state
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

     /* service providing */

      inMessage = (Message) sconi.readObject ();                     // get service request
      try
      { outMessage = pStationInter.processAndReply (inMessage);         // process it
      }
      catch (MessageException e)
      { GenericIO.writelnString ("Thread " + getName () + ": " + e.getMessage () + "!");
        GenericIO.writelnString (e.getMessageVal ().toString ());
        System.exit (1);
      }
      sconi.writeObject (outMessage);                                // send service reply
      sconi.close ();                                                // close the communication channel
   }
}
