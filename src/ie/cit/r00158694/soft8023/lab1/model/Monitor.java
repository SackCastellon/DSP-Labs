/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Monitor {

	private final Set<IClient> clients = new HashSet<>();
	private final Set<String> files = new HashSet<>();
	private final Map<IClient, String> lockedFiles = new HashMap<>();

	private static Monitor instance = null;

	private Monitor() {
		// TODO
	}

	/**
	 * Returns a unique instance of the Monitor.
	 *
	 * @return The monitor instance
	 */
	public static Monitor getInstance() {
		if (instance == null) instance = new Monitor();
		return instance;
	}

	/**
	 * Subscribes the client to the monitor so it will receive an {@link UpdateEvent} every time something changes.
	 *
	 * @param client The client to add
	 */
	public void addClient(IClient client) { clients.add(client); }

	/**
	 * Un-subscribes the client from the monitor so it will no longer receive an UpdateEvent when something changes.
	 *
	 * @param client The client to remove
	 */
	public void removeClient(IClient client) { clients.remove(client); }

	/**
	 * Returns a (sorted) list of the clients subscribed to the monitor to receive updates.
	 *
	 * @return The list of clients
	 */
	public List<IClient> getClients() {
		ArrayList<IClient> list = new ArrayList<>(clients);
		list.sort(Comparator.comparing(IClient::getClientName));
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
	public boolean addFile(IClient client, String file) {
		boolean add = files.add(file);
		if (add) notifyClients(new UpdateEvent(client, Action.ADDED, file));
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
	public boolean removeFile(IClient client, String file) {
		boolean remove = !lockedFiles.containsValue(file) && files.remove(file);
		if (remove) notifyClients(new UpdateEvent(client, Action.REMOVED, file));
		return remove;
	}

	/**
	 * Returns a (sorted) list of the files in the folder being monitored.
	 *
	 * @return The list of files
	 */
	public List<String> getFiles() {
		ArrayList<String> list = new ArrayList<>(files);
		list.sort(String::compareToIgnoreCase);
		return list;
	}

	/**
	 * Returns a map of the locked files in the folder being monitored.<br>
	 * The key is a client and the value is the file that the client is locking.
	 *
	 * @return The map of locked files
	 */
	public Map<IClient, String> getLockedFiles() { return lockedFiles; }

	/**
	 * Plays a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client playing the file
	 * @param file   The file being played
	 *
	 * @return If the file is played successfully {@code true} otherwise {@code false}
	 */
	public boolean playFile(IClient client, String file) {
		boolean play = files.contains(file);
		if (play) {
			String previous = lockedFiles.get(client);
			if (previous != null) stopPlayingFile(client, previous);
			lockedFiles.put(client, file);
			notifyClients(new UpdateEvent(client, Action.STARTED_PLAYING, file));
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
	public boolean stopPlayingFile(IClient client, String file) {
		boolean stop = lockedFiles.remove(client, file);
		if (stop) notifyClients(new UpdateEvent(client, Action.STOPPED_PLAYING, file));
		return stop;
	}

	/**
	 * Sends the given {@link UpdateEvent} to all the subscribed clients
	 *
	 * @param event The update event
	 */
	private void notifyClients(UpdateEvent event) { clients.forEach(client -> client.update(event)); }
}
