package fr.magistry.taigime;

import android.os.Bundle;
import android.preference.CheckBoxPreference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;


public class OptionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
		SharedPreferences settings = getSharedPreferences("TAIGI_IME", 0);
	   // boolean bTailuo = settings.getBoolean("small", false);
	    //boolean bFuzzy = settings.getBoolean("fuzzy", true);
	    /*CheckBox cbSize = (CheckBox) this.getWindow().findViewById(R.id.options_smallsize_cb);
	    CheckBox cbFuzzy = (CheckBox) this.getWindow().findViewById(R.id.options_fuzzy_ru_cb);
	    cbSize.setChecked(bTailuo);
	    cbFuzzy.setChecked(bFuzzy);
	    cbSize.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	    	public void onCheckedChanged(CompoundButton cb,boolean state){
	    		getSharedPreferences("TAIGI_IME", 0).edit().putBoolean("small", state).commit();
	    	}
	    });
	    cbFuzzy.setOnCheckedChangeListener(new OnCheckedChangeListener(){
	    	public void onCheckedChanged(CompoundButton cb,boolean state){
	    		getSharedPreferences("TAIGI_IME", 0).edit().putBoolean("fuzzy", state).commit();
	    	}
	    });
	    */
	    LinearLayout container = (LinearLayout) this.getWindow().findViewById(R.id.LinearLayout1);
	    String[] dialects = getResources().getStringArray(R.array.dialect_list);
	    Context ctxt = container.getContext();
	    for(String d: dialects){
			CheckBox cb = new CheckBox(ctxt);
			cb.setText(d);
			cb.setChecked(settings.getBoolean(d, true));
			cb.setOnCheckedChangeListener(build_listener_from_string(d));
			container.addView(cb);
	    }
	    
	}

	private OnCheckedChangeListener build_listener_from_string(final String s){
		return new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getSharedPreferences("TAIGI_IME",0).edit().putBoolean(s, isChecked).commit();
			}
		};
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_options, menu);
		return true;
	}

}
