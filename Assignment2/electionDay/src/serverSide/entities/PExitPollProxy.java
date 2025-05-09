package serverSide.entities;

import clientSide.interfaces.ExitPoll.IExitPoll_all;
// import java.io.IOException;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
// import java.net.Socket;

import commInfra.Message;
import commInfra.MessageException;
import commInfra.MessageType;
import commInfra.ServerCom;

/**
 * PMExitPollClientProxy - Thread that handles communication with an exit poll client.
 * It implements the protocol for interaction.
 * It manages the Thread life cycle, the state and the communication channels
 */
public class PExitPollProxy extends Thread {
    private static int nProxy = 0; // Number of instantiayed threads.
   private ServerCom.ServerComHandler sconi; // communication channel
   private IExitPoll_all exitPoll; 
   private int voterId;

  /**
   *  Instantiation of a client proxy.
   *
   *     @param sconi communication channel
   *     @param exitPoll interface to the barber shop
   */

   public PExitPollProxy (ServerCom.ServerComHandler sconi, IExitPoll_all exitPoll)
   {
      super ("ExitPollProxy_" + PExitPollProxy.getProxyId ());
      this.sconi = sconi;
      this.exitPoll = exitPoll;
   }


   public PExitPollProxy getInstance (ServerCom.ServerComHandler sconi, IExitPoll_all pStationInter)
   {
      PExitPollProxy proxy = new PExitPollProxy (sconi, pStationInter);
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
      { cl = Class.forName ("serverSide.entities.PExitPollProxy");
      }
      catch (ClassNotFoundException e)
      { System.out.println("Data type PExitPollProxy was not found!");
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


   @Override
   public void run ()
   {
      Message inMessage = null,                                      // service request
              outMessage = null;                                     // service reply

      
     /* service providing */ // versão ServerCom nossa, em caso de erro mudar para a versão do prof
      inMessage = sconi.readMessage();                                // get service request
      // inMessage = (Message) sconi.readObject ();                     // get service request
      try
      { outMessage = processAndReply(inMessage);         // process it
      }
      catch (MessageException e)
      {
         System.out.println("Message exception on serverSide: " + e.getMessage());
         e.printStackTrace ();
         // outMessage = new Message (Message.ERROR, "Message exception on serverSide: " + e.getMessage()); // send error message
         System.exit (1);
      }
      sconi.writeMessage(outMessage);                                // send service reply
      sconi.close ();                                                // close the communication channel
   }

   public Message processAndReply (Message inMessage) throws MessageException
   {
      Message outMessage = null;                                     // outgoing message
      switch (inMessage.getType())
      {  
         //  VOTER MESSAGES
         case EXIT_POLL_ENTER:  ((PExitPollProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());             
            exitPoll.exitPollingStation(inMessage.getId(), inMessage.getVotingOption(), inMessage.didHeVote());
            outMessage = new Message (MessageType.VOTER_ENTER_GRANTED,((PExitPollProxy) Thread.currentThread ()).getVoterId());                          
         break;

         case EXIT_POLL_INQUIRY:             
            exitPoll.inquire();
            outMessage = new Message (MessageType.EXIT_POLL_RESPONSE);                          
         break;

         case EXIT_POLL_CLOSE:             
            exitPoll.closeIn(inMessage.getCloseIn());;
            outMessage = new Message (MessageType.EXIT_POLL_CLOSED);                          
         break;
         
         default:
         // Handle all other cases 
            outMessage = new Message(MessageType.ERROR);
         break;
      }

     return (outMessage);
   }
}
