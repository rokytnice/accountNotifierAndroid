/*  $Id: HBCICallbackIOStreams.java 136 2009-07-25 12:09:24Z kleiner $

 This file is part of HBCI4Java
 Copyright (C) 2001-2008  Stefan Palme

 HBCI4Java is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 HBCI4Java is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.rochlitz.hbci.callback;

import java.util.Date;
import java.util.StringTokenizer;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.InvalidUserDataException;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.manager.LogFilter;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.INILetter;
import org.kapott.hbci.status.HBCIMsgStatus;
import org.rochlitz.hbci.callback.DialogBean.DialogTyp;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Callback-Klasse für Ein-/Ausgabe über IO-Streams. Dabei handelt es sich eine
 * Callback-Klasse, die Ausgaben auf einem StringBuffer ausgibt und Eingaben
 * über einen BufferedReader liest. Die Klasse
 * {@link org.kapott.hbci.callback.HBCICallbackConsole HBCICallbackConsole} ist
 * eine abgeleitete Klasse, welche STDOUT und STDIN für die beiden I/O-Streams
 * verwendet.
 * 
 * CallbackIOStreams
 */
public class HBCICallbackAndroidInteractive extends AbstractHBCICallback {

	protected HBCIBusiness kausz;
	protected StringBuilder outStream = new StringBuilder();

	/**
	 * Instanz mit vorgegebenem OUT- und INPUT-Stream erzeugen.
	 * 
	 * @param out
	 *            Stream, welcher für die Ausgabe verwendet wird.
	 * @param input
	 *            Stream, der für das Einlesen von Antworten verwendet wird
	 */
	public HBCICallbackAndroidInteractive(TextView out, EditText input) {
	}

	/**
	 * Instanz mit vorgegebenem OUT- und INPUT-Stream erzeugen.
	 * 
	 * @param out
	 *            Stream, welcher für die Ausgabe verwendet wird.
	 * @param input
	 *            Stream, der für das Einlesen von Antworten verwendet wird
	 */
	public HBCICallbackAndroidInteractive() {
		super();
	}

	/** Gibt des INPUT-Stream zurück. */
	// protected EditText getInStream() {
	// return inStream;
	// }
	//
	/** Gibt den verwendeten OUTPUT-Stream zurück. */
	protected StringBuilder getOutStream() {
		return outStream;
	}
	protected void resetOutStream() {
		this.outStream = new StringBuilder();
		
	}

	public HBCICallbackAndroidInteractive(HBCIBusiness testKontoAuszug) {
		this.kausz = testKontoAuszug;
	}

	/**
	 * Schreiben von Logging-Ausgaben in einen <code>StringBuffer</code>. Diese
	 * Methode implementiert die Logging-Schnittstelle des
	 * {@link org.kapott.hbci.callback.HBCICallback}-Interfaces</a>. Die
	 * Log-Informationen, die dieser Methode übergeben werden, werden formatiert
	 * auf dem jeweiligen <code>outStream</code> ausgegeben. In dem ausgegebenen
	 * String sind in enthalten das Log-Level der Message, ein Zeitstempel im
	 * Format "<code>yyyy.MM.dd HH:mm:ss.SSS</code>", die Namen der ThreadGroup
	 * und des Threads, aus dem heraus die Log-Message erzeugt wurde, der
	 * Klassenname der Klasse, welche die Log-Ausgabe erzeugt hat sowie die
	 * eigentliche Log-Message
	 */
	public synchronized void log(String msg, int level, Date date,
			StackTraceElement trace) {
		String line = createDefaultLogLine(msg, level, date, trace);
		getOutStream().append(line);
	}

