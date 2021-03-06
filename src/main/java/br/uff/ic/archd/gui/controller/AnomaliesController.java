/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.archd.gui.controller;

import br.uff.ic.archd.db.dao.Constants;
import br.uff.ic.archd.git.service.JavaProjectsService;
import br.uff.ic.archd.git.service.ProjectRevisionsService;
import br.uff.ic.archd.gui.view.AnomalieChart;
import br.uff.ic.archd.gui.view.AnomaliesView;
import br.uff.ic.archd.javacode.JavaConstructorService;
import br.uff.ic.archd.model.Project;
import br.uff.ic.archd.service.mining.AnomalieList;
import br.uff.ic.archd.service.mining.AnomaliesAnaliser;
import br.uff.ic.archd.service.mining.GenericAnomalies;
import br.uff.ic.archd.service.mining.ProjectAnomalies;
import br.uff.ic.dyevc.application.branchhistory.model.BranchRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.LineRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.RevisionsBucket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author wallace
 */
public class AnomaliesController implements ActionListener {

    private Project project;
    private AnomaliesView anomaliesView;
    private ProjectAnomalies projectAnomalies;
    private List<String> packages;
    private List<String> classes;
    private List<String> methods;
    private List<String> anomalies;
    private int anomalieIndex;

    AnomaliesController(Project project, ProjectAnomalies projectAnomalies) {
        this.project = project;

        this.projectAnomalies = projectAnomalies;
        if (projectAnomalies == null) {
            createAnomalies();
        }

        anomalieIndex = 0;

        anomalies = this.projectAnomalies.getAnomalies();
        packages = this.projectAnomalies.getPackages();
        classes = this.projectAnomalies.getClasses();
        methods = this.projectAnomalies.getMethods();
        orderByName(methods);

        anomalies.add(0, "ALL ANOMALIES");
        String anomaliesArr[] = new String[anomalies.size()];
        for (int i = 0; i < anomalies.size(); i++) {
            anomaliesArr[i] = anomalies.get(i);
        }

        String packagesArr[] = new String[packages.size()];
        for (int i = 0; i < packages.size(); i++) {
            packagesArr[i] = packages.get(i);
        }

        String classesArr[] = new String[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            classesArr[i] = classes.get(i);
        }

        String methodsArr[] = new String[methods.size()];
        for (int i = 0; i < methods.size(); i++) {
            methodsArr[i] = methods.get(i);
        }

        anomaliesView = new AnomaliesView(anomaliesArr, packagesArr, classesArr, methodsArr);
        anomaliesView.setController(this);

        anomaliesView.setPackageDisable();
        anomaliesView.setClassDisable();
        anomaliesView.setMethodDisable();
        for (String str : anomalies) {
            if (str.equals("GOD PACKAGE")) {
                anomaliesView.setPackageEnable();
            } else if (str.equals("GOD CLASS") || str.equals("MISPLACED CLASS")) {
                anomaliesView.setClassEnable();
            } else if (str.equals("FEATURE ENVY") || str.equals("SHOTGUN SURGERY") || str.equals("GOD METHOD")) {
                anomaliesView.setMethodEnable();
            }
        }

        anomaliesView.setVisible(true);

    }

