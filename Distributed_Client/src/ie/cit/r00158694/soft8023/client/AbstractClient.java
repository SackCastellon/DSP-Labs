/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.client;

import ie.cit.r00158694.soft8023.common.IMonitor;
import ie.cit.r00158694.soft8023.common.SharedFile;
import ie.cit.r00158694.soft8023.common.UpdateEvent;
import ie.cit.r00158694.soft8023.common.UpdateEvent.Action;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ie.cit.r00158694.soft8023.common.UpdateEvent.Action.ADD;
import static ie.cit.r00158694.soft8023.common.UpdateEvent.Action.DELETE;
import static ie.cit.r00158694.soft8023.common.UpdateEvent.Action.READ;
import static ie.cit.r00158694.soft8023.common.UpdateEvent.Action.RELEASE;

public abstract class AbstractClient {

	private String id;
	private final String name;
	private IMonitor monitor;

	private final List<Pair<Action, String>> waitForFiles = new ArrayList<>();

	public AbstractClient(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public IMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}

	public final void sleep(TimeUnit timeUnit, long timeout) {
		try { timeUnit.sleep(timeout); } catch (InterruptedException ignored) { }
	}

	public final boolean addFile(String filePath, String fileName) {
		return addFile(new SharedFile(new File(filePath), fileName));
	}

	public boolean addFile(SharedFile file) {
		throw new UnsupportedOperationException("Add operation is not supported");
	}

	public boolean deleteFile(String file) {
		throw new UnsupportedOperationException("Delete operation is not supported");
	}

	public final boolean deleteFileAndSleep(String file) {
		boolean deleted = deleteFile(file);
		if (!deleted) waitForFiles.add(new Pair<>(DELETE, file));
		return deleted;
	}

	public boolean readFile(String file) {
		throw new UnsupportedOperationException("Read operation is not supported");
	}

	public final boolean readFileAndSleep(String file) {
		boolean read = readFile(file);
		if (!read) waitForFiles.add(new Pair<>(READ, file));
		return read;
	}

	public final boolean readFileAndDiscard(String file) {
		return readFile(file);
	}

	public boolean releaseFile(String file) {
		throw new UnsupportedOperationException("Release operation not supported");
	}

	public Optional<Consumer<AbstractClient>> update(UpdateEvent event) {
		if (event.getAction() == RELEASE || event.getAction() == ADD) {
			Optional<Pair<Action, String>> optional = waitForFiles
					.stream()
					.filter(pair -> event.getFileName().equals(pair.getValue()))
					.findFirst();

			if (optional.isPresent()) {
				switch (optional.get().getKey()) {
					case READ:
						return Optional.of(abstractClient -> {
							if (abstractClient.readFile(optional.get().getValue()))
								waitForFiles.remove(optional.get());
						});
					case DELETE:
						return Optional.of(abstractClient -> {
							if (abstractClient.deleteFile(optional.get().getValue()))
								waitForFiles.remove(optional.get());
						});
				}
			}
		}
		return Optional.empty();
	}
}
