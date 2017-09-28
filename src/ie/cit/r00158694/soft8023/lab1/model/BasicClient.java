/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model;

import java.util.Objects;
import java.util.function.Consumer;

public class BasicClient implements IClient {

	private final String clientName;

	private Consumer<UpdateEvent> onUpdate;

	public BasicClient(String name, Monitor monitor) {
		this.clientName = name;
		monitor.addClient(this);
	}

	@Override
	public void setOnUpdate(Consumer<UpdateEvent> onUpdate) { this.onUpdate = onUpdate; }

	@Override
	public void update(UpdateEvent event) {
		System.out.printf("[%s] %s -> %s -> %s\n", clientName, event.getClient().getClientName(), event.getAction(), event.getFile());
		if (onUpdate != null) onUpdate.accept(event);
	}

	@Override
	public String getClientName() { return clientName; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BasicClient)) return false;
		BasicClient client = (BasicClient) o;
		return Objects.equals(clientName, client.clientName);
	}

	@Override
	public int hashCode() { return Objects.hash(clientName); }

	@Override
	public String toString() { return "Client: " + clientName; }
}
