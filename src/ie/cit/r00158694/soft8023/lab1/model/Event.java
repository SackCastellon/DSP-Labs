/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import java.util.Date;

public class Event {
	private final Client client;
	private final Action action;
	private final String file;
	private final Date time;

	public Event(Client client, Action action, String file) {
		this.client = client;
		this.action = action;
		this.file = file;
		this.time = new Date();
	}

	public Client getClient() { return client; }

	public Action getAction() { return action; }

	public String getFile() { return file; }

	public Date getDate() { return time; }
}
