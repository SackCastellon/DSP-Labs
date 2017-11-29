/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.concurrency;

import ie.cit.r00158694.soft8023.concurrency.client.AbstractClient;
import ie.cit.r00158694.soft8023.concurrency.client.FullClient;
import ie.cit.r00158694.soft8023.concurrency.monitor.ResourceMonitor;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MainConcurrency {

	private static final Map<String, AbstractClient> CLIENTS = new HashMap<>();
	private static final Map<AbstractClient, List<Runnable>> CLIENT_TASKS = new HashMap<>();
	private static final ResourceMonitor RESOURCE_MONITOR = ResourceMonitor.getInstance();

	public static void main(String[] args) throws Exception {

		File inputFile = new File("input.txt");
		Scanner scanner = new Scanner(inputFile);

		int nClients = scanner.nextInt();
		for (int i = 0; i < nClients; i++) {
			String name = scanner.next();
			FullClient client = new FullClient(name, RESOURCE_MONITOR);
			CLIENTS.put(name, client);
			CLIENT_TASKS.put(client, new LinkedList<>());
		}

		scanner.nextLine(); // Workaround

		// First we read the file and save every action for every client
		while (scanner.hasNextLine() && scanner.hasNext()) {
			AbstractClient client = CLIENTS.get(scanner.next());
			List<Runnable> tasks = CLIENT_TASKS.get(client);

			switch (scanner.next()) {
				case "wait": {
					int timeout = scanner.nextInt();
					TimeUnit timeUnit = TimeUnit.valueOf(scanner.next().toUpperCase());
					tasks.add(() -> client.sleep(timeUnit, timeout));
					break;
				}
				case "share": {
					String filePath = scanner.next();
					String fileName = scanner.next();
					tasks.add(() -> client.addFile(filePath, fileName));
					break;
				}
				case "requestS": {
					String fileName = scanner.next();
					tasks.add(() -> client.readFileAndSleep(fileName));
					break;
				}
				case "requestD": {
					String fileName = scanner.next();
					tasks.add(() -> client.readFileAndDiscard(fileName));
					break;
				}
				case "release": {
					String fileName = scanner.next();
					tasks.add(() -> client.releaseFile(fileName));
					break;
				}
				case "delete": {
					String fileName = scanner.next();
					tasks.add(() -> client.deleteFileAndSleep(fileName));
					break;
				}
			}

			scanner.nextLine(); // Workaround
		}

		CLIENT_TASKS.forEach((client, runnables) -> {
			Thread thread = new Thread(() -> runnables.forEach(Runnable::run), client.toString());
			thread.start();
		});
	}
}
