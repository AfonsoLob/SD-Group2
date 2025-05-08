package serverSide.entities;

import clientSide.interfaces.Pollingstation.IPollingStation_all;
import commInfra.Message;
import commInfra.MessageException;
import commInfra.MessageType;

import commInfra.ServerCom;
import serverSide.sharedRegions.MPollingStation;


public class MPollingStationProxy extends Thread{
  
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

   public MPollingStationProxy (ServerCom.ServerComHandler sconi, IPollingStation_all pStationInter)
   {
      super ("PollingStationProxy_" + MPollingStationProxy.getProxyId ());
      this.sconi = sconi;
      this.pollingStation = pStationInter;
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
      { GenericIO.writelnString ("Thread " + getName () + ": " + e.getMessage () + "!");
        GenericIO.writelnString (e.getMessageVal ().toString ());
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
         case VOTER_ENTER_REQUEST:  ((MPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (pollingStation.enterPollingStation(inMessage.getId ()))
               outMessage = new Message (MessageType.VOTER_ENTER_GRANTED,((MPollingStationProxy) Thread.currentThread ()).getVoterId());                          
            else 
               outMessage = new Message (MessageType.ERROR);
         break;

         case ID_CHECK_REQUEST:  ((MPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (pollingStation.waitIdValidation(inMessage.getId ()))
               outMessage = new Message (MessageType.ID_VALID,((MPollingStationProxy) Thread.currentThread ()).getVoterId());                          
            else 
               outMessage = new Message (MessageType.ID_INVALID,((MPollingStationProxy) Thread.currentThread ()).getVoterId());                          
         break;

         case VOTE_CAST_REQUEST:  ((MPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (inMessage.getVotingOption() == 1)
               pollingStation.voteA(inMessage.getId ());
            else
               pollingStation.voteB(inMessage.getId ());
            outMessage = new Message (MessageType.VOTE_CAST_DONE,((MPollingStationProxy) Thread.currentThread ()).getVoterId());
         break;

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

         default:
         // Handle all other cases 
            outMessage = new Message(MessageType.ERROR);
         break;
      }

     return (outMessage);
   }
}
