/**
 * 
 */
package fr.magistry.taigime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
/**
 * @author pierre
 *
 */
public class Converter extends SQLiteOpenHelper {
	 public static final int DB_VERSION = 32;
	 private static final long PADDING_USERLEX = 100000000;
	 private static final String DATABASE_NAME = "TaigIME";
	 private static final String TABLE_NAME = "convert";
	 private static final String TABLE_CREATE =
	                "CREATE TABLE " + TABLE_NAME + " (" +
	                "Wid INTEGER PRIMARY KEY, " +
	                 "Bopomo TEXT, " +
	                 "Hanji TEXT, " +
	                 "Tailo TEXT, " +
	                 "lieu TEXT, " +
	                 "Freq INT);";
	 private static final String USERTABLE_CREATE =
             "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "_USER (" +
             "Wid INTEGER UNIQUE, " + 
              "Bopomo TEXT, " +
              "Hanji TEXT, " +
              "Tailo TEXT, " +
              "lieu TEXT, " +
              "Freq INT, UNIQUE (Bopomo,Hanji)  ON CONFLICT IGNORE );";
	 private SQLiteStatement mInsertUserStmt_by_wid;
	 private SQLiteStatement mInsertUserStmt_nowid;
	 private SQLiteStatement mIncrementUserStmt_by_wid;
	 private SQLiteStatement mGetMaxWidStmt;
	 
	 private Context mCtxt;
	 private SQLiteDatabase mDB;
	 private boolean mbFuzzy;
	private SharedPreferences mSettings;
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db)  {
		mDB = db;
		db.execSQL(TABLE_CREATE);
		db.execSQL(USERTABLE_CREATE);
		try {
			notifyUpgrade();
			loadData();
		}
		catch( IOException e) {
			//Log.e("IME_DATA", "exception during create..loadData;");
			
		}
		
	}
	
