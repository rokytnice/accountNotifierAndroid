package org.rochlitz.hbci.tests.apps;

import org.rochlitz.hbci.callback.DialogBean;
import org.rochlitz.hbci.callback.DialogBean.DialogTyp;

import android.util.Log;

public class DialogBuffer {
	
	
	TestUmsatzAbrufen kausz;
	StringBuilder dialogOutput=new StringBuilder();
	String userInput;
	boolean inputFinished = false;
	
	public DialogBuffer(TestUmsatzAbrufen kausz){
		this.kausz = kausz;
	}
	
	public String readLine(){
		this.inputFinished=false;
		
		kausz.doPublishProgress();

		while (!this.isInputFinished()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Log.d("giroNotifier",e.getLocalizedMessage(),e);
			}
		}

		dialogOutput=new StringBuilder();
		return this.userInput;
	}
	
	private boolean isInputFinished() {
		return this.inputFinished;
	}

	public void println(String line){
		dialogOutput.append(line);
	}

	public void println(StringBuffer line){
		dialogOutput.append(line);
	}

	
	public void flush(){
		
	}

	public void print(String line) {
		 println(line);
	}

	public TestUmsatzAbrufen getKausz() {
		return kausz;
	}

	public void setKausz(TestUmsatzAbrufen kausz) {
		this.kausz = kausz;
	}

	public StringBuilder getDialogOutput() {
		return dialogOutput;
	}

	public void setDialogOutput(StringBuilder dialogOutput) {
		this.dialogOutput = dialogOutput;
	}

	public String getUserInput() {
		return userInput;
	}

	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}

	public void setInputFinished(boolean inputFinished) {
		this.inputFinished = inputFinished;
	}
}
