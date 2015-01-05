/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.archd.db.dao;

import br.uff.ic.archd.javacode.JavaClass;
import br.uff.ic.archd.javacode.JavaInterface;
import br.uff.ic.archd.javacode.JavaPackage;
import br.uff.ic.archd.javacode.JavaProject;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class HsqldbClassesDao implements ClassesDao {

    private Connection connection;

    public HsqldbClassesDao() {
        try {
            File file = new File(Constants.DB_DIR);
            if (!file.exists()) {
                file.mkdirs();

            }
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:file:" + Constants.DB_DIR, "archd", "123");
            DatabaseMetaData dbData = connection.getMetaData();
            ResultSet tables = dbData.getTables(null, null, "JAVA_CLASSES", null);
            //System.out.println("NEXT "+tables.next());
            if (!tables.next()) {


                System.out.println("NAO POSSUI TABELA JAVA_CLASSES");
                Statement stm = connection.createStatement();

                stm.executeUpdate("create table JAVA_CLASSES (id bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                        + "name varchar(1000),"
                        + "superclass varchar(1000),"
                        + "path varchar(1000),"
                        + "revision_id varchar(1000),"
                        + "access_to_foreign_data_number int,"
                        + "number_of_direct_connections int,"
                        + "PRIMARY KEY (id));");
            } else {
                System.out.println("TABELA JAH EXISTE JAVA_CLASSES");
            }

            //stm.execute("SHUTDOWN");

        } catch (ClassNotFoundException e) {
            System.out.println("Erro ao carregar o driver JDBC. " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro de SQL: " + e.getMessage());
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERRO " + e.getMessage());
        }


    }

    @Override
    public void save(JavaClass javaClass) {
        try {
            Statement stm = connection.createStatement();
            System.out.println("Java class: " + javaClass);
            System.out.println("insert into JAVA_CLASSES (name ,"
                    + "superclass ,"
                    + "path ,"
                    + "revision_id ,"
                    + "java_package ) "
                    + " VALUES ('" + javaClass.getFullQualifiedName() + "',"
                    + (javaClass.getSuperClass() == null ? "null" : "'" + javaClass.getSuperClass().getFullQualifiedName() + "'") + ","
                    + "'" + javaClass.getPath() + "',"
                    + "'" + javaClass.getRevisionId() + "');");
            stm.executeUpdate("insert into JAVA_CLASSES (name ,"
                    + "superclass ,"
                    + "path ,"
                    + "revision_id,"
                    + "access_to_foreign_data_number,"
                    + "number_of_direct_connections)"
                    + " VALUES ('" + javaClass.getFullQualifiedName() + "',"
                    + (javaClass.getSuperClass() == null ? "null" : "'" + javaClass.getSuperClass().getFullQualifiedName() + "'") + ","
                    + "'" + javaClass.getPath() + "',"
                    + "'" + javaClass.getRevisionId() + "',"+javaClass.getAccessToForeignDataNumber()+","+javaClass.getNumberOfDirectConnections()+");");


            ResultSet rs = stm.executeQuery("CALL IDENTITY();");
            long id = 0;
            if (rs.next()) {
                id = rs.getLong(1);
            }
            /*ResultSet genKeys = stm.getGeneratedKeys();
             long id = 0;
             if (genKeys.next()) {
             id = genKeys.getLong(1);
             }*/
            javaClass.setId(id);
            System.out.println("Classe ID: " + javaClass.getId());

        } catch (SQLException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    @Override
    public List<JavaClass> getAllJavaClass() {
        return null;

    }

    @Override
    public List<JavaClass> getJavaClassesByRevisionId(JavaProject javaProject, String revisionId) {
        List<JavaClass> javaClasses = new LinkedList();
        try {
            Statement stm = connection.createStatement();
            
            //long t1 = System.currentTimeMillis();
            ResultSet rs = stm.executeQuery("select * from JAVA_CLASSES where revision_id='" + revisionId + "';");
            //long t2 = System.currentTimeMillis();
            //System.out.println("Pegar todas as classes de uma revisão (somente o select) : "+(t2-t1)+"  milisegundos");
            
            int i = 0;

            while (rs.next()) {
                String superClassString = rs.getString("superclass");

                JavaClass superClass = null;
                if (superClassString != null) {
                    String superClassSplit[] = superClassString.split("\\.");
                    String superClassName = superClassSplit[superClassSplit.length - 1];
                    int n = superClassString.length() - (superClassName.length() + 1);
                    if (n < 0) {
                        n = 0;
                    }
                    String superClassPackage = superClassString.substring(0, n);
                    superClass = (JavaClass) javaProject.getClassByName(superClassString);
                    if (superClass == null) {
                        superClass = new JavaClass();

                        superClass.setName(superClassName);
                        JavaPackage javaPackage = javaProject.getPackageByName(superClassPackage);
                        if (javaPackage == null) {
                            javaPackage = new JavaPackage(superClassPackage);
                            javaProject.addPackage(javaPackage);
                        }
                        superClass.setRevisionId(revisionId);
                        superClass.setJavaPackage(javaPackage);
                        javaPackage.addJavaAbstract(superClass);
                        javaProject.addClass(superClass);

                    }

                }
                String path = rs.getString("path");
                String classString = rs.getString("name");
                String classSplit[] = classString.split("\\.");
                String className = classSplit[classSplit.length - 1];
                int n = classString.length() - (className.length() + 1);
                if (n < 0) {
                    n = 0;
                }
                String classPackage = classString.substring(0, n);
                JavaClass javaClass = (JavaClass) javaProject.getClassByName(classString);
                if (javaClass == null) {
                    javaClass = new JavaClass();

                    javaClass.setName(className);
                    JavaPackage javaPackage = javaProject.getPackageByName(classPackage);
                    if (javaPackage == null) {
                        javaPackage = new JavaPackage(classPackage);
                        javaProject.addPackage(javaPackage);
                    }
                    javaClass.setJavaPackage(javaPackage);
                    javaPackage.addJavaAbstract(javaClass);
                }
                javaClass.setPath(path);
                javaClass.setRevisionId(revisionId);
                javaClass.setSuperClass(superClass);
                javaClass.setId(Long.valueOf(rs.getString("id")));
                javaClass.setAccessToForeignDataNumber(Integer.valueOf(rs.getString("access_to_foreign_data_number")));
                javaClass.setNumberOfDirectConnections(Integer.valueOf(rs.getString("number_of_direct_connections")));
                javaClasses.add(javaClass);
                javaProject.addClass(javaClass);
                i++;
            }
            //stm.execute("SHUTDOWN");
            //System.out.println("QUANTIDADE: " + i);
        } catch (Exception e) {
            System.out.println("ERRO get classes: " + e.getMessage());
        }
        return javaClasses;
    }
}
