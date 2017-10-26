/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1.client;

import ie.cit.r00158694.soft8023.lab1.monitor.ResourceMonitor;
import ie.cit.r00158694.soft8023.lab1.monitor.SharedFile;
import ie.cit.r00158694.soft8023.lab1.monitor.UpdateEvent;
import ie.cit.r00158694.soft8023.lab1.monitor.UpdateEvent.Action;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractClient {

	private final String clientName;
	private final ResourceMonitor resourceMonitor;

	private Consumer<UpdateEvent> consumer;

	private final List<Pair<Action, String>> waitForFiles = new ArrayList<>();

	public AbstractClient(String name, ResourceMonitor resourceMonitor) {
		this.clientName = name;
		this.resourceMonitor = resourceMonitor;

		resourceMonitor.addClient(this);
	}

	public String getClientName() {
		return clientName;
	}

	protected ResourceMonitor getResourceMonitor() {
		return resourceMonitor;
	}

	public final void sleep(TimeUnit timeUnit, long timeout) {
		try {
			System.out.printf("[%1$s] %1$s is going to sleep for %2$d %3$s\n", getClientName(), timeout, timeUnit.toString().toLowerCase());
			timeUnit.sleep(timeout);
		} catch (InterruptedException ignored) {
		}
	}

	public final boolean addFile(String filePath, String fileName) {
		return addFile(new SharedFile(new File(filePath), fileName));
	}

	public boolean addFile(SharedFile file) {
		throw new UnsupportedOperationException("This client cannot add files");
	}

	protected boolean deleteFile(String file) {
		throw new UnsupportedOperationException("This client cannot delete files");
	}

	public boolean deleteFileAndSleep(String file) {
		boolean b = deleteFile(file);
		if (!b) waitForFiles.add(new Pair<>(Action.DELETE, file));
		return b;
	}

	protected boolean readFile(String file) {
		throw new UnsupportedOperationException("This client cannot read files");
	}

	public final boolean readFileAndSleep(String file) {
		boolean b = readFile(file);
		if (!b) waitForFiles.add(new Pair<>(Action.READ, file));
		return b;
	}

	public final boolean readFileAndDiscard(String file) {
		return readFile(file);
	}

	public final boolean releaseFile(String file) {
		return getResourceMonitor().releaseFile(this, file);
	}

	public void update(UpdateEvent event) {
		System.out.printf("[%s] %s -> %s -> %s\n", clientName, event.getClient().getClientName(), event.getAction(), event.getFileName());

		if (consumer != null) consumer.accept(event);

		if (event.getAction() == Action.RELEASE || event.getAction() == Action.ADD) {
			waitForFiles.stream().filter(pair -> event.getFileName().equals(pair.getValue())).findFirst().ifPresent(actionStringPair -> {
				boolean b = false;
				switch (actionStringPair.getKey()) {
					case READ:
						b = getResourceMonitor().readFile(this, actionStringPair.getValue());
						break;
					case DELETE:
						b = getResourceMonitor().deleteFile(this, actionStringPair.getValue());
						break;
				}

				if (b) waitForFiles.remove(actionStringPair);
			});
		}
	}

	public final void setOnUpdate(Consumer<UpdateEvent> consumer) {
		this.consumer = consumer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FullClient)) return false;
		FullClient client = (FullClient) o;
		return Objects.equals(clientName, client.getClientName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientName);
	}

	@Override
	public String toString() {
		return "AbstractClient: " + clientName;
	}
}