    public void createAnomalies() {
        JavaConstructorService javaContructorService = new JavaConstructorService();
        ProjectRevisionsService projectRevisionsService = new ProjectRevisionsService();
        AnomaliesAnaliser anomaliesAnaliser = new AnomaliesAnaliser();
        try {
            ProjectRevisions projectRevisions = projectRevisionsService.getProject(project.getPath(), project.getName());
            System.out.println("ORIGINAL ROOT: " + projectRevisions.getRoot().getId());
            System.out.println("ORIGINAL HEAD: " + projectRevisions.getBranchesRevisions().get(0).getHead().getId());
            System.out.println("Vai limpar");
            ProjectRevisions newProjectRevisions = cleanProjectRevisionsLine(projectRevisions);
            System.out.println("Limpou");
            this.projectAnomalies = anomaliesAnaliser.getAnomalies(newProjectRevisions, project, javaContructorService);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ProjectRevisions cleanProjectRevisionsLine(ProjectRevisions projectRevisions) {
        List<BranchRevisions> branches = new LinkedList();
        ProjectRevisions newProjectRevisions = new ProjectRevisions(projectRevisions.getName());
        RevisionsBucket revisionsBucket = new RevisionsBucket();
        //Revision newRoot = new Revision(projectRevisions.getRoot().getId());
        int count = 0;
        //newProjectRevisions.setRoot(newRoot);
        for (BranchRevisions branchRevisions : projectRevisions.getBranchesRevisions()) {
            Revision newHead = revisionsBucket.getRevisionById(branchRevisions.getHead().getId());
            if (newHead == null) {
                newHead = new Revision(branchRevisions.getHead().getId());
                revisionsBucket.addRevision(newHead);
            }
            BranchRevisions newBranchRevisions = new BranchRevisions(branchRevisions.getName(), newHead);
            LineRevisions lineRevisions = branchRevisions.getLinesRevisions().get(0);
            LineRevisions newLineRevisions = new LineRevisions(newHead);
            Revision aux = lineRevisions.getHead();
            Revision newRevision = revisionsBucket.getRevisionById(aux.getId());
            if (newRevision == null) {
                newRevision = new Revision(aux.getId());
                revisionsBucket.addRevision(newRevision);
            }
            Revision prox = newRevision;
            newLineRevisions.addRevision(newRevision);
            revisionsBucket.addRevision(newRevision);
            int i = 0;
            while (aux != null) {
                i++;
                //System.out.println("I: "+i);
                aux = aux.getPrev().get(aux.getPrev().size() - 1);
                newRevision = revisionsBucket.getRevisionById(aux.getId());
                if (newRevision == null) {
                    newRevision = new Revision(aux.getId());
                    revisionsBucket.addRevision(newRevision);
                }
                newRevision.addNext(prox);
                prox.addPrev(newRevision);
                prox = newRevision;
                newLineRevisions.addRevision(newRevision);
                revisionsBucket.addRevision(newRevision);
                count++;
                if (aux.getPrev().size() == 0) {
                    aux = null;
                }
            }
            newProjectRevisions.setRoot(prox);
            newBranchRevisions.addLineRevisions(lineRevisions);
            branches.add(newBranchRevisions);

        }
        System.out.println("Count: " + count);
        newProjectRevisions.setBranchesRevisions(branches);
        newProjectRevisions.setRevisionsBucket(revisionsBucket);
        return newProjectRevisions;
    }

    private void showAnomalies(int index) {
        anomalieIndex = index;
        String anomalie = anomalies.get(index);
        if (anomalie.equals("ALL ANOMALIES")) {
            for (String str : anomalies) {
                if (str.equals("GOD PACKAGE")) {
                    anomaliesView.setPackageEnable();
                } else if (str.equals("GOD CLASS") || str.equals("MISPLACED CLASS")) {
                    anomaliesView.setClassEnable();
                } else if (str.equals("FEATURE ENVY") || str.equals("SHOTGUN SURGERY") || str.equals("GOD METHOD")) {
                    anomaliesView.setMethodEnable();
                }
                packages = this.projectAnomalies.getPackages();
                classes = this.projectAnomalies.getClasses();
                methods = this.projectAnomalies.getMethods();
                orderByName(methods);
                anomaliesView.setPackages(packages);
                anomaliesView.setClasses(classes);
                anomaliesView.setMethods(methods);

                String text = "ALL ANOMALIES: \n";

                int typesOfAnomalies[] = new int[24];
                for (int i = 0; i < typesOfAnomalies.length; i++) {
                    typesOfAnomalies[i] = 0;
                }
                int totalOfAnomalies = 0;
                long numberOfRevisionsWithAnomalie = 0;
                long numberOfRevisionsWithoutAnomalie = 0;
                for (String genericName : packages) {
                    GenericAnomalies genericAnomalies = projectAnomalies.getPackageAnomalies(genericName);
                    List<String> anomaliesNames = genericAnomalies.getAnomalies();
                    for (String anomalieName : anomaliesNames) {
                        AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalieName);
                        typesOfAnomalies[anomalieList.getTypeOfAnomalie() - 1]++;
                        totalOfAnomalies++;
                        numberOfRevisionsWithAnomalie = numberOfRevisionsWithAnomalie + anomalieList.getNumberOfRevisionsWithAnomalie();
                        numberOfRevisionsWithoutAnomalie = numberOfRevisionsWithoutAnomalie + anomalieList.getNumberOfRevisionsWithoutAnomalie();
                    }
                }

                for (String genericName : classes) {
                    GenericAnomalies genericAnomalies = projectAnomalies.getClassAnomalies(genericName);
                    List<String> anomaliesNames = genericAnomalies.getAnomalies();
                    for (String anomalieName : anomaliesNames) {
                        AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalieName);
                        typesOfAnomalies[anomalieList.getTypeOfAnomalie() - 1]++;
                        totalOfAnomalies++;
                        numberOfRevisionsWithAnomalie = numberOfRevisionsWithAnomalie + anomalieList.getNumberOfRevisionsWithAnomalie();
                        numberOfRevisionsWithoutAnomalie = numberOfRevisionsWithoutAnomalie + anomalieList.getNumberOfRevisionsWithoutAnomalie();
                    }
                }

                for (String genericName : methods) {
                    GenericAnomalies genericAnomalies = projectAnomalies.getMethodAnomalies(genericName);
                    List<String> anomaliesNames = genericAnomalies.getAnomalies();
                    for (String anomalieName : anomaliesNames) {
                        AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalieName);
                        typesOfAnomalies[anomalieList.getTypeOfAnomalie() - 1]++;
                        totalOfAnomalies++;
                        numberOfRevisionsWithAnomalie = numberOfRevisionsWithAnomalie + anomalieList.getNumberOfRevisionsWithAnomalie();
                        numberOfRevisionsWithoutAnomalie = numberOfRevisionsWithoutAnomalie + anomalieList.getNumberOfRevisionsWithoutAnomalie();
                    }
                }





                double percentagem = numberOfRevisionsWithAnomalie;
                percentagem = percentagem / (numberOfRevisionsWithAnomalie + numberOfRevisionsWithoutAnomalie);
                percentagem = percentagem * 100;

                text = text + "          Number Of Revisions With Problem: " + numberOfRevisionsWithAnomalie
                        + "          Number Of Revisions Without Problem: " + numberOfRevisionsWithoutAnomalie + "          (Incidence: "
                        + (percentagem) + " %)"
                        + "\n           Items Afected By Anomalies: " + (packages.size() + classes.size() + methods.size())
                        + "\n           Types Of Anomalies:";
                int congenital = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];
                int adquired = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11]+
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int notCorrected = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20];
                int corrected = typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11] +
                        typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int simplePattern = typesOfAnomalies[0] + typesOfAnomalies[3] + typesOfAnomalies[6] + typesOfAnomalies[9]+
                        typesOfAnomalies[12] + typesOfAnomalies[15] + typesOfAnomalies[18] + typesOfAnomalies[21];
                int doublePattern = typesOfAnomalies[1] + typesOfAnomalies[4] + typesOfAnomalies[7] + typesOfAnomalies[10]+
                        typesOfAnomalies[13] + typesOfAnomalies[16] + typesOfAnomalies[19] + typesOfAnomalies[22];
                int complexPattern = typesOfAnomalies[2] + typesOfAnomalies[5] + typesOfAnomalies[8] + typesOfAnomalies[11]+
                        typesOfAnomalies[14] + typesOfAnomalies[17] + typesOfAnomalies[20] + typesOfAnomalies[23];
                
                int bornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int bornAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + 
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23] ;
                
                int congenitalBornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5];
                int congenitalBorntAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];

                int adquiredBornWithTheClass = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int adquiredBornAfterTheClass =  typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                
                percentagem = congenital;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Congenital: " + congenital + " times (" + percentagem + " %)";
                percentagem = adquired;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Adquired: " + adquired + " times (" + percentagem + " %)";

                percentagem = notCorrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Not corrected: " + notCorrected + " times (" + percentagem + " %)";
                percentagem = corrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Corrected: " + corrected + " times (" + percentagem + " %)";

                percentagem = simplePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Simple Pattern: " + simplePattern + " times (" + percentagem + " %)";
                percentagem = doublePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Double Pattern: " + doublePattern + " times (" + percentagem + " %)";
                percentagem = complexPattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Recurrent Pattern: " + complexPattern + " times (" + percentagem + " %) \n";
                
