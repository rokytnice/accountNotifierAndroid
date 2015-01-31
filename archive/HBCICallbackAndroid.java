package org.rochlitz.hbci.callback;  
/*  $Id: HBCICallbackSwing.java 138 2009-07-26 10:11:13Z kleiner $

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


 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;

import org.kapott.hbci.callback.HBCICallbackIOStreams;
import org.kapott.hbci.exceptions.AbortedException;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.InvalidUserDataException;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.passport.HBCIPassport;

/** Default-Implementation einer Callback-Klasse für Anwendungen mit GUI.
    Diese Klasse überschreibt die <code>callback()</code>-Methode. Benötigte Nutzereingaben 
    werden hier nicht mehr über STDIN abgefragt, sondern es wird ein neues Top-Level-Window
    erzeugt, welches die entsprechende Meldung sowie ein Feld zur Eingabe
    der Antwort enthält. Kernel-Meldungen und erwartete Nutzeraktionen werden ebenfalls
    durch ein neues Top-Level-Window realisiert. Die Methoden <code>log()</code> und
    <code>status()</code> werden nicht überschrieben, so dass diese weiterhin das Verhalten
    der {@link org.kapott.hbci.callback.HBCICallbackConsole}-Klasse zeigen. */
public class HBCICallbackAndroid
    extends HBCICallbackIOStreams
{
    public static final boolean ACTION_BLOCKING=true;
    public static final boolean ACTION_NOT_BLOCKING=false;
    public static final boolean DIALOG_MODAL=true;
    public static final boolean DIALOG_NOT_MODAL=false;

    private final class SyncObject
    {
        private boolean stopCalled=false;
        
        public synchronized void startWaiting()
        {
            if (!stopCalled) {
                try {
                    wait();
                } catch (Exception e) {
                    throw new HBCI_Exception("*** error in sync object",e);
                }
            }
        }

        public synchronized void stopWaiting()
        {
            stopCalled=true;
            notify();
        }
    }
    
    protected Hashtable passports;
    
    public HBCICallbackAndroid()
    {
        super( System.out, new BufferedReader(new InputStreamReader(System.in)) );
        passports=new Hashtable();
    }
    
    public void callback(final HBCIPassport passport,int reason,String msg,int datatype,StringBuffer retData)
    {
        if (msg==null)
            msg="";
            
        Hashtable currentData=(Hashtable)passports.get(passport);
        if (currentData==null) {
            currentData=new Hashtable();
            currentData.put("passport",passport);
            currentData.put("dataRequested",Boolean.FALSE);
            currentData.put("proxyRequested",Boolean.FALSE);
            currentData.put("msgcounter",new Integer(0));
            passports.put(passport,currentData);
        }
        currentData.put("reason",new Integer(reason));
        currentData.put("msg",msg);
        
        if (retData!=null)
            currentData.put("retData",retData);
        
        try {
            switch (reason) {
                case NEED_PASSPHRASE_LOAD:
                case NEED_PASSPHRASE_SAVE:
                    needSecret(currentData,"passphrase");
                    break;
                case NEED_SOFTPIN:
                    needSecret(currentData,"softpin");
                    break;
                case NEED_PT_PIN:
                    needSecret(currentData,"ptpin");
                    break;
                case NEED_PT_TAN:
                    needSecret(currentData,"pttan");
                    break;
                    
                case NEED_COUNTRY:
                    if (!((Boolean)currentData.get("dataRequested")).booleanValue())
                        needRDHData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("data_country"));
                    break;
                case NEED_BLZ:
                    if (!((Boolean)currentData.get("dataRequested")).booleanValue())
                        needRDHData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("data_blz"));
                    break;
                case NEED_HOST:
                    if (!((Boolean)currentData.get("dataRequested")).booleanValue())
                        needRDHData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("data_host"));
                    break;
                case NEED_PORT:
                    if (!((Boolean)currentData.get("dataRequested")).booleanValue())
                        needRDHData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("data_port"));
                    break;
                case NEED_FILTER:
                    if (!((Boolean)currentData.get("dataRequested")).booleanValue())
                        needRDHData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("data_filter"));
                    break;
                case NEED_USERID:
                    if (!((Boolean)currentData.get("dataRequested")).booleanValue())
                        needRDHData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("data_userid"));
                    break;
                case NEED_CUSTOMERID:
                    if (!((Boolean)currentData.get("dataRequested")).booleanValue())
                        needRDHData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("data_customerid"));
                    break;
                    
                case NEED_CHIPCARD:
                    needAction(currentData,ACTION_NOT_BLOCKING,"chipcard");
                    break;
                case NEED_HARDPIN:
                    needAction(currentData,ACTION_NOT_BLOCKING,"hardpin");
                    break;
                case NEED_REMOVE_CHIPCARD:
                    needAction(currentData,ACTION_BLOCKING,"remove");
                    break;
                    
                case HAVE_CHIPCARD:
