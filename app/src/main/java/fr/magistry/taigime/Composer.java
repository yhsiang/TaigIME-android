/**
 * 
 */
package fr.magistry.taigime;

import java.util.ArrayList;  

/**
 * @author pierre
 *
 */
public class Composer {
	private CandidateView mCandidateView;
	private TaigIMEService mIMS;
	private Converter mDict;
	private StringBuffer mRawInput;
	private ArrayList<TaigiSyl> mAnalyzedInput;
	private ArrayList<Candidate> mSelection;
	private ArrayList<Candidate> mCandidateList;
	
	public Composer(CandidateView cv, TaigIMEService ims){
		mCandidateView = cv;
		mIMS = ims;
		mDict = new Converter(ims.getBaseContext());
		mRawInput = new StringBuffer();
		mAnalyzedInput = new ArrayList<TaigiSyl>();
		mSelection = new ArrayList<Candidate>();
		mCandidateList = new ArrayList<Candidate>();
	}
	
	
	public void push(String letter){
		if(letter == "-" && mRawInput.length() == 0){
			mIMS.getCurrentInputConnection().commitText("-", 1);
			return;
		}
		mRawInput.append(letter);
		inputHasChanged();
		mIMS.setCandidatesViewShown(true);
	}
	
	public boolean delete(){
		int s = mSelection.size();
		if(s > 0){
			Candidate lastc = mSelection.remove(s-1);
			mRawInput.insert(0,lastc.getInputed());
			selectionHasChanged();
			inputHasChanged();
			return true;
		}
		else {
			int len = mRawInput.length();
			if (len>0) {
				mRawInput.deleteCharAt(len-1);
				inputHasChanged();
				return true;
			}	
			else {
				return false;
				//mIMS.getCurrentInputConnection().deleteSurroundingText(1, 0);
			}
		}
	}
	
	public void pickSuggestion(int index){
		if(index == 0){
			mIMS.getCurrentInputConnection().setComposingText("",0);
			mIMS.getCurrentInputConnection().commitText(mRawInput, 1);
			mRawInput = new StringBuffer();
			mSelection.clear();
			mCandidateList.clear();
			inputHasChanged();
			selectionHasChanged();
		}
		else{
			index--; // l'index de la view est décallé par la case rawInput
			if(mCandidateList.size() > index) {
				Candidate choosen = mCandidateList.get(index);
				String inputed = choosen.getInputed();
				mSelection.add(choosen);
				mRawInput.delete(0, inputed.length());
				selectionHasChanged();
				inputHasChanged();
			}
		}
		
	}
	
	public boolean accept(){
		if(mRawInput.length() > 0){
			this.pickSuggestion(mCandidateView.getSelectedIndex());
			return true;
		}
		if(mSelection.size() >0){
			recordUse(false);
			mIMS.getCurrentInputConnection().setComposingText("",1);
			mIMS.getCurrentInputConnection().commitText(buildSelectionString(), 1);
			mSelection.clear();
			return true;
		}
		
		return false;
	}
	
	
	private void selectionHasChanged(){
		mIMS.getCurrentInputConnection().setComposingText(buildSelectionString(),1);
		if( mRawInput.length() ==0)
			accept();
	}
	
	public CharSequence getSelectionString(){
		return buildSelectionString();
	}
	
	private CharSequence buildSelectionString() {
		StringBuffer result = new StringBuffer();
		for(Candidate c : mSelection){
			TaigiWord w = c.getWord();
			if(mCandidateView.isOutputTRS())
				result.append(w.getTailuo());
			else
				result.append(w.getHanji());
		}
		return result.toString();
	}

	private CharSequence buildSelectionStringTL() {
		StringBuffer result = new StringBuffer();
		for(Candidate c : mSelection){
			TaigiWord w = c.getWord();
			result.append(w.getTailuo());
			result.append("-");
		}
		int l = result.length();
		if(l > 0)
			result.deleteCharAt(l-1);
		return result.toString();
	}

	private void recordUse(boolean TL){
		StringBuffer bopomo = new StringBuffer();
		StringBuffer hanji = new StringBuffer();
		StringBuffer tailo = new StringBuffer();
		int count = 0;
		for(Candidate c : mSelection){
			TaigiWord w = c.getWord();
			mDict.recordUse(w.getWid(), w.getBopomo(), w.getHanji(), w.getTailuo(), TL);
			bopomo.append(w.getBopomo());
			bopomo.append("-");
			hanji.append(w.getHanji());
			tailo.append(w.getTailuo());
			tailo.append("-");
			count++;
		}
		int lb = bopomo.length();
		if(lb > 0){
			bopomo.deleteCharAt(lb-1);
		}
		int lt = tailo.length();
		if(lt > 0){
			tailo.deleteCharAt(lt-1);
		}
		if (count > 1)
			mDict.recordUse(-1, bopomo.toString(), hanji.toString(), tailo.toString(), TL);
	}
	
	private void inputHasChanged(){
		if(mIMS.isTailoKeyboard())
			mAnalyzedInput = TaigiSyl.parseTRS(mRawInput.toString());
		else
			mAnalyzedInput = TaigiSyl.parseBopomo(mRawInput.toString());
		buildCandidateList();
		mCandidateView.setSuggestions(Candidate.buildSuggestions(mRawInput.toString(), mCandidateList), true, true);
		selectionHasChanged();
	}
	
	private void buildCandidateList(){
		mCandidateList.clear();
		if (mAnalyzedInput.size() == 0){
			return;
		}
		String rawInput = mRawInput.toString();
		ArrayList<TaigiWord> entries = mDict.getCandidats(mAnalyzedInput);
		if(entries.size()>0){
			
			for(TaigiWord w :entries){
				mCandidateList.add(new Candidate(rawInput,w));
			}
		}
		if (entries.size() == 0 || mAnalyzedInput.size() > 1){
			ArrayList<TaigiSyl> minilist = new ArrayList<TaigiSyl>(); 
			minilist.add(mAnalyzedInput.get(0));
			entries = mDict.getCandidats(minilist);
			if(entries.size()>0){
				for(TaigiWord w :entries){
					mCandidateList.add(new Candidate(minilist.get(0).getInputed(),w));
				}
			}
		}
		
	}


	public void setCandidateView(CandidateView cv) {
		mCandidateView = cv;
		
	}

 
	public boolean acceptTL() {
		if(mSelection.size() >0){
			recordUse(true);
			mIMS.getCurrentInputConnection().setComposingText("",1);
			mIMS.getCurrentInputConnection().commitText(buildSelectionStringTL(), 1);
			mSelection.clear();
			return true;
		}
		return false;
		
	}


	public void flush() {
		if(mCandidateList.size() >1){
			pickSuggestion(1);
		}
		accept();
	}


	public void close() {
		mDict.close();
		this.purge();
	}


	public void purge() {
		mCandidateList.clear();
		mAnalyzedInput.clear();
		mRawInput = new StringBuffer();
		mSelection.clear();
		
	}

}
