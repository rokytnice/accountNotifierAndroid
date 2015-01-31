package org.rochlitz.hbci.callback;

public class DialogBean {
	
	final public DialogTyp dialog;
	private String dialogOutPutText;
	private String userInput;
	private boolean inputFinished = false;
	private boolean inputDialog;
	
	
	public DialogBean(DialogTyp dialog, String dialogOutPutText, boolean inputFinished,
			String userInput, boolean input) {
		super();
		this.dialog = dialog;
		this.dialogOutPutText = dialogOutPutText;
		this.userInput = userInput;
		this.inputFinished = inputFinished;
		this.setInputDialog(input);
	}


	
	public DialogTyp getDialog() {
		return dialog;
	}



	public String getDialogOutPutText() {
		return dialogOutPutText;
	}



	public void setDialogOutPutText(String dialogOutPutText) {
		this.dialogOutPutText = dialogOutPutText;
	}



	public String getUserInput() {
		return userInput;
	}


	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}


	public boolean isInputFinished() {
		return inputFinished;
	}


	public void setInputFinished(boolean inputFinished) {
		this.inputFinished = inputFinished;
	}


	
	
	
	public boolean isInputDialog() {
		return inputDialog;
	}



	public void setInputDialog(boolean inputDialog) {
		this.inputDialog = inputDialog;
	}





	public enum DialogTyp {ENTERSECRET,ENTERBLZ}

}
