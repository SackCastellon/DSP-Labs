/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import java.util.Objects;
import java.util.function.Consumer;

public class Client {

	private final String clientName;

	private Consumer<Event> onUpdate;

	public Client(String name) { this.clientName = name; }

	public void setOnUpdate(Consumer<Event> onUpdate) { this.onUpdate = onUpdate; }

	public void update(Event event) { if (onUpdate != null) onUpdate.accept(event); }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Client)) return false;
		Client client = (Client) o;
		return Objects.equals(clientName, client.clientName);
	}

	@Override
	public int hashCode() { return Objects.hash(clientName); }

	@Override
	public String toString() { return clientName; }
}
