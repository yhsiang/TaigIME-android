/**
 * 
 */
package fr.magistry.taigime;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author pierre
 *
 */
public class TaigiSyl {
	
	
	private static Pattern mPatternBopomoSyl = Pattern.compile("(?:(ㄉ|ㄒ|ㄙ|ㄏ|ㆠ|ㄐ|ㄗ|ㄑ|ㄘ|ㆣ|ㄖ|ㄍ|ㄎ|ㄌ|ㄇ|ㄋ|ㄥ|ㄅ|ㄆ|ㄊ)?([ㄚㄧㄨㄜㄛㄝ]+°?|ㄇ|ㄥ)(?:(ㄣ|ㄥ|ㄇ)|(ㄅ|ㄉ|ㄍ|ㄏ))?([1-9])?)|(ㄉ|ㄒ|ㄙ|ㄏ|ㆠ|ㄐ|ㄗ|ㄑ|ㄘ|ㆣ|ㄖ|ㄍ|ㄎ|ㄌ|ㄇ|ㄋ|ㄥ|ㄅ|ㄆ|ㄊ)-?");
	private static Pattern mPatternTRSSyl = Pattern.compile( "(?:(p|b|ph|m|t|th|n|l|k|g|kh|ng|h|ts|j|tsh|s)?([aeiou+]+(?:nn|N)?|ng|m)(?:(ng|m|n|r)|(p|t|h|k))?([1-9])?)|(p|b|ph|m|t|th|n|l|k|g|kh|ng|h|tsi|tshi|si|ts|ji|j|tsh|s)-?-?");
	private static final int G_INITIALE = 1;
	private static final int G_VOYELLE = 2;
	private static final int G_FINALE = 3;
	private static final int G_ENTRANT = 4;
	private static final int G_TON = 5;
	private static final int G_INITIALE_SEULE = 6;
	
	private String Initiale = "";
	private String Mediane = "";
	private String Finale = "";
	private String TonEntrant = "";
	private int    Ton = 0;
	private String Inputed;
	public TaigiSyl(String input) {
		Inputed = input;
	}
	
	public String getInputed(){
		return Inputed;
	}
	
	public String getInitiale() {
		return Initiale;
	}
	public void setInitiale(String initiale) {
		Initiale = initiale;
	}
	public String getMediane() {
		return Mediane;
	}
	
	private static int getUTFSize(String s) {
	      int len = (s == null) ? 0
	                            : s.length();
	      int l   = 0;

	      for (int i = 0; i < len; i++) {
	          int c = s.charAt(i);
	          if ((c >= 0x0001) && (c <= 0x007F)) {
	              l++;
	          } else if (c > 0x07FF) {
	              l += 3;
	          } else {
	              l += 2;
	          }
	      }

	      return l;
	  }
	public void setMediane(String mediane) {
		int l = mediane.length();
		
		StringBuffer sb = new StringBuffer();
		for(int i = l; i < 3;i++){
			sb.append("Ø");
		}
		sb.append(mediane);
		Mediane = sb.toString();
	}
	public String getFinale() {
		return Finale;
	}
	public void setFinale(String finale) {
		Finale = finale;
	}
	public int getTon() {
		return Ton;
	}
	public void setTon(int ton) {
		Ton = ton;
	}
	
	public void setTonEntrant(String tonEntrant) {
		TonEntrant = tonEntrant;
	}
	public String getTonEntrant(){
		return TonEntrant;
	}
	
	public void updateBopomo(String bopomo){
		
	}
	
