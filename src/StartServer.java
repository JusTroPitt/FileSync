

import java.rmi.Naming;
import java.util.Scanner;

public class StartServer  {
		
	
	public static void main (String args[]) {
		
		//Aqui estamos vinculando la implementación remota con el servidor
		try {
			;
			System.out.println("Introduzca el nombre de la carpeta remota. Si no, se le asignará una carpeta por defecto");
			Scanner teclado = new Scanner(System.in);
			String ruta = teclado.nextLine();

			if (ruta.isBlank()) {
				ruta = "DirectorioRemoto";
			}
			
			ServerImplementation m1 = new ServerImplementation(ruta); //Creamos el objeto
			Naming.rebind("RMIRegistry", m1); //Creamos el registry 
			System.out.print("Servidor listo");
			teclado.close();
			
		}
		catch(Exception e) {
			System.out.print("Error" + e);
		}
		
	}

}
