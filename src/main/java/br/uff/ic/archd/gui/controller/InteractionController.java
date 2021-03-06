/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.archd.gui.controller;

import br.uff.ic.archd.git.service.JavaProjectsService;
import br.uff.ic.archd.git.service.ProjectRevisionsService;
import br.uff.ic.archd.service.mining.CreateMiningFile;
import br.uff.ic.archd.gui.view.InteractionViewer;
import br.uff.ic.archd.javacode.JavaAbstract;
import br.uff.ic.archd.javacode.JavaAbstractExternal;
import br.uff.ic.archd.javacode.JavaAttribute;
import br.uff.ic.archd.javacode.JavaClass;
import br.uff.ic.archd.javacode.JavaConstructorService;
import br.uff.ic.archd.javacode.JavaData;
import br.uff.ic.archd.javacode.JavaInterface;
import br.uff.ic.archd.javacode.JavaMethod;
import br.uff.ic.archd.javacode.JavaMethodInvocation;
import br.uff.ic.archd.javacode.JavaPackage;
import br.uff.ic.archd.javacode.JavaPrimitiveType;
import br.uff.ic.archd.javacode.JavaProject;
import br.uff.ic.archd.javacode.ProjectAuthors;
import br.uff.ic.archd.javacode.RevisionAuthor;
import br.uff.ic.archd.model.Project;
import br.uff.ic.dyevc.application.branchhistory.model.BranchRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.LineRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.ProjectRevisions;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.RevisionsBucket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author wallace
 */
public class InteractionController implements ActionListener {

    private InteractionViewer interactionViewer;
    private JavaConstructorService javaConstructorService;
    private ProjectRevisionsService projectRevisionsService;
    private JavaProject javaProject;
    private ProjectRevisions projectRevisions;
    private ProjectRevisions newProjectRevisions;
    List<JavaProject> javaProjects;
    private Project project;
    private Revision currentRevision;
    private ProjectAuthors projectAuthors;

