package serverSide.entities;
// import clientSide.interfaces.Pollingstation.IPollingStation_all;
import serverSide.interfaces.Pollingstation.IPollingStation_all;
import commInfra.Message;
import commInfra.MessageException;
import commInfra.MessageType;
import commInfra.ServerCom;



public class PPollingStationProxy extends Thread{
  
   private static int nProxy = 0; // Number of instantiayed threads.
   private ServerCom.ServerComHandler sconi; // communication channel
   private IPollingStation_all pollingStation; 
   private int voterId;

  /**
   *  Instantiation of a client proxy.
   *
   *     @param sconi communication channel
   *     @param pStationInter interface to the barber shop
   */

   public PPollingStationProxy (ServerCom.ServerComHandler sconi, IPollingStation_all pStationInter)
   {
      super ("PollingStationProxy_" + PPollingStationProxy.getProxyId ());
      this.sconi = sconi;
      this.pollingStation = pStationInter;
   }


   public static PPollingStationProxy getInstance (ServerCom.ServerComHandler sconi, IPollingStation_all pStationInter)
   {
      PPollingStationProxy proxy = new PPollingStationProxy (sconi, pStationInter);
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
      { 
         cl = Class.forName ("serverSide.entities.PPollingStationProxy");
      }
      catch (ClassNotFoundException e)
      { System.out.println("Data type PPollingStationProxy was not found!");
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
      { 
         outMessage = processAndReply(inMessage);         // process it
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
         case VOTER_ENTER_REQUEST:  ((PPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (pollingStation.enterPollingStation(inMessage.getId ()))
               outMessage = new Message (MessageType.VOTER_ENTER_GRANTED,((PPollingStationProxy) Thread.currentThread ()).getVoterId());                          
            else 
               outMessage = new Message (MessageType.ERROR);
         break;

         case ID_CHECK_REQUEST:  ((PPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (pollingStation.waitIdValidation(inMessage.getId ()))
               outMessage = new Message (MessageType.ID_VALID,((PPollingStationProxy) Thread.currentThread ()).getVoterId());                          
            else 
               outMessage = new Message (MessageType.ID_INVALID,((PPollingStationProxy) Thread.currentThread ()).getVoterId());                          
         break;

         case VOTE_CAST_REQUEST:  ((PPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (inMessage.getVotingOption())
               pollingStation.voteA(inMessage.getId ());
            else
               pollingStation.voteB(inMessage.getId ());
            outMessage = new Message (MessageType.VOTE_CAST_DONE,((PPollingStationProxy) Thread.currentThread ()).getVoterId());
         break;

         case POLLING_STATION_IS_OPEN:                              
            if (pollingStation.isOpen())
               outMessage = new Message (MessageType.POLLING_STATION_READY);
            else
               outMessage = new Message (MessageType.POLLING_STATION_CLOSED);

         // CLERK MESSAGES
         case POLLING_STATION_OPEN:                              
            pollingStation.openPollingStation();
            outMessage = new Message (MessageType.POLLING_STATION_READY);
         break;

         case POLLING_STATION_CLOSE:                              
            pollingStation.closePollingStation();
            outMessage = new Message (MessageType.POLLING_STATION_CLOSED);
         break;

         case VALIDATE_NEXT_VOTER:                              
            if(pollingStation.callNextVoter())
               outMessage = new Message (MessageType.ID_VALID);
            else
               outMessage = new Message (MessageType.ID_INVALID);
         break;

         case VOTERS_QUEUE_REQUEST:                              
            int queueSize = pollingStation.numberVotersInQueue();
            outMessage = new Message (MessageType.VOTERS_QUEUE_RESPONSE, queueSize);
         break;

         default:
         // Handle all other cases 
            outMessage = new Message(MessageType.ERROR);
         break;
      }

     return (outMessage);
   }
}
