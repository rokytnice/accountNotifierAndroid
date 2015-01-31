package org.rochlitz.hbci.tests.apps;

import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.rochlitz.hbci.callback.HBCICallbackAndroid;

/**
 * @author A Roc
 * CREATE PASSPORT
 */
public class TestUmsatzAbrufen extends BasisApp
{
    public static void main(String[] args)
    {
        Properties props=new Properties();
        
        // zu verwendendes passport konfigurieren
        props.setProperty("client.passport.default", "PinTan");
        
		props.setProperty("client.passport.PinTan.filename", fileanme);
        props.setProperty("client.passport.PinTan.init", "1");

        // loglevel und -filter auf maximales logging setzen
        props.setProperty("log.loglevel.default", "5");
        props.setProperty("log.filter", "0");
        
        HBCIUtils.init(props, new HBCICallbackConsole());
//        HBCIUtils.init(props, new HBCICallbackAndroid());
        
        // hbcihandler instanziieren
        HBCIPassport passport=AbstractHBCIPassport.getInstance();
        HBCIHandler  handler=new HBCIHandler("300", passport);
        
        // eigenes konto
        Konto myAccount = passport.getAccounts()[0];
        Konto myAccount1 = passport.getAccounts()[1];
        Konto myAccount2 = passport.getAccounts()[2];
        
        myAccount.number="4900xxxxx";
        
        // gegenkonto
//        Konto targetAccount=new Konto("DE", "20041133", "4900585");
//        targetAccount.name="Andre Rochlitz";
//        targetAccount.bic="COBADEHD001";
//        targetAccount.iban="DE532004113304900xxxxx";
        // sepa-ueberweisung erzeugen
        HBCIJob job=handler.newJob("SaldoReqAll");
        	    		 
        // daten fuer eigenes konto setzen
//        job.setParam("acc", myAccount2);
        job.setParam("my.blz", "20041133");
        job.setParam("my.number", "4900xxxxx");
        job.setParam("my.subnumber", "00");
        
        // daten fuer gegenkonto setzen
//        job.setParam("dst", myAccount2);
        // betrag + waehrung
//        job.setParam("btg", new Value(2,"EUR"));
        // verwendungszweck (nur EINE Zeile, dafuer aber mehr als 27 zeichen erlaubt)
//        job.setParam("usage", "Test Verwendungszweck");
        
//        HBCIJob job=handler.newJob("SaldoReqAll");
//        job.setParam("my.bic", "COBADEHD001");
//        job.setParam("my.iban", "DE532004113304900xxxxx");
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
}
