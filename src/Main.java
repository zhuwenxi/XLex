import java.util.List;

import com.awesome.XLex.Logger;
import com.awesome.XLex.Token;
import com.awesome.XLex.XLex;

public class Main {

	public static void main(String[] args) {
		String filename = "./Test/main.c";
		XLex xlex = new XLex(filename);
		
		List<Token> tokens = xlex.getTokens();
		for (Token t : tokens) {
			Logger.println(t);
		}
	}

}

class TestKey {
	public int key;
	public TestKey(int key) {
		this.key = key;
	}
	
	@Override
	public int hashCode() {
		return this.key;
	}
	
	@Override
	public boolean equals(Object o) {
		TestKey that = (TestKey)o;
		
		return that != null && this.key == that.key;
	}
}

class TestValue {
	public String value;
	public TestValue(String value) {
		this.value = value;
	}
}
