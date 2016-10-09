import com.awesome.regexp.FiniteAutomata;
import com.awesome.regexp.Logger;
import com.awesome.regexp.Regexp;

public class Main {

	public static void main(String[] args) {
		FiniteAutomata nfa = new Regexp("ab|cd").getNfa();
		
		Logger.println(nfa.transDiag);
	}

}
