package es.um.redes.nanoFiles.udp.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;
	/**
	 * Nombre/IP del host donde se ejecuta el directorio
	 */
	private String directoryHostname;





	public static class DownloadedFile {
		public final String filename;
		public final long filesize;
		public final byte[] data;
		public final String filehash;

		public DownloadedFile(String filename, long fsize, byte[] data, String filehash) {
			this.filename = filename;
			this.filesize = fsize;
			this.data = data;
			this.filehash = filehash;
		}
	}

	public DirectoryConnector(String hostname) throws IOException {
		// Guardamos el string con el nombre/IP del host
		directoryHostname = hostname;
		
		directoryAddress = new InetSocketAddress(InetAddress.getByName(directoryHostname), DIRECTORY_PORT);
		
		socket = new DatagramSocket();
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * TODO: (Boletín SocketsUDP) Enviar datos en un datagrama al directorio y
		 * recibir una respuesta. El array devuelto debe contener únicamente los datos
		 * recibidos, *NO* el búfer de recepción al completo.
		 */
		DatagramPacket sendPacket = new DatagramPacket(requestData, requestData.length, directoryAddress);
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP package failed to be sent!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, directoryAddress);
		int i = 0;
		while (i < MAX_NUMBER_OF_ATTEMPTS) {
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.receive(responsePacket);
				break;
			} catch(SocketTimeoutException e) {
				System.err.println("DirectoryConnector.sendAndReceiveDatagrams: Waiting time limit exceeded! \n Let's try again!");
			} catch (IOException e) {
				System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP package failed to be received!");
				e.printStackTrace();
				System.exit(-1);
			}
			i++;
		}
		if (i == 100000) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: Maximum number of tries exceeded. Communication wasn't possible.");
			System.exit(-1);
		}
		
		
		response = Arrays.copyOfRange(responseData, 0, responsePacket.getLength());
		/*
		 * TODO: (Boletín SocketsUDP) Una vez el envío y recepción asumiendo un canal
		 * confiable (sin pérdidas) esté terminado y probado, debe implementarse un
		 * mecanismo de retransmisión usando temporizador, en caso de que no se reciba
		 * respuesta en el plazo de TIMEOUT. En caso de salte el timeout, se debe volver
		 * a enviar el datagrama y tratar de recibir respuestas, reintentando como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
		 */
		
		
		/*
		 * TODO: (Boletín SocketsUDP) Las excepciones que puedan lanzarse al
		 * leer/escribir en el socket deben ser capturadas y tratadas en este método. Si
		 * se produce una excepción de entrada/salida (error del que no es posible
		 * recuperarse), se debe informar y terminar el programa.
		 */
		/*
		 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.
		 */



		/* ESTO YA NO ES ÚTIL PORQUE EN DIRDL LLENO AL MAXIMO LOS DATAGRAMAS!!
		 * 
		 * if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
			}
		 */
		
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
		/*
		 * TODO: (Boletín SocketsUDP) Probar el correcto funcionamiento de
		 * sendAndReceiveDatagrams. Se debe enviar un datagrama con la cadena "ping" y
		 * comprobar que la respuesta recibida empieza por "pingok". En tal caso,
		 * devuelve verdadero, falso si la respuesta no contiene los datos esperados.
		 */
		boolean success = false;
		
		String cadenaEnvio = "ping";
		String cadenaEsperada = "pingok";
		
		byte[] bytesEnvio = cadenaEnvio.getBytes();
		
		byte[] bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
		String cadenaRespuesta = new String(bytesRespuesta, 0, bytesRespuesta.length);
		
		success = cadenaRespuesta.equals(cadenaEsperada);

		return success;
	}

	public String getDirectoryHostname() {
		return directoryHostname;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que
	 * usa un protocolo compatible. Este método no usa mensajes bien formados.
	 * 
	 * @return Verdadero si
	 */
	public boolean pingDirectoryRaw() {
		boolean success = false;
		/*
		 * TODO: (Boletín EstructuraNanoFiles) Basándose en el código de
		 * "testSendAndReceive", contactar con el directorio, enviándole nuestro
		 * PROTOCOL_ID (ver clase NanoFiles). Se deben usar mensajes "en crudo" (sin un
		 * formato bien definido) para la comunicación.
		 * 
		 * PASOS: 1.Crear el mensaje a enviar (String "ping&protocolId"). 2.Crear un
		 * datagrama con los bytes en que se codifica la cadena : 4.Enviar datagrama y
		 * recibir una respuesta (sendAndReceiveDatagrams). : 5. Comprobar si la cadena
		 * recibida en el datagrama de respuesta es "welcome", imprimir si éxito o
		 * fracaso. 6.Devolver éxito/fracaso de la operación.
		 */
		String cadenaEnvio = "ping&" + NanoFiles.PROTOCOL_ID;
		String cadenaEsperada = "welcome";
		byte[] bytesEnvio = cadenaEnvio.getBytes();
		byte[] bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
		String cadenaRespuesta = new String(bytesRespuesta, 0, bytesRespuesta.length);
		
		if (success = cadenaRespuesta.equals(cadenaEsperada)) {
			System.out.println("ping command worked successfully!");
		} else {
			System.err.println("ping command failed to work.");
		}
		
		return success;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que es
	 * compatible.
	 * 
	 * @return Verdadero si el directorio está operativo y es compatible
	 */
	public boolean pingDirectory() {
		boolean success = false;
		/*
		 * TODO: (Boletín MensajesASCII) Hacer ping al directorio 1.Crear el mensaje a
		 * enviar (objeto DirMessage) con atributos adecuados (operation, etc.) NOTA:
		 * Usar como operaciones las constantes definidas en la clase DirMessageOps :
		 * 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		 * 3.Crear un datagrama con los bytes en que se codifica la cadena : 4.Enviar
		 * datagrama y recibir una respuesta (sendAndReceiveDatagrams). : 5.Convertir
		 * respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
		 * 6.Extraer datos del objeto DirMessage y procesarlos 7.Devolver éxito/fracaso
		 * de la operación
		 */
		
		DirMessage mensajeEnvio = new DirMessage(DirMessageOps.OPERATION_PING, NanoFiles.PROTOCOL_ID);
		
		String cadenaEnvio = mensajeEnvio.toString();
		byte[] bytesEnvio = cadenaEnvio.getBytes();
		byte[] bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
		String cadenaRespuesta = new String(bytesRespuesta, 0, bytesRespuesta.length);
		DirMessage mensajeRespuesta = DirMessage.fromString(cadenaRespuesta);
		
		if ("OK".equals(mensajeRespuesta.getStatus())) success = true;
		
		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean registerFileServer(int serverPort) {
		boolean success = false;

		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		DirMessage mensajeEnvio = new DirMessage(DirMessageOps.OPERATION_SERVE, serverPort, NanoFiles.peerNickname);
		
		String cadenaEnvio = mensajeEnvio.toString();
		byte[] bytesEnvio = cadenaEnvio.getBytes();
		byte[] bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
		String cadenaRespuesta = new String(bytesRespuesta, 0, bytesRespuesta.length);
		DirMessage mensajeRespuesta = DirMessage.fromString(cadenaRespuesta);
		
		if ("OK".equals(mensajeRespuesta.getStatus())) {
			success = true;
			if (mensajeRespuesta.getCode() == 1) {
				NanoFiles.peerNickname = mensajeRespuesta.getNickname();
				System.out.println("The received nickname was already being used by another serving peer");
				System.out.println("Directory assigned new nickname: " + NanoFiles.peerNickname);
				System.out.println("You will now be identified by this new nickname");
			}
		}
		

		return success;
	}

	/**
	 * Método para obtener la lista de ficheros alojados en el directorio. Para cada
	 * fichero se debe obtener un objeto FileInfo con nombre, tamaño y hash.
	 * 
	 * @return Los ficheros disponibles en el directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		List<FileInfo> filelist = new ArrayList<>(); 
		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		DirMessage mensajeEnvio;
		String cadenaEnvio;
		byte[] bytesEnvio;
		byte[] bytesRespuesta;
		String cadenaRespuesta;
		DirMessage mensajeRespuesta;
		
		int bloqueSolicitado = 0;
		int ultimoBloque = 0;
		while (ultimoBloque == 0) {
			mensajeEnvio = new DirMessage(DirMessageOps.OPERATION_DIRFILES, bloqueSolicitado);
			cadenaEnvio = mensajeEnvio.toString();
			bytesEnvio = cadenaEnvio.getBytes();
			bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
			cadenaRespuesta = new String(bytesRespuesta, 0, bytesRespuesta.length);
			mensajeRespuesta = DirMessage.fromString(cadenaRespuesta);
			
			if ("ERROR".equals(mensajeRespuesta.getStatus())) {
				return null;
			}
			
			filelist.addAll(mensajeRespuesta.getFicheros());
			ultimoBloque = mensajeRespuesta.getCode();
			bloqueSolicitado++;
		}
		

		return filelist.toArray(new FileInfo[0]);
	}

	public Map<String, InetSocketAddress> getPeerList() {
		Map<String, InetSocketAddress> peers = new LinkedHashMap<String, InetSocketAddress>();
		
		DirMessage mensajeEnvio = new DirMessage(DirMessageOps.OPERATION_PEERS);
		
		String cadenaEnvio = mensajeEnvio.toString();
		byte[] bytesEnvio = cadenaEnvio.getBytes();
		byte[] bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
		String cadenaRespuesta = new String(bytesRespuesta, 0, bytesRespuesta.length);
		DirMessage mensajeRespuesta = DirMessage.fromString(cadenaRespuesta);
		
		peers.putAll(mensajeRespuesta.getPeers());
		return peers;
	}

	/**
	 * Ampliación peerdl *
	 */
	private static boolean esMiPropiaDireccion(InetSocketAddress direccionRemota) {
	    if (direccionRemota.getPort() != NFServer.PORT) {
	        return false;
	    }

	    try {
	        InetAddress addr = direccionRemota.getAddress();
	        
	        if (addr.isLoopbackAddress()) return true;

	        return java.net.NetworkInterface.getByInetAddress(addr) != null;

	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public Map<FileInfo, List<InetSocketAddress>> searchFilesByHash(String hashSubstring, InetSocketAddress servidor) {
		Map<FileInfo, List<InetSocketAddress>> results = new LinkedHashMap<FileInfo, List<InetSocketAddress>>();
		
		Map<String, InetSocketAddress> peerList = getPeerList();
		NFConnector nfConnector = null;
		Collection<InetSocketAddress> direcciones = peerList.values();
		try {
			for (InetSocketAddress direccion : direcciones) {
				if (esMiPropiaDireccion(direccion)) continue;
				nfConnector = new NFConnector(direccion);
				DirectoryConnector.DownloadedFile dl = nfConnector.gotFile(false, hashSubstring);
				if (dl == null || dl.filesize == -1) {
					continue;
				} else if (dl.filesize == -2) {
					FileInfo error1 = new FileInfo(null, null, -1, null);
					FileInfo error2 = new FileInfo(null, null, -2, null);
					results.clear();
					results.put(error1, null);
					results.put(error2, null);
					break;
				} else {
					boolean dentro = false;
					
					for (Map.Entry<FileInfo, List<InetSocketAddress>> entry : results.entrySet()) {
					    FileInfo key = entry.getKey();
					    List<InetSocketAddress> value = entry.getValue();
					    if (key.fileHash.equals(dl.filehash)) {
							dentro = true;
							value.add(direccion);
							break;
						} 
					}
					if (!dentro) {
						FileInfo key = new FileInfo(dl.filehash, dl.filename, dl.filesize, null);
						List<InetSocketAddress> value = new ArrayList<>();
						value.add(direccion);
						results.put(key, value);
					}
				}
				nfConnector.closeConnector();
			}
			
		} catch (UnknownHostException e) {
			System.err.println("Server not found: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage());
			return null;
		} finally {
			if (nfConnector != null) nfConnector.closeConnector();
		}
		


		return results;
	}

	private static int posicionIncioPrimerBloque(byte[] bytes) {
		// Buscamos la secuencia 10, 10 (que es \n\n en ASCII)
	    for (int i = 0; i < bytes.length - 1; i++) {
	        if (bytes[i] == 10 && bytes[i+1] == 10) {
	            return i + 2; 
	        }
	    }
	    return -1;
	}
	
	private static String getFromBytes (byte[] bytes, String field) {
		int longitud;
		int inicio;
		String campo = field.toLowerCase();
		if ("code".equals(campo)) {
			longitud = DirMessage.RESPONSE_DIRDL_AVERAGE_HEADER_LENGTH + 3 + 1;//3 = |ERROR|-|OK| ; 1 = |-n| - |0|	
			inicio = 2;
		} else {
			longitud = DirMessage.RESPONSE_DIRDL_FIRST_MAX_HEADER_LENGTH;
			if ("name".equals(campo)) {
				inicio = 3;
			} else if ("size".equals(campo)) {
				inicio = 4;
			} else if ("hash".equals(campo)) {
				inicio = 5;
			} else {
				return null;
			}
		}
			
		String cabecera = new String(bytes, 0, longitud);
		
	    int inicioLineaHash = DirMessage.getPosicionNesima(cabecera, '\n', inicio);
	    int finLineaHash = DirMessage.getPosicionNesima(cabecera, '\n', inicio+1);
	    String lineaHash = cabecera.substring(inicioLineaHash+1, finLineaHash).trim();
	    String valorHash = DirMessage.CampoYValor(lineaHash)[1];
	    
	    return valorHash;
	}
	
	
	private static int getCodeFromBytes (byte[] bytes) {
		return Integer.parseInt(getFromBytes(bytes, "code"));	
	}
	
	private static String getNameFromBytes (byte[] bytes) {
		return getFromBytes(bytes, "name");
	}
	private static long getSizeFromBytes (byte[] bytes) {
	    return Long.parseLong(getFromBytes(bytes, "size"));		
	}
	private static String getHashFromBytes (byte[] bytes) {
		return getFromBytes(bytes, "hash");		
	}
	
	
	
	public DownloadedFile downloadFileFromDirectory(String hashSubstring) {
		byte[] fileData = null;
		String filename = null;
		long filesize = -1;
		String filehash = null;
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //Para anidar data[]
		
		DirMessage mensajeEnvio;
		String cadenaEnvio;
		byte[] bytesEnvio;
		byte[] bytesRespuesta;
		
		int bloqueSolicitado = 0;
		int ultimoBloque = 0;
		while (ultimoBloque < 2) {
			mensajeEnvio = new DirMessage(DirMessageOps.OPERATION_DIRDL, hashSubstring, bloqueSolicitado);
			cadenaEnvio = mensajeEnvio.toString();
			bytesEnvio = cadenaEnvio.getBytes();
			bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
			
			int codigo = getCodeFromBytes(bytesRespuesta);
			
			
			if (codigo < 0) {
				if (codigo == -1) System.out.println("Ther is no file whose hash contains the specified substring");
				else {
					System.out.println("There are multiple files whose hash contains the specified substring.");
					System.out.println("Please try again with a more specific hash substring");
				}
				return null;
			}
			
			if (codigo == 0 || codigo == 3) {
				filename = getNameFromBytes(bytesRespuesta);
				filesize = getSizeFromBytes(bytesRespuesta);
				filehash = getHashFromBytes(bytesRespuesta);
				
				
			} 
			int posInicio = posicionIncioPrimerBloque(bytesRespuesta);
			int cantidadDatos = bytesRespuesta.length - posInicio;
			buffer.write(bytesRespuesta, posInicio, cantidadDatos);
			
			ultimoBloque = codigo;
			bloqueSolicitado++;
		}
		
		
		fileData = buffer.toByteArray();

		return new DownloadedFile(filename, filesize, fileData, filehash);
	}

	/**
	 * Método para darse de baja como servidor de ficheros.
	 * 
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y ha dado de baja sus ficheros.
	 */
	public boolean unregisterFileServer() {
		boolean success = false;
		
		DirMessage mensajeEnvio = new DirMessage(DirMessageOps.OPERATION_QUIT, NanoFiles.peerNickname);
		
		String cadenaEnvio = mensajeEnvio.toString();
		byte[] bytesEnvio = cadenaEnvio.getBytes();
		byte[] bytesRespuesta = sendAndReceiveDatagrams(bytesEnvio);
		String cadenaRespuesta = new String(bytesRespuesta, 0, bytesRespuesta.length);
		DirMessage mensajeRespuesta = DirMessage.fromString(cadenaRespuesta);
		
		if ("OK".equals(mensajeRespuesta.getStatus())) success = true;
		
		return success;
	}





}
