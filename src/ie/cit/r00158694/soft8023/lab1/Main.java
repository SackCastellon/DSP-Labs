/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1;

import ie.cit.r00158694.soft8023.lab1.model.ResourceMonitor;
import ie.cit.r00158694.soft8023.lab1.model.client.Client;
import ie.cit.r00158694.soft8023.lab1.model.client.FullClient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

	private static final Map<String, Client> CLIENTS = new HashMap<>();
	private static final ResourceMonitor RESOURCE_MONITOR = ResourceMonitor.getInstance();

	public static void main(String[] args) throws Exception {

		File inputFile = new File("input.txt");
		Scanner scanner = new Scanner(inputFile);

		int nClients = scanner.nextInt();
		for (int i = 0; i < nClients; i++) {
			String name = scanner.next();
			FullClient client = new FullClient(name, RESOURCE_MONITOR);
			CLIENTS.put(name, client);
		}

		scanner.nextLine(); // Workaround

		while (scanner.hasNextLine() && scanner.hasNext()) {
			Client client = CLIENTS.get(scanner.next());
			switch (scanner.next()) {
				case "wait":
					int timeout = scanner.nextInt();
					TimeUnit timeUnit = TimeUnit.valueOf(scanner.next().toUpperCase());
					client.sleep(timeUnit, timeout);
					break;
				case "share":
					client.addFile(scanner.next(), scanner.next());
					break;
				case "requestS":
					client.readFileAndSleep(scanner.next());
					break;
				case "requestD":
					client.readFileAndDiscard(scanner.next());
					break;
				case "release":
					client.releaseFile(scanner.next());
					break;
				case "delete":
					client.deleteFile(scanner.next());
					break;
			}

			scanner.nextLine(); // Workaround
		}
	}
}
