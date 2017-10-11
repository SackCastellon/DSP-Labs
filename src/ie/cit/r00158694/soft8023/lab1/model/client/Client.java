/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.model.client;

import ie.cit.r00158694.soft8023.lab1.model.Action;
import ie.cit.r00158694.soft8023.lab1.model.ResourceMonitor;
import ie.cit.r00158694.soft8023.lab1.model.SharedFile;
import ie.cit.r00158694.soft8023.lab1.model.UpdateEvent;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class Client {

	private final String clientName;
	private final ResourceMonitor resourceMonitor;

	private Consumer<UpdateEvent> consumer;

	private String waitForFile = null;

	public Client(String name, ResourceMonitor resourceMonitor) {
		this.clientName = name;
		this.resourceMonitor = resourceMonitor;

		resourceMonitor.addClient(this);
	}

	public String getClientName() { return clientName; }

	protected ResourceMonitor getResourceMonitor() { return resourceMonitor; }

	public final void sleep(TimeUnit timeUnit, long timeout) {
		try {
			System.out.printf("[%1$s] %1$s is going to sleep for %2$d %3$s\n", getClientName(), timeout, timeUnit.toString().toLowerCase());
			timeUnit.sleep(timeout);
		} catch (InterruptedException ignored) {}
	}

	public final boolean addFile(String filePath, String fileName) { return addFile(new SharedFile(new File(filePath), fileName)); }

	public boolean addFile(SharedFile file) { throw new UnsupportedOperationException("This client cannot add files"); }

	public boolean deleteFile(String file) { throw new UnsupportedOperationException("This client cannot delete files"); }

	protected boolean readFile(String file) { throw new UnsupportedOperationException("This client cannot read files"); }

	public final boolean readFileAndSleep(String file) {
		boolean b = readFile(file);
		if (!b) waitForFile = file;
		return b;
	}

	public final boolean readFileAndDiscard(String file) { return readFile(file); }

	public final boolean releaseFile(String file) { return getResourceMonitor().releaseFile(this, file); }

	public void update(UpdateEvent event) {
		if (event.getClient() == this) {
			System.out.printf("[%s] %s -> %s -> %s\n", clientName, event.getClient().getClientName(), event.getAction(), event.getFileName());
		}

		if (consumer != null) consumer.accept(event);

		if (waitForFile != null && event.getFileName().equals(waitForFile) && event.getAction() == Action.RELEASE) {
			getResourceMonitor().readFile(this, waitForFile);
			waitForFile = null;
		}
	}

	public final void setOnUpdate(Consumer<UpdateEvent> consumer) { this.consumer = consumer; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FullClient)) return false;
		FullClient client = (FullClient) o;
		return Objects.equals(clientName, client.getClientName());
	}

	@Override
	public int hashCode() { return Objects.hash(clientName); }

	@Override
	public String toString() { return "Client: " + clientName; }
}
