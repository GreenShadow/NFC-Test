package com.GreenShadow.nfc_test;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;

public class MainActivity extends Activity {
	private NfcAdapter nfcAdpater;
	private Intent intent=new Intent();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdpater=NfcAdapter.getDefaultAdapter(this);
        if(nfcAdpater==null)
			new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage("您的设备不支持NFC")
				.setPositiveButton("确定", null)
				.show();
        else if(!nfcAdpater.isEnabled())
        	new AlertDialog.Builder(this)
        		.setTitle("错误！")
        		.setMessage("请在设置中打开NFC！")
        		.setPositiveButton("确定", 
        				new DialogInterface.OnClickListener(){
        				@Override
        				public void onClick(DialogInterface dialog, int which){
        					//dialog.dismiss();
        					ComponentName cn = new ComponentName("com.android.settings","com.android.settings.Settings$WirelessSettingsActivity");
			    			intent.setComponent(cn);
			    			intent.setAction("android.intent.action.VIEW");
			    			startActivityForResult(intent,0);
        				}
        		})
        		.show();
    }

    @Override
    public void onPause(){
    	super.onPause();
    	nfcAdpater.disableForegroundDispatch(this);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
	    if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
	    	processIntent(getIntent());
	   	else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction()))
	   		processIntent(getIntent());
	   	else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()))
    		processIntent(getIntent());
    	//nfcAdpater.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListArray);
    }

  //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);  
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private void processIntent(Intent intent){
    	Tag tagFromIntent=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    	TextView textView=(TextView)findViewById(R.id.textView);
    	for (String tech : tagFromIntent.getTechList())
    		textView.append("\n"+tech);
            //System.out.println(tech);
    	boolean auth = false;
    	MifareClassic mfc = MifareClassic.get(tagFromIntent);
    	try{
    		String metaInfo = "";  
            mfc.connect();  
            int type = mfc.getType();//获取TAG的类型  
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数  
            String typeS = "";
            switch (type) {
            case MifareClassic.TYPE_CLASSIC:  
                typeS = "TYPE_CLASSIC";  
                break;
            case MifareClassic.TYPE_PLUS:  
                typeS = "TYPE_PLUS";  
                break;  
            case MifareClassic.TYPE_PRO:  
                typeS = "TYPE_PRO";  
                break;  
            case MifareClassic.TYPE_UNKNOWN:  
                typeS = "TYPE_UNKNOWN";  
                break;  
            }  
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"  
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";  
            for (int j = 0; j < sectorCount; j++) {  
                //Authenticate a sector with key A.  
                auth = mfc.authenticateSectorWithKeyA(j,  
                        MifareClassic.KEY_DEFAULT);  
                int bCount;  
                int bIndex;  
                if (auth) {  
                    metaInfo += "Sector " + j + ":验证成功\n";  
                    // 读取扇区中的块  
                    bCount = mfc.getBlockCountInSector(j);  
                    bIndex = mfc.sectorToBlock(j);  
                    for (int i = 0; i < bCount; i++) {  
                        byte[] data = mfc.readBlock(bIndex);  
                        metaInfo += "Block " + bIndex + " : "  
                                + bytesToHexString(data) + "\n";  
                        bIndex++;  
                    }  
                } else {  
                    metaInfo += "Sector " + j + ":验证失败\n";  
                }  
            }
            textView.append(metaInfo);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
}