package classes;

public class Prova_val_ref {
	
	public int altezza;
	public int larghezza;
	
	public Prova_val_ref(int h, int w) {
		this.altezza = h;
		this.larghezza = w;
	}
	
	public Prova_val_ref(Prova_val_ref copy) {
		this.altezza = copy.altezza;
		this.larghezza = copy.larghezza;
	}
	
	public void increment(int i) {
		this.altezza+=i;
		this.larghezza+=i;
	}
	
	public String toString() {
		return (this.getClass().getName()+": W= "+String.valueOf(this.altezza)+", H="+String.valueOf(this.larghezza)+"\n");
	}
}