	public String getSqliteString(boolean fuzzy){
		if(Initiale != "" && Mediane.equals("ØØØ")){
			return Initiale +".___._._._";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(Initiale.equals("") ? "Ø"  : Initiale);
		sb.append(".");
		sb.append(Mediane);
		sb.append(".");
		sb.append(Finale.equals("") ? "Ø"  : Finale);
		sb.append(".");
		sb.append(TonEntrant.equals("") ? "_"  : TonEntrant);
		sb.append(".");
		sb.append((Ton == 0 || !fuzzy) ? "_"  : String.valueOf(Ton));
		return sb.toString();
	}
	
	
	
	
	public static ArrayList<TaigiSyl> parseBopomo(String bopomo){
		bopomo = bopomo.replace("ㄢ", "ㄚㄣ").replace("ㄞ","ㄚㄧ").replace("ㄠ","ㄚㄨ").replace("ㄤ","ㄚㄥ");
		ArrayList<TaigiSyl> result = parseString(mPatternBopomoSyl, bopomo, true);
		return result;
	}
	
	public static ArrayList<TaigiSyl> parseTRS(String trs){
		ArrayList<TaigiSyl> result = parseString(mPatternTRSSyl, trs, false);
		return result;
	}
	
	private static TaigiSyl IPA_of_TRS(TaigiSyl syl){
		
		if((syl.Mediane.startsWith("i")   || 
			syl.Mediane.startsWith("Øi")  ||
			syl.Mediane.startsWith("ØØi") ) && 
		   (syl.Initiale.startsWith("j") || 
			syl.Initiale.startsWith("s") || syl.Initiale.startsWith("ts") ))
				syl.Initiale += "i";
		syl.Initiale = syl.Initiale.replace("tsi","tɕ")
				.replace("ji","ʑ")
				.replace("si","ɕ")
				.replace("tshi","tɕʰ")
				.replace("ts","ts")
				.replace("kh","kʰ")
				.replace("ng","ŋ")
				.replace("j","dz")
				.replace("g","g")
				.replace("ph","pʰ")
				.replace("b","b")
				.replace("h","h")
				.replace("k","k")
				.replace("m","m")
				.replace("l","l")
				.replace("th","tʰ")
				.replace("n","n")
				.replace("p","p")
				.replace("s","s")
				.replace("t","t")
				.replace("tsh","tsʰ");
		
		if(syl.Mediane.endsWith("o") && 
				(syl.Finale != "" || syl.TonEntrant.equals("p") || syl.TonEntrant.equals("t") || syl.TonEntrant.equals("k")))
			syl.Mediane += "o";
		syl.setMediane(syl.Mediane.replace("ng","ŋ")
		.replace("unn","ũ")
		.replace("ann","ã")
		.replace("m","m")
		.replace("inn","ĩ")
		.replace("enn","ẽ")
		.replace("onn","ɔ̃")
		.replace("oo","ɔ"));
		
		syl.TonEntrant = syl.TonEntrant
		.replace("t","t")
		.replace("h","ʔ")
		.replace("k","k")
		.replace("p","p");
		
		syl.Finale = syl.Finale
		.replace("ng","ŋ")
		.replace("m","m")
		.replace("n","n");
	
	return syl;
	}
	
	private static TaigiSyl IPA_of_Bopomo(TaigiSyl syl){
		syl.Initiale = syl.Initiale.replace("ㄐ","tɕ")
	     .replace("ㆢ","ʑ")
	     .replace("ㄗ","ts")
	     .replace("ㄎ","kʰ")
	     .replace("ㄫ","ŋ")
	     .replace("ㆡ","dz")
	     .replace("ㆣ","g")
	     .replace("ㄆ","pʰ")
	     .replace("ㆠ","b")
	     .replace("ㄑ","tɕʰ")
	     .replace("ㄏ","h")
	     .replace("ㄍ","k")
	     .replace("ㄇ","m")
	     .replace("ㄌ","l")
	     .replace("ㄊ","tʰ")
	     .replace("ㄋ","n")
	     .replace("ㄅ","p")
	     .replace("ㄙ","s")
	     .replace("ㄉ","t")
	     .replace("ㄒ","ɕ")
	     .replace("ㄘ","tsʰ")
	     .replace("ㄥ","ŋ");

		syl.TonEntrant = syl.TonEntrant.replace("ㆵ","t")
	     .replace("ㆷ","ʔ")
	     .replace("ㆶ","k")
	     .replace("ㆴ","p")
	     .replace("ㄅ","p")
	     .replace("ㄉ","t")
	     .replace("ㄍ","k")
	     .replace("ㄏ","ʔ");

	    syl.Finale = syl.Finale.replace("ㄥ","ŋ")
	     .replace("ㄇ","m")
	     .replace("ㄣ","n");
	    
	    syl.setMediane(syl.Mediane.replace("ㆭ","ŋ")
	    .replace("ㄥ","ŋ")
	    .replace("ㄜ°","ɔ̃")
	    .replace("ㄛ°","ɔ̃")
	    .replace("ㆧ","ɔ̃")
	    .replace("ㆥ","ẽ")
	    .replace("ㆧ","ɔ̃")
	    .replace("ㄛ","ɔ")
	    .replace("ㄨ°","ũ")
	    .replace("ㆫ","ũ")
	    .replace("ㄚ°","ã")
	    .replace("ㆩ","ã")
	    .replace("ㄚ","a")
	    .replace("ㆤ","e")
	    .replace("ㄝ","e")
	    .replace("ㄧ°","ĩ")
	    .replace("ㆪ","ĩ")
	    .replace("ㄇ","m")
	    .replace("ㄜ","o")
	    .replace("ㄧ","i")
	    .replace("ㄨ","u"));

		
		return syl;
	}
	
	
	private static ArrayList<TaigiSyl> parseString(Pattern pat, String input, boolean isBopomo){
		Matcher m = pat.matcher(input);
    	ArrayList<TaigiSyl> result = new ArrayList<TaigiSyl>();
    	while(m.find()){
    		TaigiSyl syl = new TaigiSyl(m.group(0));
    	    int ng = m.groupCount();
    		if(m.group(G_INITIALE_SEULE) != null){
    			syl.setInitiale(m.group(G_INITIALE_SEULE));
    		}
    		else {
    		    for(int i=1;i<ng;i++){
    		    	if(m.group(i)!=null) {
    		    		switch(i){
    		    		case G_INITIALE: syl.setInitiale(m.group(i));
    		    		break;
    		    		case G_VOYELLE: syl.setMediane(m.group(i));
    		    		break;
    		    		case G_FINALE: syl.setFinale(m.group(i));
    		    		break;
    		    		case G_TON: syl.setTon(Integer.valueOf(m.group(i)));
    		    		break;
    		    		case G_ENTRANT: syl.setTonEntrant(m.group(i));
    		    		}
    		    	}
    		    }
    		}
    		if(isBopomo)
    			syl = IPA_of_Bopomo(syl);
    		else
    			syl = IPA_of_TRS(syl);
    		result.add(syl);
    	}
		return result;
	}
	

}
