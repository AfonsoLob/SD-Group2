package serverSide.sharedRegions;

import commInfra.Message;
import commInfra.MessageType;
import commInfra.MessageException;
import serverSide.entities.PMPollingStationProxy;


public class PollingStationInterface {
    /**
   *  Reference to the barber shop.
   */

   private final MPollingStation pollingStation;

  /**
   *  Instantiation of an interface to the barber shop.
   *
   *    @param pollingStation reference to the barber shop
   */

   public PollingStationInterface (MPollingStation pollingStation)
   {
      this.pollingStation = pollingStation;
   }

  /**
   *  Processing of the incoming messages.
   *
   *  Validation, execution of the corresponding method and generation of the outgoing message.
   *
   *    @param inMessage service request
   *    @return service reply
   *    @throws MessageException if the incoming message is not valid
   */

   public Message processAndReply (Message inMessage) throws MessageException
   {
      Message outMessage = null;                                     // outgoing message

     /* validation of the incoming message */

      // switch (inMessage.getType())
      // { 
      //    case MessageType.REQCUTH:  if ((inMessage.getCustId () < 0) || (inMessage.getCustId () >= SimulPar.N))
      //                                 throw new MessageException ("Invalid voter id!", inMessage);
      //                                 else if ((inMessage.getCustState () < CustomerStates.DAYBYDAYLIFE) || (inMessage.getCustState () > CustomerStates.CUTTHEHAIR))
      //                                         throw new MessageException ("Invalid customer state!", inMessage);
      //                              break;
      //   case MessageType.SLEEP:    if ((inMessage.getBarbId () < 0) || (inMessage.getBarbId () >= SimulPar.M))
      //                                 throw new MessageException ("Invalid barber id!", inMessage);
      //                              break;
      //   case MessageType.CALLCUST: if ((inMessage.getBarbId () < 0) || (inMessage.getBarbId () >= SimulPar.M))
      //                                 throw new MessageException ("Invalid barber id!", inMessage);
      //                                 else if ((inMessage.getBarbState () < BarberStates.SLEEPING) || (inMessage.getBarbState () > BarberStates.INACTIVITY))
      //                                         throw new MessageException ("Invalid barber state!", inMessage);
      //                              break;
      //   case MessageType.RECPAY:   if ((inMessage.getBarbId () < 0) || (inMessage.getBarbId () >= SimulPar.M))
      //                                 throw new MessageException ("Invalid barber id!", inMessage);
      //                                 else if ((inMessage.getBarbState () < BarberStates.SLEEPING) || (inMessage.getBarbState () > BarberStates.INACTIVITY))
      //                                         throw new MessageException ("Invalid barber state!", inMessage);
      //                                         else if ((inMessage.getCustId () < 0) || (inMessage.getCustId () >= SimulPar.N))
      //                                                 throw new MessageException ("Invalid customer id!", inMessage);
      //                              break;
      //   case MessageType.ENDOP:    if ((inMessage.getBarbId () < 0) || (inMessage.getBarbId () >= SimulPar.M))
      //                                 throw new MessageException ("Invalid barber id!", inMessage);
      //                              break;
      //   case MessageType.SHUT:     // check nothing
      //                              break;

      //   default:                   throw new MessageException ("Invalid message type!", inMessage);
      // }

     /* processing */

      switch (inMessage.getType())
      {  
         //  VOTER MESSAGES
         case VOTER_ENTER_REQUEST:  ((PMPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (pollingStation.enterPollingStation(inMessage.getId ()))
               outMessage = new Message (MessageType.VOTER_ENTER_GRANTED,((PMPollingStationProxy) Thread.currentThread ()).getVoterId());                          
            else 
               outMessage = new Message (MessageType.ERROR);
         break;

         case ID_CHECK_REQUEST:  ((PMPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (pollingStation.waitIdValidation(inMessage.getId ()))
               outMessage = new Message (MessageType.ID_VALID,((PMPollingStationProxy) Thread.currentThread ()).getVoterId());                          
            else 
               outMessage = new Message (MessageType.ID_INVALID,((PMPollingStationProxy) Thread.currentThread ()).getVoterId());                          
         break;

         case VOTE_CAST_REQUEST:  ((PMPollingStationProxy) Thread.currentThread ()).setVoterId (inMessage.getId ());                              
            if (inMessage.getVotingOption() == 1)
               pollingStation.voteA(inMessage.getId ());
            else
               pollingStation.voteB(inMessage.getId ());
            outMessage = new Message (MessageType.VOTE_CAST_DONE,((PMPollingStationProxy) Thread.currentThread ()).getVoterId());
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
