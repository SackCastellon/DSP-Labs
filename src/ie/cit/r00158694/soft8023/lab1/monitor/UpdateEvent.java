/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.monitor;

import ie.cit.r00158694.soft8023.lab1.client.Client;

import java.util.Date;

public class UpdateEvent {

	private final Client client;
	private final Action action;
	private final String fileName;
	private final Date time;

	public UpdateEvent(Client client, Action action, String fileName) {
		this.client = client;
		this.action = action;
		this.fileName = fileName;
		this.time = new Date();
	}

	/**
	 * Return the client that caused the update
	 *
	 * @return The client
	 */
	public Client getClient() { return client; }

	/**
	 * Returns the action that the client executed to cause the update.
	 *
	 * @return The action
	 */
	public Action getAction() { return action; }

	/**
	 * Returns the fileName over which the client executed the action to caused the update
	 *
	 * @return The fileName
	 */
	public String getFileName() { return fileName; }

	/**
	 * Returns the date at which the client executed the action that caused the update
	 *
	 * @return The date
	 */
	public Date getDate() { return time; }

	public enum Action {

		ADD("action.add"), DELETE("action.remove"), READ("action.read"), RELEASE("action.release");

		private final String key;

		Action(String key) { this.key = key; }

		/**
		 * Returns the localization key used to translate this action text.
		 *
		 * @return The localization key
		 */
		public String getKey() { return key; }
	}
}
