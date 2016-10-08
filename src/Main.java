import com.awesome.regexp.Regexp;

public class Main {

	public static void main(String[] args) {
		System.out.println(new Regexp("a|b").match("ab"));
	}

}
