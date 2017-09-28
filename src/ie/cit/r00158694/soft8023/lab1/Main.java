/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work by Juan José González Abril is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023.lab1;

import ie.cit.r00158694.soft8023.lab1.model.BasicClient;
import ie.cit.r00158694.soft8023.lab1.model.Monitor;

public class Main {

	public static void main(String[] args) {

		Monitor monitor = Monitor.getInstance();

		BasicClient client1 = new BasicClient("Pepe", monitor);
		BasicClient client2 = new BasicClient("Jose", monitor);
		BasicClient client3 = new BasicClient("Luis", monitor);

		monitor.addFile(client1, "Hello");
		System.out.println();
		monitor.playFile(client2, "Hello");
		System.out.println();
		monitor.addFile(client2, "Animals");
		System.out.println();
		monitor.playFile(client1, "Hello");
		System.out.println();
		monitor.stopPlayingFile(client2, "Hello");
		System.out.println();
		monitor.addFile(client3, "Apollo");
		System.out.println();
		monitor.addFile(client2, "Burn");
		System.out.println();
		monitor.removeFile(client1, "Hello");
		System.out.println();
		monitor.playFile(client3, "Burn");
		System.out.println();
		monitor.removeFile(client1, "Apollo");
		System.out.println();
		monitor.stopPlayingFile(client3, "Burn");
	}
}