	Converter(Context ctxt){
		super(ctxt,DATABASE_NAME,null, DB_VERSION);
		mCtxt = ctxt;
		mDB = this.getReadableDatabase();
		final Resources resources = mCtxt.getResources();
		resources.getStringArray(R.array.Tailuo);
		resources.getStringArray(R.array.Bopomo);
		new HashSet<String>(Arrays.asList(resources.getStringArray(R.array.Bopomo_initiales)));
		new HashSet<String>(Arrays.asList(resources.getStringArray(R.array.Bopomo_finales)));
		Pattern.compile(resources.getString(R.string.Bopomo_re));
		
		mSettings = mCtxt.getSharedPreferences("TAIGI_IME", 0);
	    mSettings.getBoolean("tailuo", true);
	    
	    mbFuzzy = mSettings.getBoolean("fuzzy", true);
	    
	    mDB.compileStatement("INSERT INTO " + TABLE_NAME + "(Bopomo,Hanji,Tailo,Freq) VALUES (?,?,?,?);");
	    mInsertUserStmt_by_wid = mDB.compileStatement("INSERT OR IGNORE INTO " + TABLE_NAME + "_USER(Wid,Bopomo,Hanji,Tailo,Freq,lieu) SELECT Wid,Bopomo,Hanji,Tailo,0 as Freq,lieu FROM " + TABLE_NAME + " WHERE Wid=?");
	    mIncrementUserStmt_by_wid = mDB.compileStatement("UPDATE " + TABLE_NAME + "_USER SET Freq = Freq + 1 WHERE Wid=?");
	    mInsertUserStmt_nowid = mDB.compileStatement("INSERT INTO " + TABLE_NAME + "_USER (Wid,Bopomo,Hanji,Tailo,Freq,lieu) VALUES (?,?,?,?,1,'user');");
	    mGetMaxWidStmt = mDB.compileStatement("SELECT coalesce(max(Wid),-1) FROM " + TABLE_NAME + "_USER;");
	   // mDB.close();
	}
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		mDB = db;
		//db.delete(TABLE_NAME,null, null);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME +"_USER");
		db.execSQL(TABLE_CREATE);
		db.execSQL(USERTABLE_CREATE);
		try {
			Toast toast = Toast.makeText(mCtxt, "Upgrading Database...", Toast.LENGTH_LONG);
		    toast.show();
		    notifyUpgrade();
			loadData();
		    //onCreate(db);
		}
		catch( IOException e) {
			//Log.e("IME_DATA", "exception during create..loadData;");
		}

	}

	private void notifyUpgrade(){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(mCtxt)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("installing/upgrading TaigIME")
		        .setContentText("This may take some time...");
		NotificationManager mNotificationManager =
			    (NotificationManager) mCtxt.getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setData(Uri.parse("http://plus.google.com/communities/101493598847860391958"));
		PendingIntent pi = PendingIntent.getActivity(mCtxt, 0, i, 0);
		mBuilder.setContentIntent(pi);
		mNotificationManager.notify(12, mBuilder.build());
	}
	
    private void loadData() throws IOException {
        final Resources resources = mCtxt.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.data);
        
        //BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream),64);
        InputStreamReader reader = new InputStreamReader(inputStream);
        InsertHelper ih = new InsertHelper(mDB, TABLE_NAME);
        final int WidCol = ih.getColumnIndex("Wid");
        final int BpmCol = ih.getColumnIndex("Bopomo");
        final int HjCol = ih.getColumnIndex("Hanji");
        final int TlCol = ih.getColumnIndex("Tailo");
        final int Licol = ih.getColumnIndex("lieu");
        final int FrCol = ih.getColumnIndex("Freq");
        try {
        	//mDB.beginTransaction();
        	//mInsertStmt = mDB.compileStatement("INSERT INTO " + TABLE_NAME + "(Wid,Bopomo,Hanji,Tailo,Freq) VALUES (?,?,?,?,?);");
            String[] line;
            String[] strings ;
            char[] buffer = new char[4096];
            int nCharRead = 0;
            int oldPosition = 0;
            while (nCharRead != -1){
            	nCharRead = reader.read(buffer,oldPosition,4096-oldPosition);
            	if(nCharRead == -1){
            		continue;
            	}
            	strings = String.valueOf(buffer,0,oldPosition+nCharRead).split("\n",-1);
            	if(strings.length < 2){
            		oldPosition += nCharRead;
            		continue;
            	}
            	else {
            		for(int i=0;i < strings.length -1;i++){
            			line = TextUtils.split(strings[i], "\t");
            			if (line.length < 3) continue;
                		//long id = addOne(Long.valueOf(line[0]), line[1], line[2], line[4], Integer.valueOf( line[3]));
            			ih.prepareForInsert();
            			ih.bind(WidCol, Long.valueOf(line[0]));
            			ih.bind(BpmCol, line[1]);
            			ih.bind(HjCol, line[2]);
            			ih.bind(FrCol, Integer.valueOf(line[3]));
            			ih.bind(TlCol,line[4]);
            			ih.bind(Licol, line[5]);
            			long id = ih.execute();
                		if (id < 0) {
                			//Log.e("IME_DATA", "unable to add: " + line[0].trim());
                		}
            		}
            		int charindex=0;
            		for(char c : strings[strings.length -1].toCharArray()){
            			buffer[charindex] = c;
            			charindex ++;
            		}            		
            		oldPosition = charindex;
            	}
            }
            strings = String.valueOf(buffer,0,oldPosition).split("\n");
            for(String s : strings){
            	line = s.split("\t");
            	if (line.length < 3) continue;
        		//long id = addOne(Long.valueOf(line[0]), line[1], line[2], line[4], Integer.valueOf( line[3]));
        		ih.prepareForInsert();
    			ih.bind(WidCol, Long.valueOf(line[0]));
    			ih.bind(BpmCol, line[1]);
    			ih.bind(HjCol, line[2]);
    			ih.bind(FrCol, Integer.valueOf(line[3]));
    			ih.bind(TlCol,line[4]);
    			ih.bind(Licol, line[5]);
    			long id = ih.execute();
        		if (id < 0) {
        			//Log.e("IME_DATA", "unable to add: " + line[0].trim());
        		}
            }
            //mDB.setTransactionSuccessful();
        } finally {
            reader.close();
            ih.close();
            //mDB.endTransaction();
            mDB.setLockingEnabled(true);
        }
    }
    public void recordUse(long wid, String bopomo, String hanji, String tailo, boolean inTL){
    //	mDB = this.getReadableDatabase();
    	if(wid>=0){ 
    		//mot du lexique officiel OU existant (on a un Wid)
    		mInsertUserStmt_by_wid.bindLong(1, wid);
    		mInsertUserStmt_by_wid.execute();
    		mIncrementUserStmt_by_wid.bindLong(1, wid);
    		mIncrementUserStmt_by_wid.execute();
    	}
    	else {
    		//nouveau mot
    		//(Wid,Bopomo,Hanji,Tailo)
    		wid = mGetMaxWidStmt.simpleQueryForLong();
    		if (wid < 100000000){ 
    			//pas encore de mot utilisateur
    			wid = PADDING_USERLEX;
    		}
    		else
    			wid++;
    		mInsertUserStmt_nowid.bindLong(1, wid);
    		mInsertUserStmt_nowid.bindString(2, bopomo );
    		mInsertUserStmt_nowid.bindString(3, hanji);
    		mInsertUserStmt_nowid.bindString(4, tailo);
    		mInsertUserStmt_nowid.execute();
    	
    	}
    //	mDB.close();
    }
    
    
