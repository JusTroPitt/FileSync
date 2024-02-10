import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;

public class ServerImplementation extends UnicastRemoteObject implements RMIRegistry {

	private static final long serialVersionUID = 1L;

	private static Path path;

	public ServerImplementation(String pathServer) throws RemoteException {
		super();
		// pathServer sería la "carpeta madre" del server
		path = Paths.get(pathServer);
		
		if (path.toFile().exists()) {
			System.out.println("La carpeta se ha introducido correctamente");
		} else {
			boolean x = path.toFile().mkdir();

			if (x) {
				System.out.println("La carpeta se ha creado correctamente");

			}

			else{
				System.out.println("No se ha podido crear la carpeta. Será creada una carpeta por defecto en su lugar");
				path = Paths.get("DirectorioLocal");
			}

		}

		System.out.println("La ruta absoluta es: \n" + path.toFile().getAbsolutePath());
	}

	@Override
	public void subirArchivoAlServer(byte[] datos, String archivo, int length) throws IOException {

		Path pathArchivo = Paths.get(path + "/" + archivo);
		File serverpathfile = pathArchivo.toFile();
		serverpathfile.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(serverpathfile);
		
		out.write(datos);
		out.flush();
		out.close();

		System.out.println(serverpathfile.getName() + " subido");

	}

	@Override
	public byte[] descargarArchivoDelServer(String archivo) throws RemoteException {

		byte[] datos;

		Path pathArchivo = Paths.get(path + "/" + archivo);
		File serverpathfile = pathArchivo.toFile();
		datos = new byte[(int) serverpathfile.length()];
		FileInputStream in;
		try {
			in = new FileInputStream(serverpathfile);
			try {
				in.read(datos, 0, datos.length);
			} catch (IOException e) {

				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {

				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		return datos;

	}

	@Override
	public HashSet<String> getLista() throws IOException, RemoteException {
		HashSet<String> listado = new HashSet<>();
		File[] archivos = path.toFile().listFiles((File a) -> a.isFile());
		for (File f : archivos) {
			listado.add(f.getName());

		}

		File[] directorios = path.toFile().listFiles((File a) -> a.isDirectory());
		if (directorios.length > 0 || !directorios.equals(null)) {
			for (File f : directorios) {
				String pathDirectorio = f.getName();
				HashSet<String> archivosDirectorio = listarDirectorios(pathDirectorio);
				// System.out.println(path + "\n" + pathDirectorio.toString());
				listado.addAll(archivosDirectorio);
			}
		}
		return listado;
	}

	private HashSet<String> listarDirectorios(String directorio) throws IOException {
		Path pathV = Paths.get(path + "/" + directorio);
		// System.out.println(pathV);
		HashSet<String> listado = new HashSet<>();
		File[] archivos = pathV.toFile().listFiles((File a) -> a.isFile());
		for (File f : archivos) {
			listado.add(directorio + "/" + f.getName());

		}

		File[] directorios = pathV.toFile().listFiles((File a) -> a.isDirectory());

		if (directorios.length > 0 || !directorios.equals(null)) {
			// System.out.println(directorios.length);

			for (File f : directorios) {
				// System.out.println(f.getName());
				String pathDirectorio = directorio + "/" + f.getName();
				// System.out.println(pathDirectorio);

				HashSet<String> archivosDirectorio = listarDirectorios(pathDirectorio);

				listado.addAll(archivosDirectorio);
				// System.out.println(archivosDirectorio);
			}

		}
		return listado;
	}

	@Override
	public ArrayList<byte[]> getHash(String archivo) throws RemoteException {

		Path pathArchivo = Paths.get(path + "/" + archivo);

		FileChannel fn = null;
		ArrayList<byte[]> h = new ArrayList<>();
		try {
			// Leemos el archivo e indicamos que el buffer tenga una capacidad de 512kB
			fn = (FileChannel) Files.newByteChannel(pathArchivo);
			ByteBuffer buffer = ByteBuffer.allocate(512 * 1024);
			// Indicamosue queremos un hash en md5
			MessageDigest msd = MessageDigest.getInstance("MD5");
			// Creamos un bucle que termine cuando no lea nada mas en el archivo
			while (fn.read(buffer) != -1) {
				byte[] a = new byte[buffer.position()];
				buffer.flip();
				buffer.get(a);
				byte[] b = msd.digest(a);
				// Hacemos la conversion de bytes a hexadecimal
				StringBuilder result = new StringBuilder();
				for (byte aByte : b) {
					result.append(String.format("%02x", aByte));

				}
				// añadimos a nuestra variable h el resultado en bytes
				h.add(result.toString().getBytes());
				// Limpiamos el buffer
				buffer.clear();

			}
			// Si queremos hacer la comprobacion lo hacemos con la linea de abajo comentada
			// System.out.println(new String(h.get(0)));
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			try {
				fn.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return h;

	}

	@Override
	public long getUltimaModificacion(String archivo) throws RemoteException {

		Path pathArchivo = Paths.get(path + "/" + archivo);

		long ultimaModificacion = pathArchivo.toFile().lastModified();

		return ultimaModificacion;
	}

}
