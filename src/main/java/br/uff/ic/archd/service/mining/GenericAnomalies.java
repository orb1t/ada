/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.archd.service.mining;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author wallace
 */
public class GenericAnomalies {

    private int artifactBirthNumber;
    private int parentArtifactBirthNumber;
    private String genericName;
    private String genericLastName;
    private HashSet<String> alternativeNames;
    private List<String> alternativeNameList;
    private int numberOfRevisions;
    private HashMap<String, AnomalieList> anomalieHashMap;

    public GenericAnomalies(String genericName, int numberOfRevisions, int revisionBirthNumber, int classBirthNumber) {
        this.genericName = genericName;
        this.parentArtifactBirthNumber = classBirthNumber;
        this.artifactBirthNumber = revisionBirthNumber;
        this.numberOfRevisions = numberOfRevisions;
        anomalieHashMap = new HashMap();
        alternativeNames = new HashSet();
    }

    public void addAnomalie(String anomalie, int k) {
        AnomalieList anomalieList = anomalieHashMap.get(anomalie);
        if (anomalieList == null) {
            anomalieList = new AnomalieList(getNumberOfRevisions(), artifactBirthNumber, getParentArtifactBirthNumber());

            anomalieHashMap.put(anomalie, anomalieList);
        }
        anomalieList.setAnomalieOcurrence(k);
    }
    
    public void addNotAnomalie(String anomalie, int k) {
        AnomalieList anomalieList = anomalieHashMap.get(anomalie);
        if (anomalieList == null) {
            anomalieList = new AnomalieList(getNumberOfRevisions(), artifactBirthNumber, getParentArtifactBirthNumber());

            anomalieHashMap.put(anomalie, anomalieList);
        }
        anomalieList.setNotAnomalieOcurrence(k);
    }

    public void addAlternativeName(String name) {
        alternativeNames.add(name);
    }

    public List<String> getAlternativeNames() {
        if (alternativeNameList == null) {
            alternativeNameList = new LinkedList();
            Iterator<String> it = alternativeNames.iterator();
            while (it.hasNext()) {
                alternativeNameList.add(it.next());
            }
        }
        return alternativeNameList;
    }

    /**
     * @return the className
     */
    public String getGenericName() {
        return genericName;
    }
    
    public boolean isGenericName(String name){
        boolean flag = false;
        if(genericName.equals(name)){
            flag = true;
        }else {
            flag = alternativeNames.contains(name);
        }
        return flag;
    }

    public List<String> getAnomalies() {
        List<String> auxList = new LinkedList();
        Set<String> auxSet = anomalieHashMap.keySet();
        Iterator it = auxSet.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            auxList.add(str);
        }
        return auxList;
    }

    public AnomalieList getAnomalieList(String anomalieName) {
        return anomalieHashMap.get(anomalieName);
    }

    public boolean haveAnomalie(String anomalieName) {
        if (anomalieHashMap.get(anomalieName) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void classifyAnomalies() {
        Collection<AnomalieList> anomaliesCollection = anomalieHashMap.values();
        Iterator it = anomaliesCollection.iterator();
        while (it.hasNext()) {
            AnomalieList anomalieList = (AnomalieList) it.next();
            anomalieList.classifyAnomalie();
        }
    }

    /**
     * @return the revisionBirthNumber
     */
    public int getArtifactBirthNumber() {
        return artifactBirthNumber;
    }

    /**
     * @return the genericLastName
     */
    public String getGenericLastName() {
        return genericLastName;
    }

    /**
     * @param genericLastName the genericLastName to set
     */
    public void setGenericLastName(String genericLastName) {
        this.genericLastName = genericLastName;
    }

    /**
     * @return the parentArtifactBirthNumber
     */
    public int getParentArtifactBirthNumber() {
        return parentArtifactBirthNumber;
    }

    /**
     * @return the numberOfRevisions
     */
    public int getNumberOfRevisions() {
        return numberOfRevisions;
    }
}