//    public String[] getSuggestions(String bopomo) {
//    	Log.d("inputed", bopomo);
//    	String inputed = new String(bopomo);
//        String selection = "bopomo LIKE ? ";
//        String groupBy = "Hanji";
//        String order = "Freq DESC  LIMIT 200";
//        StringBuilder request; 
//        StringBuilder reste = new StringBuilder();
//        List<String[]> syllabes = build_request_string(bopomo,mbFuzzy,mbFuzzy);
//        for(int limit=syllabes.size()-1;limit>=0;limit--){
//        	request = new StringBuilder();
//        	for(String[] fields : syllabes) {
//        		for(String letter  : fields){
//        			request.append(letter);
//        		}
//        		request.append("-");
//        	}
//        	int reqlength = request.length();
//        	if(reqlength>0)
//        		request.deleteCharAt(reqlength-1);
//        	Log.d("req",request.toString());
//        	String[] selectionArgs = new String[] {request.toString()};//{bopomo+"%"};
//        	Cursor cursor = mDB.query(TABLE_NAME,new String[] {"Hanji"}, selection, selectionArgs, groupBy, null,order);
//        
//        	if(cursor == null) {
//        		String[] last = syllabes.remove(limit);
//        		StringBuilder minibuf = new StringBuilder();
//        		if(reste.length() > 0)
//        			reste.insert(0,"-");
//        		for(int k=0 ;k<last.length;k++ )
//        			minibuf.append(last[k]);
//        		reste.insert(0, minibuf);
//        		continue;
//        	}
//        	int count = cursor.getCount();
//        	if (!cursor.moveToFirst() || count == 0 ) {
//        		String[] last = syllabes.remove(limit);
//        		StringBuilder minibuf = new StringBuilder();
//        		if(reste.length() > 0)
//        			reste.insert(0,"-");
//        		for(int k=0 ;k<last.length;k++ )
//        			minibuf.append(last[k]);
//        		reste.insert(0, minibuf);
//        		cursor.close();
//        		continue;
//        	}
//        	
//        	String[] suggestions = new String[count+2];
//        	for(int i=2;i<count+2;i++){
//        		
//        		suggestions[i] = cursor.getString(0);
//        		if(!cursor.moveToNext()) break;
//        	}
//        	suggestions[0] = reste.toString().replaceAll("Ø", "").replaceAll("_","").replaceAll("%","");
//        	suggestions[1] = inputed;
//        	cursor.close();
//        	Log.d("return many","" );
//        	return suggestions;
//        }
//
//        Log.d("return", "one");
//    	return new String [] {inputed,inputed};//request.toString().replaceAll("Ø","").replaceAll("_","")};
//    	
//    		
//    }
//	public String bopomoToTailuo(String bopomo){
//		StringBuilder tl = new StringBuilder();
//		bopomo = bopomo.replace("ㄢ", "ㄚㄣ").replace("ㄞ","ㄚㄧ").replace("ㄠ","ㄚㄨ").replace("ㄤ","ㄚㄥ");
//		for(String[] syl : decomposeSyl(bopomo,false,false)){
//			for(String phon : syl) {
//				for(int i=0;i<phon.length();i++){
//					for(int j=0;j<mBopomo.length;j++){
//						if(phon.startsWith(mBopomo[j], i)){
//							tl.append(mTailuo[j]);
//							if(mBopomo[j].length() > 1)
//								i++;
//							break;
//						}
//					}				
//				}
//			if( Character.isDigit(syl[syl.length-1].charAt(0))){}
//			}
//			tl.append("-");
//		}
//		int l = tl.length();
//		if(l<2)
//			return "";
//		return tl.substring(0,l-1);
//	}

    private String buildDialectClause(){
    	String[] dialects = mCtxt.getResources().getStringArray(R.array.dialect_list);
    	StringBuffer sb = new StringBuffer("( ");
    	for(String d : dialects){
    		if(mSettings.getBoolean(d, true))
    			sb.append("lieu = '"+d+"' OR " );
    	}
    	sb.append("lieu='STD' or lieu='user' )");
    	return sb.toString();
    }

	public ArrayList<TaigiWord> getCandidats(ArrayList<TaigiSyl> Input) {
		//mDB = this.getReadableDatabase();
        String selection = "bopomo LIKE ? AND "+ buildDialectClause();
        String groupBy = "Hanji";
        String order = "Freq DESC  LIMIT 200";
        StringBuilder request; 
       	request = new StringBuilder();
       	for(TaigiSyl s : Input) {
       		request.append(s.getSqliteString(mbFuzzy));
       		request.append("-");
       	}
       	int reqlength = request.length();
       	if(reqlength>0)
       		request.deleteCharAt(reqlength-1);
       	String[] tables = {(TABLE_NAME + "_USER"), TABLE_NAME};
       	ArrayList<TaigiWord> result = new ArrayList<TaigiWord>();
       	for (String table : tables){
       		String[] selectionArgs = new String[] {request.toString()};//{bopomo+"%"};
       		Cursor cursor = mDB.query(table,new String[] {"Wid","Bopomo","Hanji", "Tailo"}, selection, selectionArgs, groupBy, null,order);
       		if(cursor == null) {
       			//echec de la requete
       			continue;
       			//return result;

       		}
       		int count = cursor.getCount();
       		if (!cursor.moveToFirst() || count == 0 ) {
       			//requete vide
       			cursor.close();
       			continue;
       			//return result;
       		}       	

       		for(int i=0;i<count;i++){
       			TaigiWord w = new TaigiWord(cursor.getLong(0), cursor.getString(1), cursor.getString(2),cursor.getString(3));
       			if( !result.contains(w)){
       			    result.add(w);
       			}
       			if(!cursor.moveToNext()) break;
       		}
       		cursor.close();
       	}
       	//mDB.close();
       	return result;
	}
	
	
	@Override
	public synchronized void close(){
		mDB.close();
	}
	
	
	
}

