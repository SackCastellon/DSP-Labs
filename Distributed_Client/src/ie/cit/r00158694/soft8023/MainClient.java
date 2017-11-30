/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023;

import ie.cit.r00158694.soft8023.client.AbstractClient;
import ie.cit.r00158694.soft8023.client.ClientNotFoundException;
import ie.cit.r00158694.soft8023.client.FullClient;
import ie.cit.r00158694.soft8023.common.IMonitor;
import ie.cit.r00158694.soft8023.common.UpdateEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MainClient {

	private static final int SERVER_PORT = 1234;
	private static final int REGISTRY_PORT = 4321;
	private static final String RESOURCE_MONITOR = "RESOURCE_MONITOR";
	private static final String SERVER_HOST = "localhost";

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Expected 2 argument: <client_name> <actions_file_path>");
			System.exit(1);
		}

		String clientName = args[0];
		String actionsFilePath = args[1];

		final LinkedList<Consumer<AbstractClient>> actions = getActions(clientName, actionsFilePath);
		final AbstractClient client = new FullClient(clientName);

		try (Socket server = new Socket(SERVER_HOST, SERVER_PORT);
			 DataInputStream fromServer = new DataInputStream(server.getInputStream());
			 DataOutputStream toServer = new DataOutputStream(server.getOutputStream())) {

			Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
			IMonitor monitor = (IMonitor) registry.lookup(RESOURCE_MONITOR);

			System.out.printf("[%s] [%s] Connected to server on %s\n", getTime(), clientName, server.getInetAddress());
			toServer.writeUTF(client.getName());
			client.setId(fromServer.readUTF());
			client.setMonitor(monitor);

			// While server is running
			while (fromServer.readBoolean()) {
				// If there is a notification
				if (fromServer.readBoolean()) {
					String notification = fromServer.readUTF(); // Read the notification
					UpdateEvent.deserialize(notification).ifPresent(updateEvent -> {
						System.out.println(updateEvent.toString());
						client.update(updateEvent).ifPresent(actions::addFirst);
					});
				}

				// Execute action if available
				Consumer<AbstractClient> action;
				if ((action = actions.poll()) != null) action.accept(client);
			}
		} catch (IOException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	private static LinkedList<Consumer<AbstractClient>> getActions(String clientName, String actionsFilePath) {
		LinkedList<Consumer<AbstractClient>> actions = new LinkedList<>();

		try (Scanner scanner = new Scanner(new File(actionsFilePath))) {
			boolean existsClient = false;
			for (int i = 0, j = scanner.nextInt(); i < j; i++) existsClient |= clientName.equals(scanner.next());
			if (!existsClient)
				throw new ClientNotFoundException(String.format("Actions for client '%s' not found\n", clientName));

			scanner.nextLine();

			while (scanner.hasNext()) {
				if (clientName.equals(scanner.next())) {
					switch (scanner.next()) {
						case "wait": {
							int timeout = scanner.nextInt();
							TimeUnit timeUnit = TimeUnit.valueOf(scanner.next().toUpperCase());
							actions.add((client) -> client.sleep(timeUnit, timeout));
							break;
						}
						case "share": {
							String filePath = scanner.next();
							String fileName = scanner.next();
							actions.add((client) -> client.addFile(filePath, fileName));
							break;
						}
						case "requestS": {
							String fileName = scanner.next();
							actions.add((client) -> client.readFileAndSleep(fileName));
							break;
						}
						case "requestD": {
							String fileName = scanner.next();
							actions.add((client) -> client.readFileAndDiscard(fileName));
							break;
						}
						case "release": {
							String fileName = scanner.next();
							actions.add((client) -> client.releaseFile(fileName));
							break;
						}
						case "delete": {
							String fileName = scanner.next();
							actions.add((client) -> client.deleteFileAndSleep(fileName));
							break;
						}
					}
				}
				scanner.nextLine();
			}
		} catch (FileNotFoundException | ClientNotFoundException e) {
			System.err.println(e.getMessage());
		}
		return actions;
	}

	public static String getTime() {
		return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
	}
}