//                percentagem = bornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Born With The Class: " + bornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = bornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Born After The Class: " + bornAfterTheClass + " times (" + percentagem + " %)";
//                
//                percentagem = congenitalBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Congenital Born With The Class: " + congenitalBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = congenitalBorntAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Congenital Born After The Class: " + congenitalBorntAfterTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Adquired Born With The Class: " + adquiredBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Adquired Born After The Class: " + adquiredBornAfterTheClass + " times (" + percentagem + " %) \n";
                for (int i = 0; i < 12; i++) {
                    percentagem = typesOfAnomalies[i] + typesOfAnomalies[i+12];
                    percentagem = percentagem / totalOfAnomalies;
                    percentagem = percentagem * 100;
                    text = text + "\n               " + getTypeOfAnomalie(i + 1) + ": " + typesOfAnomalies[i] + " times (" + percentagem + " %)";
                }
                
                
                

                anomaliesView.setInformation(text);


            }

        } else {
            anomaliesView.setPackageDisable();
            anomaliesView.setClassDisable();
            anomaliesView.setMethodDisable();
            String text = anomalie + ": \n";
            if (anomalie.equals("GOD PACKAGE")) {
                packages = this.projectAnomalies.getPackagesByAnomalie(anomalie);
                anomaliesView.setPackages(packages);
                anomaliesView.setPackageEnable();

                int typesOfAnomalies[] = new int[24];
                for (int i = 0; i < typesOfAnomalies.length; i++) {
                    typesOfAnomalies[i] = 0;
                }
                int totalOfAnomalies = 0;
                long numberOfRevisionsWithAnomalie = 0;
                long numberOfRevisionsWithoutAnomalie = 0;
                

                for (String genericName : packages) {
                    GenericAnomalies genericAnomalies = projectAnomalies.getPackageAnomalies(genericName);
                    AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalie);
                    typesOfAnomalies[anomalieList.getTypeOfAnomalie() - 1]++;
                    totalOfAnomalies++;
                    numberOfRevisionsWithAnomalie = numberOfRevisionsWithAnomalie + anomalieList.getNumberOfRevisionsWithAnomalie();
                    numberOfRevisionsWithoutAnomalie = numberOfRevisionsWithoutAnomalie + anomalieList.getNumberOfRevisionsWithoutAnomalie();
                }

                double percentagem = numberOfRevisionsWithAnomalie;
                percentagem = percentagem / (numberOfRevisionsWithAnomalie + numberOfRevisionsWithoutAnomalie);
                percentagem = percentagem * 100;

                text = text + "          Number Of Revisions With Problem: " + numberOfRevisionsWithAnomalie
                        + "          Number Of Revisions Without Problem: " + numberOfRevisionsWithoutAnomalie + "          (Incidence: "
                        + (percentagem) + " %)"
                        + "\n           Packages Afected By Anomalie: " + (packages.size())
                        + "\n           Types Of Anomalies:";
                int congenital = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];
                int adquired = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11]+
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int notCorrected = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20];
                int corrected = typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11] +
                        typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int simplePattern = typesOfAnomalies[0] + typesOfAnomalies[3] + typesOfAnomalies[6] + typesOfAnomalies[9]+
                        typesOfAnomalies[12] + typesOfAnomalies[15] + typesOfAnomalies[18] + typesOfAnomalies[21];
                int doublePattern = typesOfAnomalies[1] + typesOfAnomalies[4] + typesOfAnomalies[7] + typesOfAnomalies[10]+
                        typesOfAnomalies[13] + typesOfAnomalies[16] + typesOfAnomalies[19] + typesOfAnomalies[22];
                int complexPattern = typesOfAnomalies[2] + typesOfAnomalies[5] + typesOfAnomalies[8] + typesOfAnomalies[11]+
                        typesOfAnomalies[14] + typesOfAnomalies[17] + typesOfAnomalies[20] + typesOfAnomalies[23];
                
                int bornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int bornAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + 
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23] ;
                
                int congenitalBornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5];
                int congenitalBorntAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];

                int adquiredBornWithTheClass = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int adquiredBornAfterTheClass =  typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                
                percentagem = congenital;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Congenital: " + congenital + " times (" + percentagem + " %)";
                percentagem = adquired;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Adquired: " + adquired + " times (" + percentagem + " %)";

                percentagem = notCorrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Not corrected: " + notCorrected + " times (" + percentagem + " %)";
                percentagem = corrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Corrected: " + corrected + " times (" + percentagem + " %)";

                percentagem = simplePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Simple Pattern: " + simplePattern + " times (" + percentagem + " %)";
                percentagem = doublePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Double Pattern: " + doublePattern + " times (" + percentagem + " %)";
                percentagem = complexPattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Recurrent Pattern: " + complexPattern + " times (" + percentagem + " %) \n";
                
