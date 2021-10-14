package Analizador;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class Sintactico {

private static Token sig;	//El token actual de la lista
private static int i = 0;	//Recorre la lista de tokens
private static List<Token> tokens;	//Lista de tokens

private static List<Integer> parse = new ArrayList<Integer>();	//Parse (conjunto de reglas ejecutadas)

private static List<ElemTS> TS = new ArrayList<ElemTS>();	//Tabla de simbolos general
private static List<ElemTS> temporalTS = new ArrayList<ElemTS>();	//Tabla de simbolos temporal
private static int TSact = 0;	//Indica TS actual
private static int TScont = 1;	//Contador para generar TS

private static String tipoRet = "vacio";
private static List<String> tipoParam = new ArrayList<String>(); //Lista con tipos de parametro
private static List<String> tipoParam2 = new ArrayList<String>();

private static int desp = 0;	//Desplazamiento de pila
private static int despFun = 0;
public static int id = 0;	//Contador para el puntero de la id

private static boolean inFun = false; //Indica si estamos en una funcion
private static int lineaRet = 0;

public static void main(String[] args) {
	BufferedWriter bwTokens = null;
	BufferedWriter bwParse = null;
	BufferedWriter bwTS = null;
	
	Scanner scan = new Scanner(System.in);
    System.out.println("Inserte la direccion del fichero con el codigo que desee ejecutar");
    System.out.println("Por ejemplo: C:\\Users\\ASUS\\Desktop\\User\\Codigo.txt");
    String input = scan.nextLine();
    System.out.println("Inserte la direccion de la carpeta en la que quiera recibir los archivos");
    System.out.println("Por ejemplo: C:\\Users\\ASUS\\Desktop\\User");
    String output = scan.nextLine();
    
    input.replace("\\","\\\\");
    output.replace("\\","\\\\");
    
	try {
		
		String tokensPrint = "";
		String parsePrint = "";
		String tsPrint = "";
		
		//Llamamos al anLex para sacar la lista de tokens
		tokens = Lexico.listaTokens(input);
		sig = tokens.get(i);
		
		//Inicializa el analizador sintactico por el axioma
		P();
		
		////////////////////////////////////////////////////////////////////////
		Iterator<Token> it = tokens.iterator();
		Token a;
		while(it.hasNext()) {
			tokensPrint += it.next() + " ";
		}
		
		Iterator<Integer> it2 = parse.iterator();
		while(it2.hasNext()) {
			parsePrint += it2.next() + " ";
		}
		
		Iterator<ElemTS> it3 = TS.iterator();
		ElemTS elem;
		for(int i=0; i<TScont; i++) {
			tsPrint += "";
			it3 = TS.iterator();
			tsPrint += "#"+i+":";
			while(it3.hasNext()) {
				elem = it3.next();
				if(elem.getTS()==i)
					tsPrint += "\n" + elem + "\n";
			}
			tsPrint += "\n";
		}
		
		File fileTokens = new File(output+"\\Tokens.txt");
		File fileParse = new File(output+"\\Parse.txt");
		File fileTS = new File(output+"\\TS.txt");
		
		////////////////////////////////////////////////////////////////////////
		
		if (!fileTokens.exists())
		     fileTokens.createNewFile();
		
		if (!fileParse.exists())
		     fileParse.createNewFile();
		
		if (!fileTS.exists())
		     fileTS.createNewFile();
		
		FileWriter fwTokens = new FileWriter(fileTokens);
		bwTokens = new BufferedWriter(fwTokens);
		bwTokens.write(tokensPrint);
	    
	    FileWriter fwParse = new FileWriter(fileParse);
		bwParse = new BufferedWriter(fwParse);
		bwParse.write(parsePrint);
	    
	    FileWriter fwTS = new FileWriter(fileTS);
		bwTS = new BufferedWriter(fwTS);
		bwTS.write(tsPrint);
		
		/*
		//Imprime la lista de tokens
		Iterator<Token> it = tokens.iterator();
		Token a;
		while(it.hasNext()) {
			System.out.print(it.next() + " ");
		}
		
		
		System.out.println("\n");
		
		//Imprime el parse
		Iterator<Integer> it2 = parse.iterator();
		while(it2.hasNext()) {
			System.out.print(it2.next() + " ");
		}
		
		
		System.out.println("\n");
		
		Iterator<ElemTS> it3 = TS.iterator();
		ElemTS elem;
		
		for(int i=0; i<TScont; i++) {
			System.out.println("");
			it3 = TS.iterator();
			System.out.println("TS #"+i+":");
			while(it3.hasNext()) {
				elem = it3.next();
				if(elem.getTS()==i)
					System.out.print(elem + "\n");
			}
		}*/
		
		System.out.println("Programa ejecutado correctamente");
	} catch (Exception e) {
		e.printStackTrace();
		
	} finally { 
		try{
			if(bwTokens!=null)
				bwTokens.close();
			if(bwParse!=null)
				bwParse.close();
			if(bwTS!=null)
				bwTS.close();
			}catch(Exception ex){
		    	System.out.println("Error al cerrar el BufferedWriter"+ex);
			}
		}
	
}

public static void P() throws Exception {
	if(sig.getNombre().equals("Alert") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("If") || sig.getNombre().equals("Input")
			|| sig.getNombre().equals("Let") || sig.getNombre().equals("Return")) {
		parse.add(1);
		B();
		P();
		return;
	}
	
	if(sig.getNombre().equals("Function")) {
		parse.add(2);
		F();
		P();
		return;
	}
	
	if(i>=tokens.size()) {
		parse.add(3);
		return;
	}

	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void F() throws Exception {
	if(sig.getNombre().equals("Function")) {
		parse.add(4);
		equipara(sig, "Function");
		String tipoDev = H();
		
		////////////////////////////////////////////////////////////////////////
		
		ElemTS elem = null;	//Elemento funcion que vamos a añadir a la TS
		
		//Comprueba que el id de la funcion no este repetido
		if(buscar(TS, sig.getLexema(), 0)!=null)
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Funcion *"+sig.getLexema()+"* repetida");
			
		elem = new ElemTS(sig.getLexema(), 0, id);
		
		sig.setNum(id);	//Asignamos el puntero del token id
		id++;
		
		//Ponemos los atributos de la funcion
		elem.setEtiqFuncion("etiq_"+sig.getLexema());
		elem.setTipo("funcion");
		elem.setTipoRetorno(tipoDev);
		
		//Cambiamos la TS por una nueva
		TSact = TScont;
		TScont++;
		
		//Inicializamos las variables privadas
		tipoParam = new ArrayList<String>();
		temporalTS = new ArrayList<ElemTS>();
		tipoRet = "vacio";
		
		//Evita nullpointer cuando se repite el nombre de la funcion como parametro
		TS.add(new ElemTS(elem.getLexema(), 0, id));
		
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "Id");
		equipara(sig, "AbrirPar");
		A();
		
		////////////////////////////////////////////////////////////////////////
		
		//Declaramos que estamos en la funcion
		inFun = true;
		
		//Evita nullpointer cuando se repite el nombre de la funcion como parametro
		TS.remove(TS.size()-1);
		
		//Actualizamos los parametros de la funcion en la TS
		elem.setNumParam(tipoParam.size());
		elem.setTipoParam(tipoParam);
		
		//Insertamos la funcion en la TS
		TS.add(elem);
		
		//Añadimos los parametros de la TSactual a la TS
		Iterator<ElemTS> it = temporalTS.iterator();
		while(it.hasNext()) {
			TS.add(it.next());
		}
		
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "CerrarPar");
		equipara(sig, "AbrirLlave");
		C();
		
		////////////////////////////////////////////////////////////////////////
		
		//Comprobamos que el tipo de retorno sea correcto
		if(!tipoRet.equals(tipoDev))
			throw new Exception("Error semantico [linea "+lineaRet+"]: Tipo de retorno incorrecto\n");

		//Pasamos de la TS de la funcion a la TS general
		TSact = 0;
		despFun = 0;
		
		//Salimos de la funcion
		inFun = false;
		
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "CerrarLlave");
		
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String H() throws Exception {
	if(sig.getNombre().equals("Id")) {
		parse.add(6);
		return "vacio";
	}
	
	if(sig.getNombre().equals("TipoBoolean") || sig.getNombre().equals("TipoCadena")
			|| sig.getNombre().equals("TipoNumber")) {
		parse.add(5);
		//T();
		return T();
	}
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void A() throws Exception {
	if(sig.getNombre().equals("CerrarPar")) {
		parse.add(8);
		return;
	}
	
	if(sig.getNombre().equals("TipoBoolean") || sig.getNombre().equals("TipoCadena")
			|| sig.getNombre().equals("TipoNumber")) {
		parse.add(7);
		
		////////////////////////////////////////////////////////////////////////
		
		String param = T();
		
		//Añadimos el tipo de parametro
		tipoParam.add(param);
		
		ElemTS elem = null;	//Elemento funcion que vamos a añadir a la TS
		
		//Comprueba que el id no este repetido
		if(buscar(TS, sig.getLexema(), 0)!=null || buscar(TS, sig.getLexema(), TSact)!=null)
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Variable *"+sig.getLexema()+"* repetida");
		
		//Inicializamos el elemento
		elem = new ElemTS(sig.getLexema(), TSact, id);
				
		sig.setNum(id);	//Asignamos el puntero de la id
		id++;
				
		//Ponemos los atributos
		elem.setTipo(param);
		elem.setDespl(despFun);
		despFun += desplazamiento(param);
		//Añadimos el elemento a una TS temporal (para no meterlo antes de la funcion en la TS)
		temporalTS.add(elem);
		
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "Id");
		K();
		
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void K() throws Exception {
	if(sig.getNombre().equals("CerrarPar")) {
		parse.add(10);
		return;
	}
	
	if(sig.getNombre().equals("Coma")){
		parse.add(9);
		equipara(sig, "Coma");
		
		////////////////////////////////////////////////////////////////////////
		
		String param = T();

		//Añadimos el tipo de parametro
		tipoParam.add(param);

		ElemTS elem = null;	//Elemento funcion que vamos a añadir a la TS

		//Comprueba que el id de la funcion no este repetido
		if(buscar(TS, sig.getLexema(), -1)!=null || buscar(TS, sig.getLexema(), TSact)!=null)
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Variable *"+sig.getLexema()+"* repetida");

		elem = new ElemTS(sig.getLexema(), TSact, id);

		sig.setNum(id);	//Asignamos el puntero de la id
		id++;

		//Ponemos los atributos
		elem.setTipo(param);
		elem.setDespl(despFun);
		despFun += desplazamiento(param);

		temporalTS.add(elem);

		////////////////////////////////////////////////////////////////////////

		equipara(sig, "Id");
		K();
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void C() throws Exception {
	if(sig.getNombre().equals("CerrarLlave")) {
		parse.add(12);
		return;
	}
	
	if(sig.getNombre().equals("Alert") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("If") || sig.getNombre().equals("Input")
			|| sig.getNombre().equals("Let") || sig.getNombre().equals("Return")){
		parse.add(11);
		B();
		C();
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void B() throws Exception {
	
	if(sig.getNombre().equals("Alert") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("Input") || sig.getNombre().equals("Return")){
		parse.add(15);
		S();
		return;
	}
	
	if(sig.getNombre().equals("If")){
		parse.add(13);
		equipara(sig, "If");
		equipara(sig, "AbrirPar");
		String E = E();
		equipara(sig, "CerrarPar");
		B1();
		
		////////////////////////////////////////////////////////////////////////
		
		//El if tiene que ser booleano
		if(!E.equals("boolean"))
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: La condicion del if ha de ser booleana");

		return;
	}
	
	if(sig.getNombre().equals("Let")){
		parse.add(14);
		equipara(sig, "Let");
		String T = T();
		
		////////////////////////////////////////////////////////////////////////
		
		ElemTS elem = new ElemTS(sig.getLexema(), TSact, id);
		
		if(buscar(TS, sig.getLexema(), 0)!=null || buscar(TS, "sig.getLexema()", TSact)!=null)
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Variable *"+sig.getLexema()+"* repetida");
		
		sig.setNum(id);	//Asignamos el puntero del token id
		id++;
		elem.setTipo(T);
		
		//Movemos el desplazamiento de la pila
		if(inFun) {
			elem.setDespl(despFun);
			despFun += desplazamiento(T);
		} else {
			elem.setDespl(desp);
			desp += desplazamiento(T);
		}
		
		TS.add(elem);
		
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "Id");
		equipara(sig, "PuntoComa");
		
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void B1() throws Exception {
	
	if(sig.getNombre().equals("Alert") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("Input") || sig.getNombre().equals("Return")){
		parse.add(16);
		S();
		return;
	}
	
	if(sig.getNombre().equals("AbrirLlave")){
		parse.add(17);
		equipara(sig, "AbrirLlave");
		C();
		equipara(sig, "CerrarLlave");
		B2();
		return;
	}
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void B2() throws Exception {
	
	if(sig.getNombre().equals("Alert") || sig.getNombre().equals("CerrarLlave")
			|| sig.getNombre().equals("Function") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("If") || sig.getNombre().equals("Input")
			|| sig.getNombre().equals("Let") || sig.getNombre().equals("Return")
			|| i>=tokens.size()){
		parse.add(18);
		return;
	}
	
	if(sig.getNombre().equals("Else")){
		parse.add(19);
		equipara(sig, "Else");
		equipara(sig, "AbrirLlave");
		C();
		equipara(sig, "CerrarLlave");
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String T() throws Exception {
	
	if(sig.getNombre().equals("TipoBoolean")){
		parse.add(21);
		equipara(sig, "TipoBoolean");
		return "boolean";
	}
	
	if(sig.getNombre().equals("TipoNumber")){
		parse.add(20);
		equipara(sig, "TipoNumber");
		return "entero";
	}
	
	if(sig.getNombre().equals("TipoCadena")){
		parse.add(22);
		equipara(sig, "TipoCadena");
		return "cadena";
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void S() throws Exception {
	
	if(sig.getNombre().equals("Alert")){
		parse.add(24);
		equipara(sig, "Alert");
		equipara(sig, "AbrirPar");
		String E = E();
		
		////////////////////////////////////////////////////////////////////////
		
		if(E.equals("boolean"))
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: No se puede imprimir ese tipo de valor");
	
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "CerrarPar");
		equipara(sig, "PuntoComa");
		
		return;
	}
	
	if(sig.getNombre().equals("Id")){
		parse.add(23);
		////////////////////////////////////////////////////////////////////////
		
		ElemTS elem = buscar(TS, sig.getLexema(), 0);
		
		if(elem == null) {
			elem = buscar(TS, sig.getLexema(), TSact);
			if(elem == null)
				throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Variable o funcion *"+sig.getLexema()+"* no existente");
		}
		
		sig.setNum(elem.getPunteroTS());

		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "Id");
		String S1 = S1();
		
		////////////////////////////////////////////////////////////////////////
		
		if(!elem.getTipo().equals(S1) && !S1.equals("llamadaFun"))
			throw new Exception("Error semantico [linea "+tokens.get(i-1).getLinea()+"]: Variable o funcion *"+elem.getLexema()+"* con tipo diferente al asignado");
		
		////////////////////////////////////////////////////////////////////////
		
		return;
	}
	
	if(sig.getNombre().equals("Input")){
		parse.add(25);
		equipara(sig, "Input");
		equipara(sig, "AbrirPar");
		
		////////////////////////////////////////////////////////////////////////
		ElemTS elem = buscar(TS, sig.getLexema(), 0);

		if(elem == null) {
			elem = buscar(TS, sig.getLexema(), TSact);
			if(elem == null)
				throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Variable o funcion *"+sig.getLexema()+"* no existente");
		}

		sig.setNum(elem.getPunteroTS());

		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "Id");
		equipara(sig, "CerrarPar");
		equipara(sig, "PuntoComa");
		return;
	}
	
	if(sig.getNombre().equals("Return")){
		parse.add(26);
		equipara(sig, "Return");
		tipoRet = X();
		lineaRet = sig.getLinea();
		equipara(sig, "PuntoComa");
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+" ]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String S1() throws Exception {
	
	if(sig.getNombre().equals("AbrirPar")){
		parse.add(29);
		equipara(sig, "AbrirPar");
		L();
		equipara(sig, "CerrarPar");
		
		////////////////////////////////////////////////////////////////////////
		
		if(!tipoParam.equals(tipoParam2))
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Parametros de llamada a la funcion incorrectos");
		
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "PuntoComa");
		return "llamadaFun";
	}
	
	if(sig.getNombre().equals("Asignacion")){
		parse.add(27);
		equipara(sig, "Asignacion");
		String E = E();
		equipara(sig, "PuntoComa");
		return E;
	}
	
	if(sig.getNombre().equals("AsignacionSuma")){
		parse.add(28);
		equipara(sig, "AsignacionSuma");
		String E = E();
		
		////////////////////////////////////////////////////////////////////////
		
		if(!E.equals("entero"))
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: No se puede sumar un valor no numerico");

		////////////////////////////////////////////////////////////////////////

		equipara(sig, "PuntoComa");
		return E;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void L() throws Exception {
	
	if(sig.getNombre().equals("CerrarPar")){
		parse.add(31);
		return;
	}
	
	if(sig.getNombre().equals("AbrirPar") || sig.getNombre().equals("Cadena")
			|| sig.getNombre().equals("Entero") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("Not")){
		parse.add(30);
		tipoParam2.add(E());
		Q();
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void Q() throws Exception {
	
	if(sig.getNombre().equals("CerrarPar")){
		parse.add(33);
		return;
	}
	
	if(sig.getNombre().equals("Coma")){
		parse.add(32);
		equipara(sig, "Coma");
		tipoParam2.add(E());
		Q();
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String X() throws Exception {
	
	if(sig.getNombre().equals("PuntoComa")){
		parse.add(35);
		return "vacio";
	}
	
	if(sig.getNombre().equals("AbrirPar") || sig.getNombre().equals("Cadena")
			|| sig.getNombre().equals("Entero") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("Not")){
		parse.add(34);
		return E();
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String E() throws Exception {
	
	if(sig.getNombre().equals("AbrirPar") || sig.getNombre().equals("Cadena")
			|| sig.getNombre().equals("Entero") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("Not")){
		parse.add(36);
		String R = R();
		String E1 = E1();
		
		////////////////////////////////////////////////////////////////////////
		
		//Si no hay ==, devolvemos el tipo de R
		if(E1.equals("vacio"))
			return R;
		else {
			if(!R.equals("entero") || !E1.equals("entero"))
				//Ambos operadores han de ser enteros
				throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Operador invalido para la operacion igualdad");
			//Si hay ==, devolvemos un boolean
			return "boolean";
		}
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String E1() throws Exception {
	
	if(sig.getNombre().equals("CerrarPar") || sig.getNombre().equals("Coma")
			|| sig.getNombre().equals("PuntoComa")){
		parse.add(38);
		return "vacio";
	}
	
	if(sig.getNombre().equals("Igualdad")) {
		parse.add(37);
		equipara(sig,"Igualdad");
		String R = R();
		String E1 = E1();
		
		////////////////////////////////////////////////////////////////////////
		/*TECNICAMENTE NO SE PUEDE HACER DOS ==, PORQUE LA OPERACION SOLO ESTA
		DEFINIDA PARA OPERADORES ENTEROS, DEVOLVIENDO UN VALOR BOOLEAN*/
		
		//Comprobamos que, en caso de haber mas de una igualdad, los tipos sean iguales
		if(E1.equals("vacio"))
			return R;
		else {
			if(!R.equals("entero") || !E1.equals("entero"))
				//Ambos operadores han de ser enteros
				throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Operador invalido para la operacion igualdad");
			//Si hay ==, devolvemos un boolean
			return "boolean";
		}
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String R() throws Exception {
	
	if(sig.getNombre().equals("AbrirPar") || sig.getNombre().equals("Cadena")
			|| sig.getNombre().equals("Entero") || sig.getNombre().equals("Id")
			|| sig.getNombre().equals("Not")){
		parse.add(39);
		String U = U();
		String R1 = R1();
		
		////////////////////////////////////////////////////////////////////////
		
		if(R1.equals("vacio"))
			return U;
		else {
			if(!U.equals("entero") || !R1.equals("entero"))
				//Ambos operadores han de ser enteros
				throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Operador invalido para la operacion suma");
			//Si hay ==, devolvemos un boolean
			return U;
		}
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String R1() throws Exception {
	
	if(sig.getNombre().equals("Coma") || sig.getNombre().equals("CerrarPar")
			|| sig.getNombre().equals("PuntoComa") || sig.getNombre().equals("Igualdad")){
		parse.add(41);
		return "vacio";
	}
	
	if(sig.getNombre().equals("Suma")) {
		parse.add(40);
		equipara(sig,"Suma");
		String U = U();
		String R1 = R1();
		
		////////////////////////////////////////////////////////////////////////
		if(R1.equals("vacio"))
			return U;
		else {
			if(!U.equals("entero") || !R1.equals("entero"))
				//Ambos operadores han de ser enteros
				throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Operador invalido para la operacion suma");
			//Si hay ==, devolvemos un boolean
			return U;
		}
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
	
}

public static String U() throws Exception {
	
	if(sig.getNombre().equals("AbrirPar") || sig.getNombre().equals("Cadena")
			|| sig.getNombre().equals("Entero") || sig.getNombre().equals("Id")){
		parse.add(43);
		//V();
		return V();
	}
	
	if(sig.getNombre().equals("Not")){
		parse.add(42);
		equipara(sig, "Not");
		String U = U();
		
		////////////////////////////////////////////////////////////////////////
		
		//Comprobamos que sea un valor booleano
		if(!U.equals("boolean"))
			throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Operador no valido");

		return U;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static String V() throws Exception {
	
	if(sig.getNombre().equals("AbrirPar")){
		parse.add(45);
		equipara(sig, "AbrirPar");
		String E = E();
		equipara(sig, "CerrarPar");
		return E;
	}
	
	if(sig.getNombre().equals("Cadena")){
		parse.add(47);
		equipara(sig, "Cadena");
		return "cadena";
	}
	
	if(sig.getNombre().equals("Entero")){
		parse.add(46);
		equipara(sig, "Entero");
		return "entero";
	}
	
	if(sig.getNombre().equals("Id")){
		parse.add(44);
		
		////////////////////////////////////////////////////////////////////////
		
		//Buscamos el tipo de la id en la TS y lo devolvemos
		ElemTS elem = buscar(TS, sig.getLexema(), 0);

		if(elem == null) {
			elem = buscar(TS, sig.getLexema(), TSact);
			if(elem == null)
				throw new Exception("Error semantico [linea "+tokens.get(i).getLinea()+"]: Variable o funcion *"+sig.getLexema()+"* no existente");
		}

		sig.setNum(elem.getPunteroTS());
		
		////////////////////////////////////////////////////////////////////////
		
		equipara(sig, "Id");
		V1();
		
		////////////////////////////////////////////////////////////////////////
		return elem.getTipo();
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

public static void V1() throws Exception {
	
	if(sig.getNombre().equals("Coma") || sig.getNombre().equals("CerrarPar")
			|| sig.getNombre().equals("PuntoComa") || sig.getNombre().equals("Igualdad")
			|| sig.getNombre().equals("Suma")){
		parse.add(49);
		return;
	}
	
	if(sig.getNombre().equals("AbrirPar")) {
		parse.add(48);
		equipara(sig,"AbrirPar");
		L();
		equipara(sig,"CerrarPar");
		return;
	}
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

//Funcion que equipara un token con un string, avanzando el cursor
public static void equipara(Token tk, String str) throws Exception {
	if(tk.getNombre().equals(str)) {
		i++;
		if(i<tokens.size())
			sig = tokens.get(i);
			//i++;
		return;
	}
	
	throw new Exception("Error sintactico [linea "+tokens.get(i).getLinea()+"]: "+tokens.get(i).getNombre()+" no esperado");
}

//Funcion auxiliar que busca un elemento en la TS especificada por su nombre y su numero de TS(-1 busca en todas)
public static ElemTS buscar(List<ElemTS> lista, String str, int num) {
	Iterator<ElemTS> it = lista.iterator();
	ElemTS n = null;
	ElemTS res = null;
	boolean found = false;
	
	while(it.hasNext() && !found) {
		n = it.next();
		if(n.getLexema().equals(str)) {
			if(num==-1||num==n.getTS()) {
				found = true;
				res = n;
			}
		}
	}
	
	return res;
}

//Funcion auxiliar que devuelve el desplazamiento en funcion del tipo
public static int desplazamiento(String tipo) {
	if(tipo.equals("entero") || tipo.equals("boolean"))
		return 1;
	if(tipo.equals("cadena"))
		return 64;
	return 0;
}

}



