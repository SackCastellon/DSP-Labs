/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateEvent {

	private final String clientName;
	private final Action action;
	private final String fileName;
	private final LocalDateTime time;

	public UpdateEvent(String clientName, Action action, String fileName) {
		this(clientName, action, fileName, LocalDateTime.now());
	}

	private UpdateEvent(String clientName, Action action, String fileName, LocalDateTime time) {
		this.clientName = clientName;
		this.action = action;
		this.fileName = fileName;
		this.time = time;
	}

	/**
	 * Return the clientName that caused the update
	 *
	 * @return The clientName
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * Returns the action that the clientName executed to cause the update.
	 *
	 * @return The action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Returns the fileName over which the clientName executed the action to caused the update
	 *
	 * @return The fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Returns the date at which the clientName executed the action that caused the update
	 *
	 * @return The date
	 */
	public LocalDateTime getDate() {
		return time;
	}

	public String serialize() {
		return Stream.of(clientName, action.name(), fileName, time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).map(s -> s.replaceAll("@", "@#")).reduce((s1, s2) -> s1 + "@@" + s2).orElse("");
	}

	public static Optional<UpdateEvent> deserialize(String string) {
		try {
			List<String> list = Stream.of(string.split("@@")).map(s -> s.replaceAll("@#", "@")).collect(Collectors.toList());
			String clientName = list.get(0);
			Action action = Action.valueOf(list.get(1));
			String fileName = list.get(2);
			LocalDateTime time = LocalDateTime.parse(list.get(3));
			return Optional.of(new UpdateEvent(clientName, action, fileName, time));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public String toString() {
		return String.format("[%s] %s -> %s -> %s", time.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), clientName, action.name(), fileName);
	}

	public enum Action {ADD, DELETE, READ, RELEASE}
}
