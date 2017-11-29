/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMonitor extends Remote {

	/**
	 * Adds a file to the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client adding the file
	 * @param file     The file being added
	 *
	 * @return If the file is added successfully {@code true} otherwise {@code false}
	 */
	boolean addFile(String clientId, SharedFile file) throws RemoteException;

	/**
	 * Removes a fileName from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client removing the fileName
	 * @param fileName The fileName being removed
	 *
	 * @return If the fileName is removed successfully {@code true} otherwise {@code false}
	 */
	boolean deleteFile(String clientId, String fileName) throws RemoteException;

	/**
	 * Reads a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client reading the file
	 * @param fileName The file being played
	 *
	 * @return If the file is played successfully {@code true} otherwise {@code false}
	 */
	boolean readFile(String clientId, String fileName) throws RemoteException;

	/**
	 * Stops reading a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client to stop reading the file
	 * @param fileName The file to be stopped playing
	 *
	 * @return If the file is stopped playing successfully {@code true} otherwise {@code false}
	 */
	boolean releaseFile(String clientId, String fileName) throws RemoteException;
}
