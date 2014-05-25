package fr.magistry.taigime;


//import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
//import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
//import android.util.Log;
//import fr.magistry.taigime.R;
/**
 * 
 */

/**
 * @author pierre
 *
 */





public class TaigIMEService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

	private Keyboard mBopomoKeyboard;
	private Keyboard mSmallKeyboard;
	private Keyboard mTailuoKeyboard;
	private int mCurrentKeyboard = -1;
	private Keyboard mSymbolsKeyboard;
	private Keyboard[] mKeyboards; 
    private int mLastDisplayWidth;
	private KeyboardView mInputView;
	private CandidateView mCandidateView;
	//private Converter mConverter;
	private boolean mCurrentIsSmall = false; 
	private boolean mCurrentIsSymbols = false;
	private Typeface mFont;
	//public TaigIMEService() {
		// TODO Auto-generated constructor stub
	//}
	private StringBuilder mComposing = new StringBuilder();
    private Composer mComposer;
	/**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        if (mCurrentKeyboard != -1) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
//        if (mConverter == null){
//        	mConverter = new Converter(getBaseContext());
//        }
        mCurrentIsSmall = getSharedPreferences("TAIGI_IME", 0).getBoolean("small", false);
        mBopomoKeyboard = new Keyboard(this, R.xml.bopomo);
        mSmallKeyboard = new Keyboard(this, R.xml.bopomo12);
        mSymbolsKeyboard = new Keyboard(this, R.xml.symbols);
        mTailuoKeyboard = new Keyboard(this, R.xml.qwerty);
        mKeyboards = new Keyboard[] {mBopomoKeyboard, mSmallKeyboard, mTailuoKeyboard};
        mCurrentKeyboard = mCurrentIsSmall ? 1 : 0 ;
        getSharedPreferences("TAIGI_IME", 0);
        getApplicationContext();
        
        mFont = Typeface.createFromAsset(getAssets(), "fonts/bpm.ttf"); 
        for(Key k :mBopomoKeyboard.getKeys()){
        	if(k.label != null){
        		
        		CustomKeyIcon icon = new CustomKeyIcon(k,mFont);
        		icon.setTypeface(mFont);
        		k.label = null;
        		k.icon = icon;
        		
        	}
        }
        for(Key k :mSmallKeyboard.getKeys()){
            	if(k.label != null){
            		CustomKeyIcon icon = new CustomKeyIcon(k,mFont);
            		icon.setTypeface(mFont);
            		k.label = null;
            		k.icon = icon;
            		
            	}
        } 
        if(mCandidateView == null){
        	mCandidateView = new CandidateView(getBaseContext());
        	mCandidateView.setService(this);
            mCandidateView.setTypeface(mFont);
             
        }
        mComposer = new Composer(mCandidateView,this);
        
    }
	
	
	/**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() {
        mInputView = new KeyboardView(getBaseContext(), null);
        mInputView.setOnKeyboardActionListener((OnKeyboardActionListener) this);
        mInputView.setKeyboard(mKeyboards[mCurrentKeyboard]);
        mInputView.setPreviewEnabled(true);
        if(mCandidateView == null){
        	mCandidateView = new CandidateView(getBaseContext());
        	mCandidateView.setService(this);
            mCandidateView.setTypeface(mFont);
        }
        mComposer = new Composer(mCandidateView,this);
        
        if(mComposing.length() > 0){
        	getCurrentInputConnection().setComposingText(mComposing,1);
        }
        
        return mInputView;
    }

    @Override 
    public View onCreateCandidatesView (){
    	mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        mCandidateView.setTypeface(mFont);
        if (mComposer == null){
        	mComposer = new Composer(mCandidateView, this);
        }
        else {
        	mComposer.setCandidateView(mCandidateView);
        }
        mCandidateView.setComposer(mComposer);
        return mCandidateView;
    }

//    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
//        if (completions != null) {
//            mCompletions = completions;
//            if (false && completions == null) {
//                setSuggestions(null, false, false);
//                return;
//            }
//            
//            String[] suggestions = mConverter.getSuggestions(mComposing.toString());            
//            ArrayList<String> test = new ArrayList<String>(Arrays.asList(suggestions));
//            List<String> stringList = new ArrayList<String>();
//            for (int i=0; i<(completions != null ? completions.length : 0); i++) {
//                CompletionInfo ci = completions[i];
//                if (ci != null) stringList.add(ci.getText().toString());
//            }
//            setSuggestions(test, true, true);
//        }
//    }
    
//    private void buildSuggestions(){
//    	// if(mComposing.length() == 0)
//    	//	 return;
//    	 String[] completions = mConverter.getSuggestions(mComposing.toString());
//    	 int count = completions.length;
//         ArrayList<String> stringList = new ArrayList<String>();
//         mCompletions = new CompletionInfo[count];
//         for (int i=0; i< completions.length; i++) {
//             CompletionInfo ci = new CompletionInfo((long)i,i,completions[i]);
//             mCompletions[i] = ci;
//             if (ci != null) stringList.add(completions[i]);
//         }
//         Log.d("show", stringList.get(0));
//         setSuggestions(stringList, true, true);
//    }
//    
/*    public void setSuggestions(ArrayList<String> suggestions, boolean completions,
            boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }
  */  
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// TODO Auto-generated method stub
		if(mCurrentIsSymbols && primaryCode > 0){
			getCurrentInputConnection().commitText(String.valueOf((char)primaryCode), 1);
			return;
		}
		switch(primaryCode){
		case -42: //Community
			Community();
			break;
		case 10 : //return
			if(!mComposer.accept()){
				this.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			}
			break; 
		case -20: //TL key
			//mComposer.acceptTL();
			mCandidateView.setOutputTRS(!mCandidateView.isOutputTRS());
			mCandidateView.invalidate(); 
			break;
		case -10: //dictLookup
			//TODO
			lookupDict(mComposer.getSelectionString());
			return;
		case 32 : //space
			if(!mComposer.accept()){
				commit(" ");
			}
			break;
		case -5: //del 
			if(!mComposer.delete()){
				getCurrentInputConnection().deleteSurroundingText(1, 0);
			}
			break;
		case -3: // changement de clavier
			mCurrentKeyboard += 1;
			if(mCurrentKeyboard == mKeyboards.length)
				mCurrentKeyboard = 0;
			mCurrentIsSymbols = false;
			mInputView.setKeyboard(mKeyboards[mCurrentKeyboard]);
			mComposing.setLength(0);
			break;
		case -2: //chiffres et symboles 
			mCurrentIsSymbols = !mCurrentIsSymbols;
			if(mCurrentIsSymbols)
				mInputView.setKeyboard(mSymbolsKeyboard);
			else 
				mInputView.setKeyboard(mKeyboards[mCurrentKeyboard]);
			mComposing.setLength(0);
			break;			
		default :
				if((primaryCode == 65292 || primaryCode == 12290 || primaryCode == 65311 || primaryCode == 65281 || primaryCode == 46 || primaryCode == 44) ){
					//ponctuation
					mComposer.flush();
					commit(String.valueOf((char)primaryCode));
					
				}
				else {
					mComposer.push(String.valueOf((char)primaryCode));
					//mComposing.append(convertBopomo(primaryCode));
				}
			break;
		}
		//if(mComposing.length() != 0)
			//getCurrentInputConnection().setComposingText(mComposing, 1);
		
		//buildSuggestions();		
	}

	private void Community() {
		// TODO Auto-generated method stub
		//http://twblg.dict.edu.tw/holodict_new/result.jsp?radiobutton=0&limit=20&querytarget=1&sample=%E1%B8%BF&submit.x=20&submit.y=20
		//String url = "http://taigime.magistry.fr/community/";
		//Intent i = new Intent(Intent.ACTION_VIEW);
		//i.setData(Uri.parse(url));
		Intent i = new Intent(this,CommunityActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		getBaseContext().startActivity(i); 
		
	}


	private void commit(String text){
    	getCurrentInputConnection().commitText(text, 1);
    }
	@Override
	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub
	
		
	}


	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub

		
	}


	@Override
	public void onText(CharSequence text) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub
		
	}


