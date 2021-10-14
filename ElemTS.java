package Analizador;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ElemTS {
	
	private int TS;
	private String lexema;
	private String tipo;
	private int despl;
	private int numParam;
	private List<String> tipoParam = new ArrayList<String>();
	private int modoParam = 1;	//1=Valor, 2=Referencia
	private String tipoRetorno;
	private String etiqFuncion;
	private int punteroTS;
	
	ElemTS(String lexema, int TS, int punteroTS){
		this.lexema = lexema;
		this.TS = TS;
		this.punteroTS = punteroTS;
	}

	public int getPunteroTS() {
		return punteroTS;
	}

	public void setPunteroTS(int punteroTS) {
		this.punteroTS = punteroTS;
	}



	public int getTS() {
		return TS;
	}

	public void setTS(int tS) {
		TS = tS;
	}



	public String getLexema() {
		return lexema;
	}

	public void setLexema(String lexema) {
		this.lexema = lexema;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getDespl() {
		return despl;
	}

	public void setDespl(int despl) {
		this.despl = despl;
	}

	public int getNumParam() {
		return numParam;
	}

	public void setNumParam(int numParam) {
		this.numParam = numParam;
	}

	public List<String> getTipoParam() {
		return tipoParam;
	}

	public void setTipoParam(List<String> tipoParam) {
		this.tipoParam = tipoParam;
	}

	public int getModoParam() {
		return modoParam;
	}

	public void setModoParam(int modoParam) {
		this.modoParam = modoParam;
	}

	public String getTipoRetorno() {
		return tipoRetorno;
	}

	public void setTipoRetorno(String tipoRetorno) {
		this.tipoRetorno = tipoRetorno;
	}

	public String getEtiqFuncion() {
		return etiqFuncion;
	}

	public void setEtiqFuncion(String etiqFuncion) {
		this.etiqFuncion = etiqFuncion;
	}

	@Override
	public String toString() {
		String res = "* '"+lexema+"'";
		res += "\n + tipo : '"+tipo+"'";
		
		if(tipo!="funcion")
			res += "\n + despl : '"+despl+"'";
		else {
			res += "\n + numParam : '"+numParam+"'";
			
			int i = 0;
			
			Iterator<String> it = tipoParam.iterator();
			while(it.hasNext()) {
				res += "\n + tipoParam"+i+" : '"+it.next()+"'";
				res += "\n + modoParam"+i+" : '"+1+"'";
			}
			
			res += "\n + tipoRetorno : '"+tipoRetorno+"'";
			res += "\n + etiqFuncion : '"+etiqFuncion+"'";
		}
		
		return res;
	}
	
	
}
