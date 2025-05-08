package serverSide.sharedRegions;

import commInfra.Message;

import serverSide.main.SimulPar;
import clientSide.entities.VoterStates;

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

      switch (inMessage.getMsgType ())

      { case MessageType.REQCUTH:  ((BarberShopClientProxy) Thread.currentThread ()).setCustomerId (inMessage.getCustId ());
                                   ((BarberShopClientProxy) Thread.currentThread ()).setCustomerState (inMessage.getCustState ());
                                   if (bShop.goCutHair ())
                                      outMessage = new Message (MessageType.CUTHDONE,
                                                                ((BarberShopClientProxy) Thread.currentThread ()).getCustomerId (),
                                                                ((BarberShopClientProxy) Thread.currentThread ()).getCustomerState ());
                                      else outMessage = new Message (MessageType.BSHOPF,
                                                                     ((BarberShopClientProxy) Thread.currentThread ()).getCustomerId (),
                                                                     ((BarberShopClientProxy) Thread.currentThread ()).getCustomerState ());
                                   break;
        case MessageType.SLEEP:    ((BarberShopClientProxy) Thread.currentThread ()).setBarberId (inMessage.getBarbId ());
                                   if (bShop.goToSleep ())
                                      outMessage = new Message (MessageType.SLEEPDONE,
                                                                ((BarberShopClientProxy) Thread.currentThread ()).getBarberId (), true);
                                      else outMessage = new Message (MessageType.SLEEPDONE,
                                                                     ((BarberShopClientProxy) Thread.currentThread ()).getBarberId (), false);
                                   break;
        case MessageType.CALLCUST: ((BarberShopClientProxy) Thread.currentThread ()).setBarberId (inMessage.getBarbId ());
                                   ((BarberShopClientProxy) Thread.currentThread ()).setBarberState (inMessage.getBarbState ());
                                   int custId = bShop.callACustomer ();
                                   outMessage = new Message (MessageType.CCUSTDONE,
                                                             ((BarberShopClientProxy) Thread.currentThread ()).getBarberId (),
                                                             ((BarberShopClientProxy) Thread.currentThread ()).getBarberState (), custId);
                                   break;
        case MessageType.RECPAY:   ((BarberShopClientProxy) Thread.currentThread ()).setBarberId (inMessage.getBarbId ());
                                   ((BarberShopClientProxy) Thread.currentThread ()).setBarberState (inMessage.getBarbState ());
                                   bShop.receivePayment (inMessage.getCustId ());
                                   outMessage = new Message (MessageType.RPAYDONE,
                                                             ((BarberShopClientProxy) Thread.currentThread ()).getBarberId (),
                                                             ((BarberShopClientProxy) Thread.currentThread ()).getBarberState ());
                                   break;
        case MessageType.ENDOP:    bShop.endOperation (inMessage.getBarbId ());
                                   outMessage = new Message (MessageType.EOPDONE, inMessage.getBarbId ());
                                   break;
        case MessageType.SHUT:     bShop.shutdown ();
                                   outMessage = new Message (MessageType.SHUTDONE);
                                   break;
      }

     return (outMessage);
   }
}
