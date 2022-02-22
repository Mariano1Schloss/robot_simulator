package burger;

public class Point {
	int[] value;
	Point(int[] v){ 
		value = new int[]{v[0], v[1]};
	}
	
	@Override
	public boolean equals(Object o) {
		return ( (((Point)o).value[0] == value[0]) && (((Point)o).value[1] == value[1]) ) ;
	}
}