//                percentagem = bornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Born With The Class: " + bornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = bornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Born After The Class: " + bornAfterTheClass + " times (" + percentagem + " %)";
//                
//                percentagem = congenitalBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Congenital Born With The Class: " + congenitalBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = congenitalBorntAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Congenital Born After The Class: " + congenitalBorntAfterTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Adquired Born With The Class: " + adquiredBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Adquired Born After The Class: " + adquiredBornAfterTheClass + " times (" + percentagem + " %) \n";
                for (int i = 0; i < 12; i++) {
                    percentagem = typesOfAnomalies[i] + typesOfAnomalies[i+12];
                    percentagem = percentagem / totalOfAnomalies;
                    percentagem = percentagem * 100;
                    text = text + "\n               " + getTypeOfAnomalie(i + 1) + ": " + typesOfAnomalies[i] + " times (" + percentagem + " %)";
                }
                


            } else if (anomalie.equals("GOD CLASS") || anomalie.equals("MISPLACED CLASS")) {
                classes = this.projectAnomalies.getClassesByAnomalie(anomalie);
                anomaliesView.setClasses(classes);
                anomaliesView.setClassEnable();



                int typesOfAnomalies[] = new int[24];
                for (int i = 0; i < typesOfAnomalies.length; i++) {
                    typesOfAnomalies[i] = 0;
                }
                int totalOfAnomalies = 0;
                long numberOfRevisionsWithAnomalie = 0;
                long numberOfRevisionsWithoutAnomalie = 0;

                for (String genericName : classes) {
                    GenericAnomalies genericAnomalies = projectAnomalies.getClassAnomalies(genericName);
                    AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalie);
                    typesOfAnomalies[anomalieList.getTypeOfAnomalie() - 1]++;
                    totalOfAnomalies++;
                    numberOfRevisionsWithAnomalie = numberOfRevisionsWithAnomalie + anomalieList.getNumberOfRevisionsWithAnomalie();
                    numberOfRevisionsWithoutAnomalie = numberOfRevisionsWithoutAnomalie + anomalieList.getNumberOfRevisionsWithoutAnomalie();
                }

                double percentagem = numberOfRevisionsWithAnomalie;
                percentagem = percentagem / (numberOfRevisionsWithAnomalie + numberOfRevisionsWithoutAnomalie);
                percentagem = percentagem * 100;

                text = text + "          Number Of Revisions With Problem: " + numberOfRevisionsWithAnomalie
                        + "          Number Of Revisions Without Problem: " + numberOfRevisionsWithoutAnomalie + "          (Incidence: "
                        + (percentagem) + " %)"
                        + "\n           Classes Afected By Anomalie: " + (classes.size())
                        + "\n           Types Of Anomalies:";
                int congenital = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];
                int adquired = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11]+
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int notCorrected = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20];
                int corrected = typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11] +
                        typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int simplePattern = typesOfAnomalies[0] + typesOfAnomalies[3] + typesOfAnomalies[6] + typesOfAnomalies[9]+
                        typesOfAnomalies[12] + typesOfAnomalies[15] + typesOfAnomalies[18] + typesOfAnomalies[21];
                int doublePattern = typesOfAnomalies[1] + typesOfAnomalies[4] + typesOfAnomalies[7] + typesOfAnomalies[10]+
                        typesOfAnomalies[13] + typesOfAnomalies[16] + typesOfAnomalies[19] + typesOfAnomalies[22];
                int complexPattern = typesOfAnomalies[2] + typesOfAnomalies[5] + typesOfAnomalies[8] + typesOfAnomalies[11]+
                        typesOfAnomalies[14] + typesOfAnomalies[17] + typesOfAnomalies[20] + typesOfAnomalies[23];
                
                int bornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int bornAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + 
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23] ;
                
                int congenitalBornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5];
                int congenitalBorntAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];

                int adquiredBornWithTheClass = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int adquiredBornAfterTheClass =  typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                
                percentagem = congenital;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Congenital: " + congenital + " times (" + percentagem + " %)";
                percentagem = adquired;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Adquired: " + adquired + " times (" + percentagem + " %)";

                percentagem = notCorrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Not corrected: " + notCorrected + " times (" + percentagem + " %)";
                percentagem = corrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Corrected: " + corrected + " times (" + percentagem + " %)";

                percentagem = simplePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Simple Pattern: " + simplePattern + " times (" + percentagem + " %)";
                percentagem = doublePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Double Pattern: " + doublePattern + " times (" + percentagem + " %)";
                percentagem = complexPattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Recurrent Pattern: " + complexPattern + " times (" + percentagem + " %) \n";
                
