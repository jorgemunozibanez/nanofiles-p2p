package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;




public class NFServer implements Runnable {

	public static final int PORT = 10003;



	private ServerSocket serverSocket = null;
	private boolean keepRunning;
	private List<Socket> activeSockets;
	private InetSocketAddress serverSocketAddress;

	public NFServer() throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		serverSocketAddress = new InetSocketAddress(PORT);
		
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		
		serverSocket = new ServerSocket();
	    serverSocket.bind(serverSocketAddress);
	    keepRunning = true;
	    activeSockets = Collections.synchronizedList(new ArrayList<Socket>());
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			/*
			 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			/*
			 * TODO: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */
			try {
				Socket socket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				PeerMessage mensaje = PeerMessage.readMessageFromInputStream(dis);
				mensaje.writeMessageToOutputStream(dos);
			} catch (IOException e) {
				System.out.println("Server exception: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */
		
		Socket socket;
		while (keepRunning) {
			try {
				socket = serverSocket.accept();
				startServerThread(socket);
			} catch (IOException e) {
				if (!keepRunning) {
	                System.out.println("File server thread is shutting down...");
	            } else {
	                System.err.println("Server failed to accept new client: " + e.getMessage());
	            }
			}
		}
	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */
	
	public void startServerThread (Socket socket) {
		activeSockets.add(socket);
		NFServerThread st = new NFServerThread(socket, activeSockets);
		st.start();
	}
	
	public void stopServer () {
		keepRunning = false;
		if (serverSocket != null) {
            try {
				serverSocket.close();
			} catch (IOException e) {
				//Silencio.
			} 
        }
		synchronized (activeSockets) {
	        for (Socket s : activeSockets) {
	            try {
	                if (s != null) {
	                    s.close(); 
	                }
	            } catch (IOException e) {
	                // Silencio.
	            }
	        }
	        activeSockets.clear(); 
	    }
	}



	public int getPort() {
		return (serverSocket != null) ? serverSocket.getLocalPort() : -1;
	}
	
	public InetSocketAddress getServerSocketAddress(){
		return serverSocketAddress;
	}
	
	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */
		try {
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			PeerMessage mensajeRec;
			PeerMessage mensajeRes;
			while (true) {
				mensajeRec = PeerMessage.readMessageFromInputStream(dis);
				byte opcode = mensajeRec.getOpcode();
				FileInfo[] ficheros = NanoFiles.db.getFiles();
				
				switch (opcode) {
				case PeerMessageOps.OPCODE_PEERFILES: {
					if (ficheros.length == 0) {
						mensajeRes = new PeerMessage(PeerMessageOps.OPCODE_EMPTY_LIST);
					} else {
						mensajeRes = new PeerMessage(PeerMessageOps.OPCODE_FILES_INFO, ficheros);
					}
					mensajeRes.writeMessageToOutputStream(dos);
					break;
				}
				
				case PeerMessageOps.OPCODE_GOT_FILE: {
					FileInfo[] coincidencias = FileInfo.lookupHashSubstring(ficheros, mensajeRec.getSubhash());
					int numcoin = coincidencias.length;
					if (numcoin == 0) {
						mensajeRes = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
						mensajeRes.writeMessageToOutputStream(dos);
					} else if (numcoin > 1) {
						mensajeRes = new PeerMessage(PeerMessageOps.OPCODE_MULTIPLE_FILES_FOUND);
						mensajeRes.writeMessageToOutputStream(dos);
					} else {
						FileInfo fichero = coincidencias[0];
						long size = fichero.fileSize;
						String hash = fichero.fileHash;
						short lhash = (short) hash.length();
						String name = fichero.fileName;
						short lname = (short) name.length();
						mensajeRes = new PeerMessage(PeerMessageOps.OPCODE_FILE_LOCATED, size, lhash, hash, lname, name);
						mensajeRes.writeMessageToOutputStream(dos);
						if (mensajeRec.getGetFile() == true) {
							int lData = 64000;
							long offset = 0;
							boolean last = false;
							byte[] data;
							while (lData == 64000) {
								data = FileInfo.leerFragmento(fichero.filePath, offset, lData);
								int longitudDatos = data.length;
								if (longitudDatos < lData) {
									lData = longitudDatos;
									last = true;
								} else {
									offset += lData;
								}
								mensajeRes = new PeerMessage(PeerMessageOps.OPCODE_SEND_CHUNK, last, lData, data);
								mensajeRes.writeMessageToOutputStream(dos);
							}
							
						}
					}
					break;
				}
				
				case PeerMessageOps.OPCODE_GET_FRAGMENT: {
					FileInfo fichero = FileInfo.lookupHash(ficheros, mensajeRec.getHash());
					long offset = mensajeRec.getOffset();
					int fragmentSize = mensajeRec.getFragmentSize();
					long finalPos = offset + (long) fragmentSize;
					int lData;
					boolean last = false;
					byte[] data;
					while (offset < finalPos) {
						if (offset + 64000 < finalPos) {
							lData = 64000;
						} else {
							lData = (int) (finalPos - offset);
							last = true;
						}
						data = FileInfo.leerFragmento(fichero.filePath, offset, lData);
						mensajeRes = new PeerMessage(PeerMessageOps.OPCODE_SEND_CHUNK, last, lData, data);
						mensajeRes.writeMessageToOutputStream(dos);
						offset += lData;
					}
					break;
				}
				
				
				default: {
					System.err.println("Unexpected message opcode: \"" + opcode + "\" from client at addr " + socket.getRemoteSocketAddress());
					System.exit(-1);
				}
				}
			}
		} catch (EOFException e) {
			//Silencio.
		} catch (IOException e) {
			System.err.println("Cannot communicate with client at addr " + socket.getRemoteSocketAddress());
			e.printStackTrace();
		}
	}
}