	/**
	 * Diese Methode reagiert auf alle möglichen Callback-Ursachen. Bei
	 * Callbacks, die nur Informationen an den Anwender übergeben sollen, werden
	 * diese auf dem <code>outStream</code> ausgegeben. Bei Callbacks, die
	 * Aktionen vom Anwender erwarten (Einlegen der Chipkarte), wird eine
	 * entsprechende Aufforderung ausgegeben. Bei Callbacks, die eine Eingabe
	 * vom Nutzer erwarten, wird die entsprechende Eingabeaufforderung
	 * ausgegeben und die Eingabe vom <code>inStream</code> gelesen.
	 */
	public void callback(HBCIPassport passport, int reason, String msg,
			int datatype, StringBuffer retData) {
		
		resetOutStream();
		// TODO AR
		getOutStream().append(HBCIUtilsInternal.getLocMsg("CALLB_PASS_IDENT",passport.getClientData("init")));
		
		try {
			INILetter iniletter;
			LogFilter logfilter = LogFilter.getInstance();
			Date date;
			String st;
			resetOutStream();
			switch (reason) {
			case NEED_PASSPHRASE_LOAD:
			case NEED_PASSPHRASE_SAVE:
				getOutStream().append(msg+": ");
				// //TODO AR getOutStream().flush();

				st=getDialogData(getOutStream().toString(), true);
				if (reason == NEED_PASSPHRASE_SAVE) {
					getOutStream().append(msg+" (again): ");
					// //TODO AR getOutStream().flush();

					String st2=getDialogData(getOutStream().toString(), true);
					if (!st.equals(st2))
					throw new InvalidUserDataException(HBCIUtilsInternal.getLocMsg("EXCMSG_PWDONTMATCH"));
				}
				// TODO AR
				 logfilter.addSecretData(st,"X",LogFilter.FILTER_SECRETS);
				retData.replace(0,retData.length(),st);
				break;

			case NEED_CHIPCARD:
				getOutStream().append(msg);
				break;

			case NEED_HARDPIN:
				getOutStream().append(msg);
				break;

			case NEED_SOFTPIN:
			case NEED_PT_PIN:
			case NEED_PT_TAN:
			case NEED_PROXY_PASS:
				getOutStream().append(msg+": ");
				//getOutStream().flush();
				String secret=getDialogData(getOutStream().toString(), true);
				logfilter.addSecretData(secret,"X",LogFilter.FILTER_SECRETS);
				retData.replace(0,retData.length(),secret);
				break;

			case HAVE_HARDPIN:
				HBCIUtils.log("end of entering hardpin", HBCIUtils.LOG_DEBUG);
				break;

			case HAVE_CHIPCARD:
				HBCIUtils.log("end of waiting for chipcard",
						HBCIUtils.LOG_DEBUG);
				break;

			case NEED_COUNTRY:
			case NEED_BLZ:
			case NEED_HOST:
			case NEED_PORT:
			case NEED_FILTER:
			case NEED_USERID:
			case NEED_CUSTOMERID:
			case NEED_PROXY_USER:

				 getOutStream().append(msg+" ["+retData.toString()+"]: ");
				// //TODO AR getOutStream().flush();
				st = getDialogData(getOutStream().toString(), true);
				if (st.length() == 0)
					st = retData.toString();

				if (reason == NEED_BLZ) {
					logfilter.addSecretData(st, "X", LogFilter.FILTER_MOST);
				} else if (reason == NEED_USERID || reason == NEED_CUSTOMERID
						|| reason == NEED_PROXY_USER) {
					logfilter.addSecretData(st, "X", LogFilter.FILTER_IDS);
				}

				retData.replace(0, retData.length(), st);
				break;

			case NEED_NEW_INST_KEYS_ACK:
				getOutStream().append(msg);
				iniletter = new INILetter(passport, INILetter.TYPE_INST);
				 getOutStream().append(HBCIUtilsInternal.getLocMsg("EXPONENT")+": "+HBCIUtils.data2hex(iniletter.getKeyExponentDisplay()));
                 getOutStream().append(HBCIUtilsInternal.getLocMsg("MODULUS")+": "+HBCIUtils.data2hex(iniletter.getKeyModulusDisplay()));
                 getOutStream().append(HBCIUtilsInternal.getLocMsg("HASH")+": "+HBCIUtils.data2hex(iniletter.getKeyHashDisplay()));
                 getOutStream().append("<ENTER>=OK, \"ERR\"=ERROR: ");
				
				
				  retData.replace(0,retData.length(),st = getDialogData(getOutStream().toString(), true) );
				
				break;

			case HAVE_NEW_MY_KEYS:
				iniletter = new INILetter(passport, INILetter.TYPE_USER);
				date = new Date();
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("DATE")+": "+HBCIUtils.date2StringLocal(date));
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("TIME")+": "+HBCIUtils.time2StringLocal(date));
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("BLZ")+": "+passport.getBLZ());
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("USERID")+": "+passport.getUserId());
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("KEYNUM")+": "+passport.getMyPublicSigKey().num);
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("KEYVERSION")+": "+passport.getMyPublicSigKey().version);
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("EXPONENT")+": "+HBCIUtils.data2hex(iniletter.getKeyExponentDisplay()));
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("MODULUS")+": "+HBCIUtils.data2hex(iniletter.getKeyModulusDisplay()));
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("HASH")+": "+HBCIUtils.data2hex(iniletter.getKeyHashDisplay()));
				getOutStream().append(msg);
				break;

			case HAVE_INST_MSG:
				getOutStream().append(msg);
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("CONTINUE"));
				getDialogData(getOutStream().toString(), false);
				break;

			case NEED_REMOVE_CHIPCARD:
				getOutStream().append(msg);
				break;

			case HAVE_CRC_ERROR:
				getOutStream().append(msg);

				int idx = retData.indexOf("|");
				String blz = retData.substring(0, idx);
				String number = retData.substring(idx + 1);

				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("BLZ")+" ["+blz+"]: ");
				// //TODO AR getOutStream().flush();
				String s=getDialogData(getOutStream().toString(), false);
				if (s.length()==0)
				s=blz;
				blz=s;

				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("ACCNUMBER")+" ["+number+"]: ");
				// //TODO AR getOutStream().flush();
				s=getDialogData(getOutStream().toString(), true);
				if (s.length()==0)
				s=number;
				number=s;

				logfilter.addSecretData(blz, "X", LogFilter.FILTER_MOST);
				logfilter.addSecretData(number, "X", LogFilter.FILTER_IDS);

				retData.replace(0, retData.length(), blz + "|" + number);
				break;

			case HAVE_IBAN_ERROR:
				getOutStream().append(msg);

				String iban = retData.toString();
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("IBAN")+" ["+iban+"]: ");
				// //TODO AR getOutStream().flush();
				 String newiban=getDialogData(getOutStream().toString(), true); if
				  (newiban.length()!=0 && !newiban.equals(iban)) {
				  retData.replace(0,retData.length(),newiban);
				  logfilter.addSecretData(newiban,"X",LogFilter.FILTER_IDS); }
				 
				break;

			case HAVE_ERROR:
				getOutStream().append(msg);
				getOutStream().append("<ENTER>=OK, \"ERR\"=ERROR: ");
				// //TODO AR getOutStream().flush();
				retData.replace(0,retData.length(),st = getDialogData(getOutStream().toString(), true) );
				break;

			case NEED_SIZENTRY_SELECT:
				StringTokenizer tok = new StringTokenizer(retData.toString(),
						"|");
				while (tok.hasMoreTokens()) {
					String entry = tok.nextToken();
					StringTokenizer tok2 = new StringTokenizer(entry, ";");

					String tempblz;
					getOutStream().append(tok2.nextToken()+": "+
					HBCIUtilsInternal.getLocMsg("BLZ")+"="+(tempblz=tok2.nextToken())+
					" ("+HBCIUtils.getNameForBLZ(tempblz)+") "+
					HBCIUtilsInternal.getLocMsg("USERID")+"="+tok2.nextToken());
				}
				getOutStream().append(HBCIUtilsInternal.getLocMsg("CALLB_SELECT_ENTRY")+": ");
				// //TODO AR getOutStream().flush();
				retData.replace(0,retData.length(),st = getDialogData(getOutStream().toString(), true) );
				break;

			case NEED_PT_SECMECH:
				String[] entries = retData.toString().split("\\|");
				int len = entries.length;
				for (int i = 0; i < len; i++) {
					String entry = entries[i];
					String[] values = entry.split(":");

					getOutStream().append(values[0]+": "+values[1]);
				}
				// TODO AR
				getOutStream().append(HBCIUtilsInternal.getLocMsg("CALLB_SELECT_ENTRY")+": ");
				// //TODO AR getOutStream().flush();
				retData.replace(0,retData.length(),st = getDialogData(getOutStream().toString(), true) );
				break;

			case NEED_INFOPOINT_ACK:
				getOutStream().append(msg);
				getOutStream().append(retData);
				// TODO AR
				 getOutStream().append("Press <RETURN> to send this data; enter \"NO\" to NOT send this data: ");
				// //TODO AR getOutStream().flush();
				// TODO AR
				 retData.replace(0,retData.length(),st = getDialogData(getOutStream().toString(), true) );
				 break;

			case NEED_CONNECTION:
			case CLOSE_CONNECTION:
