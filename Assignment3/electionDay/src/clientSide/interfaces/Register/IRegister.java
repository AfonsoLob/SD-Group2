package clientSide.interfaces.Register;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Defines the RMI interface for a registration service.
 * This service allows other RMI services to bind themselves with a name
 * and for clients or other services to look them up.
 */
public interface IRegister extends Remote {

    /**
     * Binds a remote object to a name in this registry.
     *
     * @param name the name to associate with the remote object.
     * @param obj  a reference to the remote object.
     * @throws RemoteException if a remote communication error occurs.
     * @throws AlreadyBoundException if the name is already bound.
     */
    void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException;

    /**
     * Returns a reference to the remote object associated with the specified name.
     *
     * @param name the name to look up.
     * @return a reference to the remote object.
     * @throws RemoteException if a remote communication error occurs.
     * @throws NotBoundException if the name is not currently bound.
     */
    Remote lookup(String name) throws RemoteException, NotBoundException;

    /**
     * Removes the binding for the specified name from this registry.
     *
     * @param name the name of the binding to remove.
     * @throws RemoteException if a remote communication error occurs.
     * @throws NotBoundException if the name is not currently bound.
     */
    void unbind(String name) throws RemoteException, NotBoundException;

    /**
     * Returns an array of the names bound in this registry.
     *
     * @return an array of names currently bound.
     * @throws RemoteException if a remote communication error occurs.
     */
    String[] list() throws RemoteException;
}