//                    removeActionWindow(currentData,"chipcard");
                    break;
                case HAVE_HARDPIN:
//                    removeActionWindow(currentData,"hardpin");
                    break;
                    
                case NEED_NEW_INST_KEYS_ACK:
                    ackInstKeys(currentData,"ackinstkeys");
                    break;
                case HAVE_NEW_MY_KEYS:
                    haveNewMyKeys(currentData,"ackmykeys");
                    break;
                    
                case HAVE_INST_MSG:
                    showInstMessage(currentData,"instmsg",retData!=null);
                    break;
                case NEED_CONNECTION:
                case CLOSE_CONNECTION:
//                    showConnectionMessage(currentData,"connmsg");
                    break;
                    
                case HAVE_CRC_ERROR:
                    correctAccountData(currentData,"crcerror");
                    break;
                case HAVE_IBAN_ERROR:
                    correctIBANData(currentData,"ibanerror");
                    break;
                case HAVE_ERROR:
                    handleError(currentData,"error");
                    break;
                    
                case NEED_SIZENTRY_SELECT:
                    needSIZEntrySelect(currentData,"sizentryselect");
                    break;
                    
                case NEED_PT_SECMECH:
                    needPTSecMech(currentData,"pt_method");
                    break;
                    
                case NEED_PROXY_USER:
                    if (!((Boolean)currentData.get("proxyRequested")).booleanValue())
                        needProxyData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("proxy_user"));
                    break;
                case NEED_PROXY_PASS:
                    if (!((Boolean)currentData.get("proxyRequested")).booleanValue())
                        needProxyData(currentData);
                    retData.replace(0,retData.length(),(String)currentData.get("proxy_pass"));
                    break;
                case NEED_INFOPOINT_ACK:
                    ackInfoPoint(currentData,"accinfopoint");
                    break;

                default:
                    throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_CALLB_UNKNOWN",Integer.toString(reason)));
            }
        } catch (Exception e) {
            throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_CALLB_ERR"),e);
        }
    }
    
    private void needSecret(final Hashtable currentData,final String winname)
    {
        final SyncObject sync=new SyncObject();
        
        final int[] aborted=new int[1];
        aborted[0]=0;
                //TODO AR
            	String passphrase="082254";
                
                    StringBuffer retData=(StringBuffer)currentData.get("retData");
                    retData.replace(0,retData.length(),passphrase);
                    
        if (aborted[0]==1)
            throw new AbortedException(HBCIUtilsInternal.getLocMsg("EXCMSG_USR_ABORT"));
        else if (aborted[0]==2)
            throw new InvalidUserDataException(HBCIUtilsInternal.getLocMsg("EXCMSG_PWDONTMATCH"));
    }
    
    private void correctAccountData(final Hashtable currentData,final String winname)
    {
       
    }

    private void correctIBANData(final Hashtable currentData,final String winname)
    {
         
    }

    private void needRDHData(final Hashtable currentData)
    {
       
    }
    
    private void needProxyData(final Hashtable currentData)
    {
         
    }
    
    private void needAction(final Hashtable currentData,final boolean blocking,final String winname)
    {
       
    }
    
    
    private void ackInstKeys(final Hashtable currentData,final String winname)
    {
       
    }
    
    private void handleError(final Hashtable currentData,final String winname)
    {
         
    }

    private void haveNewMyKeys(final Hashtable currentData,final String winname)
    {
         
    }
    
    protected void showInstMessage(final Hashtable currentData,final String winname)
    {
        showInstMessage(currentData,winname,true);
    }
    
    protected void showInstMessage(final Hashtable currentData,final String winname,final boolean blocking)
    {
         
    }

    protected void showConnectionMessage(final Hashtable currentData,final String winname)
    {
       
    }

    private void needSIZEntrySelect(final Hashtable currentData,final String winname)
    {
       
    }

    private void needPTSecMech(final Hashtable currentData,final String winname)
    {
         
    }

    private void ackInfoPoint(final Hashtable currentData,final String winname)
    {
        
    }
    
}
