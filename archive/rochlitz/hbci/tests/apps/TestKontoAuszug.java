package org.rochlitz.hbci.tests.apps;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.rochlitz.hbci.callback.HBCICallbackAndroid;

public class TestKontoAuszug extends BasisApp
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
        
        HBCIUtils.init(props, new HBCICallbackAndroid());
        
        // hbcihandler instanziieren
        HBCIPassport passport=AbstractHBCIPassport.getInstance();
        HBCIHandler  handler=new HBCIHandler("300", passport);
        
        // eigenes konto
        Konto myAccountGiro = passport.getAccounts()[2];
        
        HBCIJob job=handler.newJob("KUmsAll");
        	    		 
        // daten fuer eigenes konto setzen
        job.setParam("my.blz", "20041133");
        job.setParam("my.number", "490058500");
        job.setParam("my.subnumber", "00");
        Calendar calStart = new GregorianCalendar(2015, 00, 01);
        Calendar calEnd = new GregorianCalendar(2015, 00, 01);
        job.setParam("startdate", calStart.getTime());
        job.setParam("enddate",  calEnd.getTime());
        job.addToQueue();

        System.out.println("cal start "+calStart.toString());
        System.out.println("cal end "+calEnd.toString());
        
     // execute dialog
        HBCIExecStatus dialogStatus=handler.execute();
        System.out.println("status:");
        System.out.println(dialogStatus);
        
        GVRKUms umsResults=(GVRKUms)job.getJobResult();

        for(int i=0; i< umsResults.getFlatData().size();i++ ){
        	UmsLine btag = (UmsLine) umsResults.getFlatData().get(i);
        	System.out.println("value "+ btag.value +" | date "+btag.bdate + " |  neuer slado " +btag.saldo   );
        	System.out.println("toString "+ btag.usage.toString() );
        	System.out.println("");
        }
        
        Properties results = job.getJobResult().getResultData();
        
        writeToFile(results);
        // print information about complete dialog
        if (!dialogStatus.isOK()) {
            System.out.println("some error has occured during execution of the HBCI dialog:");
            System.out.println(dialogStatus.getErrorString());
        }

        // check each business task
        if (job.getJobResult().isOK()) {
            System.out.println("saldo information for account "+myAccountGiro);
//            System.out.println(job.getJobResult().toString());
        } else {
            System.out.println("an error occured in task SaldoRequest");
            System.out.println(job.getJobResult().getJobStatus().getErrorString());
        }
        
        // aufraeumen
        handler.close();
    }
    
    private static void writeToFile(Properties results){
    	
    	String filePath = "C:\\workspace\\grioNotifier\\kontoNotifier\\out\\KontoAuszug"+System.currentTimeMillis()+".txt";
 
    	try {
			results.store( new FileOutputStream(filePath), String.valueOf(System.currentTimeMillis()) );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    	 
		 
    	
    }
}
