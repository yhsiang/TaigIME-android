/**
 * 
 */
package fr.magistry.taigime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pierre
 *
 */


public class Candidate {
	private TaigiWord mWord;
	private String mInput;
	
	public Candidate(String input, TaigiWord w){
		mWord = w;
		mInput = input;
			
	}
	
	public TaigiWord getWord(){
		return mWord;
	}
	public String getInputed(){
		return mInput;
	}
	
	public static ArrayList<Candidate> buildSuggestions(String input, List<Candidate> liste){
		ArrayList<Candidate> result = new ArrayList<Candidate>();
		result.add(new Candidate(input, new TaigiWord(-1,input,"","")));
		for(Candidate c : liste){
			result.add(c);
		}
		return result;
	} 
	
}
