package triangle;

public class Triangle {

	public static enum Type {
		INVALID, SCALENE, EQUILATERAL, ISOSCELES
	};
	
	public static Type classify(int a, int b, int c) {
		int trian;
		if (a <= 0 || b <= 0 || c <= 0)
			return Type.INVALID;
		trian = 0;
		if (a == b)
			trian = trian + 1;
		if (a == c)
			trian = trian + 2;
		if (b == c)
			trian = trian + 3;
		if (trian == 0)
			if (a + b <= c || a + c <= b || b + c <= a)
				return Type.INVALID;
			else
				return Type.SCALENE;
		if (trian > 3)
			return Type.EQUILATERAL;
		if (trian == 1 && a + b > c)
			return Type.ISOSCELES;
		else if (trian == 2 && a + c > b)
			return Type.ISOSCELES;
		else if (trian == 3 && b + c > a)
			return Type.ISOSCELES;
		return Type.INVALID;
	}
}