//				 getOutStream().append(msg);
//				// //TODO AR
//				getOutStream().append(HBCIUtilsInternal.getLocMsg("CONTINUE"));
//				getDialogData(getOutStream().toString(), false);
				break;

			default:
				throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg(
						"EXCMSG_CALLB_UNKNOWN", Integer.toString(reason)));
			}
		} catch (Exception e) {
			throw new HBCI_Exception(
					HBCIUtilsInternal.getLocMsg("EXCMSG_CALLB_ERR"), e);
		}
	}

	

	public String getDialogData(String dialogOutput, boolean input)
			throws InterruptedException {
		String result;
		String userInput = "";
		boolean userInputFinish = false;

		DialogBean dBean = new DialogBean(DialogTyp.ENTERSECRET, dialogOutput,
				userInputFinish, userInput,input);

		kausz.setDialogBean(dBean);
		kausz.doPublishProgress();

		while (!dBean.isInputFinished()) {
			Thread.sleep(200);
		}

		result = dBean.getUserInput();
		return result;
	}

	/**
	 * Wird diese Methode von <em>HBCI4Java</em> aufgerufen, so wird der
	 * aktuelle Bearbeitungsschritt (mit evtl. vorhandenen zusätzlichen
	 * Informationen) auf <code>outStream</code> ausgegeben.
	 */
	public synchronized void status(HBCIPassport passport, int statusTag,
			Object[] o) {
		try {
			resetOutStream();
		switch (statusTag) {
		case STATUS_INST_BPD_INIT:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA"));
		    getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INST_BPD_INIT_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA_DONE",passport.getBPDVersion()));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INST_GET_KEYS:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INST_GET_KEYS_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS_DONE"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_SEND_KEYS:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_SEND_KEYS_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS_DONE"));
			// TODO AR
			getOutStream().append("status: "+((HBCIMsgStatus)o[0]).toString());
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INIT_SYSID:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INIT_SYSID_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID_DONE",o[1].toString()));
			// TODO AR
			getOutStream().append("status: "+((HBCIMsgStatus)o[0]).toString());
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INIT_SIGID:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INIT_SIGID_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID_DONE",o[1].toString()));
			// TODO AR
			getOutStream().append("status: "+((HBCIMsgStatus)o[0]).toString());
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INIT_UPD:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_INIT_UPD_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA_DONE",passport.getUPDVersion()));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_LOCK_KEYS:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_LOCK_KEYS_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK_DONE"));
			// TODO AR
			getOutStream().append("status: "+((HBCIMsgStatus)o[0]).toString());
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_DIALOG_INIT:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_DIALOG_INIT_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT_DONE",o[1]));
			// TODO AR
			getOutStream().append("status: "+((HBCIMsgStatus)o[0]).toString());
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_SEND_TASK:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_NEW_JOB",((HBCIJob)o[0]).getName()));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_SEND_TASK_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_JOB_DONE",((HBCIJob)o[0]).getName()));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_DIALOG_END:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_DIALOG_END_DONE:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END_DONE"));
			// TODO AR
			getOutStream().append("status: "+((HBCIMsgStatus)o[0]).toString());
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_CREATE:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_CREATE",o[0].toString()));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_SIGN:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_SIGN"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_CRYPT:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_CRYPT"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_SEND:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_SEND"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_RECV:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_RECV"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_PARSE:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_PARSE",o[0].toString()+")"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_DECRYPT:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_DECRYPT"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_MSG_VERIFY:
			// TODO AR
			getOutStream().append("  "+HBCIUtilsInternal.getLocMsg("STATUS_MSG_VERIFY"));
			getDialogData(getOutStream().toString(), false);
			break;
		case STATUS_SEND_INFOPOINT_DATA:
			// TODO AR
			getOutStream().append(HBCIUtilsInternal.getLocMsg("STATUS_SEND_INFOPOINT_DATA"));
			getDialogData(getOutStream().toString(), false);
			break;
		default:
			throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg(
					"STATUS_INVALID", Integer.toString(statusTag)));
		}
		} catch (InterruptedException e) {
			// TODO AR  Auto-generated catch block
			e.printStackTrace();
			throw new HBCI_Exception(e);
		}
		
	}
}