//                percentagem = bornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Born With The Class: " + bornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = bornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Born After The Class: " + bornAfterTheClass + " times (" + percentagem + " %)";
//                
//                percentagem = congenitalBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Congenital Born With The Class: " + congenitalBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = congenitalBorntAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Congenital Born After The Class: " + congenitalBorntAfterTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Adquired Born With The Class: " + adquiredBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Adquired Born After The Class: " + adquiredBornAfterTheClass + " times (" + percentagem + " %) \n";
                for (int i = 0; i < 12; i++) {
                    percentagem = typesOfAnomalies[i] + typesOfAnomalies[i+12];
                    percentagem = percentagem / totalOfAnomalies;
                    percentagem = percentagem * 100;
                    text = text + "\n               " + getTypeOfAnomalie(i + 1) + ": " + typesOfAnomalies[i] + " times (" + percentagem + " %)";
                }
                


            } else if (anomalie.equals("FEATURE ENVY") || anomalie.equals("SHOTGUN SURGERY") || anomalie.equals("GOD METHOD")) {
                methods = this.projectAnomalies.getMethodsByAnomalie(anomalie);
                orderByName(methods);
                System.out.println("Methods number: " + methods.size());
                anomaliesView.setMethods(methods);
                anomaliesView.setMethodEnable();



                int typesOfAnomalies[] = new int[24];
                for (int i = 0; i < typesOfAnomalies.length; i++) {
                    typesOfAnomalies[i] = 0;
                }
                int totalOfAnomalies = 0;
                long numberOfRevisionsWithAnomalie = 0;
                long numberOfRevisionsWithoutAnomalie = 0;

                for (String genericName : methods) {
                    GenericAnomalies genericAnomalies = projectAnomalies.getMethodAnomalies(genericName);
                    AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalie);
                    typesOfAnomalies[anomalieList.getTypeOfAnomalie() - 1]++;
                    totalOfAnomalies++;
                    numberOfRevisionsWithAnomalie = numberOfRevisionsWithAnomalie + anomalieList.getNumberOfRevisionsWithAnomalie();
                    numberOfRevisionsWithoutAnomalie = numberOfRevisionsWithoutAnomalie + anomalieList.getNumberOfRevisionsWithoutAnomalie();
                }

                double percentagem = numberOfRevisionsWithAnomalie;
                percentagem = percentagem / (numberOfRevisionsWithAnomalie + numberOfRevisionsWithoutAnomalie);
                percentagem = percentagem * 100;

                text = text + "          Number Of Revisions With Problem: " + numberOfRevisionsWithAnomalie
                        + "          Number Of Revisions Without Problem: " + numberOfRevisionsWithoutAnomalie + "          (Incidence: "
                        + (percentagem) + " %)"
                        + "\n           Methods Afected By Anomalie: " + (methods.size())
                        + "\n           Types Of Anomalies:";
                int congenital = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];
                int adquired = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11]+
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int notCorrected = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] +
                        typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20];
                int corrected = typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11] +
                        typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                int simplePattern = typesOfAnomalies[0] + typesOfAnomalies[3] + typesOfAnomalies[6] + typesOfAnomalies[9]+
                        typesOfAnomalies[12] + typesOfAnomalies[15] + typesOfAnomalies[18] + typesOfAnomalies[21];
                int doublePattern = typesOfAnomalies[1] + typesOfAnomalies[4] + typesOfAnomalies[7] + typesOfAnomalies[10]+
                        typesOfAnomalies[13] + typesOfAnomalies[16] + typesOfAnomalies[19] + typesOfAnomalies[22];
                int complexPattern = typesOfAnomalies[2] + typesOfAnomalies[5] + typesOfAnomalies[8] + typesOfAnomalies[11]+
                        typesOfAnomalies[14] + typesOfAnomalies[17] + typesOfAnomalies[20] + typesOfAnomalies[23];
                
                int bornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5] +
                        typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int bornAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17] + 
                        typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23] ;
                
                int congenitalBornWithTheClass = typesOfAnomalies[0] + typesOfAnomalies[1] + typesOfAnomalies[2] + typesOfAnomalies[3] + typesOfAnomalies[4] + typesOfAnomalies[5];
                int congenitalBorntAfterTheClass = typesOfAnomalies[12] + typesOfAnomalies[13] + typesOfAnomalies[14] + typesOfAnomalies[15] + typesOfAnomalies[16] + typesOfAnomalies[17];

                int adquiredBornWithTheClass = typesOfAnomalies[6] + typesOfAnomalies[7] + typesOfAnomalies[8] + typesOfAnomalies[9] + typesOfAnomalies[10] + typesOfAnomalies[11];
                int adquiredBornAfterTheClass =  typesOfAnomalies[18] + typesOfAnomalies[19] + typesOfAnomalies[20] + typesOfAnomalies[21] + typesOfAnomalies[22] + typesOfAnomalies[23];
                
                percentagem = congenital;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Congenital: " + congenital + " times (" + percentagem + " %)";
                percentagem = adquired;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Adquired: " + adquired + " times (" + percentagem + " %)";

                percentagem = notCorrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Not corrected: " + notCorrected + " times (" + percentagem + " %)";
                percentagem = corrected;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Corrected: " + corrected + " times (" + percentagem + " %)";

                percentagem = simplePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n\n               Simple Pattern: " + simplePattern + " times (" + percentagem + " %)";
                percentagem = doublePattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Double Pattern: " + doublePattern + " times (" + percentagem + " %)";
                percentagem = complexPattern;
                percentagem = percentagem / totalOfAnomalies;
                percentagem = percentagem * 100;
                text = text + "\n               Recurrent Pattern: " + complexPattern + " times (" + percentagem + " %) \n";
                
