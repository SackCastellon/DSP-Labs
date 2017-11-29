/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.server;

import ie.cit.r00158694.soft8023.common.IMonitor;
import ie.cit.r00158694.soft8023.common.SharedFile;
import ie.cit.r00158694.soft8023.common.UpdateEvent;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class ResourceMonitor extends UnicastRemoteObject implements IMonitor {

	private final Map<String, String> clients = new HashMap<>();
	private final Map<String, LinkedList<UpdateEvent>> notificationLists = new HashMap<>();
	private final Set<SharedFile> files = new HashSet<>();
	private final Map<String, String> lockedFiles = new HashMap<>();

	private static ResourceMonitor instance = null;

	private final int maxSimultaneousAccess;
	private final File sharedFolder; // TODO

	private ResourceMonitor(int maxSimultaneousAccess, File sharedFolder) throws RemoteException {
		super();

		this.maxSimultaneousAccess = maxSimultaneousAccess;
		this.sharedFolder = sharedFolder;
	}

	/**
	 * Returns a unique instance of the Monitor.
	 *
	 * @return The monitor instance
	 */
	public static ResourceMonitor getInstance() throws RemoteException {
		if (instance == null) instance = new ResourceMonitor(2, new File("SharedFolder"));
		return instance;
	}

	/**
	 * Subscribes the client to the monitor so it will receive an {@link UpdateEvent} every time something changes.
	 *
	 * @param clientName       The name of the client to add
	 * @param notificationList The list of pending notifications for the client
	 *
	 * @return The ID assigned to the client added
	 */
	public String addClient(String clientName, LinkedList<UpdateEvent> notificationList) {
		String id = Long.toHexString(new Random().nextLong()).toUpperCase();
		synchronized (clients) { clients.put(id, clientName); }
		synchronized (notificationLists) { notificationLists.put(id, notificationList); }
		return id;
	}

	/**
	 * Un-subscribes the client from the monitor so it will no longer receive an UpdateEvent when something changes.
	 *
	 * @param clientId The ID of the client to remove
	 */
	public boolean removeClient(String clientId) {
		Optional<String> name = getName(clientId);
		synchronized (clients) { return name.isPresent() && clients.remove(clientId, name.get()); }
	}

	private Optional<String> getName(String clientId) {
		synchronized (clients) { return Optional.ofNullable(clients.get(clientId)); }
	}

	/**
	 * Adds a file to the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client adding the file
	 * @param file     The file being added
	 *
	 * @return If the file is added successfully {@code true} otherwise {@code false}
	 */
	@Override
	public boolean addFile(String clientId, SharedFile file) {
		Optional<String> name = getName(clientId);
		boolean add = false;

		if (name.isPresent()) {
			synchronized (files) { add = files.add(file); }

			if (add) notifyClients(new UpdateEvent(name.get(), UpdateEvent.Action.ADD, file.getName()));
		}

		return add;
	}

	/**
	 * Removes a fileName from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client removing the fileName
	 * @param fileName The fileName being removed
	 *
	 * @return If the fileName is removed successfully {@code true} otherwise {@code false}
	 */
	@Override
	public boolean deleteFile(String clientId, String fileName) {
		Optional<String> name = getName(clientId);
		boolean delete = false;

		if (name.isPresent()) {
			synchronized (lockedFiles) {
				if (!lockedFiles.containsValue(fileName))
					synchronized (files) {
						delete = files.removeIf(f -> f.getName().equals(fileName));
					}
			}

			if (delete) notifyClients(new UpdateEvent(name.get(), UpdateEvent.Action.DELETE, fileName));
		}

		return delete;
	}

	/**
	 * Reads a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client reading the file
	 * @param fileName The file being played
	 *
	 * @return If the file is played successfully {@code true} otherwise {@code false}
	 */
	@Override
	public boolean readFile(String clientId, String fileName) {
		Optional<String> name = getName(clientId);
		boolean read = false;

		if (name.isPresent()) {
			synchronized (lockedFiles) {
				read = (!fileName.equals(lockedFiles.get(clientId)) && lockedFiles.containsValue(fileName) && lockedFiles.values().stream().filter(fileName::equals).count() < maxSimultaneousAccess);
				if (!read)
					synchronized (files) { read = files.stream().anyMatch(f -> f.getName().equals(fileName)); }

				if (read) {
					String previous = lockedFiles.get(clientId);
					if (previous != null) releaseFile(clientId, previous);
					lockedFiles.put(clientId, fileName);
				}

			}

			if (read) notifyClients(new UpdateEvent(name.get(), UpdateEvent.Action.READ, fileName));
		}

		return read;
	}

	/**
	 * Stops reading a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param clientId The id of the client to stop reading the file
	 * @param fileName The file to be stopped playing
	 *
	 * @return If the file is stopped playing successfully {@code true} otherwise {@code false}
	 */
	@Override
	public boolean releaseFile(String clientId, String fileName) {
		Optional<String> name = getName(clientId);
		boolean release = false;

		if (name.isPresent()) {
			synchronized (lockedFiles) { release = lockedFiles.remove(clientId, fileName); }

			if (release) notifyClients(new UpdateEvent(name.get(), UpdateEvent.Action.RELEASE, fileName));
		}

		return release;
	}

	/**
	 * Returns a (sorted) list of the files in the folder being monitored.
	 *
	 * @return The list of files
	 */
	public List<SharedFile> getFiles() {
		return new ArrayList<>(files);
	}

	/**
	 * Returns a map of the locked files in the folder being monitored.<br>
	 * The key is a client and the value is the file that the client is locking.
	 *
	 * @return The map of locked files
	 */
	public Map<String, String> getLockedFiles() {
		return new HashMap<>(lockedFiles);
	}

	/**
	 * Sends the given {@link UpdateEvent} to all the subscribed clients
	 *
	 * @param event The update event
	 */
	private void notifyClients(UpdateEvent event) {
		System.out.println("[Resource Monitor] " + event.toString());
		synchronized (notificationLists) {
			notificationLists.forEach((s, list) -> list.add(event));
		}
	}
}
