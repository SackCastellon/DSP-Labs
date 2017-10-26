/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.monitor;

import ie.cit.r00158694.soft8023.lab1.client.AbstractClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceMonitor {

	private final Set<AbstractClient> clients = new HashSet<>();
	private final Set<SharedFile> files = new HashSet<>();
	private final Map<AbstractClient, String> lockedFiles = new HashMap<>();

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
	public boolean addClient(AbstractClient client) {
		synchronized (clients) {
			return clients.add(client);
		}
	}

	/**
	 * Un-subscribes the client from the monitor so it will no longer receive an UpdateEvent when something changes.
	 *
	 * @param client The client to remove
	 */
	public boolean removeClient(AbstractClient client) {
		synchronized (clients) {
			return clients.remove(client);
		}
	}

	/**
	 * Returns a (sorted) list of the clients subscribed to the monitor to receive updates.
	 *
	 * @return The list of clients
	 */
	public List<AbstractClient> getClients() {
		ArrayList<AbstractClient> list = new ArrayList<>(clients);
		list.sort(Comparator.comparing(AbstractClient::getClientName));
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
	public boolean addFile(AbstractClient client, SharedFile file) {
		synchronized (files) {
			boolean add = files.add(file);
			if (add) notifyClients(new UpdateEvent(client, UpdateEvent.Action.ADD, file.getName()));
			return add;
		}
	}

	/**
	 * Removes a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client removing the file
	 * @param file   The file being removed
	 *
	 * @return If the file is removed successfully {@code true} otherwise {@code false}
	 */
	public boolean deleteFile(AbstractClient client, String file) {
		synchronized (files) {
			synchronized (lockedFiles) {
				boolean delete = !lockedFiles.containsValue(file) && files.removeIf(f -> f.getName().equals(file));
				if (delete) notifyClients(new UpdateEvent(client, UpdateEvent.Action.DELETE, file));
				return delete;
			}
		}
	}

	/**
	 * Plays a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client playing the file
	 * @param file   The file being played
	 *
	 * @return If the file is played successfully {@code true} otherwise {@code false}
	 */
	public boolean readFile(AbstractClient client, String file) {
		synchronized (files) {
			synchronized (lockedFiles) {
				boolean read = files.stream().anyMatch(f -> f.getName().equals(file)) && !file.equals(lockedFiles.get(client)) && getLockedFiles().values().stream().filter(file::equals).count() < maxSimultaneousAccess;
				if (read) {
					String previous = lockedFiles.get(client);
					if (previous != null) releaseFile(client, previous);
					lockedFiles.put(client, file);
					notifyClients(new UpdateEvent(client, UpdateEvent.Action.READ, file));
				}
				return read;
			}
		}
	}

	/**
	 * Stops playing a file from the folder being monitored and sends an update to all the subscribed clients.
	 *
	 * @param client The client to stop playing the file
	 * @param file   The file to be stopped playing
	 *
	 * @return If the file is stopped playing successfully {@code true} otherwise {@code false}
	 */
	public boolean releaseFile(AbstractClient client, String file) {
		synchronized (lockedFiles) {
			boolean release = lockedFiles.remove(client, file);
			if (release) notifyClients(new UpdateEvent(client, UpdateEvent.Action.RELEASE, file));
			return release;
		}
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
	public Map<AbstractClient, String> getLockedFiles() {
		return lockedFiles;
	}

	/**
	 * Sends the given {@link UpdateEvent} to all the subscribed clients
	 *
	 * @param event The update event
	 */
	private void notifyClients(UpdateEvent event) {
		synchronized (clients) {
			clients.forEach(client -> client.update(event));
		}
	}
}
