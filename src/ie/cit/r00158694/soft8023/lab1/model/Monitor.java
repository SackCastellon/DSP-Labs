/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Monitor {

	private final List<Client> clients = new ArrayList<>();
	private final List<String> files = new ArrayList<>();
	private final Map<Client, String> lockedFiles = new HashMap<>();

	private final String folder;

	public Monitor(String folder) { this.folder = folder; }

	public void addClient(Client client) { clients.add(client); }

	public void removeClient(Client client) { clients.remove(client); }

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

	public List<Client> getClients() { return clients; }

	public List<String> getFiles() { return files; }

	public Map<Client, String> getLockedFiles() { return lockedFiles; }
}
