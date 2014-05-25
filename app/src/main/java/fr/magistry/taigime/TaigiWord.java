/**
 * 
 */
package fr.magistry.taigime;

/**
 * @author pierre
 *
 */
public class TaigiWord {
	//private String inputed;
	private long wid;
	private String bopomo;
	private String hanji;
	private String tailuo;
	
	public TaigiWord(long id, String bpm, String hj, String tl){
	//	inputed = input;
		wid = id;
		hanji = hj;
		tailuo = tl;
		bopomo = bpm;
		
	}

	public String getHanji() {
		return hanji;
	}
	public String getTailuo(){
		return tailuo;
	}
	public String getBopomo(){
		return bopomo;
	}
	public long getWid(){
		return wid;
	}
//	public String getInputed(){
	//	return inputed;
	//}

	@Override
	public boolean equals(Object other){
		if (this == other)
			return true;
		if (!(other instanceof TaigiWord))
			return false;
		return this.wid == ((TaigiWord)other).wid;
	}
	
}
