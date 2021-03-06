/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.archd.db.dao;

import br.uff.ic.archd.javacode.JavaPackage;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class HsqldbJavaPackageDao implements JavaPackageDao{

    private Connection connection;
    
    public HsqldbJavaPackageDao() {
        try {
            File file = new File(Constants.DB_DIR);
            if(!file.exists()){
                file.mkdirs();
                
            }
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:file:" + Constants.DB_DIR, "archd", "123");
            DatabaseMetaData dbData = connection.getMetaData();
            ResultSet tables = dbData.getTables(null, null, "JAVA_PACKAGES", null);
            //System.out.println("NEXT "+tables.next());
            if (!tables.next()) {


                System.out.println("NAO POSSUI TABELA JAVA_PACKAGES");
                Statement stm = connection.createStatement();

                stm.executeUpdate("create table JAVA_PACKAGES (package_signature varchar(1000),"
                        +"original_signature varchar(1000)"
                        + ");");
            }else{
                System.out.println("TABELA JAH EXISTE JAVA_PACKAGES");
            }

            //stm.execute("SHUTDOWN");

        } catch (ClassNotFoundException e) {
            System.out.println("Erro ao carregar o driver JDBC. ");
        } catch (SQLException e) {
            System.out.println("Erro de SQL: " + e);
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERRO " + e.getMessage());
        }


    }
    
    @Override
    public void save(JavaPackage javaPackage) {
        try {
            Statement stm = connection.createStatement();
            System.out.println("insert into JAVA_PACKAGES (package_signature, original_signature)"
                    + " VALUES ('" + javaPackage.getName() + "'"
                    +", '" + javaPackage.getOriginalSignature() + "'"
                    + ");");
            stm.executeUpdate("insert into JAVA_PACKAGES (package_signature, original_signature)"
                    + " VALUES ('" + javaPackage.getName() + "'"
                    +", '" + javaPackage.getOriginalSignature() + "'"
                    + ");");
            //stm.execute("SHUTDOWN");
        } catch (SQLException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    @Override
    public List<JavaPackage> getAllJavaPackage() {
        List<JavaPackage> javaPackages = new LinkedList();
        try {
            Statement stm = connection.createStatement();
            ResultSet rs = stm.executeQuery("select * from JAVA_PACKAGES ;");
            int i = 0;
            while (rs.next()) {
                JavaPackage  javaPackage = new JavaPackage(rs.getString("package_signature"));
                javaPackage.setOriginalSignature(rs.getString("original_signature"));
                javaPackages.add(javaPackage);
                i++;
            }
            //stm.execute("SHUTDOWN");
            System.out.println("QUANTIDADE: " + i);
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
        return javaPackages;
    }
    
}
