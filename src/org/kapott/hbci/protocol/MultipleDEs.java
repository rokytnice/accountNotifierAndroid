
/*  $Id: MultipleDEs.java 62 2008-10-22 17:03:26Z kleiner $

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

package org.kapott.hbci.protocol;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.protocol.factory.DEFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public final class MultipleDEs
     extends MultipleSyntaxElements
{
    private char delimiter;
    private List valids;

    protected SyntaxElement createAndAppendNewElement(Node deref, String path, int idx, Document syntax)
    {
        SyntaxElement ret=null;
        addElement((ret=DEFactory.getInstance().createDE(deref, getName(), path, idx, syntax)));
        return ret;
    }
    
    private void initData(Node dedef, char delimiter, String path, Document syntax)
    {
        this.delimiter = delimiter;
        this.valids=new ArrayList();
    }

    public MultipleDEs(Node dedef, char delimiter, String path, Document syntax)
    {
        super(dedef, path, syntax);
        initData(dedef,delimiter,path,syntax);
    }

    public void init(Node dedef, char delimiter, String path, Document syntax)
    {
        super.init(dedef, path, syntax);
        initData(dedef,delimiter,path,syntax);
    }

    protected boolean storeValidValueInDE(String destPath,String value)
    {
        boolean ret = false;

        // wenn dieses de gemeint ist
        if (destPath.equals(getPath())) {
            valids.add(value);
            ret=true;
        }

        return ret;
    }

    protected void validateOneElement(SyntaxElement elem, int idx)
    {
        ((DE)elem).setValids(valids);
        super.validateOneElement(elem,idx);
    }

    public String toString(int zero)
    {
        StringBuffer ret = new StringBuffer(128);
        boolean first=true;

        for (Iterator i = getElements().listIterator(); i.hasNext(); ) {
            if (!first)
                ret.append(delimiter);
            first=false;

            DE de = (DE)(i.next());
            if (de != null)
                ret.append(de.toString(0));
        }

        return ret.toString();
    }

    // -------------------------------------------------------------------------------------------------------

    protected SyntaxElement parseAndAppendNewElement(Node ref, String path, char predelim, int idx, StringBuffer res, int fullResLen,Document syntax, Hashtable predefs,Hashtable valids)
    {
        SyntaxElement ret=null;
        
        if (idx!=0 && valids!=null) {
            String header=getPath()+".value";
            for (Enumeration e=valids.keys();e.hasMoreElements();) {
                String key=(String)(e.nextElement());
                
                if (key.startsWith(header) &&    
                        key.indexOf(".",header.length())==-1) {
                    
                    int dotPos=key.lastIndexOf('.');
                    String newkey=key.substring(0,dotPos)+
                                  HBCIUtilsInternal.withCounter("",idx)+
                                  key.substring(dotPos);
                    valids.put(newkey,valids.get(key));
                }
            }
        }
        
        addElement((ret=DEFactory.getInstance().createDE(ref, getName(), path, predelim, idx, res, fullResLen, syntax, predefs,valids)));
        return ret;
    }
    
    private void initData(Node deref, char delimiter, String path, char predelim0, char predelim1, StringBuffer res, int fullResLen,Document syntax, Hashtable predefs,Hashtable valids)
    {
        this.delimiter=delimiter;
        this.valids=new ArrayList();
    }

    public MultipleDEs(Node deref, char delimiter, String path, char predelim0, char predelim1, StringBuffer res, int fullResLen, Document syntax, Hashtable predefs,Hashtable valids)
    {
        super(deref, path, predelim0, predelim1, res, fullResLen, syntax, predefs,valids);
        initData(deref,delimiter,path,predelim0,predelim1,res,fullResLen,syntax,predefs,valids);
    }

    public void init(Node deref, char delimiter, String path, char predelim0, char predelim1, StringBuffer res, int fullResLen, Document syntax, Hashtable predefs,Hashtable valids)
    {
        super.init(deref, path, predelim0, predelim1, res, fullResLen, syntax, predefs,valids);
        initData(deref,delimiter,path,predelim0,predelim1,res,fullResLen,syntax,predefs,valids);
    }

    public void getElementPaths(Properties p,int[] segref,int[] degref,int[] deref)
    {
        if (getElements().size()!=0) {
            for (Iterator i=getElements().iterator();i.hasNext();) {
                SyntaxElement e=(SyntaxElement)(i.next());
                if (e!=null) {
                    e.getElementPaths(p,segref,degref,deref);
                }
            }
        } else {
            if (deref==null) {
                p.setProperty(Integer.toString(segref[0])+
                              ":"+Integer.toString(degref[0]),getPath());
                degref[0]++;
            } else {
                p.setProperty(Integer.toString(segref[0])+
                              ":"+
                              Integer.toString(degref[0])+
                              ","+
                              Integer.toString(deref[0]),
                              getPath());
                deref[0]++;
            }
        }
    }
    
    public void destroy()
    {
        List children=getElements();
        for (Iterator i=children.iterator();i.hasNext();) {
            DEFactory.getInstance().unuseObject(i.next());
        }
        valids.clear();
        valids=null;
        
        super.destroy();
    }
}
