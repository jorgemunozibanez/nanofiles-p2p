package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = -1;

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con un par
	 * servidor de ficheros (valores posibles del campo "operation").
	 */

	public static final byte OPCODE_PEERFILES = 0x00;
	public static final byte OPCODE_FILES_INFO = 0x01;
	public static final byte OPCODE_EMPTY_LIST = 0x02;
	
	public static final byte OPCODE_GOT_FILE = 0x10;
	public static final byte OPCODE_FILE_LOCATED = 0x11;
	public static final byte OPCODE_FILE_NOT_FOUND = 0x12;
	public static final byte OPCODE_MULTIPLE_FILES_FOUND = 0x13;
	
	public static final byte OPCODE_GET_FRAGMENT = 0x20;
	
	public static final byte OPCODE_SEND_CHUNK = 0x31;



	/*
	 * TODO: (Boletín MensajesBinarios) Definir constantes con nuevos opcodes de
	 * mensajes definidos anteriormente, añadirlos al array "valid_opcodes" y añadir
	 * su representación textual a "valid_operations_str" EN EL MISMO ORDEN.
	 */
	private static final Byte[] _valid_opcodes = { OPCODE_INVALID_CODE,
			OPCODE_PEERFILES,
			OPCODE_FILES_INFO,
			OPCODE_EMPTY_LIST,
			OPCODE_GOT_FILE,
			OPCODE_FILE_LOCATED,
			OPCODE_FILE_NOT_FOUND,
			OPCODE_MULTIPLE_FILES_FOUND,
			OPCODE_GET_FRAGMENT,
			OPCODE_SEND_CHUNK
	};
	private static final String[] _valid_operations_str = { "INVALID_OPCODE",
			"PEERFILES",
			"FILES_INFO",
			"EMPTY_LIST",
			"GOT_FILE",
			"FILE_LOCATED",
			"FILE_NOT_FOUND",
			"MULTIPLE_FILES_FOUND",
			"GET_FRAGMENT",
			"SEND_CHUNK"
	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}

	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
