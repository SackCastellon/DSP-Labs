/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import ie.cit.r00158694.soft8023.lab1.model.client.Client;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceMonitor {

	private final Set<Client> clients = new HashSet<>();
	private final Set<SharedFile> files = new HashSet<>();
	private final Map<Client, String> lockedFiles = new HashMap<>();

	private static ResourceMonitor instance = null;

	private final int maxSimultaneousAccess;
	private final File sharedFolder; // TODO

	private ResourceMonitor(int maxSimultaneousAccess, File sharedFolder) {
		this.maxSimultaneousAccess = maxSimultaneousAccess;
		this.sharedFolder = sharedFolder;
	}

	/**
	 * Returns a unique instance of the Monitor.
	 *
	 * @return The monitor instance
	 */
	public static ResourceMonitor getInstance() {
		if (instance == null) instance = new ResourceMonitor(2, new File("SharedFolder"));
		return instance;
	}

	/**
	 * Subscribes the client to the monitor so it will receive an {@link UpdateEvent} every time something changes.
	 *
	 * @param client The client to add
	 */
	public boolean addClient(Client client) { return clients.add(client); }

	/**
	 * Un-subscribes the client from the monitor so it will no longer receive an UpdateEvent when something changes.
	 *
	 * @param client The client to remove
	 */
	public boolean removeClient(Client client) { return clients.remove(client); }

	/**
	 * Returns a (sorted) list of the clients subscribed to the monitor to receive updates.
	 *
	 * @return The list of clients
	 */
	public List<Client> getClients() {
		ArrayList<Client> list = new ArrayList<>(clients);
		list.sort(Comparator.comparing(Client::getClientName));
		return list;
	}

	/**
	 * Adds a file to the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client adding the file
	 * @param file   The file being added
	 *
	 * @return If the file is added successfully {@code true} otherwise {@code false}
	 */
	public boolean addFile(Client client, SharedFile file) {
		boolean add = files.add(file);
		if (add) notifyClients(new UpdateEvent(client, Action.ADD, file.getName()));
		return add;
	}

	/**
	 * Removes a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client removing the file
	 * @param file   The file being removed
	 *
	 * @return If the file is removed successfully {@code true} otherwise {@code false}
	 */
	public boolean deleteFile(Client client, String file) {
		boolean remove = !lockedFiles.containsValue(file) && files.removeIf(f -> f.getName().equals(file));
		if (remove) notifyClients(new UpdateEvent(client, Action.REMOVE, file));
		return remove;
	}

	/**
	 * Plays a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client playing the file
	 * @param file   The file being played
	 *
	 * @return If the file is played successfully {@code true} otherwise {@code false}
	 */
	public boolean readFile(Client client, String file) {
		boolean play = files.stream().anyMatch(f -> f.getName().equals(file)) && !file.equals(lockedFiles.get(client)) && getLockedFiles().values().stream().mapToInt(s -> s.equals(file) ? 1 : 0)
				.sum() < maxSimultaneousAccess;
		if (play) {
			String previous = lockedFiles.get(client);
			if (previous != null) releaseFile(client, previous);
			lockedFiles.put(client, file);
			notifyClients(new UpdateEvent(client, Action.READ, file));
		}
		return play;
	}

	/**
	 * Stops playing a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client to stop playing the file
	 * @param file   The file to be stopped playing
	 *
	 * @return If the file is stopped playing successfully {@code true} otherwise {@code false}
	 */
	public boolean releaseFile(Client client, String file) {
		boolean stop = lockedFiles.remove(client, file);
		if (stop) notifyClients(new UpdateEvent(client, Action.RELEASE, file));
		return stop;
	}

	/**
	 * Returns a (sorted) list of the files in the folder being monitored.
	 *
	 * @return The list of files
	 */
	public List<SharedFile> getFiles() {
		ArrayList<SharedFile> list = new ArrayList<>(files);
		list.sort(SharedFile::compareTo);
		return list;
	}

	/**
	 * Returns a map of the locked files in the folder being monitored.<br>
	 * The key is a client and the value is the file that the client is locking.
	 *
	 * @return The map of locked files
	 */
	public Map<Client, String> getLockedFiles() { return lockedFiles; }

	/**
	 * Sends the given {@link UpdateEvent} to all the subscribed clients
	 *
	 * @param event The update event
	 */
	private void notifyClients(UpdateEvent event) {
		System.out.println();
		clients.forEach(client -> client.update(event));
	}
}
