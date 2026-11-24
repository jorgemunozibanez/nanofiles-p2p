package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 32000; // 65535 - 8 (UDP header) - 20 (IP header)
	

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	private static final String RESPONSE_TAG = "response"; // All response messages end with this string
	private static final int RESPONSE_TAG_LENGTH = RESPONSE_TAG.length(); // Length of response tag
	
	
	
	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	
	/*
	 * TODO: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */
	
	/**
	 * Campo que indica el protocolo (operacion ping)
	 */
	private static final String FIELDNAME_PROTOCOL = "protocol";
	
	/**
	 * Campo que indica el numero del bloque solicitado (operacion dirfiles y dirdl)
	 */
	private static final String FIELDNAME_BLOCK = "block";
	
	/**
	 * Campo que indica el nickname del par (operacion serve, respuesta peers y quit)
	 */
	private static final String FIELDNAME_NICKNAME = "nickname";
	
	/**
	 * Campo que indica el puerto del par (operacion serve y respuesta peers)
	 */
	private static final String FIELDNAME_PORT = "port";
	
	/**
	 * Campo que indica el estado general de la respuesta: OK/ERROR (todos los mensajes de respuesta)
	 */
	private static final String FIELDNAME_STATUS = "status";
	
	/**
	 * Campo que indica el código de la información específica de la respuesta (todos los mensajes de respuesta)
	 */
	private static final String FIELDNAME_CODE = "code";
	
	/**
	 * Campo que indica el nombre del fichero (respuesta dirfiles y respuesta dirdl)
	 */
	private static final String FIELDNAME_NAME = "name";
	
	/**
	 * Campo que indica el tamaño del fichero (respuesta dirfiles y respuesta dirdl)
	 */
	private static final String FIELDNAME_SIZE = "size";
	
	/**
	 * Campo que indica el hash del fichero (respuesta dirfiles y respuesta dirdl)
	 */
	private static final String FIELDNAME_HASH = "hash";

	/**
	 * Campo que indica la ruta del fichero (respuesta dirfiles)
	 */
	private static final String FIELDNAME_PATH = "path";
	
	/**
	 * Campo que indica el nuevo nickname asidnado por el directorio (respuesta serve)
	 */
	private static final String FIELDNAME_NEWNICK = "newnick";
	
	/**
	 * Campo que indica la direccion IP del peer (respuesta peers)
	 */
	private static final String FIELDNAME_IP = "ip";
	
	/**
	 * Campo que indica el subhash del fichero (dirdl)
	 */
	private static final String FIELDNAME_SUBHASH = "subhash";
	
	
	
	
	
	
	
	
	
	/**
	 * Ampliación dirfiles
	 */
	//Longitud de la cadena que contiene los primeros campos del mensaje de respuesta de dirfiles.
	private static final int RESPONSE_DIRFILES_HEADER_LENGTH = 
			(FIELDNAME_OPERATION + DELIMITER + DirMessageOps.RESPONSE_DIRFILES + END_LINE +
			 FIELDNAME_STATUS + DELIMITER + "OK" + END_LINE +
			 FIELDNAME_CODE + DELIMITER + "0" + END_LINE).length();
	
	//Calculamos el espacio real disponible en un paquete de respuesta dirfiles  
	//quitando del maximo la longitud de los primeros campos de la respuesta 
	//y luego restando la longitud del caracter endline final (= 1).
	public static final int RESPONSE_DIRFILES_REAL_PACKET_SIZE = 
			DirMessage.PACKET_MAX_SIZE - DirMessage.RESPONSE_DIRFILES_HEADER_LENGTH - 1;
	
	//Espacio ocupado por el nombre de los campos + delimitadores + fin de línea por cada archivo incluido
	//en la operación dirfiles 
	public static final int RESPONSE_DIRFILES_FIXED_SIZE_PER_FILE = (4+1+1)*4;

	
	/**
	 * Ampliación dirdl
	 */
	//Longitud suficiente para incluir todos los primeros campos del PRIMER mensaje de respuesta de dirfiles.
	public static final int RESPONSE_DIRDL_FIRST_MAX_HEADER_LENGTH = 200; //better safe than sorry
	
	//Longitud de la cadena que contiene los primeros campos del PRIMER mensaje de respuesta de dirfiles.
	//Incluye el espacio ocupado por los campos operation, status, code, name, size y hash.
	private static final int RESPONSE_DIRDL_FIRST_HEADER_LENGTH = 
			(FIELDNAME_OPERATION + DELIMITER + DirMessageOps.RESPONSE_DIRDL + END_LINE +
			 FIELDNAME_STATUS + DELIMITER + "OK" + END_LINE +
			 FIELDNAME_CODE + DELIMITER + "0" + END_LINE).length() + (4+1+1)*3;
	
	//Metodo para calcular el tamaño REAL de la cabecera en el PRIMER paquete de respuesta dirdl
	private static int responseDirdlRealFirstPacketHeaderSize (String name, long size, String hash) {
		int cabecera = RESPONSE_DIRDL_FIRST_HEADER_LENGTH + name.getBytes().length +
		           String.valueOf(size).getBytes().length + hash.getBytes().length; 
		return cabecera; //EndLine final
	}
	
	//Metodo para calcular el espacio real disponible en el PRIMER paquete de respuesta dirdl
	//teniendo en cuenta el nombre, tamaño y hash del fichero
	public static int responseDirdlRealFirstPacketSize (String name, long size, String hash) {
		return DirMessage.PACKET_MAX_SIZE - responseDirdlRealFirstPacketHeaderSize(name, size, hash) - 1; //EndLine final
	}
	
	//Longitud de la cadena que contiene los primeros campos del RESTO de paquetes de respuesta de dirfiles.
	//Incluye el espacio ocupado por los campos operation, status y code.
	public static final int RESPONSE_DIRDL_AVERAGE_HEADER_LENGTH = 
			(FIELDNAME_OPERATION + DELIMITER + DirMessageOps.RESPONSE_DIRDL + END_LINE +
			 FIELDNAME_STATUS + DELIMITER + "OK" + END_LINE +
			 FIELDNAME_CODE + DELIMITER + "0" + END_LINE).length();
	
	//Metodo para calcular el espacio real disponible en el RESTO de paquetes de respeusta dirdl
	public static final int RESPONSE_DIRDL_REAL_AVERAGE_PACKET_SIZE = 
			DirMessage.PACKET_MAX_SIZE - RESPONSE_DIRDL_AVERAGE_HEADER_LENGTH - 1;	
		
		
		
	
	
	
	
		
	
	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId;
	/*
	 * TODO: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */
	
	/**
	 * Posición del bloque de la lista de ficheros solicitado por dirfiles.
	 */
	private int block;
	
	/**
	 * Estado del mensaje de respuesta, para comprobar el estado general de todos los mensajes de respuesta.
	 */
	private String status;
	
	/**
	 * Código del mensaje de respuesta, para comprobar la información específica de todos los mensajes de respuesta.
	 */
	private int code;
	
	/**
	 * Lista de ficheros devueltos, para almacenar la información necesaria de cada uno.
	 */
	private List<FileInfo> ficheros;
	
	/**
	 * Apodo del par que quiere servir ficheros. Puede también contener el nuevo apodo asignado por el directorio. 
	 */
	private String nickname;
	
	/**
	 * Número del puerto TCP en el que escucha el servidor del par.
	 */
	private int port;
	
	/**
	 * Lista de peers devueltos.
	 */
	private Map<String, InetSocketAddress> registeredPeers;
	
	/**
	 * Subhash del fichero buscado.
	 */
	private String subhash;
	
	/**
	 * Nombre del fichero descargado.
	 */
	private String name;
	
	/**
	 * Tamaño del fichero descargado.
	 */
	private long size;
	
	/**
	 * Hash del fichero descargado.
	 */
	private String hash;
	


	
	
	
	
	
	
	
	
	public DirMessage(String op) {
		operation = op;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */

	//ping & nick
	public DirMessage(String op, String s) {
		this(op);
		if (operation.equals(DirMessageOps.OPERATION_PING)) {
			protocolId = s;
		} else if (operation.equals(DirMessageOps.OPERATION_QUIT)) {
			nickname = s;
		} else {
			throw new RuntimeException(
					"DirMessage: constructor called for message of unexpected type (" + operation + ")");
		}
		
	}
	
	//dirfiles
	public DirMessage(String op, int blockent) {
		this(op);
		if (!operation.equals(DirMessageOps.OPERATION_DIRFILES)) {
			throw new RuntimeException(
					"DirMessage: constructor called for message of unexpected type (" + operation + ")");
		}
		block = blockent;
	}
	
	//serve
	public DirMessage(String op, int portent, String nicknameent) {
		this(op);
		if (!operation.equals(DirMessageOps.OPERATION_SERVE)) {
			throw new RuntimeException(
					"DirMessage: constructor called for message of unexpected type (" + operation + ")");
		}
		port = portent;
		nickname = nicknameent;
	}

	//all response messages & dirdl
	public DirMessage(String op, String s, int i) {
		this(op);
		if (operation.substring(Math.max(0, operation.length()-RESPONSE_TAG_LENGTH)).equals(RESPONSE_TAG)) {
			status = s;
			code = i;
			
			if (operation.equals(DirMessageOps.RESPONSE_DIRFILES)) {
				ficheros = new ArrayList<>();
			} else if (operation.equals(DirMessageOps.RESPONSE_PEERS)) {
				registeredPeers = new LinkedHashMap<>();
			}
		} else if (operation.equals(DirMessageOps.OPERATION_DIRDL)) {
			subhash = s;
			block = i;
		} else {
			throw new RuntimeException(
					"DirMessage: constructor called for message of unexpected type (" + operation + ")");
		}
		
	}
	
	//response serve 1
	public DirMessage(String op, String statusent, int codeent, String newnickent) {
		this(op, statusent, codeent);
		if (!(operation.equals(DirMessageOps.RESPONSE_SERVE) && code == 1)) {
			throw new RuntimeException(
					"DirMessage: constructor called for message of unexpected type (" + operation + ")");
		}
		nickname = newnickent;
	}
	
	//response dirdl 0 & 3
		public DirMessage(String op, String statusent, int codeent, String nameent, String hashent, long sizeent) {
			this(op, statusent, codeent);
			if (!(operation.equals(DirMessageOps.RESPONSE_DIRDL) && (code == 0 || code == 3))) {
				throw new RuntimeException(
							"DirMessage: constructor called for message of unexpected type (" + operation + ")");
			}
			name = nameent;
			size = sizeent;
			hash = hashent;
			
		}
	
		
		
	
	
	
	
	
	
	public String getOperation() {
		return operation;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	
	//PING
	public void setProtocolID(String protocolIdent) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		this.protocolId = protocolIdent;
	}

	public String getProtocolId() {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: getProtocolId called for message of unexpected type (" + operation + ")");
		}
		return protocolId;
	}
	
	//DIRFILES & DIRDL
	public void setBlock(int blockent) {
		if (!(operation.equals(DirMessageOps.OPERATION_DIRFILES) || operation.equals(DirMessageOps.OPERATION_DIRDL))) {
			throw new RuntimeException(
					"DirMessage: setBlock called for message of unexpected type (" + operation + ")");
		}
		this.block = blockent;
	}

	public int getBlock() {
		if (!(operation.equals(DirMessageOps.OPERATION_DIRFILES) || operation.equals(DirMessageOps.OPERATION_DIRDL))) {
			throw new RuntimeException(
					"DirMessage: getBlock called for message of unexpected type (" + operation + ")");
		}
		return block;
	}
	
	//SERVE
	public void setPort(int portent) {
		if (!operation.equals(DirMessageOps.OPERATION_SERVE)) {
			throw new RuntimeException(
					"DirMessage: setPort called for message of unexpected type (" + operation + ")");
		}
		this.port = portent;
	}

	public int getPort() {
		if (!operation.equals(DirMessageOps.OPERATION_SERVE)) {
			throw new RuntimeException(
					"DirMessage: getPort called for message of unexpected type (" + operation + ")");
		}
		return port;
	}
	
	public void setNickname(String nicknameent) {
		if (!operation.equals(DirMessageOps.OPERATION_SERVE) && !(operation.equals(DirMessageOps.RESPONSE_SERVE) && code == 1) &&
				!operation.equals(DirMessageOps.OPERATION_QUIT)) {
			throw new RuntimeException(
					"DirMessage: setNickname called for message of unexpected type (" + operation + ")");
		}
		this.nickname = nicknameent;
	}

	public String getNickname() {
		if (!operation.equals(DirMessageOps.OPERATION_SERVE) && !(operation.equals(DirMessageOps.RESPONSE_SERVE) && code == 1) &&
				!operation.equals(DirMessageOps.OPERATION_QUIT)) {
			throw new RuntimeException(
					"DirMessage: getNickname called for message of unexpected type (" + operation + ")");
		}
		return nickname;
	}
	
	//DIRDL
	public void setSubhash(String Subhashent) {
		if (!operation.equals(DirMessageOps.OPERATION_DIRDL) && !(operation.equals(DirMessageOps.RESPONSE_DIRDL) && code == 1)) {
			throw new RuntimeException(
					"DirMessage: setSubhash called for message of unexpected type (" + operation + ")");
		}
		this.subhash = Subhashent;
	}

	public String getSubhash() {
		if (!operation.equals(DirMessageOps.OPERATION_DIRDL) && !(operation.equals(DirMessageOps.RESPONSE_DIRDL) && code == 1)) {
			throw new RuntimeException(
					"DirMessage: getSubhash called for message of unexpected type (" + operation + ")");
		}
		return subhash;
	}
	
	//ALL RESPONSE MESSAGES
	public void setStatus(String statusent) {
		if (!operation.substring(Math.max(0, operation.length()-RESPONSE_TAG_LENGTH)).equals(RESPONSE_TAG)) {
			throw new RuntimeException(
					"DirMessage: setStatus called for message of unexpected type (" + operation + ")");
		}
		this.status = statusent;
	}

	public String getStatus() {
		if (!operation.substring(Math.max(0, operation.length()-RESPONSE_TAG_LENGTH)).equals(RESPONSE_TAG)) {
			throw new RuntimeException(
					"DirMessage: getStatus called for message of unexpected type (" + operation + ")");
		}
		return status;
	}	

	public void setCode(int codent) {
		if (!operation.substring(Math.max(0, operation.length()-RESPONSE_TAG_LENGTH)).equals(RESPONSE_TAG)) {
			throw new RuntimeException(
					"DirMessage: setCode called for message of unexpected type (" + operation + ")");
		}
		this.code = codent;
	}

	public int getCode() {
		if (!operation.substring(Math.max(0, operation.length()-RESPONSE_TAG_LENGTH)).equals(RESPONSE_TAG)) {
			throw new RuntimeException(
					"DirMessage: getCode called for message of unexpected type (" + operation + ")");
		}
		return code;
	}
	
	//DIRFILES RESPONSE
	public List<FileInfo> getFicheros() {
		if (!operation.equals(DirMessageOps.RESPONSE_DIRFILES)) {
			throw new RuntimeException(
					"DirMessage: getFicheros called for message of unexpected type (" + operation + ")");
		}
		return Collections.unmodifiableList(this.ficheros);
	}

	public void setFicheros(List<FileInfo> ficheros) {
		if (!operation.equals(DirMessageOps.RESPONSE_DIRFILES)) {
			throw new RuntimeException(
					"DirMessage: setFicheros called for message of unexpected type (" + operation + ")");
		}
		this.ficheros = ficheros;
	}
	
	public void addFichero(FileInfo fichero) {
		ficheros.add(fichero);
	}
	
	//PEERS RESPONSE
	public Map<String, InetSocketAddress> getPeers() {
		if (!operation.equals(DirMessageOps.RESPONSE_PEERS)) {
			throw new RuntimeException(
					"DirMessage: getPeers called for message of unexpected type (" + operation + ")");
		}
		return Collections.unmodifiableMap(this.registeredPeers);
	}

	public void setPeers(Map<String, InetSocketAddress> peers) {
		if (!operation.equals(DirMessageOps.RESPONSE_PEERS)) {
			throw new RuntimeException(
					"DirMessage: setPeers called for message of unexpected type (" + operation + ")");
		}
		this.registeredPeers = peers;
	}
	
	public void addPeer(String nick, InetAddress ip, int port) {
		InetSocketAddress direccion = new InetSocketAddress(ip, port);
		registeredPeers.put(nick, direccion);
	}
	
	//DIRDL RESPONSE
	public String getName() {
		if (!(operation.equals(DirMessageOps.RESPONSE_DIRDL) && (code == 0 || code == 3))) {
			throw new RuntimeException(
					"DirMessage: getName called for message of unexpected type (" + operation + ")");
		}
		return name;
	}

	public void setName(String name) {
		if (!(operation.equals(DirMessageOps.RESPONSE_DIRDL) && (code == 0 || code == 3))) {
			throw new RuntimeException(
					"DirMessage: setName called for message of unexpected type (" + operation + ")");
		}
		this.name = name;
	}

	public long getSize() {
		if (!(operation.equals(DirMessageOps.RESPONSE_DIRDL) && (code == 0 || code == 3))) {
			throw new RuntimeException(
					"DirMessage: getSize called for message of unexpected type (" + operation + ")");
		}
		return size;
	}

	public void setSize(long size) {
		if (!(operation.equals(DirMessageOps.RESPONSE_DIRDL) && (code == 0 || code == 3))) {
			throw new RuntimeException(
					"DirMessage: setSize called for message of unexpected type (" + operation + ")");
		}
		this.size = size;
	}

	public String getHash() {
		if (!(operation.equals(DirMessageOps.RESPONSE_DIRDL) && (code == 0 || code == 3))) {
			throw new RuntimeException(
					"DirMessage: getHash called for message of unexpected type (" + operation + ")");
		}
		return hash;
	}

	public void setHash(String hash) {
		if (!(operation.equals(DirMessageOps.RESPONSE_DIRDL) && (code == 0 || code == 3))) {
			throw new RuntimeException(
					"DirMessage: setHash called for message of unexpected type (" + operation + ")");
		}
		this.hash = hash;
	}

	
	
	
	
	
	
	
	

	/**
	 * Ampliación dirdl
	 */
	//Devuelve fieldname y value de una línea
	public static String[] CampoYValor (String linea) {
		int idx = linea.indexOf(DELIMITER); // Posición del delimitador
		String campo = linea.substring(0, idx).toLowerCase(); // minúsculas
		String valor = linea.substring(idx + 1).trim();
		return new String[] {campo, valor};
	}
	
	//Devuelve la posición n-ésima de un carácter en un String
	public static int getPosicionNesima (String s, char c, int n) {
		int pos = -1;
		for (int i = 0; i < n; i++){
			pos = s.indexOf(c, pos+1);
			if (pos == -1) break;
		}
		return pos;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */
		
		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		
		
		
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		
		DirMessage m = null;
		String path = null;
		String name = null;
		long size = 0;
		String hash = null;
		FileInfo fichero;
		String nick = null;
		int puerto = 0;
		InetAddress ip = null; 


		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				if (value.equals(DirMessageOps.RESPONSE_DIRFILES)) m.setFicheros(new ArrayList<>());
				if (value.equals(DirMessageOps.RESPONSE_PEERS)) m.setPeers(new LinkedHashMap<>());
				continue;
			}
			
			case FIELDNAME_PROTOCOL: {
				assert (m != null);
				m.setProtocolID(value);
				continue;
			}
			
			case FIELDNAME_BLOCK: {
				assert (m != null);
				m.setBlock(Integer.parseInt(value));
				continue;
			}

			case FIELDNAME_STATUS: {
				assert (m != null);
				m.setStatus(value);
				continue;
			}
			
			case FIELDNAME_CODE: {
				assert (m != null);
				m.setCode(Integer.parseInt(value));
				continue;
			}
			
			case FIELDNAME_PATH: {
				assert (m != null);
				path = value;
				continue;
			}
			
			case FIELDNAME_NAME: {
				assert (m != null);
				if (m.getOperation().equals(DirMessageOps.OPERATION_DIRDL)) m.setName(value);
				else name = value;
				continue;
			}
			
			case FIELDNAME_SIZE: {
				assert (m != null);
				if (m.getOperation().equals(DirMessageOps.OPERATION_DIRDL)) m.setSize(Integer.parseInt(value));
				else size = Integer.parseInt(value);
				continue;
			}
			
			case FIELDNAME_HASH: {
				assert (m != null);
				if (m.getOperation().equals(DirMessageOps.OPERATION_DIRDL)) m.setHash(value);
				else {
					hash = value;
					fichero = new FileInfo(hash, name, size, path);
					m.addFichero(fichero);
				}
				
				continue;
			}
			
			case FIELDNAME_PORT: {
				assert (m != null);
				if (m.getOperation().equals(DirMessageOps.OPERATION_SERVE)){
					m.setPort(Integer.parseInt(value));
				} else {
					puerto = Integer.parseInt(value);
					m.addPeer(nick, ip, puerto);
				}
				continue;
			}
			
			case FIELDNAME_NICKNAME: {
				assert (m != null);
				if (m.getOperation().equals(DirMessageOps.RESPONSE_PEERS)){
					nick = value;
				} else {
					m.setNickname(value);
				}
				continue;
			}
			
			case FIELDNAME_NEWNICK: {
				assert (m != null);
				m.setNickname(value);
				continue;
			}
			
			case FIELDNAME_IP: {
				assert (m != null);
				try {
					ip = InetAddress.getByName(value);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				continue;
			}
			
			case FIELDNAME_SUBHASH: {
				assert (m != null);
				m.setSubhash(value);
				continue;
			}
			
			
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}
		
		return m;
	}

	
	
	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		
		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
			sb.append(FIELDNAME_PROTOCOL + DELIMITER + protocolId + END_LINE);
			break;
		}
		
		case DirMessageOps.RESPONSE_PING: {
			sb.append(FIELDNAME_STATUS + DELIMITER + status + END_LINE);
			sb.append(FIELDNAME_CODE + DELIMITER + code + END_LINE);
			break;
		}
		
		case DirMessageOps.OPERATION_DIRFILES: {
			sb.append(FIELDNAME_BLOCK + DELIMITER + block + END_LINE);
			break;
		}
		
		case DirMessageOps.RESPONSE_DIRFILES: {
			sb.append(FIELDNAME_STATUS + DELIMITER + status + END_LINE);
			sb.append(FIELDNAME_CODE + DELIMITER + code + END_LINE);
			
			for (FileInfo fichero : ficheros) {
				sb.append(FIELDNAME_PATH + DELIMITER + fichero.filePath + END_LINE);
				sb.append(FIELDNAME_NAME + DELIMITER + fichero.fileName + END_LINE);
				sb.append(FIELDNAME_SIZE + DELIMITER + fichero.fileSize + END_LINE);
				sb.append(FIELDNAME_HASH + DELIMITER + fichero.fileHash + END_LINE);
			}
			break;
		}
		
		case DirMessageOps.OPERATION_SERVE: {
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		
		case DirMessageOps.RESPONSE_SERVE: {
			sb.append(FIELDNAME_STATUS + DELIMITER + status + END_LINE);
			sb.append(FIELDNAME_CODE + DELIMITER + code + END_LINE);
			if (code == 1) {
				sb.append(FIELDNAME_NEWNICK + DELIMITER + nickname + END_LINE);
			}
			break;
		}
		
		case DirMessageOps.OPERATION_PEERS: {
			break;
		}
		
		case DirMessageOps.RESPONSE_PEERS: {
			sb.append(FIELDNAME_STATUS + DELIMITER + status + END_LINE);
			sb.append(FIELDNAME_CODE + DELIMITER + code + END_LINE);
			registeredPeers.forEach((key, value) -> {
				sb.append(FIELDNAME_NICKNAME + DELIMITER + key + END_LINE);
				sb.append(FIELDNAME_IP + DELIMITER + value.getAddress().getHostAddress() + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + value.getPort() + END_LINE);
			});
			break;
		}
		
		case DirMessageOps.OPERATION_DIRDL: {
			sb.append(FIELDNAME_SUBHASH + DELIMITER + subhash + END_LINE);
			sb.append(FIELDNAME_BLOCK + DELIMITER + block + END_LINE);
			break;
		}
		
		case DirMessageOps.RESPONSE_DIRDL: {
			sb.append(FIELDNAME_STATUS + DELIMITER + status + END_LINE);
			sb.append(FIELDNAME_CODE + DELIMITER + code + END_LINE);
			
			if ("OK".equals(status)) {
				if (code == 0 || code == 3) {
					sb.append(FIELDNAME_NAME + DELIMITER + name + END_LINE);
					sb.append(FIELDNAME_SIZE + DELIMITER + size + END_LINE);
					sb.append(FIELDNAME_HASH + DELIMITER + hash + END_LINE);
				}
			}
			
			break;
		}
		
		case DirMessageOps.OPERATION_QUIT: {
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		
		case DirMessageOps.RESPONSE_QUIT: {
			sb.append(FIELDNAME_STATUS + DELIMITER + status + END_LINE);
			sb.append(FIELDNAME_CODE + DELIMITER + code + END_LINE);
			break;
		}
		
		}

		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

}