//                percentagem = bornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Born With The Class: " + bornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = bornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Born After The Class: " + bornAfterTheClass + " times (" + percentagem + " %)";
//                
//                percentagem = congenitalBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Congenital Born With The Class: " + congenitalBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = congenitalBorntAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Congenital Born After The Class: " + congenitalBorntAfterTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornWithTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n\n               Adquired Born With The Class: " + adquiredBornWithTheClass + " times (" + percentagem + " %)";
//                percentagem = adquiredBornAfterTheClass;
//                percentagem = percentagem / totalOfAnomalies;
//                percentagem = percentagem * 100;
//                text = text + "\n               Adquired Born After The Class: " + adquiredBornAfterTheClass + " times (" + percentagem + " %) \n";
                for (int i = 0; i < 12; i++) {
                    percentagem = typesOfAnomalies[i] + typesOfAnomalies[i+12];
                    percentagem = percentagem / totalOfAnomalies;
                    percentagem = percentagem * 100;
                    text = text + "\n               " + getTypeOfAnomalie(i + 1) + ": " + typesOfAnomalies[i] + " times (" + percentagem + " %)";
                }
                
            }

            anomaliesView.setInformation(text);

        }
    }

    private void showMethod(int index) {
        String anomalie = anomalies.get(anomalieIndex);
        String methodName = methods.get(index);
        GenericAnomalies genericAnomalies = projectAnomalies.getMethodAnomalies(methodName);
        JPanel chartPanel = new AnomalieChart(genericAnomalies, anomalie);

        String text = "METHOD: " + methodName +"      last name: "+genericAnomalies.getGenericLastName()+"\n";
        if(!genericAnomalies.getAlternativeNames().isEmpty()){
            text = text+"Alternative Names: ";
        }
        for (String aux : genericAnomalies.getAlternativeNames()) {
            text = text+" " + aux + "   ,  ";
        }
        text = text+"\n";
        if (anomalie.equals("ALL ANOMALIES")) {
            List<String> anomaliesNames = genericAnomalies.getAnomalies();
            for (String str : anomaliesNames) {
                AnomalieList anomalieList = genericAnomalies.getAnomalieList(str);
                double percentagem = anomalieList.getNumberOfRevisionsWithAnomalie();
                percentagem = percentagem / (anomalieList.getNumberOfRevisionsWithAnomalie() + anomalieList.getNumberOfRevisionsWithoutAnomalie());
                percentagem = percentagem * 100;

                text = text + "ANOMALIE: " + str + "\n    Number Of Revisions With Problem: " + anomalieList.getNumberOfRevisionsWithAnomalie()
                        + "          Number Of Revisions Without Problem: " + anomalieList.getNumberOfRevisionsWithoutAnomalie() + "          (Incidence: "
                        + (percentagem) + " %)"
                        + "\n    Revision Birth: " + anomalieList.getArtifactBirthNumber()
                        + "\n    Class Birth: " + anomalieList.getParentArtifactBirthNumber()
                        + "\n    Anomalie Birth: " + (anomalieList.getAnomalieBirthNumber() + anomalieList.getArtifactBirthNumber())
                        + "\n    Type: " + getTypeOfAnomalie(anomalieList.getTypeOfAnomalie()) + "\n\n";
                System.out.println("Type: " + anomalieList.getTypeOfAnomalie());
            }
        } else {
            AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalie);
            double percentagem = anomalieList.getNumberOfRevisionsWithAnomalie();
            percentagem = percentagem / (anomalieList.getNumberOfRevisionsWithAnomalie() + anomalieList.getNumberOfRevisionsWithoutAnomalie());
            percentagem = percentagem * 100;
            text = text + "ANOMALIE: " + anomalie + "\n    Number Of Revisions With Problem: " + anomalieList.getNumberOfRevisionsWithAnomalie()
                    + "          Number Of Revisions Without Problem: " + anomalieList.getNumberOfRevisionsWithoutAnomalie() + "          (Incidence: "
                    + (percentagem) + " %)"
                    + "\n    Revision Birth: " + anomalieList.getArtifactBirthNumber()
                    + "\n    Class Birth: " + anomalieList.getParentArtifactBirthNumber()
                    + "\n    Anomalie Birth: " + (anomalieList.getAnomalieBirthNumber() + anomalieList.getArtifactBirthNumber())
                    + "\n    Type: " + getTypeOfAnomalie(anomalieList.getTypeOfAnomalie())+"\n\n";
            System.out.println("Type: " + anomalieList.getTypeOfAnomalie());

        }

        anomaliesView.setInformation(text);
        //System.out.println("TEXTO:\n" + text);


        anomaliesView.setChartPanel(chartPanel);
    }

