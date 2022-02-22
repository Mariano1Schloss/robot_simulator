package model;

public abstract class EntityDescriptor implements Situated{
	
	protected int y, x;
	
	public EntityDescriptor (int [] location) {
		this.x= location[0];
		this.y = location[1];
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
