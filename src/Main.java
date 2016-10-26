import com.awesome.XLex.Token;
import com.awesome.XLex.XLex;

public class Main {

	public static void main(String[] args) {
		XLex xlex = new XLex();
		Token[] tokenStream = xlex.getTokenStream("./Test/main.c");
//		File file = new File(".");
//		Logger.println(file.getAbsolutePath());
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