    InteractionController(Project project) {

        long antTime = System.currentTimeMillis();

        projectRevisionsService = new ProjectRevisionsService();
        javaConstructorService = new JavaConstructorService();
        newProjectRevisions = null;
        try {
            projectRevisions = projectRevisionsService.getProject(project.getPath(), project.getName());
            projectAuthors = projectRevisionsService.getProjectAuthors();
            System.out.println("ORIGINAL ROOT: " + projectRevisions.getRoot().getId());
            System.out.println("ORIGINAL HEAD: " + projectRevisions.getBranchesRevisions().get(0).getHead().getId());
            System.out.println("Vai limpar");
            newProjectRevisions = cleanProjectRevisionsLine(projectRevisions);
            System.out.println("Limpou");
            System.out.println("Number of Revisions: " + newProjectRevisions.getRevisionsBucket().getRevisionCollection().size());
            System.out.println("Branches: " + newProjectRevisions.getBranchesRevisions().size());
            System.out.println("ROOT: " + newProjectRevisions.getRoot().getId());
            System.out.println("HEAD: " + newProjectRevisions.getBranchesRevisions().get(0).getHead().getId());
            System.out.println("Ultima revisão: " + newProjectRevisions.getBranchesRevisions().get(0).getHead().getId() + "    next: " + newProjectRevisions.getBranchesRevisions().get(0).getHead().getNext().size()
                    + "     prev: " + newProjectRevisions.getBranchesRevisions().get(0).getHead().getPrev().size());
            System.out.println("penultima revisão: " + newProjectRevisions.getBranchesRevisions().get(0).getHead().getPrev().get(0).getId());

            System.out.println("Numero total de autores: " + projectAuthors.getAuthors().size());
            Collection<Revision> collRevision = newProjectRevisions.getRevisionsBucket().getRevisionCollection();
            for (String author : projectAuthors.getAuthors()) {
                Iterator<Revision> it = collRevision.iterator();
                int numOfRevisions = 0;
                while (it.hasNext()) {
                    RevisionAuthor autAux = projectAuthors.getRevisionAuthor(it.next().getId());
                    if (autAux.getAuthor().equals(author)) {
                        numOfRevisions++;
                    }
                }
                System.out.println("Author: " + author + "    numero: " + numOfRevisions);
            }

        } catch (Exception e) {
            System.out.println("Erro Revisions: " + e.getMessage());
        }

        //javaProject = javaConstructorService.createProjects(project.getCodeDirs(), project.getPath());
        /*List<String> newCodeDirs = new LinkedList();
         for(String codeDir :  project.getCodeDirs()){
         String newCodeDir = codeDir.substring(project.getPath().length(),codeDir.length());
         if(newCodeDir.startsWith("/")){
         newCodeDir = newCodeDir.substring(1);
         }
         System.out.println("Code Dir: "+newCodeDir);
         newCodeDirs.add(newCodeDir);
         }*/
        //javaProject = javaConstructorService.getProjectByRevision(project.getName(), project.getCodeDirs(), project.getPath(), "revisionteste");
        //javaProjects = javaConstructorService.getAllProjectsRevision(project.getName(), project.getCodeDirs(), project.getPath(), newProjectRevisions);
        //javaProject = javaProjects.get(javaProjects.size() - 1);
        //System.out.println("Revision do projeto: " + javaProject.getRevisionId());
        this.project = project;
        //javaProject = javaConstructorService.createProjectsFromXML("/home/wallace/.archd/HISTORY/1/");
        currentRevision = newProjectRevisions.getBranchesRevisions().get(0).getHead();
        javaProject = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), currentRevision.getId(), newProjectRevisions.getName());
        List<JavaAbstract> list = javaProject.getClasses();
        orderByName(list);
        String classesString[] = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            classesString[i] = list.get(i).getFullQualifiedName();
        }
        list = javaProject.getInterfaces();
        orderByName(list);
        String InterfacesString[] = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            InterfacesString[i] = list.get(i).getFullQualifiedName();
        }

        interactionViewer = new InteractionViewer(classesString, InterfacesString);
        interactionViewer.setController(this);

        showDados();

        //showStatistics();
        //******** parte de baixo comente e descometne a vontade
        //writeInFilesStatistics();
        //criar arquivo para minerar
        //CreateMiningFile createMiningFile = new CreateMiningFile(project.getName());
        //createMiningFile.createMethodsFileMiningByFile(newProjectRevisions, project, javaConstructorService);
        //createMiningFile.createClassesFileMining(newProjectRevisions, project, javaConstructorService);
        javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), currentRevision.getId(), newProjectRevisions.getName());
        interactionViewer.setRevisionLabel(currentRevision.getId());

        long proxTime = System.currentTimeMillis();
        System.out.println("ROOT: " + newProjectRevisions.getRoot().getId());
        System.out.println("HEAD: " + newProjectRevisions.getBranchesRevisions().get(0).getHead().getId());
        System.out.println("TEMPO TOTAL: " + ((proxTime - antTime) / 1000));

        interactionViewer.setVisible(true);
    }

    private void showDados() {
        //interactionViewer

        interactionViewer.cleanText();
        interactionViewer.appendDadosText("******************* Número total de classes: " + javaProject.getClasses().size());
        interactionViewer.appendDadosText("******************* Número total de interfaces: " + javaProject.getInterfaces().size());
        interactionViewer.appendDadosText("******************* Número total de classes externas chamadas diretamente: " + javaProject.getNumberOfViewExternalClasses());
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("************ Classes Mestres ************** numero: " + javaProject.getLeaderClasses().size() + "");
        for (JavaAbstract javaClazz : javaProject.getLeaderClasses()) {
            interactionViewer.appendDadosText(javaClazz.getFullQualifiedName());
        }

        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("************ Classes Possivelmente Mestres ************** numero: " + javaProject.getPossibleLeaderClasses().size() + "");
        for (JavaAbstract javaClazz : javaProject.getPossibleLeaderClasses()) {
            interactionViewer.appendDadosText(javaClazz.getFullQualifiedName());
        }

        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("************ Classes Inteligentes ************** numero: " + javaProject.getSimpleSmartClasses().size() + "");
        for (JavaClass javaClazz : javaProject.getSimpleSmartClasses()) {
            interactionViewer.appendDadosText(javaClazz.getFullQualifiedName());
        }

        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("************ Classes Completamente Inteligentes ************** numero: " + javaProject.getFullSmartClasses().size() + "");
        for (JavaClass javaClazz : javaProject.getFullSmartClasses()) {
            interactionViewer.appendDadosText(javaClazz.getFullQualifiedName());
        }

        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("");
        interactionViewer.appendDadosText("************ Classes Ingenuas ************** numero: " + javaProject.getFoolClasses().size() + "");
        for (JavaClass javaClazz : javaProject.getFoolClasses()) {
            interactionViewer.appendDadosText(javaClazz.getFullQualifiedName());
        }
    }

    private void showStatistics() {
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n***************************8 ESTATISTICA ***************************");
        int count = 0;

        Revision rev = newProjectRevisions.getRoot();
        JavaProject aux = null;
        int revs = 0;
        int k = 0;
        while (rev != null) {
            //JavaProject jp = javaProjects.get(i);
            JavaProject jp = null;
            //System.out.println("REV ID: "+rev.getId());
            jp = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), rev.getId(), newProjectRevisions.getName());

            k++;
            boolean flag = false;
            for (JavaAbstract javaAbstract : jp.getClasses()) {
                boolean flag2 = false;
                JavaClass jc = (JavaClass) javaAbstract;
                for (JavaMethod jm : jc.getMethods()) {
                    if (jm.getCyclomaticComplexity() > 20) {
                        count++;
                        if (!flag) {
                            flag = true;
                            System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                        }
                        if (!flag2) {
                            flag2 = true;
                            System.out.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                        }
                        System.out.println("Método: " + jm.getMethodSignature() + "     Complexity: " + jm.getCyclomaticComplexity() + "         size: " + jm.getSizeInChars());
                    }
                }
            }
            if (rev.getNext().size() == 0) {
                rev = null;
            } else {
                rev = rev.getNext().get(0);
            }
            revs++;
        }

        System.out.println("REVISOES VISTAS: " + revs);
        System.out.println("COUNT: " + count);

        rev = newProjectRevisions.getRoot();
        revs = 0;
        k = 0;
        while (rev != null) {
            //JavaProject jp = javaProjects.get(i);
            JavaProject jp = null;
            jp = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), rev.getId(), newProjectRevisions.getName());

            k++;
            boolean flag = false;
            int methodCont = 0;
            int mc = 0;
            for (JavaAbstract javaAbstract : jp.getClasses()) {
                boolean flag2 = false;
                JavaClass jc = (JavaClass) javaAbstract;

                for (JavaMethod jm : jc.getMethods()) {

                    if (jm.getCyclomaticComplexity() > 20) {
                        count++;
                        mc++;
                        //System.out.println("Método: "+jm.getMethodSignature()+"     Complexity: "+jm.getCyclomaticComplexity()+"         size: "+jm.getSizeInChars());
                    }
                    methodCont++;
                }

            }
            System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
            System.out.println("Methods: " + methodCont + "     metodos complexos: " + mc);
            if (rev.getNext().size() == 0) {
                rev = null;
            } else {
                rev = rev.getNext().get(0);
            }
            revs++;
        }

        System.out.println("***************************************************************************8");

        rev = newProjectRevisions.getRoot();
        revs = 0;
        k = 0;
        while (rev != null) {
            //JavaProject jp = javaProjects.get(i);
            JavaProject jp = null;
            jp = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), rev.getId(), newProjectRevisions.getName());

            k++;
            boolean flag = false;
            int methodCont = 0;
            int mc = 0;
            for (JavaAbstract javaAbstract : jp.getClasses()) {
                boolean flag2 = false;
                JavaClass jc = (JavaClass) javaAbstract;
                int ctc = jp.getClassesThatCall(javaAbstract).size();
                int ctu = jp.getClassesThatUsing(javaAbstract).size();
                if (ctc > 5 || ctu > 5) {
                    if (!flag) {
                        flag = true;
                        System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                    }
                    System.out.println("CLASS: " + jc.getFullQualifiedName() + "      Class using: " + ctu + "   class call: " + ctc);
                }

            }

            if (rev.getNext().size() == 0) {
                rev = null;
            } else {
                rev = rev.getNext().get(0);
            }
            revs++;
        }

        System.out.println("***************************************************************************8");

        rev = newProjectRevisions.getRoot();
        revs = 0;
        k = 0;
        JavaProject ant = null;
        while (rev != null) {
            //JavaProject jp = javaProjects.get(i);
            JavaProject jp = null;
            jp = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), rev.getId(), newProjectRevisions.getName());

            k++;
            boolean flag = false;
            for (JavaAbstract javaAbstract : jp.getClasses()) {
                boolean flag2 = false;
                JavaClass jc = (JavaClass) javaAbstract;
                JavaClass antClass = null;
                if (ant != null) {
                    antClass = (JavaClass) ant.getClassByName(jc.getFullQualifiedName());
                }
                for (JavaMethod jm : jc.getMethods()) {
                    if (antClass != null) {
                        JavaMethod antMethod = antClass.getMethodBySignature(jm.getMethodSignature());
                        if (antMethod != null) {
                            if (jm.getCyclomaticComplexity() != antMethod.getCyclomaticComplexity()) {
                                if (!flag) {
                                    flag = true;
                                    System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                }
                                if (!flag2) {
                                    flag2 = true;
                                    System.out.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                                }
                                System.out.println("Método: " + jm.getMethodSignature() + "     Complexity: " + jm.getCyclomaticComplexity() + "         size: " + jm.getSizeInChars()
                                        + "     diff complexity: " + (jm.getCyclomaticComplexity() - antMethod.getCyclomaticComplexity()) + "      diff size: " + (jm.getSizeInChars() - antMethod.getSizeInChars()));
                            }
                        }
                    }

                }
            }
            if (rev.getNext().size() == 0) {
                rev = null;
            } else {
                rev = rev.getNext().get(0);
            }
            ant = jp;
            revs++;
        }

        System.out.println("***************************************************************************8");

        rev = newProjectRevisions.getRoot();
        revs = 0;
        k = 0;
        ant = null;
        while (rev != null) {
            //JavaProject jp = javaProjects.get(i);
            JavaProject jp = null;
            //System.out.println("REV ID: "+rev.getId());
            jp = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), rev.getId(), newProjectRevisions.getName());

            k++;
            boolean flag = false;
            for (JavaAbstract javaAbstract : jp.getClasses()) {
                boolean flag2 = false;
                JavaClass jc = (JavaClass) javaAbstract;
                JavaClass antClass = null;
                if (ant != null) {
                    antClass = (JavaClass) ant.getClassByName(jc.getFullQualifiedName());
                }
                for (JavaMethod jm : jc.getMethods()) {
                    if (antClass != null) {
                        JavaMethod antMethod = antClass.getMethodBySignature(jm.getMethodSignature());
                        if (antMethod != null) {
                            if (jm.getCyclomaticComplexity() != antMethod.getCyclomaticComplexity()) {
                                if (!flag) {
                                    flag = true;
                                    System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                }
                                if (!flag2) {
                                    flag2 = true;
                                    System.out.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                                }
                                System.out.println("Método: " + jm.getMethodSignature() + "     Complexity: " + jm.getCyclomaticComplexity() + "         size: " + jm.getSizeInChars()
                                        + "     diff complexity: " + (jm.getCyclomaticComplexity() - antMethod.getCyclomaticComplexity()) + "      diff size: " + (jm.getSizeInChars() - antMethod.getSizeInChars()));
                            }
                        }
                    }

                }
            }
            if (rev.getNext().size() == 0) {
                rev = null;
            } else {
                rev = rev.getNext().get(0);
            }
            ant = jp;
            revs++;
        }

        System.out.println("***************************************************************************8");

    }

    private ProjectRevisions cleanProjectRevisions(ProjectRevisions projectRevisions) {
        List<BranchRevisions> branches = new LinkedList();
        ProjectRevisions newProjectRevisions = new ProjectRevisions(projectRevisions.getName());
        RevisionsBucket revisionsBucket = new RevisionsBucket();
        Revision newRoot = revisionsBucket.getRevisionById(projectRevisions.getRoot().getId());
        if (newRoot == null) {
            newRoot = new Revision(projectRevisions.getRoot().getId());
            revisionsBucket.addRevision(newRoot);
        }
        int count = 0;
        newProjectRevisions.setRoot(newRoot);

        for (BranchRevisions branchRevisions : projectRevisions.getBranchesRevisions()) {
            Revision newHead = revisionsBucket.getRevisionById(branchRevisions.getHead().getId());
            if (newHead == null) {
                newHead = new Revision(branchRevisions.getHead().getId());
            }
            BranchRevisions newBranchRevisions = new BranchRevisions(branchRevisions.getName(), newHead);

            for (LineRevisions lineRevisions : branchRevisions.getLinesRevisions()) {
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
                while (aux != null) {
                    int i = 0;
                    while (i < 900 && aux.getPrev().size() != 0) {
                        aux = aux.getPrev().get(0);
                        i++;
                    }
                    newRevision = revisionsBucket.getRevisionById(aux.getId());
                    if (newRevision == null) {
                        newRevision = new Revision(aux.getId());
                        revisionsBucket.addRevision(newRevision);
                    }
                    newRevision.addNext(prox);
                    prox.addPrev(newRevision);
                    prox = newRevision;
                    newLineRevisions.addRevision(newRevision);

                    count++;
                    if (aux.getPrev().size() == 0) {
                        aux = null;
                    }
                }
                newBranchRevisions.addLineRevisions(lineRevisions);
            }
            branches.add(newBranchRevisions);

        }
        System.out.println("Count: " + count);
        newProjectRevisions.setBranchesRevisions(branches);
        newProjectRevisions.setRevisionsBucket(revisionsBucket);
        return newProjectRevisions;
    }

    private ProjectRevisions cleanProjectRevisionsLast(ProjectRevisions projectRevisions) {
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
            //System.out.println("ID: " + newRevision.getId());
            int i = 0;
            while (i < 50 && aux != null) {
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
            /*for (LineRevisions lineRevisions : branchRevisions.getLinesRevisions()) {
             System.out.println("Linha numero: " + lineRevisions.getRevisions().size());
             LineRevisions newLineRevisions = new LineRevisions(newHead);
             Revision aux = lineRevisions.getHead();
             Revision newRevision = new Revision(aux.getId());
             Revision prox = newRevision;
             newLineRevisions.addRevision(newRevision);
             revisionsBucket.addRevision(newRevision);
             int i = 0;
             while (i < 5 && aux != null) {
             i++;
             //System.out.println("I: "+i);
             aux = aux.getPrev().get(0);
             newRevision = new Revision(aux.getId());
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
             }*/
            branches.add(newBranchRevisions);

        }
        System.out.println("Count: " + count);
        newProjectRevisions.setBranchesRevisions(branches);
        newProjectRevisions.setRevisionsBucket(revisionsBucket);
        return newProjectRevisions;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(InteractionViewer.ACTION_UPDATE_CLASS)) {
            showClassFunctions(interactionViewer.getClassSelected());
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_UPDATE_INTERFACE)) {
            showClassFunctions(interactionViewer.getInterfaceSelected());
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_SEARCH_REVISION)) {
            searchRevision(interactionViewer.getSearchRevision());
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_PROX_REVISION)) {
            showProxRevision();
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_ANT_REVISION)) {
            showAntRevision();
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_CODE_CLASS)) {
            showCode(interactionViewer.getClassSelected());
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_CODE_INTERFACE)) {
            showCode(interactionViewer.getInterfaceSelected());
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_VIEW_GRAPH)) {
            showGraph();
        } else if (e.getActionCommand().equals(InteractionViewer.ACTION_VIEW_PACKAGE_GRAPH)) {
            showPackageGraph();
        }
    }

    public void showGraph() {
        if (javaProject != null) {
            GraphCreator graph = new GraphCreator();
            graph.createSimpleGraph(javaProject);
        }
    }
    
    public void showPackageGraph(){
        if (javaProject != null) {
            GraphPackageCreator graph = new GraphPackageCreator();
            graph.createSimpleGraph(javaProject);
        }
    }

    private void searchRevision(String revisionId) {
        Integer revisionNumber = null;
        try {
            revisionNumber = Integer.parseInt(revisionId);
            System.out.println("O numero é: " + revisionNumber);
        } catch (Exception e) {
            System.out.println("Não é numero");
        }
        Revision aux = null;
        if (revisionNumber == null) {
            aux = newProjectRevisions.getRevisionsBucket().getRevisionById(revisionId);
        } else {
            Revision rev = newProjectRevisions.getRoot();
            int i = 0;
            while (i < revisionNumber && rev != null) {
                i++;
                if (rev.getNext().size() == 0) {
                    rev = null;
                } else {
                    rev = rev.getNext().get(0);
                }
            }
            aux = rev;
        }
        if (aux != null) {
            System.out.println("Revision: " + aux.getId());
            javaProject = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), aux.getId(), newProjectRevisions.getName());
            currentRevision = aux;
            interactionViewer.setRevisionLabel(revisionId);
            showDados();
            updateClassesAndInterfaces();
            //mostrar os novos dados
        }

        System.out.println("&&&&&&&&&&&&&&&&&&&&&&& INFORMACOES DO AUTOR &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        RevisionAuthor revisionAuthor = projectAuthors.getRevisionAuthor(javaProject.getRevisionId());
        if (revisionAuthor != null) {
            System.out.println("Revision Author: " + revisionAuthor.getAuthor());
            System.out.println("Revision Commiter: " + revisionAuthor.getCommiter());
            System.out.println("Revision Short Message: " + revisionAuthor.getShortMessage());
            System.out.println("Revision Date: " + revisionAuthor.getCommitDate());
        }
    }

    private void showAntRevision() {
        Revision aux = currentRevision.getPrev().get(0);
        if (aux != null) {
            searchRevision(aux.getId());
        }
    }

    private void showProxRevision() {
        Revision aux = currentRevision.getNext().get(0);
        if (aux != null) {
            searchRevision(aux.getId());
        }
    }

    private void showCode(String className) {
        JavaAbstract javaAbstract = javaProject.getClassByName(className);
        if (javaAbstract != null) {
            //File file = new File(javaAbstract.getPath());
            try {
                String text = IOUtils.toString(new FileReader(javaAbstract.getPath()));
                interactionViewer.setCodeText(text);
            } catch (Exception e) {
            }
            System.out.println("CLASS: " + javaAbstract.getPath());
        }
    }

    private void updateClassesAndInterfaces() {
        List<JavaAbstract> list = javaProject.getClasses();
        orderByName(list);
        String classesString[] = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            classesString[i] = list.get(i).getFullQualifiedName();
        }
        list = javaProject.getInterfaces();
        orderByName(list);
        String InterfacesString[] = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            InterfacesString[i] = list.get(i).getFullQualifiedName();
        }

        interactionViewer.setClassesAndInterfaces(classesString, InterfacesString);

    }

    private void showClassFunctions(String className) {
        JavaAbstract javaAbstract = javaProject.getClassByName(className);
        String methods[] = null;
        String invocations[] = null;

        if (javaAbstract.getClass() == JavaClass.class) {
            System.out.println("\n\n ********** Classe: " + className);
            interactionViewer.appendText("\n\n ********** Classe: " + className);
            System.out.println("  Path: " + javaAbstract.getPath());
            interactionViewer.appendText("  Path: " + javaAbstract.getPath());
            if (((JavaClass) javaAbstract).getSuperClass() != null) {
                System.out.println("SuperClass: " + ((JavaClass) javaAbstract).getSuperClass().getFullQualifiedName());
                interactionViewer.appendText("SuperClass: " + ((JavaClass) javaAbstract).getSuperClass().getFullQualifiedName());
            }
            if (((JavaClass) javaAbstract).getImplementedInterfaces().size() > 0) {
                System.out.println("Impemented Interfaces: ");
                interactionViewer.appendText("Impemented Interfaces: ");
                for (JavaInterface javaInterface : ((JavaClass) javaAbstract).getImplementedInterfaces()) {
                    System.out.println("- " + javaInterface.getFullQualifiedName());
                    interactionViewer.appendText("- " + javaInterface.getFullQualifiedName());
                }
                System.out.println("");
                interactionViewer.appendText("");
            }
            if (((JavaClass) javaAbstract).getExternalImports().size() > 0) {
                System.out.println("External imports: ");
                interactionViewer.appendText("External imports: ");
                for (String externalImport : ((JavaClass) javaAbstract).getExternalImports()) {
                    System.out.println("- " + externalImport);
                    interactionViewer.appendText("- " + externalImport);
                }
                System.out.println("");
                interactionViewer.appendText("");
            }
            if (((JavaClass) javaAbstract).getAttributes().size() > 0) {
                System.out.println("Attributes: ");
                interactionViewer.appendText("Attributes: ");
                for (JavaAttribute javaAttribute : ((JavaClass) javaAbstract).getAttributes()) {
                    JavaData javaData = javaAttribute.getType();
                    if (javaData.getClass() == JavaClass.class || javaData.getClass() == JavaInterface.class) {
                        System.out.println("- " + ((JavaAbstract) javaAttribute.getType()).getFullQualifiedName() + "    " + javaAttribute.getName());
                        interactionViewer.appendText("- " + ((JavaAbstract) javaAttribute.getType()).getFullQualifiedName() + "    " + javaAttribute.getName());
                    } else if (javaData.getClass() == JavaPrimitiveType.class) {
                        System.out.println("- " + ((JavaPrimitiveType) javaAttribute.getType()).getName() + "    " + javaAttribute.getName());
                        interactionViewer.appendText("- " + ((JavaPrimitiveType) javaAttribute.getType()).getName() + "    " + javaAttribute.getName());
                    } else if (javaData.getClass() == JavaAbstractExternal.class) {
                        System.out.println("- " + ((JavaAbstractExternal) javaAttribute.getType()).getName() + "    " + javaAttribute.getName());
                        interactionViewer.appendText("- " + ((JavaAbstractExternal) javaAttribute.getType()).getName() + "    " + javaAttribute.getName());
                    }
                }
                System.out.println("");
                interactionViewer.appendText("");
            }
            interactionViewer.appendText("\n********* EXTERNAL DEPENDENCY CLASSES: ");
            for (JavaClass jc : ((JavaClass) javaAbstract).getExternalDependencyClasses()) {
                interactionViewer.appendText("ED: "+jc.getFullQualifiedName());
            }
            interactionViewer.appendText("\n**************** PACKAGE DEPENDENCY");
            for (JavaPackage jc : ((JavaClass) javaAbstract).getExternalDependencyPackages()) {
                interactionViewer.appendText("DP: "+jc.getName());
            }
            interactionViewer.appendText("\n********* INTERNAL DEPENDENCY CLASSES: ");
            for (JavaClass jc : ((JavaClass) javaAbstract).getInternalDependencyClasses()) {
                interactionViewer.appendText("ID: "+jc.getFullQualifiedName());
            }
            interactionViewer.appendText("\n********* CLIENT CLASSES: ");
            for (JavaClass jc : ((JavaClass) javaAbstract).getClientClasses()) {
                interactionViewer.appendText("ID: "+jc.getFullQualifiedName());
            }
            interactionViewer.appendText("\nACCES TO FOREIGN DATA: "+((JavaClass) javaAbstract).getAccessToForeignDataNumber());
            int weigth = 0;
            int atofdn = 0;
            for(JavaMethod jm : ((JavaClass) javaAbstract).getMethods()){
                weigth = weigth + jm.getCyclomaticComplexity();
                atofdn = atofdn + jm.getAccessToForeignDataNumber();
            }
            interactionViewer.appendText("ACCES TO FOREIGN DATA BY COUNT: "+atofdn);
            interactionViewer.appendText("WEIGHT METHOD COUNT: "+weigth);
            double tcc = ((JavaClass) javaAbstract).getNumberOfDirectConnections();
            int n = ((JavaClass) javaAbstract).getMethods().size();
            tcc = tcc / ((n * (n - 1)) / 2);
            interactionViewer.appendText("CLASS COHESION: "+tcc);
            interactionViewer.appendText("\n\n");
            methods = new String[((JavaClass) javaAbstract).getMethods().size()];
            System.out.println("Métodos: ");
            interactionViewer.appendText("Métodos: ");
            for (int i = 0; i < ((JavaClass) javaAbstract).getMethods().size(); i++) {
                JavaData returnType = ((JavaClass) javaAbstract).getMethods().get(i).getReturnType();
                String retType = returnType == null ? "vazio" : returnType.getName();
                System.out.println("- " + retType + "       " + ((JavaClass) javaAbstract).getMethods().get(i).getMethodSignature());
                System.out.println("Number Of Local Variables: " + ((JavaClass) javaAbstract).getMethods().get(i).getNumberOfLocalVariables());
                System.out.println("Number Of Parameters: " + ((JavaClass) javaAbstract).getMethods().get(i).getParameters().size());
                System.out.println("Number Of Lines: " + ((JavaClass) javaAbstract).getMethods().get(i).getNumberOfLines());
                System.out.println("Diff (in - out): " + ((JavaClass) javaAbstract).getMethods().get(i).getDiff());
                System.out.println("Modifie internal state: " + ((JavaClass) javaAbstract).getMethods().get(i).isChangeInternalState());
                System.out.println("Modifie internal state by call method: " + ((JavaClass) javaAbstract).getMethods().get(i).isChangeInternalState());
                System.out.println("Size: " + ((JavaClass) javaAbstract).getMethods().get(i).getSizeInChars());
                System.out.println("Cyclomatic complexity: " + ((JavaClass) javaAbstract).getMethods().get(i).getCyclomaticComplexity());
                interactionViewer.appendText("- " + retType + "       " + ((JavaClass) javaAbstract).getMethods().get(i).getMethodSignature());
                interactionViewer.appendText("Number Of Local Variables: " + ((JavaClass) javaAbstract).getMethods().get(i).getNumberOfLocalVariables());
                interactionViewer.appendText("Number Of Parameters: " + ((JavaClass) javaAbstract).getMethods().get(i).getParameters().size());
                interactionViewer.appendText("Number Of Lines: " + ((JavaClass) javaAbstract).getMethods().get(i).getNumberOfLines());
                interactionViewer.appendText("Acces to foreign data number: " + ((JavaClass) javaAbstract).getMethods().get(i).getAccessToForeignDataNumber());
                interactionViewer.appendText("Foreign Data Provider Number: " + ((JavaClass) javaAbstract).getMethods().get(i).getForeignDataProviderNumber());
                interactionViewer.appendText("Access to local data number: " + ((JavaClass) javaAbstract).getMethods().get(i).getAccessToLocalDataNumber());

                interactionViewer.appendText("Diff (in - out): " + ((JavaClass) javaAbstract).getMethods().get(i).getDiff());
                interactionViewer.appendText("Modifie internal state: " + ((JavaClass) javaAbstract).getMethods().get(i).isChangeInternalState());
                interactionViewer.appendText("Modifie internal state by call method: " + ((JavaClass) javaAbstract).getMethods().get(i).isChangeInternalState());
                interactionViewer.appendText("Size: " + ((JavaClass) javaAbstract).getMethods().get(i).getSizeInChars());
                interactionViewer.appendText("Cyclomatic complexity: " + ((JavaClass) javaAbstract).getMethods().get(i).getCyclomaticComplexity());
                interactionViewer.appendText("Acessor Method: " + ((JavaClass) javaAbstract).getMethods().get(i).isAnAcessorMethod());
                interactionViewer.appendText("Number of  Internal Method Invocations: " + ((JavaClass) javaAbstract).getMethods().get(i).getInternalMethodInvocations().size());

                methods[i] = ((JavaClass) javaAbstract).getMethods().get(i).getMethodSignature();
                for (JavaMethodInvocation jmi : ((JavaClass) javaAbstract).getMethods().get(i).getMethodInvocations()) {
                    //System.out.println("------ "+jmi.getJavaAbstract().getFullQualifiedName()+":"+jmi.getJavaMethod().getMethodSignature());
                    //System.out.println("JMI JAVA METHOD: "+jmi.getJavaAbstract().getFullQualifiedName());
                    if (jmi.getJavaMethod() != null) {
                        System.out.println("------ " + jmi.getJavaAbstract().getFullQualifiedName() + ":" + jmi.getJavaMethod().getMethodSignature());
                        interactionViewer.appendText("------ " + jmi.getJavaAbstract().getFullQualifiedName() + ":" + jmi.getJavaMethod().getMethodSignature() + "    is accessor: " + jmi.getJavaMethod().isAnAcessorMethod());
                    } else {
                        System.out.println("----um " + jmi.getJavaAbstract().getFullQualifiedName() + ":" + jmi.getUnknowMethodName());
                        interactionViewer.appendText("----um " + jmi.getJavaAbstract().getFullQualifiedName() + ":" + jmi.getUnknowMethodName());

                    }
                }
                interactionViewer.appendText("Chnaging Methods: ");
                for (JavaMethod changingMethod : ((JavaClass) javaAbstract).getMethods().get(i).getChangingMethods()) {
                    interactionViewer.appendText(changingMethod.getJavaAbstract().getFullQualifiedName() + "  :   " + changingMethod.getMethodSignature());
                }
                System.out.println("");
                interactionViewer.appendText("");
            }

            System.out.println("Classes that call the methods of this class: ");
            interactionViewer.appendText("Classes that call the methods of this class: ");
            for (JavaClass javaClass : javaProject.getClassesThatCall(javaAbstract)) {
                System.out.println("- " + javaClass.getFullQualifiedName());
                interactionViewer.appendText("- " + javaClass.getFullQualifiedName());
            }
            System.out.println("Classes uses this class: ");
            interactionViewer.appendText("Classes uses this class: ");
            for (JavaAbstract javaAbs : javaProject.getClassesThatUsing(javaAbstract)) {
                System.out.println("- " + javaAbs.getFullQualifiedName());
                interactionViewer.appendText("- " + javaAbs.getFullQualifiedName());
            }

            System.out.println("Dependencia Interna");
            interactionViewer.appendText("Dependencia Interna");
            for (JavaClass javaClass : ((JavaClass) javaAbstract).getInternalDependencyClasses()) {
                System.out.println("----- " + javaClass.getFullQualifiedName());
                interactionViewer.appendText("----- " + javaClass.getFullQualifiedName());
            }

            System.out.println("Dependencia Externa");
            interactionViewer.appendText("Dependencia Externa");
            for (JavaClass javaClass : ((JavaClass) javaAbstract).getExternalDependencyClasses()) {
                System.out.println("----- " + javaClass.getFullQualifiedName());
                interactionViewer.appendText("----- " + javaClass.getFullQualifiedName());
            }

            System.out.println("Dependencia de pacotes");
            interactionViewer.appendText("Dependencia de pacotes");
            for (JavaPackage javaPackage : ((JavaClass) javaAbstract).getExternalDependencyPackages()) {
                System.out.println("----- " + javaPackage.getName());
                interactionViewer.appendText("----- " + javaPackage.getName());
            }
            interactionViewer.appendText("Classe do mesmo pacote, dependencia externa: ");
            for(JavaClass jc : ((JavaClass) javaAbstract).getJavaPackage().getOnlyClasses()){
                interactionViewer.appendText("----- " + jc.getFullQualifiedName()+"  NOED: "+jc.getExternalDependencyClasses().size());
            }
            
            interactionViewer.appendText("\n\n PACKAGE INFORMATION: ");
            interactionViewer.appendText(" PACKAGE COHESION: "+((JavaClass) javaAbstract).getJavaPackage().getPackageCohesion());
            interactionViewer.appendText(" PACKAGE SIZE: "+((JavaClass) javaAbstract).getJavaPackage().getOnlyClasses().size());
            interactionViewer.appendText(" PACKAGE CLIENT CLASSES SIZE: "+((JavaClass) javaAbstract).getJavaPackage().getOnlyClasses().size());
            interactionViewer.appendText(" PACKAGE CLIENT PACKAGE SIZE: "+((JavaClass) javaAbstract).getJavaPackage().getClientPackages().size());
            interactionViewer.appendText(" PACKAGE CLIENT CLASSES: ");
            for(JavaClass jc : ((JavaClass) javaAbstract).getJavaPackage().getClientClasses()){
                interactionViewer.appendText("**** "+jc.getFullQualifiedName());
            }
            interactionViewer.appendText(" PACKAGE CLIENT PACKAGES: ");
            for(JavaPackage jp : ((JavaClass) javaAbstract).getJavaPackage().getClientPackages()){
                interactionViewer.appendText("**** "+jp.getName());
            }
            

//            for(int i = 0; i < ((JavaClass) javaAbstract).getMethods().size(); i++){
//                invocations[i] = ((JavaClass) javaAbstract).getMethods().get(i).getMethodInvocations();
//            }
        } else if (javaAbstract.getClass() == JavaInterface.class) {
            System.out.println("\n\n ********** Interface: " + className);
            interactionViewer.appendText("\n\n ********** Interface: " + className);
            System.out.println("  Path: " + javaAbstract.getPath());
            interactionViewer.appendText("  Path: " + javaAbstract.getPath());
            System.out.println("Métodos: ");
            interactionViewer.appendText("Métodos: ");
            methods = new String[((JavaInterface) javaAbstract).getMethods().size()];
            for (int i = 0; i < ((JavaInterface) javaAbstract).getMethods().size(); i++) {
                JavaData returnType = ((JavaInterface) javaAbstract).getMethods().get(i).getReturnType();
                String retType = returnType == null ? "void" : returnType.getName();
                System.out.println("- " + retType + "   " + ((JavaInterface) javaAbstract).getMethods().get(i).getMethodSignature());
                interactionViewer.appendText("- " + retType + "   " + ((JavaInterface) javaAbstract).getMethods().get(i).getMethodSignature());
                methods[i] = ((JavaInterface) javaAbstract).getMethods().get(i).getMethodSignature();
            }
        }

    }

    public void writeFileToMining() {
    }

    public void writeInFilesStatistics() {

        try {
            String path = System.getProperty("user.home") + "/.archd/";
            PrintWriter writer1 = new PrintWriter(path + "complexity_methods.txt", "UTF-8");
            PrintWriter writer2 = new PrintWriter(path + "complexity_methods_number.txt", "UTF-8");
            PrintWriter writer3 = new PrintWriter(path + "classes_using.txt", "UTF-8");
            PrintWriter writer4 = new PrintWriter(path + "diff_complexity.txt", "UTF-8");
            PrintWriter writer5 = new PrintWriter(path + "classes_complexity.txt", "UTF-8");
            PrintWriter writer6 = new PrintWriter(path + "shotgun_surgery.txt", "UTF-8");
            PrintWriter writer7 = new PrintWriter(path + "changing_methods.txt", "UTF-8");
            PrintWriter writer8 = new PrintWriter(path + "add_methods.txt", "UTF-8");
            PrintWriter writer9 = new PrintWriter(path + "feature_envy.txt", "UTF-8");
            PrintWriter writer10 = new PrintWriter(path + "god_class.txt", "UTF-8");
            PrintWriter writer11 = new PrintWriter(path + "god_method.txt", "UTF-8");
            PrintWriter writer12 = new PrintWriter(path + "god_package.txt", "UTF-8");
            PrintWriter writer13 = new PrintWriter(path + "misplaced_class.txt", "UTF-8");
            //PrintWriter writer8 = new PrintWriter(path + "changing_classes.txt", "UTF-8");

            Revision rev = newProjectRevisions.getRoot();
            JavaProject aux = null;
            int revs = 0;
            int k = 0;
            JavaProject ant = null;
            while (rev != null) {
                //JavaProject jp = javaProjects.get(i);
                JavaProject jp = null;
                //System.out.println("REV ID: "+rev.getId());
                jp = javaConstructorService.getProjectByRevisionAndSetRevision(project.getName(), project.getCodeDirs(), project.getPath(), rev.getId(), newProjectRevisions.getName());

                k++;
                boolean flag = false;
                int methodCont = 0;
                int mc = 0;
                boolean flag3 = false;
                boolean flag4 = false;
                boolean flag6 = false;
                boolean changingMethodsRevisions = false;
                boolean addMethodsRevisions = false;
                boolean featureEnvyBoolean = false;
                boolean godMethodBoolean = false;

                int totalCyclomaticComplexity = 0;
                writer5.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                for (JavaAbstract javaAbstract : jp.getClasses()) {
                    boolean flag2 = false;
                    boolean flag5 = false;
                    boolean flag7 = false;
                    boolean changingMethodsClasses = false;
                    boolean addMethodsClasses = false;

                    JavaClass jc = (JavaClass) javaAbstract;
                    JavaClass antClass = null;
                    if (ant != null) {
                        JavaAbstract antAbstract = ant.getClassByName(jc.getFullQualifiedName());
                        if (antAbstract != null) {
                            if (antAbstract.getClass() == JavaClass.class) {
                                antClass = (JavaClass) ant.getClassByName(jc.getFullQualifiedName());
                            } else {
                                System.out.println("ERA interface  em " + ant.getRevisionId() + "  num: " + (k - 1) + ",  virou classe em " + jp.getRevisionId() + " num: " + k + "  : " + antAbstract.getFullQualifiedName());
                            }
                        }

                        if (antClass != null) {
                            List<JavaMethod> addMethods = new LinkedList();
                            List<JavaMethod> removeMethods = new LinkedList();
                            List<JavaMethod> changeMethodsSignatureBefore = new LinkedList();
                            List<JavaMethod> changeMethodsSignatureAfter = new LinkedList();

                            for (JavaMethod jm : jc.getMethods()) {
                                boolean adicionou = true;
                                for (JavaMethod antMethod : antClass.getMethods()) {

                                    if (antMethod.getMethodSignature().equals(jm.getMethodSignature())) {
                                        adicionou = false;
                                        break;
                                    }

                                }
                                if (adicionou) {

                                    addMethods.add(jm);

                                    //writer8.println("+++++ " + jm.getMethodSignature());
                                }
                            }
                            for (JavaMethod antMethod : antClass.getMethods()) {
                                boolean removeu = true;
                                for (JavaMethod jm : jc.getMethods()) {

                                    if (antMethod.getMethodSignature().equals(jm.getMethodSignature())) {
                                        removeu = false;
                                        break;
                                    }

                                }
                                if (removeu) {

                                    boolean change = false;
                                    for (JavaMethod jm : addMethods) {
                                        if (antMethod.getName().equals(jm.getName())) {
                                            change = true;
                                            changeMethodsSignatureBefore.add(antMethod);
                                            changeMethodsSignatureAfter.add(jm);
                                            addMethods.remove(jm);
                                            break;
                                        }
                                    }
                                    if (!change) {
                                        removeMethods.add(antMethod);
                                    }

                                    //writer8.println("----- " + antMethod.getMethodSignature());
                                }
                            }

                            if (!addMethods.isEmpty() || !removeMethods.isEmpty() || !changeMethodsSignatureBefore.isEmpty()) {
                                writer8.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                writer8.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                                for (int i = 0; i < changeMethodsSignatureBefore.size(); i++) {
                                    writer8.println("change :  " + changeMethodsSignatureBefore.get(i).getMethodSignature() + "  --->  " + changeMethodsSignatureAfter.get(i).getMethodSignature());
                                }
                                for (JavaMethod jm : addMethods) {
                                    writer8.println("+++++ " + jm.getMethodSignature());
                                }
                                for (JavaMethod jm : removeMethods) {
                                    writer8.println("----- " + jm.getMethodSignature());
                                }

                            }
                        }

                    }
                    for (JavaMethod jm : jc.getMethods()) {

                        boolean changingMethodsMethods = false;

                        if (jm.getCyclomaticComplexity() > 20) {
                            if (!flag4) {
                                flag4 = true;
                                writer1.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                            }
                            if (!flag5) {
                                flag5 = true;
                                writer1.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                            }
                            writer1.println("Método: " + jm.getMethodSignature() + "     Complexity: " + jm.getCyclomaticComplexity() + "         size: " + jm.getSizeInChars());
                            mc++;
                        }

                        if (antClass != null) {
                            JavaMethod antMethod = antClass.getMethodBySignature(jm.getMethodSignature());
                            if (antMethod != null) {
                                if (jm.getCyclomaticComplexity() != antMethod.getCyclomaticComplexity()) {
                                    if (!flag) {
                                        flag = true;
                                        writer4.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                    }
                                    if (!flag2) {
                                        flag2 = true;
                                        writer4.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                                    }
                                    writer4.println("Método: " + jm.getMethodSignature() + "     Complexity: " + jm.getCyclomaticComplexity() + "         size: " + jm.getSizeInChars()
                                            + "     diff complexity: " + (jm.getCyclomaticComplexity() - antMethod.getCyclomaticComplexity()) + "      diff size: " + (jm.getSizeInChars() - antMethod.getSizeInChars()));
                                }

                                //ver changes methods call
                                //ver adição de métodos
                                for (JavaMethodInvocation methodInvocation : jm.getMethodInvocations()) {
                                    boolean adicionou = true;
                                    for (JavaMethodInvocation methodInvocationAnt : antMethod.getMethodInvocations()) {
                                        if (methodInvocation.getMethodName() != null) {
                                            if (methodInvocation.getMethodName().equals(methodInvocationAnt.getMethodName())) {
                                                adicionou = false;
                                                break;
                                            }
                                        } else {
                                            System.out.println("METHOD INVOCATION NULL: " + methodInvocation.getJavaAbstract().getFullQualifiedName());
                                        }
                                    }
                                    if (adicionou) {
                                        if (!changingMethodsRevisions) {
                                            changingMethodsRevisions = true;
                                            writer7.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                        }
                                        if (!changingMethodsClasses) {
                                            changingMethodsClasses = true;
                                            writer7.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                                        }
                                        if (!changingMethodsMethods) {
                                            changingMethodsMethods = true;
                                            writer7.println("\n######## Method: " + jm.getMethodSignature());
                                        }

                                        writer7.println("+++++ " + methodInvocation.getJavaAbstract().getFullQualifiedName() + ":" + methodInvocation.getMethodName());
                                    }
                                }

                                for (JavaMethodInvocation methodInvocationAnt : antMethod.getMethodInvocations()) {
                                    boolean removeu = true;
                                    for (JavaMethodInvocation methodInvocation : jm.getMethodInvocations()) {
                                        if (methodInvocationAnt.getMethodName() != null) {
                                            if (methodInvocationAnt.getMethodName().equals(methodInvocation.getMethodName())) {
                                                removeu = false;
                                                break;
                                            }
                                        } else {
                                            System.out.println("METHOD INVOCATION NULL: " + methodInvocation.getJavaAbstract().getFullQualifiedName());
                                        }
                                    }
                                    if (removeu) {
                                        if (!changingMethodsRevisions) {
                                            changingMethodsRevisions = true;
                                            writer7.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                        }
                                        if (!changingMethodsClasses) {
                                            changingMethodsClasses = true;
                                            writer7.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                                        }
                                        if (!changingMethodsMethods) {
                                            changingMethodsMethods = true;
                                            writer7.println("\n######## Method: " + jm.getMethodSignature());
                                        }

                                        writer7.println("----- " + methodInvocationAnt.getJavaAbstract().getFullQualifiedName() + ":" + methodInvocationAnt.getMethodName());
                                    }
                                }

                            } else {
                                if (!flag) {
                                    flag = true;
                                    writer4.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                                }
                                if (!flag2) {
                                    flag2 = true;
                                    writer4.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                                }
                                writer1.println("############# Método novo criado: " + jm.getMethodSignature() + "     Complexity: " + jm.getCyclomaticComplexity() + "         size: " + jm.getSizeInChars());

                            }

                        } else {
                            if (!flag) {
                                flag = true;
                                writer4.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                            }
                            if (!flag2) {
                                flag2 = true;
                                writer4.println("\n%%%%%%%%%%%% CLASSE NOVA CRIADA: " + jc.getFullQualifiedName() + "\n");
                            }
                        }
                        methodCont++;

                        if (jm.getChangingMethodsMetric() > 7 && jm.getChangingClassesMetric() > 5) {
                            if (!flag6) {
                                flag6 = true;
                                writer6.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "\n");
                            }
                            if (!flag7) {
                                flag7 = true;
                                writer6.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                            }
                            writer6.println("Método: " + jm.getMethodSignature() + "       CM: " + jm.getChangingMethodsMetric() + "       CC: " + jm.getChangingClassesMetric());
                        }

                    }
                    int ctc = jp.getClassesThatCall(javaAbstract).size();
                    int ctu = jp.getClassesThatUsing(javaAbstract).size();
                    if (ctc > 5 || ctu > 5) {
                        if (!flag3) {
                            flag3 = true;
                            writer3.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                        }
                        writer3.println("CLASS: " + jc.getFullQualifiedName() + "      Class using: " + ctu + "   class call: " + ctc);
                    }
                    writer5.print("******* CLASS: " + jc.getFullQualifiedName() + "         Total Complexity: " + jc.getTotalCyclomaticComplexity());
                    totalCyclomaticComplexity = totalCyclomaticComplexity + jc.getTotalCyclomaticComplexity();

                    //feature envy
                    if (!jc.getMethods().isEmpty()) {
                        List<JavaMethod> topValuesMethods = new LinkedList();
                        List<JavaMethod> auxList = new LinkedList();
                        List<JavaMethod> featureEnvyList = new LinkedList();
                        auxList.add(jc.getMethods().get(0));
                        for (int i = 1; i < jc.getMethods().size(); i++) {
                            JavaMethod javaMethod = jc.getMethods().get(i);
                            boolean inseriu = false;
                            for (int j = 0; j < auxList.size(); j++) {
                                JavaMethod jm2 = auxList.get(j);
                                if (javaMethod.getAccessToForeignDataNumber() > jm2.getAccessToForeignDataNumber()) {
                                    auxList.add(j, javaMethod);
                                    inseriu = true;
                                    break;
                                }
                            }
                            if (!inseriu) {
                                auxList.add(javaMethod);
                            }
                        }
                        int topNumber = jc.getMethods().size() / 10;
                        if (topNumber * 10 != jc.getMethods().size()) {
                            topNumber++;
                        }
                        for (int i = 0; i < topNumber; i++) {
                            topValuesMethods.add(auxList.get(i));
                        }

                        for (JavaMethod javaMethod : topValuesMethods) {
                            if ((javaMethod.getAccessToForeignDataNumber() >= 4)
                                    && (javaMethod.getAccessToLocalDataNumber() <= 3)
                                    && (javaMethod.getForeignDataProviderNumber() <= 3)) {
                                featureEnvyList.add(javaMethod);
                            }
                        }
                        if (!featureEnvyList.isEmpty()) {
                            if (!featureEnvyBoolean) {
                                writer9.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                                featureEnvyBoolean = true;
                            }
                            writer9.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                            for (JavaMethod javaMethod : featureEnvyList) {
                                writer9.println("########### " + javaMethod.getMethodSignature() + "        AFDN: " + javaMethod.getAccessToForeignDataNumber() + "    ALDN: " + javaMethod.getAccessToLocalDataNumber() + "     FDPN: " + javaMethod.getForeignDataProviderNumber());
                            }
                        }
                        //apagar abaixo, esta apenas pra teste do feature envy
//                        if (!jc.getMethods().isEmpty()) {
//                            
//                            writer9.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
//                            for (JavaMethod javaMethod : jc.getMethods()) {
//                                writer9.println("########### " + javaMethod.getMethodSignature() + "        AFDN: " + javaMethod.getAccessToForeignDataNumber() + "    ALDN: " + javaMethod.getAccessToLocalDataNumber() + "     FDPN: " + javaMethod.getForeignDataProviderNumber()+"    acessor? "+javaMethod.isAnAcessorMethod()+(javaMethod.isAnAcessorMethod()? "       "+javaMethod.getAccessedAttribute() :""));
//                            }
//                        }

                    }

                    //god method
                    if (!jc.getMethods().isEmpty()) {
                        List<JavaMethod> topValuesMethods = new LinkedList();
                        List<JavaMethod> auxList = new LinkedList();
                        List<JavaMethod> godMethodList = new LinkedList();
                        auxList.add(jc.getMethods().get(0));
                        for (int i = 1; i < jc.getMethods().size(); i++) {
                            JavaMethod javaMethod = jc.getMethods().get(i);
                            boolean inseriu = false;
                            for (int j = 0; j < auxList.size(); j++) {
                                JavaMethod jm2 = auxList.get(j);
                                if (javaMethod.getNumberOfLines() > jm2.getNumberOfLines()) {
                                    auxList.add(j, javaMethod);
                                    inseriu = true;
                                    break;
                                }
                            }
                            if (!inseriu) {
                                auxList.add(javaMethod);
                            }
                        }
                        int topNumber = jc.getMethods().size() / 5;
                        if (topNumber * 5 != jc.getMethods().size()) {
                            topNumber++;
                        }
                        for (int i = 0; i < topNumber; i++) {
                            topValuesMethods.add(auxList.get(i));
                        }

                        for (JavaMethod javaMethod : topValuesMethods) {
                            if ((javaMethod.getNumberOfLines() >= 70)
                                    && (javaMethod.getParameters().size() >= 4 || javaMethod.getNumberOfLocalVariables() >= 4)
                                    && (javaMethod.getCyclomaticComplexity() >= 4)) {
                                godMethodList.add(javaMethod);
                            }
                        }
                        if (!godMethodList.isEmpty()) {
                            if (!godMethodBoolean) {
                                writer11.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                                godMethodBoolean = true;
                            }
                            writer11.println("\n******* CLASS: " + jc.getFullQualifiedName() + "\n");
                            for (JavaMethod javaMethod : godMethodList) {
                                writer11.println("########### " + javaMethod.getMethodSignature() + "        LOC: " + javaMethod.getNumberOfLines() + "      NOLV: " + javaMethod.getNumberOfLocalVariables() + "    NOP: " + javaMethod.getParameters().size() + "     CYCLOCOMPLEXITY: " + javaMethod.getCyclomaticComplexity());
                            }
                        }

                    }

                }

                //GOD CLass
                if (!jp.getClasses().isEmpty()) {
                    List<JavaClass> topValuesClasses = new LinkedList();
                    List<JavaClass> auxList = new LinkedList();
                    List<JavaClass> godClassList = new LinkedList();
                    auxList.add((JavaClass) jp.getClasses().get(0));
                    for (int i = 1; i < jp.getClasses().size(); i++) {
                        JavaClass javaClass = (JavaClass) jp.getClasses().get(i);
                        boolean inseriu = false;
                        for (int j = 0; j < auxList.size(); j++) {
                            JavaClass jc2 = auxList.get(j);
                            if (javaClass.getAccessToForeignDataNumber() > jc2.getAccessToForeignDataNumber()) {
                                auxList.add(j, javaClass);
                                inseriu = true;
                                break;
                            }
                        }
                        if (!inseriu) {
                            auxList.add(javaClass);
                        }
                    }
                    int topNumber = jp.getClasses().size() / 5;
                    if (topNumber * 5 != jp.getClasses().size()) {
                        topNumber++;
                    }
                    for (int i = 0; i < topNumber; i++) {
                        topValuesClasses.add(auxList.get(i));
                    }

                    for (JavaClass javaClass : topValuesClasses) {
                        double tcc = javaClass.getNumberOfDirectConnections();
                        int n = javaClass.getMethods().size();
                        tcc = tcc / ((n * (n - 1)) / 2);
                        if ((javaClass.getAccessToForeignDataNumber() >= 4)
                                && (javaClass.getTotalCyclomaticComplexity() >= 20)
                                && (tcc <= 0.33)) {
                            godClassList.add(javaClass);
                        }
                    }
                    if (!godClassList.isEmpty()) {
                        writer10.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                        for (JavaClass javaClass : godClassList) {
                            double tcc = javaClass.getNumberOfDirectConnections();
                            int n = javaClass.getMethods().size();
                            tcc = tcc / ((n * (n - 1)) / 2);
                            writer10.println("******* CLASS: " + javaClass.getFullQualifiedName() + "        AFDN: " + javaClass.getAccessToForeignDataNumber() + "    WMC: " + javaClass.getTotalCyclomaticComplexity() + "     TCC: " + tcc);
                        }
                    }

                    //apagar abaixo, teste de god class
//                    if (!jp.getClasses().isEmpty()) {
//                        writer10.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
//                        for (JavaAbstract javaAbstract : jp.getClasses()) {
//                            JavaClass javaClass = (JavaClass) javaAbstract;
//                            double tcc = javaClass.getNumberOfDirectConnections();
//                            int n = javaClass.getMethods().size();
//                            tcc = tcc / ((n * (n - 1)) / 2);
//                            writer10.println("******* CLASS: " + javaClass.getFullQualifiedName() + "        AFDN: " + javaClass.getAccessToForeignDataNumber() + "    WMC: " + javaClass.getTotalCyclomaticComplexity() + "     TCC: " + tcc);
//                        }
//                    }
                }

                //GOD Package
                if (!jp.getPackages().isEmpty()) {
                    List<JavaPackage> topValuesPackages = new LinkedList();
                    List<JavaPackage> auxList = new LinkedList();
                    List<JavaPackage> godPackageList = new LinkedList();
                    auxList.add(jp.getPackages().get(0));
                    for (int i = 1; i < jp.getPackages().size(); i++) {
                        JavaPackage javaPackage = jp.getPackages().get(i);
                        boolean inseriu = false;
                        for (int j = 0; j < auxList.size(); j++) {
                            JavaPackage jp2 = auxList.get(j);
                            if (javaPackage.getOnlyClasses().size() > jp2.getOnlyClasses().size()) {
                                auxList.add(j, javaPackage);
                                inseriu = true;
                                break;
                            }
                        }
                        if (!inseriu) {
                            auxList.add(javaPackage);
                        }
                    }
                    int topNumber = jp.getPackages().size() / 4;
                    if (topNumber * 4 != jp.getPackages().size()) {
                        topNumber++;
                    }
                    for (int i = 0; i < topNumber; i++) {
                        topValuesPackages.add(auxList.get(i));
                    }

                    for (JavaPackage javaPackage : topValuesPackages) {
                        double packageCohesion = javaPackage.getPackageCohesion();

                        if ((javaPackage.getOnlyClasses().size() >= 20)
                                && (javaPackage.getClientClasses().size() >= 20)
                                && (javaPackage.getClientPackages().size() >= 3)) {
                            godPackageList.add(javaPackage);
                        }
                    }
                    if (!godPackageList.isEmpty()) {
                        writer12.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                        for (JavaPackage javaPackage : godPackageList) {

                            writer12.println("******* PACKAGE: " + javaPackage.getName() + "        PS: " + javaPackage.getOnlyClasses().size() + "    NOCC: " + javaPackage.getClientClasses().size() + "     NOCP: " + javaPackage.getClientPackages().size() + "      PC: " + javaPackage.getPackageCohesion());
                        }
                    }

                    //apagar abaixo, teste de god class
//                    if (!jp.getClasses().isEmpty()) {
//                        writer10.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
//                        for (JavaAbstract javaAbstract : jp.getClasses()) {
//                            JavaClass javaClass = (JavaClass) javaAbstract;
//                            double tcc = javaClass.getNumberOfDirectConnections();
//                            int n = javaClass.getMethods().size();
//                            tcc = tcc / ((n * (n - 1)) / 2);
//                            writer10.println("******* CLASS: " + javaClass.getFullQualifiedName() + "        AFDN: " + javaClass.getAccessToForeignDataNumber() + "    WMC: " + javaClass.getTotalCyclomaticComplexity() + "     TCC: " + tcc);
//                        }
//                    }
                }

                //MisplacedClass
                if (!jp.getClasses().isEmpty()) {
                    List<JavaClass> topValuesClasses = new LinkedList();
                    List<JavaClass> auxList = new LinkedList();
                    List<JavaClass> misplacedClassList = new LinkedList();
                    auxList.add((JavaClass) jp.getClasses().get(0));
                    for (int i = 1; i < jp.getClasses().size(); i++) {
                        JavaClass javaClass = (JavaClass) jp.getClasses().get(i);
                        boolean inseriu = false;
                        for (int j = 0; j < auxList.size(); j++) {
                            JavaClass jc2 = auxList.get(j);
                            if (javaClass.getExternalDependencyClasses().size() > jc2.getExternalDependencyClasses().size()) {
                                auxList.add(j, javaClass);
                                inseriu = true;
                                break;
                            }
                        }
                        if (!inseriu) {
                            auxList.add(javaClass);
                        }
                    }
                    int topNumber = jp.getClasses().size() / 4;
                    if (topNumber * 4 != jp.getClasses().size()) {
                        topNumber++;
                    }
                    for (int i = 0; i < topNumber; i++) {
                        topValuesClasses.add(auxList.get(i));
                    }

                    for (JavaClass javaClass : topValuesClasses) {
                        double classLocality = javaClass.getInternalDependencyClasses().size();
                        classLocality = classLocality / (javaClass.getInternalDependencyClasses().size() + javaClass.getExternalDependencyClasses().size());
                        if ((javaClass.getExternalDependencyClasses().size() >= 6)
                                && (javaClass.getExternalDependencyPackages().size() <= 3)
                                && (classLocality <= 0.33)) {
                            misplacedClassList.add(javaClass);
                        }
                    }
                    if (!misplacedClassList.isEmpty()) {
                        writer13.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                        for (JavaClass javaClass : misplacedClassList) {
                            double classLocality = javaClass.getInternalDependencyClasses().size();
                            classLocality = classLocality / (javaClass.getInternalDependencyClasses().size() + javaClass.getExternalDependencyClasses().size());
                            writer13.println("******* CLASS: " + javaClass.getFullQualifiedName() + "        NOED: " + javaClass.getExternalDependencyClasses().size() + "    DD: " + javaClass.getExternalDependencyPackages().size() + "     CL: " + classLocality);
                        }
                    }

                    //apagar abaixo, teste de god class
//                    if (!jp.getClasses().isEmpty()) {
//                        writer10.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
//                        for (JavaAbstract javaAbstract : jp.getClasses()) {
//                            JavaClass javaClass = (JavaClass) javaAbstract;
//                            double tcc = javaClass.getNumberOfDirectConnections();
//                            int n = javaClass.getMethods().size();
//                            tcc = tcc / ((n * (n - 1)) / 2);
//                            writer10.println("******* CLASS: " + javaClass.getFullQualifiedName() + "        AFDN: " + javaClass.getAccessToForeignDataNumber() + "    WMC: " + javaClass.getTotalCyclomaticComplexity() + "     TCC: " + tcc);
//                        }
//                    }
                }

                writer5.println("TOTAL COMPLEXITY: " + totalCyclomaticComplexity);

                writer2.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ REVISION: " + jp.getRevisionId() + "      num: " + k + "");
                writer2.println("Methods: " + methodCont + "     metodos complexos: " + mc);

                if (rev.getNext().size() == 0) {
                    rev = null;
                } else {
                    rev = rev.getNext().get(0);
                }
                ant = jp;
                revs++;
            }

            writer1.close();
            writer2.close();
            writer3.close();
            writer4.close();
            writer5.close();
            writer6.close();
            writer7.close();
            writer8.close();
            writer9.close();
            writer10.close();

            writer11.close();
            writer12.close();
            writer13.close();
            //writer8.close();

        } catch (Exception e) {
            System.out.println("Exception writefile: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void orderByName(List<JavaAbstract> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).getFullQualifiedName().compareTo(list.get(j).getFullQualifiedName()) > 0) {
                    JavaAbstract aux = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, aux);
                }
            }
        }
    }

//    public static void main(String args[]) {
//        JavaProjectsService javaprojectsService = new JavaProjectsService();
//        List<Project> projects = javaprojectsService.getProjects();
//        Project p = null;
//        for (Project project : projects) {
//            if (project.getName().equals("MapDB")) {
//                p = project;
//                break;
//            }
//        }
//        if (p != null) {
//            InteractionController interactionController = new InteractionController(p);
//        }
//    }
}
