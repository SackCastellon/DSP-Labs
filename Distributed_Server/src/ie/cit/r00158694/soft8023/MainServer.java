/*
 * Copyright (c) 2017, Juan José González Abril.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */

package ie.cit.r00158694.soft8023;

import ie.cit.r00158694.soft8023.common.UpdateEvent;
import ie.cit.r00158694.soft8023.server.ResourceMonitor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MainServer {

	private static final int SERVER_PORT = 1234;
	private static final int REGISTRY_PORT = 4321;
	private static final String RESOURCE_MONITOR = "RESOURCE_MONITOR";

	private static ResourceMonitor resourceMonitor;

	private static boolean isRunning = true;

	public static void main(String[] args) {
		System.out.printf("[%s] [Server] Server starting\n", getTime());
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
			private int i = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName(String.format("Client Handler %d", i++));
				return thread;
			}
		});
		try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
			System.out.printf("[%s] [Server] Server started\n", getTime());

			resourceMonitor = ResourceMonitor.getInstance();
			Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
			registry.rebind(RESOURCE_MONITOR, resourceMonitor);

			while (isRunning) {
				System.out.printf("[%s] [Server] Waiting for a new client\n", getTime());
				exec.execute(new ClientThread(server.accept()));
			}

			System.out.printf("[%s] [Server] Server stopping\n", getTime());
			System.out.printf("[%s] [Server] No longer waiting for new clients\n", getTime());
			exec.shutdown();
			System.out.printf("[%s] [Server] Awaiting for termination of all client threads\n", getTime());
			boolean terminated = exec.awaitTermination(5, TimeUnit.SECONDS);
			if (terminated)
				System.out.printf("[%s] [Server-Thread] All client threads terminated correctly\n", getTime());
			else System.out.printf("[%s] [Server] Timeout before termination all client threads\n", getTime());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.printf("[%s] [Server] Server stopped\n", getTime());
		}
	}

	public static String getTime() {
		return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
	}

	private static class ClientThread implements Runnable {

		private final Socket client;

		ClientThread(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			System.out.printf("[%s] [%s] Client thread starting\n", getTime(), Thread.currentThread().getName());

			final LinkedList<UpdateEvent> notifications = new LinkedList<>();

			String clientId = null;

			try (DataInputStream fromClient = new DataInputStream(client.getInputStream());
				 DataOutputStream toClient = new DataOutputStream(client.getOutputStream())) {
				System.out.printf("[%s] [%s] Client thread started\n", getTime(), Thread.currentThread().getName());
				String clientName = fromClient.readUTF();
				System.out.printf("[%s] [%s] Client '%s' connected to server from %s\n", getTime(), Thread.currentThread().getName(), clientName, client.getInetAddress());
				clientId = resourceMonitor.addClient(clientName, notifications);
				System.out.printf("[%s] [%s] Assigned ID #%s to client '%s'\n", getTime(), Thread.currentThread().getName(), clientId, clientName);
				toClient.writeUTF(clientId);

				while (isRunning) {
					toClient.writeBoolean(true); // Tell the client to keep running
					boolean hasNotification = !notifications.isEmpty();
					toClient.writeBoolean(hasNotification); // Tell the client whether there is or not a notification
					if (hasNotification) {
						UpdateEvent notification = notifications.poll();
						toClient.writeUTF(notification.serialize()); // Send the notification to the client
					}
				}

				System.out.printf("[%s] [%s] Client thread stopping\n", getTime(), Thread.currentThread().getName());
				toClient.writeBoolean(false); // Tell client to stop
				fromClient.read(); // Wait for client to disconnect TODO Check
			} catch (IOException e) {
//				e.printStackTrace();
				System.out.printf("[%s] [%s] Client disconnected\n", getTime(), Thread.currentThread().getName());
			} finally {
				resourceMonitor.removeClient(clientId);
				System.out.printf("[%s] [%s] Client thread stopped\n", getTime(), Thread.currentThread().getName());
			}
		}
	}
}