//    private String getTypeOfAnomalie(int type) {
//        if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NEVER_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Congenital Never Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_WITH_THE_CLASS) {
//            return "Congenital Not Corrected But Corrected One Time Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Congenital Not Corrected But Recurrently Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Congenital Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_WITH_THE_CLASS) {
//            return "Congenital Corrected But Corrected One Time Before Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Congenital Corrected But Recurrently Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NEVER_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Adquired Never Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_WITH_THE_CLASS) {
//            return "Adquired Not Corrected But Corrected One Time Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Adquired Not Corrected But Recurrently Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Adquired Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_WITH_THE_CLASS) {
//            return "Adquired Corrected But Corrected One Time Before Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
//            return "Adquired Corrected But Recurrently Corrected Born with the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NEVER_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Congenital Never Corrected Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_AFTER_THE_CLASS) {
//            return "Congenital Not Corrected But Corrected One Time Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Congenital Not Corrected But Recurrently Corrected Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Congenital Corrected Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_AFTER_THE_CLASS) {
//            return "Congenital Corrected But Corrected One Time Before Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Congenital Corrected But Recurrently Corrected Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NEVER_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Adquired Never Corrected Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_AFTER_THE_CLASS) {
//            return "Adquired Not Corrected But Corrected One Time Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Adquired Not Corrected But Recurrently Corrected Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Adquired Corrected Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_AFTER_THE_CLASS) {
//            return "Adquired Corrected But Corrected One Time Before Born after the class";
//        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
//            return "Adquired Corrected But Recurrently Corrected Born after the class";
//        } else {
//            return "";
//        }
//    }
    
    private String getTypeOfAnomalie(int type) {
        if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NEVER_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Congenital Not Corrected Of Simple Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_WITH_THE_CLASS) {
            return "Congenital Not Corrected Of Double Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Congenital Not Corrected Of Recurrent Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Congenital Corrected Of Simple Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_WITH_THE_CLASS) {
            return "Congenital Corrected Of Double Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Congenital Corrected Of Recurrent Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NEVER_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Adquired Not Corrected Of Simple Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_WITH_THE_CLASS) {
            return "Adquired Not Corrected Of Double Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Adquired Not Corrected Of Recurrent Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Adquired Corrected Of Simple Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_WITH_THE_CLASS) {
            return "Adquired Corrected Of Double Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_RECURRENT_CORRECTED_BORN_WITH_THE_CLASS) {
            return "Adquired Corrected Of Recurrent Pattern";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NEVER_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Congenital Never Corrected Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_AFTER_THE_CLASS) {
            return "Congenital Not Corrected But Corrected One Time Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Congenital Not Corrected But Recurrently Corrected Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Congenital Corrected Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_AFTER_THE_CLASS) {
            return "Congenital Corrected But Corrected One Time Before Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_CONGENITAL_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Congenital Corrected But Recurrently Corrected Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NEVER_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Adquired Never Corrected Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_BUT_CORRECTED_ONE_TIME_BORN_AFTER_THE_CLASS) {
            return "Adquired Not Corrected But Corrected One Time Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_NOT_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Adquired Not Corrected But Recurrently Corrected Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Adquired Corrected Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_BUT_CORRECTED_UM_TIME_BEFORE_BORN_AFTER_THE_CLASS) {
            return "Adquired Corrected But Corrected One Time Before Born after the class";
        } else if (type == Constants.ANOMALIE_TYPE_ADQUIRED_CORRECTED_RECURRENT_CORRECTED_BORN_AFTER_THE_CLASS) {
            return "Adquired Corrected But Recurrently Corrected Born after the class";
        } else {
            return "";
        }
    }

    private void showClass(int index) {
        String anomalie = anomalies.get(anomalieIndex);
        String className = classes.get(index);
        GenericAnomalies genericAnomalies = projectAnomalies.getClassAnomalies(className);
        JPanel chartPanel = new AnomalieChart(genericAnomalies, anomalie);
        
        String text = "CLASS: " + className +"      last name: "+genericAnomalies.getGenericLastName()+"\n";
        if(!genericAnomalies.getAlternativeNames().isEmpty()){
            text = text+"Alternative Names: ";
        }
        for (String aux : genericAnomalies.getAlternativeNames()) {
            text = text+" " + aux + "   ,  ";
        }
        text = text+"\n";
        if (anomalie.equals("ALL ANOMALIES")) {
            List<String> anomaliesNames = genericAnomalies.getAnomalies();
            for (String str : anomaliesNames) {
                AnomalieList anomalieList = genericAnomalies.getAnomalieList(str);
                double percentagem = anomalieList.getNumberOfRevisionsWithAnomalie();
                percentagem = percentagem / (anomalieList.getNumberOfRevisionsWithAnomalie() + anomalieList.getNumberOfRevisionsWithoutAnomalie());
                percentagem = percentagem * 100;

                text = text + "ANOMALIE: " + str + "\n    Number Of Revisions With Problem: " + anomalieList.getNumberOfRevisionsWithAnomalie()
                        + "          Number Of Revisions Without Problem: " + anomalieList.getNumberOfRevisionsWithoutAnomalie() + "          ("
                        + (percentagem) + " %)"
                        + "\n    Revision Birth: " + anomalieList.getArtifactBirthNumber()
                        + "\n    Class Birth: " + anomalieList.getParentArtifactBirthNumber()
                        + "\n    Anomalie Birth: " + (anomalieList.getAnomalieBirthNumber() + anomalieList.getArtifactBirthNumber())
                        + "\n    Type: " + getTypeOfAnomalie(anomalieList.getTypeOfAnomalie()) + "\n\n";
                System.out.println("Type: " + anomalieList.getTypeOfAnomalie());
            }
        } else {
            AnomalieList anomalieList = genericAnomalies.getAnomalieList(anomalie);
            double percentagem = anomalieList.getNumberOfRevisionsWithAnomalie();
            percentagem = percentagem / (anomalieList.getNumberOfRevisionsWithAnomalie() + anomalieList.getNumberOfRevisionsWithoutAnomalie());
            percentagem = percentagem * 100;
            text = text + "ANOMALIE: " + anomalie + "\n    Number Of Revisions With Problem: " + anomalieList.getNumberOfRevisionsWithAnomalie()
                    + "          Number Of Revisions Without Problem: " + anomalieList.getNumberOfRevisionsWithoutAnomalie() + "          ("
                    + (percentagem) + " %)"
                    + "\n    Revision Birth: " + anomalieList.getArtifactBirthNumber()
                    + "\n    Class Birth: " + anomalieList.getParentArtifactBirthNumber()
                    + "\n    Anomalie Birth: " + (anomalieList.getAnomalieBirthNumber() + anomalieList.getArtifactBirthNumber())
                    + "\n    Type: " + getTypeOfAnomalie(anomalieList.getTypeOfAnomalie())+"\n\n";
            System.out.println("Type: " + anomalieList.getTypeOfAnomalie());

        }

        anomaliesView.setInformation(text);
        
        anomaliesView.setChartPanel(chartPanel);
    }

    private void showPackage(int index) {
        String anomalie = anomalies.get(anomalieIndex);
        String packageName = packages.get(index);
        GenericAnomalies genericAnomalies = projectAnomalies.getPackageAnomalies(packageName);
        JPanel chartPanel = new AnomalieChart(genericAnomalies, anomalie);
        anomaliesView.setChartPanel(chartPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(AnomaliesView.ACTION_OK_ANOMALIES)) {
            showAnomalies(anomaliesView.getAnomalieIndex());
        } else if (e.getActionCommand().equals(AnomaliesView.ACTION_OK_METHODS)) {
            showMethod(anomaliesView.getMethodIndex());
        } else if (e.getActionCommand().equals(AnomaliesView.ACTION_OK_CLASSES)) {
            showClass(anomaliesView.getClassIndex());
        } else if (e.getActionCommand().equals(AnomaliesView.ACTION_OK_PACKAGES)) {
            showPackage(anomaliesView.getPackageIndex());
        }
    }

    private void orderByName(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).compareTo(list.get(j)) > 0) {
                    String aux = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, aux);
                }
            }
        }
    }

    /*public static void main(String args[]) {
     String path = System.getProperty("user.home") + "/.archd/";
     JavaProjectsService javaprojectsService = new JavaProjectsService();
     List<Project> projects = javaprojectsService.getProjects();
     Project p = null;
     for (Project project : projects) {
     if (project.getName().equals("MapDB")) {
     p = project;
     break;
     }
     }
     if (p != null) {

     AnomaliesController anomaliesController = new AnomaliesController(p, null);

     }
     }*/
}
