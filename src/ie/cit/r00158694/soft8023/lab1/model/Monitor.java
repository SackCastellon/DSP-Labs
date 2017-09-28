/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Monitor {

	private final Set<Client> clients = new HashSet<>();
	private final Set<String> files = new HashSet<>();
	private final Map<Client, String> lockedFiles = new HashMap<>();

	private static Monitor instance = null;

	private Monitor(String folder) {
		// TODO
	}

	public static Monitor getInstance() {
		if (instance == null) instance = new Monitor("folder");
		return instance;
	}

	public void addClient(Client client) { clients.add(client); }

	public void removeClient(Client client) { clients.remove(client); }

	public Set<Client> getClients() { return clients; }

	public boolean addFile(Client client, String file) {
		boolean add = files.add(file);
		if (add) notifyClients(new Event(client, Action.ADDED, file));
		return add;
	}

	public boolean removeFile(Client client, String file) {
		boolean remove = !lockedFiles.containsValue(file) && files.remove(file);
		if (remove) notifyClients(new Event(client, Action.REMOVED, file));
		return remove;
	}

	public Set<String> getFiles() { return files; }

	public Map<Client, String> getLockedFiles() { return lockedFiles; }

	public boolean playFile(Client client, String file) {
		boolean play = files.contains(file);
		if (play) {
			String previous = lockedFiles.get(client);
			if (previous != null) stopPlayingFile(client, previous);
			lockedFiles.put(client, file);
			notifyClients(new Event(client, Action.STARTED_PLAYING, file));
		}
		return play;
	}

	public boolean stopPlayingFile(Client client, String file) {
		boolean stop = lockedFiles.remove(client, file);
		if (stop) notifyClients(new Event(client, Action.STOPPED_PLAYING, file));
		return stop;
	}

	private void notifyClients(Event event) { clients.forEach(client -> client.update(event)); }
}