//	public void pickSuggestionManually(int index) {
//		// TODO Auto-generated method stub
//		if (index < 0 ){
//			index = mCandidateView.getSelectedIndex();
//		}
//		if (mCompletions.length > index){
//			CompletionInfo ci = mCompletions[index+1];		
//			String selection = (String) ci.getText();
//			if(mDictLookup){
//				
//				getCurrentInputConnection().setComposingText("",1);
//				lookupDict(selection);
//				return;
//			}
//			getCurrentInputConnection().commitText(selection, 1);
//			if (mComposing.length() > 0) {
//				mComposing.setLength(0);
//				mComposing.append(mCandidateView.getUnusedSuffix());
//				//if(mComposing.length() != 0)
//					//getCurrentInputConnection().setComposingText(mComposing, 1);
//				buildSuggestions();
//			}
//		}
//		
//	}
	private boolean isAppInstalled(String uri) {
		 PackageManager pm = getPackageManager();
		 boolean installed = false;
		 try {
		 pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
		 installed = true;
		 } catch (PackageManager.NameNotFoundException e) {
		 installed = false;
		 }
		 return installed;
		 }
	
	private void lookupDict(CharSequence word){
		//http://twblg.dict.edu.tw/holodict_new/result.jsp?radiobutton=0&limit=20&querytarget=1&sample=%E1%B8%BF&submit.x=20&submit.y=20
		if(isAppInstalled("org.audreyt.dict.moe"))
		{
		    Intent nextIntent = new Intent(Intent.ACTION_MAIN);
		    nextIntent.setComponent(new ComponentName("org.audreyt.dict.moe","org.audreyt.dict.moe.MoeDict"));
		    nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    nextIntent.putExtra("key", "'"+word);
		    startActivity(nextIntent);
		    mComposer.purge();
		    return;
		}
		if(word.length() == 0)
			return;
		mComposer.purge();
		Toast toast = Toast.makeText(getBaseContext(), "查辭典「"+ word + "」", Toast.LENGTH_LONG);
		toast.show();
		String url = "http://twblg.dict.edu.tw/holodict_new/result.jsp?radiobutton=0&limit=20&querytarget=1&sample=" + word + "&submit.x=20&submit.y=20";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if( event.getKeyCode() == KeyEvent.KEYCODE_DEL){
			onKey(-5,new int[] {-5} );
			return true;
		}
		if( event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			if(event.isAltPressed()) //TL
				onKey(-20,new int[] {-20});
			else
				onKey(10,new int[] {10} );
			return true;
		}
		if( event.getKeyCode() == KeyEvent.KEYCODE_SPACE){
			if(event.isAltPressed()) //Dict
				onKey(-10,new int[] {-10});
			else
				onKey(32,new int[] {32} );
			return true;
		}
		if( event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
			if(mCandidateView.prevSelectedIndex()){
        		mCandidateView.invalidate();
			    return true;
			}
			return false;
		}
        if( event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
        	if(mCandidateView.nextSelectedIndex()){
        		mCandidateView.invalidate();
        		return true;
        	}
        	return false;
		}
		char c = event.getDisplayLabel();
		int key = 0;
		switch(c){
		case '1': key = 12549; break; //ㄅ
		case '2': key = 12553; break; //ㄉ
		case '3': key = 12557; break; // ㄍ
		case '-': key = 45; break;// -
		case '7': key = 176; break;// °
		case '8': key = 12570; break;// ㄚ
		case '9': key = 12574; break;// ㄞ
		case '0': key = 12578; break;// ㄢ
		case 'Q': key = 12550; break;// ㄆ
		case 'W': key = 12554; break;// ㄊ
		case 'D': key = 12558; break;// ㄎ
		case 'R': key = 12560; break;// ㄐ
		case 'G': key = 12581; break;// ㄥ
		case 'Y': key = 12567; break;// ㄗ
		case 'U': key = 12583; break;// ㄧ
		case 'I': key = 12571; break;// ㄛ
		case 'P': key = 12579; break;// ㄣ
		case 'A': key = 12704; break;// ㄅ
		case 'S': key = 12555; break;// ㄋ
		case 'E': key = 12707; break;// ㄍ
		case 'F': key = 12561; break;// ㄑ
		case 'H': key = 12568; break;// ㄘ
		case 'J': key = 12584; break;// ㄨ
		case 'K': key = 12572; break;// ㄜ
		case 'L': key = 12576; break;// ㄠ
		case ';': key = 12580; break;// ㄤ
		case 'Z': key = 12551; break;// ㄇ
		case 'X': key = 12556; break;// ㄌ
		case 'C': key = 12559; break;// ㄏ
		case 'V': key = 12562; break;// ㄒ
		case 'B': key = 12566; break;// ㄖ
		case 'N': key = 12569; break;// ㄙ
		case ',': key = 12573; break;// ㄝ
		case '/': key = 12581; break;// ㄥ
		}
		if(key == 0) {
			return false;
		}
		onKey(key,new int[] {key} );
		return true; 
	}
	@Override
	public void onDestroy(){
		mComposing.setLength(0);
		//mConverter.close();
		mComposer.close();
		super.onDestroy();
	}
 
	public boolean isTailoKeyboard(){
		return (mCurrentKeyboard==2);
	}
} 
 