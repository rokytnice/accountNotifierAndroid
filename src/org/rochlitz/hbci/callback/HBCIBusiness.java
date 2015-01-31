package org.rochlitz.hbci.callback;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.rochlitz.hbci.passport.AbstractHBCIPassport;

import android.util.Log;

public class HBCIBusiness extends HBCIAsyncTask {

	Boolean userInput=false;
	String dto="";
	private DialogBean dBean;
	Properties props = new Properties();
	private final String APP_STORE;
	private boolean inputDialog = false;
	
	
	public HBCIBusiness(File filesDir) {
		APP_STORE = filesDir.getAbsolutePath();
	}

	public void setProperties() {
		// zu verwendendes passport konfigurieren
		props.setProperty("client.passport.default", "PinTan");
		props.setProperty("client.passport.PinTan.filename", APP_STORE+"/"
				+ "comdirect_passport_j.dat");
		props.setProperty("client.passport.PinTan.init", "1");

		// loglevel und -filter auf maximales logging setzen
		props.setProperty("log.loglevel.default", "5");
		props.setProperty("log.filter", "0");
	}

	public void doPublishProgress( ){
		publishProgress( );
	}
	
	public void getKontoauszuege() {
		 
		setProperties();

		HBCIUtils.init(props, new HBCICallbackAndroid(this));

		String header = "client.passport.PinTan.";
		String passportFile = HBCIUtils.getParam(header + "filename");

		// testWriteFIle();
		// hbcihandler instanziieren
		HBCIPassport passport = AbstractHBCIPassport.getInstance();
		HBCIHandler handler = new HBCIHandler("300", passport);

		// eigenes konto
		Konto myAccountGiro = passport.getAccounts()[2];

		HBCIJob job = handler.newJob("KUmsAll");

		// daten fuer eigenes konto setzen
		job.setParam("my.blz", "20041133");
		job.setParam("my.number", "490058500");
		job.setParam("my.subnumber", "00");
		Calendar calStart = new GregorianCalendar(2015, 00, 01);
		Calendar calEnd = new GregorianCalendar(2015, 00, 01);
		job.setParam("startdate", calStart.getTime());
		job.setParam("enddate", calEnd.getTime());
		job.addToQueue();

		log("cal start " + calStart.toString(), "");
		log("cal end " + calEnd.toString(), "");

		// execute dialog
		HBCIExecStatus dialogStatus = handler.execute();
		log("status:", "");
		log(dialogStatus.toString(), "");

		GVRKUms umsResults = (GVRKUms) job.getJobResult();

		for (int i = 0; i < umsResults.getFlatData().size(); i++) {
			UmsLine btag = (UmsLine) umsResults.getFlatData().get(i);
			log("value " + btag.value + " | date " + btag.bdate
					+ " |  neuer slado " + btag.saldo, "");
			log("toString " + btag.usage.toString(), "");
			log("", "");
		}

		Properties results = job.getJobResult().getResultData();

		// writeToFile(results);
		// print information about complete dialog
		if (!dialogStatus.isOK()) {
			log("some error has occured during execution of the HBCI dialog:",
					"");
			log(dialogStatus.getErrorString(), "");
		}

		// check each business task
		if (job.getJobResult().isOK()) {
			log("saldo information for account " + myAccountGiro, "");
			// log(job.getJobResult().toString());
		} else {
			log("an error occured in task SaldoRequest", "");
			log(job.getJobResult().getJobStatus().getErrorString(), "");
		}

		// aufraeumen
		handler.close();
	}

	
	/**
	 * create passport 
	 */
	public void getUmsatz(){
		
		setProperties();

		HBCIUtils.init(props, new HBCICallbackAndroidInteractive(this));
		
		 // hbcihandler instanziieren
        HBCIPassport passport=AbstractHBCIPassport.getInstance();
        HBCIHandler  handler=new HBCIHandler("300", passport);
        
        // eigenes konto
        Konto myAccount = passport.getAccounts()[0];
        Konto myAccount1 = passport.getAccounts()[1];
        Konto myAccount2 = passport.getAccounts()[2];
        
        myAccount.number="490058500";
        
        // gegenkonto
//        Konto targetAccount=new Konto("DE", "20041133", "4900585");
//        targetAccount.name="Andre Rochlitz";
//        targetAccount.bic="COBADEHD001";
//        targetAccount.iban="DE53200411330490058500";
        // sepa-ueberweisung erzeugen
        HBCIJob job=handler.newJob("SaldoReqAll");
        	    		 
        // daten fuer eigenes konto setzen
//        job.setParam("acc", myAccount2);
        job.setParam("my.blz", "20041133");
        job.setParam("my.number", "490058500");
        job.setParam("my.subnumber", "00");
        
        // daten fuer gegenkonto setzen
//        job.setParam("dst", myAccount2);
        // betrag + waehrung
//        job.setParam("btg", new Value(2,"EUR"));
        // verwendungszweck (nur EINE Zeile, dafuer aber mehr als 27 zeichen erlaubt)
//        job.setParam("usage", "Test Verwendungszweck");
        
//        HBCIJob job=handler.newJob("SaldoReqAll");
//        job.setParam("my.bic", "COBADEHD001");
//        job.setParam("my.iban", "DE53200411330490058500");
        // job hinzufuegen
        job.addToQueue();
        
     // execute dialog
        HBCIExecStatus dialogStatus=handler.execute();
        System.out.println("status:");
        System.out.println(dialogStatus);
        

        // print information about complete dialog
        if (!dialogStatus.isOK()) {
            System.out.println("some error has occured during execution of the HBCI dialog:");
            System.out.println(dialogStatus.getErrorString());
        }

        // check each business task
        if (job.getJobResult().isOK()) {
            System.out.println("saldo information for account "+myAccount2);
            System.out.println(job.getJobResult().toString());
        } else {
            System.out.println("an error occured in task SaldoRequest");
            System.out.println(job.getJobResult().getJobStatus().getErrorString());
        }
        
        // aufraeumen
        handler.close();
	}

	private static void log(String message, String tag) {
		Log.d(tag, message);
	}

	public void setDialogBean(DialogBean dBean) {
		this.dBean = dBean;
	}

	public DialogBean getdBean() {
		return dBean;
	}

	public void setdBean(DialogBean dBean) {
		this.dBean = dBean;
	}

	public void setInputDialog(boolean input) {

		this.inputDialog  = input;
	}

	public boolean isInputDialog() {
		return inputDialog;
	}
}
