
/*  $Id: HBCIPassportRDH2File.java 170 2009-10-13 13:06:30Z kleiner $

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

package org.kapott.hbci.passport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIKey;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.manager.LogFilter;
import org.kapott.hbci.passport.rdh2file.HBCIAccount;
import org.kapott.hbci.passport.rdh2file.RDH2File;
import org.kapott.hbci.passport.rdh2file.TLV;

/**<p>
 * Passport-Klasse f�r die Verwendung von RDH-2- und RDH-10-Schl�sseldateien mit
 * <em>HBCI4Java</em>. RDH-2/10-Schl�sseldateien sind Schl�sseldateien f�r
 * RDH-Zug�nge, die von anderer HBCI-Software erzeugt und verwendet werden (z.B.
 * von <em>VR-NetWorld</em>). Soll eine solche Schl�sseldatei sowohl mit der
 * anderen Software als auch mit <em>HBCI4Java</em> verwendet werden, so kann
 * das mit dieser Passport-Variante geschehen.</p> */    
public class HBCIPassportRDH2File
    extends AbstractRDHSWFileBasedPassport
{
    private byte[]   passphrase;
    private RDH2File filecontent;
    private int      entryIdx;    
    private String   forcedProfileVersion;
    
    public HBCIPassportRDH2File(Object init,int dummy)
    {
        super(init);
        this.forcedProfileVersion=null;
    }

    public HBCIPassportRDH2File(Object initObject)
    {
        this(initObject,0);
        setParamHeader("client.passport.RDH2File");

        String fname=HBCIUtils.getParam(getParamHeader()+".filename");
        if (fname==null) {
            throw new NullPointerException(getParamHeader()+".filename must not be null");
        }
        
        HBCIUtils.log("loading passport data from file "+fname,HBCIUtils.LOG_DEBUG);
        setFilename(fname);

        boolean init=HBCIUtils.getParam(getParamHeader()+".init","1").equals("1");
        if (init) {
            HBCIUtils.log("loading data from file "+getFilename(),HBCIUtils.LOG_DEBUG);

            setFilterType("None");
            setPort(new Integer(3000));

            if (!new File(getFilename()).canRead()) {
                HBCIUtils.log("have to create new passport file",HBCIUtils.LOG_WARN);
                askForMissingData(true,true,true,false,false,true,true);
                saveChanges();
            }
            
            try {
                if (this.passphrase==null) {
                    StringBuffer retData=new StringBuffer();
                    HBCIUtilsInternal.getCallback().callback(this,
                            HBCICallback.NEED_PASSPHRASE_LOAD,
                            HBCIUtilsInternal.getLocMsg("CALLB_NEED_PASS"),
                            HBCICallback.TYPE_SECRET,
                            retData);
                    // TODO: passwort-bedingungen nach spez. pr�fen
                    LogFilter.getInstance().addSecretData(retData.toString(),"X",LogFilter.FILTER_SECRETS);
                    setPassphrase(retData.toString().getBytes());
                }
            	
            	// daten einlesen
                FileInputStream fin=new FileInputStream(fname);
                StringBuffer    sb=new StringBuffer();
                byte[]          buffer=new byte[1024];
                int             size;
                
                while ((size=fin.read(buffer))>0) {
                    sb.append(new String(buffer,0,size,"ISO-8859-1"));
                }
                
                fin.close();
                byte[] data=sb.toString().getBytes("ISO-8859-1");
                // System.out.println("read "+data.length+" bytes from "+getFileName());
                
                // filecontent-content
                this.filecontent=new RDH2File(data, passphrase);
                this.entryIdx=0;
                
                TLV[] hbciAccounts=filecontent.getFields(HBCIAccount.class);
                if (hbciAccounts.length>1) {
                    // wenn mehrere bankverbindungen existieren, callback f�r auswahl der "richtigen"
                    StringBuffer possibilities=new StringBuffer();
                    for (int i=0;i<hbciAccounts.length;i++) {
                        HBCIAccount hbciAccount=(HBCIAccount)hbciAccounts[i];
                        if (i!=0) {
                            possibilities.append("|");
                        }
                        possibilities.append(i);
                        possibilities.append(";"+hbciAccount.getBLZ());
                        possibilities.append(";"+hbciAccount.getUserId());
                    }
                    
                    HBCIUtilsInternal.getCallback().callback(
                        this,
                        HBCICallback.NEED_SIZENTRY_SELECT,
                        "*** select one of the following entries",
                        HBCICallback.TYPE_TEXT,
                        possibilities);
                     
                    this.entryIdx=Integer.parseInt(possibilities.toString());
                }
                
                TLV[] accountFields=filecontent.getFields(HBCIAccount.class);
                if (accountFields.length!=0) {
                    // set all passport values
                    HBCIAccount hbciAccount=(HBCIAccount)(accountFields[entryIdx]);

                    setCountry(hbciAccount.getCountry());
                    setBLZ(hbciAccount.getBLZ());
                    setHost(hbciAccount.getHost());
                    setUserId(hbciAccount.getUserId());
                    setCustomerId(hbciAccount.getCustomerId());
                    setSysId(hbciAccount.getSysId());
                    setSigId(new Long(hbciAccount.getSigId()));

                    // setInstKeys()
                    setInstSigKey(filecontent.getBankSigKey(hbciAccount));
                    setInstEncKey(filecontent.getBankEncKey(hbciAccount));

                    // setUserSigKeys()
                    HBCIKey[] userkeys=hbciAccount.getUserSigKeys();
                    if (userkeys!=null) {
                        setMyPublicSigKey(userkeys[0]);
                        setMyPrivateSigKey(userkeys[1]);
                    }

                    // setUserEncKeys()
                    userkeys=hbciAccount.getUserEncKeys();
                    if (userkeys!=null) {
                        setMyPublicEncKey(userkeys[0]);
                        setMyPrivateEncKey(userkeys[1]);
                    }
                }
                                
                if (askForMissingData(true,true,true,false,false,true,true))
                    saveChanges();
            } catch (Exception e) {
                throw new HBCI_Exception("*** error while reading passport file",e);
            }
        }
    }
    
    public void saveChanges()
    {
        try {
            if (this.passphrase == null) {
                StringBuffer retData = new StringBuffer();
                HBCIUtilsInternal.getCallback().callback(this,
                        HBCICallback.NEED_PASSPHRASE_SAVE,
                        HBCIUtilsInternal.getLocMsg("CALLB_NEED_PASS"),
                        HBCICallback.TYPE_SECRET, retData);
                // TODO: passwort-bedingungen nach spez. pr�fen
                LogFilter.getInstance().addSecretData(retData.toString(),"X",LogFilter.FILTER_SECRETS);
                setPassphrase(retData.toString().getBytes());
            }

            // create temp file
            File passportfile = new File(getFilename());
            File directory = passportfile.getAbsoluteFile().getParentFile();
            String prefix = passportfile.getName() + "_";
            File tempfile = File.createTempFile(prefix, "", directory);
            
            if (filecontent==null) {
                filecontent=new RDH2File(passphrase);
            }

            TLV[]       accountFields=filecontent.getFields(HBCIAccount.class);
            HBCIAccount hbciAccount;
            if (accountFields.length==0) {
                hbciAccount = new HBCIAccount();
                filecontent.addField(hbciAccount);
            } else {
                hbciAccount = (HBCIAccount)(accountFields[entryIdx]); 
            }
            
            // save changed values in filecontent object
            hbciAccount.setCountry(getCountry());
            hbciAccount.setBLZ(getBLZ());
            hbciAccount.setHost(getHost());
            hbciAccount.setUserId(getUserId());
            hbciAccount.setCustomerId(getCustomerId());
            hbciAccount.setSysId(getSysId());
            hbciAccount.setSigId(getSigId().longValue());

            filecontent.setBankSigKey(hbciAccount, getInstSigKey());
            filecontent.setBankEncKey(hbciAccount, getInstEncKey());

            hbciAccount.setUserSigKeys(new HBCIKey[] { getMyPublicSigKey(),
                    getMyPrivateSigKey() });
            hbciAccount.setUserEncKeys(new HBCIKey[] { getMyPublicEncKey(),
                    getMyPrivateEncKey() });

            int              pversion = Integer.parseInt(getProfileVersion());
            byte[]           data = filecontent.getFileData(pversion);
            FileOutputStream fo = new FileOutputStream(tempfile);
            fo.write(data);
            fo.close();

            passportfile.delete();
            tempfile.renameTo(passportfile);
        } catch (Exception e) {
            throw new HBCI_Exception("*** saving of passport file failed", e);
        }
    }
    
    public String getProfileVersion()
    {
        String result=this.forcedProfileVersion;
        
        if (result==null) {
            HBCIUtils.log("no RDH profile version explicity specified - starting autodetection", HBCIUtils.LOG_DEBUG);
            
            if (!getHBCIVersion().startsWith("3")) { // TODO: support FinTS-4, too
                result="1";
                setProfileVersion(result);
                HBCIUtils.log("this is HBCI version '"+result+"', which only supports RDH-1", HBCIUtils.LOG_DEBUG);

            } else {
                HBCIKey key=getMyPublicSigKey();
                if (key!=null) {
                    // profil-erkennung anhand schluesselnummer
                    result=key.num;
		    setProfileVersion(result);
                    HBCIUtils.log("using user sig key num '"+result+"' as profile version" ,HBCIUtils.LOG_DEBUG);
                    
                } else {
                    key=getInstEncKey();
                    if (key.num.equals("1") || key.num.equals("2") || key.num.equals("10")) {
                        // found a sig key with a valid key num - so we use this as the profile version
                        result=key.num;
                        HBCIUtils.log("using inst enc key num '"+result+"' as RDH profile version", HBCIUtils.LOG_DEBUG);
                        
                    } else {
                        // neither user keys nor inst keys present - using highest available profile
                        HBCIUtils.log(
                            "no keys found in passport - so we use the highest available profile",
                            HBCIUtils.LOG_DEBUG);

                        // es gibt noch gar keine schl�ssel - also nehmen wir die
                        // h�chste unterst�tzte profil-nummer

                        String[][] methods=getSuppSecMethods();
                        int        maxVersion=0;
                        for (int i=0;i<methods.length;i++) {
                            String method=methods[i][0];
                            int    version=Integer.parseInt(methods[i][1]);
    
                            if (method.equals("RDH") && 
                                    (version==1 || version==2 || version==10)) 
                            {
                                // es werden nur RDH-1, RDH-2 und RDH-10 betrachtet, weil
                                // alle anderen rdh-profile nicht f�r software-l�sungen
                                // zugelassen sind
                                if (version>maxVersion) {
                                    maxVersion=version;
                                }
                            }
                        }
    
                        if (maxVersion!=0) {
                            result=Integer.toString(maxVersion);
                            setProfileVersion(result);
                        }
                        HBCIUtils.log(
                            "using RDH profile '"+result+"' taken from supported profiles (BPD)",
                            HBCIUtils.LOG_DEBUG);
                    }
                }
            }
            
        } else {
            HBCIUtils.log("using forced RDH profile version '"+result+"'", HBCIUtils.LOG_DEBUG);
        }
        
        return result;
    }
    
    public void setProfileVersion(String version)
    {
        if (version!=null) {
            Integer.parseInt(version);   // check for valid integer value
        }
        this.forcedProfileVersion = version;
    }

    public void resetPassphrase() 
    {
        this.passphrase=null;
    }
    
    public void setPassphrase(byte[] passphrase) 
    {
        this.passphrase=passphrase;
        if (this.filecontent!=null) {
            this.filecontent.setPassphrase(passphrase);
        }
    }
}
