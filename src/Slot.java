
public class Slot {
	int x;
	int y;
	
	public Slot() {} // Constructor
	
	public Slot(int x_koord, int y_koord) {
		this.x = x_koord;
		this.y = y_koord;
	}
	
	public void setX (int x_koord) {
		this.x = x_koord;
	}
	
	public void setY (int y_koord) {
		this.y = y_koord;
	}
}
