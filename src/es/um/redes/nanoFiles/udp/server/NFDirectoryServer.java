package es.um.redes.nanoFiles.udp.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;
	
	private static final int MAX_MSG_SIZE_BYTES = 64;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros alojados, servidores
	 * registrados, etc.
	 */
	
	/**
	 * Lista de ficheros alojados en el directorio.
	 */
	private FileInfo[] directoryFiles;
	
	/**
	 * Lista de servidores registrados (IP, puerto TCP).
	 */
	private LinkedHashMap<String, InetSocketAddress> registeredPeers;

	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;
	
	/**
	 * Ampliación dirfiles
	 */
	//Para guardar los puntos de corte que separan cada bloque de ficheros
	private List<Integer> puntosCorte;

	public NFDirectoryServer(double corruptionProbability, String directoryFilesPath) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * Cargar los ficheros del directorio compartido.
		 */
		File dir = new File(directoryFilesPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		directoryFiles = FileInfo.loadFilesFromFolder(directoryFilesPath);
		System.out.println("* Directory loaded " + directoryFiles.length + " files from " + directoryFilesPath);
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */
		socket = new DatagramSocket(DIRECTORY_PORT);
		
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: peers registrados, etc.)
		 */
		registeredPeers = new LinkedHashMap<String, InetSocketAddress>();
		
		
		puntosCorte = new ArrayList<>();

		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * TODO: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */
			byte[] recbuf = new byte[MAX_MSG_SIZE_BYTES];
			datagramReceivedFromClient = new DatagramPacket(recbuf, recbuf.length);
			
			/*
			 * TODO: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */
			socket.receive(datagramReceivedFromClient);
 
			// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
			double rand = Math.random();
			if (rand < messageDiscardProbability) {
				System.err.println(
						"Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
			} else {
				datagramReceived = true;
			}
		}
		
		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */
		byte[] recBytes = pkt.getData();
		String recData = new String(recBytes, 0, pkt.getLength());
		System.out.println("Received data: " + recData);
		
		/*
		 * TODO: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 */
		
		/*
		 * TODO: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 */
		
		String senData;
		String cadenaEsperada1 = "ping";
		String cadenaEsperada2 = "ping&";
		
		if (recData.equals(cadenaEsperada1)) {
			senData = "pingok";
		} else if (recData.startsWith(cadenaEsperada2)){
			String protId = recData.substring(cadenaEsperada2.length());
			if (protId.equals(NanoFiles.PROTOCOL_ID)) senData = "welcome";
			else senData = "denied";
		}
		else {
			System.err.println("Message received didn't match test message!");
			senData = "invalid";
		}
		
		byte[] senBytes = senData.getBytes();
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();
		DatagramPacket resPkt = new DatagramPacket(senBytes, senBytes.length, clientAddr);
		socket.send(resPkt);

		
		
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);
	}

	public void run() throws IOException {

		System.out.println("Directory starting...");
		System.out.println();

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}
	
	/**
	 * Ampliación dirfiles
	 */
	//Cuenta los bytes que ocupa una cadena en UTF-8
	private static int getUtf8ByteCount(CharSequence s) {
	    int cont = 0;
	    for (int i = 0, len = s.length(); i < len; i++) {
	        char ch = s.charAt(i);
	        if (ch <= 0x7F) {
	            cont++;
	        } else if (ch <= 0x7FF) {
	            cont += 2;
	        } else if (Character.isHighSurrogate(ch)) {
	            cont += 4;
	            i++; // Saltamos el low surrogate
	        } else {
	            cont += 3;
	        }
	    }
	    return cont;
	}
	
	//Obtiene los puntos de corte que dividen la lista de ficheros en bloques
	private void SetPuntosCorte() {
		puntosCorte.clear();
		int cont = 0;
		int longList = directoryFiles.length;
		if (longList != 0) {
			puntosCorte.add(0);
			for (int i = 0; i < longList; i++) {
				//Calculamos cuanto ocuparía añadir el fichero al datagrama
				FileInfo fichero = directoryFiles[i];
				int longBytes = DirMessage.RESPONSE_DIRFILES_FIXED_SIZE_PER_FILE + 
						getUtf8ByteCount(fichero.filePath) + getUtf8ByteCount(fichero.fileName) +
						getUtf8ByteCount(String.valueOf(fichero.fileSize)) + getUtf8ByteCount(fichero.fileHash);
				cont += longBytes;
				if (cont > DirMessage.RESPONSE_DIRFILES_REAL_PACKET_SIZE) {
					puntosCorte.add(i);
					cont = longBytes;
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	

	private void sendResponse(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 */
		byte[] recBytes = pkt.getData();
		String recData = new String(recBytes, 0, pkt.getLength());
		
		DirMessage recMessage = DirMessage.fromString(recData);

		/*
		 * TODO: Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 */
		String operation = recMessage.getOperation();
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();
		
		introDep(clientAddr, operation);

		/*
		 * TODO: (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */

		byte[] senBytes = switch (operation) {
		case DirMessageOps.OPERATION_PING -> {
			
			String operationField = DirMessageOps.RESPONSE_PING;
			String status = "ERROR";
			int code = -1;
			
			/*
			 * TODO: (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
			 * cliente coincide con el nuestro.
			 */
			String recProtocol = recMessage.getProtocolId();
			
			System.out.print("Client " + clientAddr);
			
			
			if (recProtocol.equals(NanoFiles.PROTOCOL_ID)) {
				status = "OK";
				code = 0;
				
				System.out.println(" uses compatible protocol " + recProtocol);
				System.out.println("Welcome to the Directory! Feel free to use any of the available services!");
			} else {
				System.out.println(" does not use compatible protocol");
				System.out.println("Received protocol was " + recProtocol + " and Directory uses " + NanoFiles.PROTOCOL_ID);
			}
			
			/*
			 * TODO: (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
			 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
			 * resultado del método.
			 */
			DirMessage msgToSend = new DirMessage(operationField, status, code);
			String senData = msgToSend.toString();
			yield senData.getBytes();
			
			
			/*
			 * TODO: (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
			 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
			 * modo de depuración en el servidor
			 */
		}
		
		case DirMessageOps.OPERATION_DIRFILES -> {
			String operationField = DirMessageOps.RESPONSE_DIRFILES;
			String status = "ERROR";
			int code = -1;
			DirMessage msgToSend = new DirMessage(operationField, status, code);
			
			int recBloque;
			if ((recBloque = recMessage.getBlock()) == 0) SetPuntosCorte();
			
			if (!puntosCorte.isEmpty()) {
				msgToSend.setStatus("OK");
				int inicio = puntosCorte.get(recBloque);
				int fin;
				
				int nextBloque = recBloque + 1;
				if (nextBloque < puntosCorte.size()) {
					System.out.println("Returning block " + recBloque + " from filelist");
					msgToSend.setCode(0);
					fin = puntosCorte.get(nextBloque);
				} else {
					System.out.println("Returning FINAL block " + recBloque + " from filelist");
					msgToSend.setCode(1);
					fin = directoryFiles.length;
					System.out.println("Filelist contains " + fin + " files");
				}
				
				for (int i = inicio; i < fin; i++) {
					msgToSend.addFichero(directoryFiles[i]);
				}
				
				
			} else {
				System.out.println("Directory folder is currently empty!");
			}
			
			String senData = msgToSend.toString();
			yield senData.getBytes();
		}
		
		case DirMessageOps.OPERATION_SERVE -> {
			DirMessage msgToSend;
			String operationField = DirMessageOps.RESPONSE_SERVE;
			String status = "ERROR";
			int code = -1;
			
			InetSocketAddress direccion = new InetSocketAddress(pkt.getAddress(), recMessage.getPort());
			if (!registeredPeers.containsValue(direccion)) {
				status = "OK";
				code = 0;
				
				String nickname = (recMessage.getNickname());
				if (registeredPeers.containsKey(nickname)) {
					code = 1;
					int version = 1;
					String newnick = nickname + "_" + version;
					while (registeredPeers.containsKey(newnick)) {
						version++;
					}
					registeredPeers.put(newnick, direccion);
					System.out.println("Registered peer nickname= " + newnick + " addr= " + direccion);
					msgToSend = new DirMessage(operationField, status, code, newnick);
				} else {
					registeredPeers.put(nickname, direccion);
					System.out.println("Registered peer nickname= " + nickname + " addr= " + direccion);
					msgToSend = new DirMessage(operationField, status, code);
				}
			} else {
				System.out.println("This IP address and TCP port are already in use!");
				msgToSend = new DirMessage(operationField, status, code);
			}
			
			String senData = msgToSend.toString();
			yield senData.getBytes();
		}
		
		case DirMessageOps.OPERATION_PEERS -> {
			String operationField = DirMessageOps.RESPONSE_PEERS;
			String status = "ERROR";
			int code = -1;
			DirMessage msgToSend = new DirMessage(operationField, status, code);
			
			
			
			if (!registeredPeers.isEmpty()) {
				msgToSend.setStatus("OK");
				msgToSend.setCode(0);
				
				registeredPeers.forEach((key, value) -> {
					msgToSend.addPeer(key, value.getAddress(), value.getPort());
				});
				
				System.out.println("Returning peerlist with " + registeredPeers.size() + " peers");
				
			} else {
				System.out.println("There are no registered peers yet!");
			}
			
			String senData = msgToSend.toString();
			yield senData.getBytes();
		}
		
		case DirMessageOps.OPERATION_DIRDL -> {
			String operationField = DirMessageOps.RESPONSE_DIRDL;
			String status = "ERROR";
			int code = -1;
			DirMessage msgToSend = new DirMessage(operationField, status, code);
			
			int inicio = 0;
			int cantidad = 0;
			byte[] data = null;
			
			FileInfo[] ficheros = FileInfo.lookupHashSubstring(directoryFiles, recMessage.getSubhash());
			if (ficheros.length == 0) {
				System.out.println("No Directory files whose hash contains the specified hash substring");
				String senData = msgToSend.toString();
				yield senData.getBytes();
			} else if (ficheros.length > 1) {
				msgToSend.setCode(-2);
				System.out.println("Multiple Directory files whose hash contains the specified hash substring");
				System.out.println("Please try again with a more specific hash substring");
				String senData = msgToSend.toString();
				yield senData.getBytes();
			} else {
				int bloque = recMessage.getBlock();
				FileInfo fichDescargado = ficheros[0];
				if (bloque == 0) {
					msgToSend.setStatus("OK");
					msgToSend.setCode(0);
					msgToSend.setName(fichDescargado.fileName);
					msgToSend.setSize(fichDescargado.fileSize);
					msgToSend.setHash(fichDescargado.fileHash);
					cantidad = DirMessage.responseDirdlRealFirstPacketSize(
							fichDescargado.fileName, fichDescargado.fileSize, fichDescargado.fileHash);
					data = FileInfo.leerFragmento(fichDescargado.filePath, inicio, cantidad);
					if (data.length < cantidad) {
						msgToSend.setCode(3);
						System.out.println("Returning FIRST and FINAL block 0 from file: " 
								+ fichDescargado.fileName);
					} else {
						System.out.println("Returning FIRST block 0 from file: " 
								+ fichDescargado.fileName);
					}
				} else {
					msgToSend.setStatus("OK");
					inicio = DirMessage.responseDirdlRealFirstPacketSize(
							fichDescargado.fileName, fichDescargado.fileSize, fichDescargado.fileHash) +
							DirMessage.RESPONSE_DIRDL_REAL_AVERAGE_PACKET_SIZE * (bloque-1);
					cantidad = DirMessage.RESPONSE_DIRDL_REAL_AVERAGE_PACKET_SIZE;
					data = FileInfo.leerFragmento(fichDescargado.filePath, inicio, cantidad);
					if (data.length < cantidad) {
						msgToSend.setCode(2);
						System.out.println("Returning FINAL block " + bloque + " from file: " 
								+ fichDescargado.fileName);
					} else {
						msgToSend.setCode(1);
						System.out.println("Returning block " + bloque + " from file: " 
								+ fichDescargado.fileName);
					}
				}
			}
			
			String senData = msgToSend.toString();
			byte[] bytesCabecera = senData.getBytes();
				
			byte[] resultado = new byte[bytesCabecera.length + data.length];

			System.arraycopy(bytesCabecera, 0, resultado, 0, bytesCabecera.length);

			System.arraycopy(data, 0, resultado, bytesCabecera.length, data.length);

			
			yield resultado;
		}
		
		case DirMessageOps.OPERATION_QUIT -> {
			String operationField = DirMessageOps.RESPONSE_QUIT;
			String status = "ERROR";
			int code = -1;
			DirMessage msgToSend = new DirMessage(operationField, status, code);
			
			String nick = recMessage.getNickname();
			if (registeredPeers.containsKey(nick)) {
				msgToSend.setStatus("OK");
				msgToSend.setCode(0);
				registeredPeers.remove(nick);
				System.out.println("Successfully removed peer in " + clientAddr + " from the Directory");
				System.out.println("Come back soon!");
			} else {
				System.out.println("Peer in " + clientAddr + " is not currently registered in the Directory!");
			}
			
			String senData = msgToSend.toString();
			yield senData.getBytes();
		}

		default -> {
			System.err.println("Unexpected message operation: \"" + operation + "\"");
			System.exit(-1);
			yield null;
		}
			
		};

		/*
		 * TODO: (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagrama
		 */
		
		DatagramPacket resPkt = new DatagramPacket(senBytes, senBytes.length, clientAddr);
		socket.send(resPkt);
		
		outroDep(clientAddr, operation);

	}
	
	private static void introDep(InetSocketAddress client, String operation) {
		System.out.println("Received datagram from client in "+ client + " - operation= " + operation);
	}
	
	private static void outroDep(InetSocketAddress client, String operation) {
		System.out.println("Sent response for operation "+ operation + " to client in " + client);
		System.out.println();
	}



}
