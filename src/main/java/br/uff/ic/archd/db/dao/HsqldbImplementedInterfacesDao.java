/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.archd.db.dao;

import br.uff.ic.archd.javacode.JavaClass;
import br.uff.ic.archd.javacode.JavaInterface;
import br.uff.ic.archd.javacode.JavaProject;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author wallace
 */
public class HsqldbImplementedInterfacesDao implements ImplementedInterfacesDao{

    private Connection connection;
    
    HsqldbImplementedInterfacesDao(){
        try {
            File file = new File(Constants.DB_DIR);
            if(!file.exists()){
                file.mkdirs();
                
            }
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:file:" + Constants.DB_DIR, "archd", "123");
            DatabaseMetaData dbData = connection.getMetaData();
            ResultSet tables = dbData.getTables(null, null, "IMPLEMENTED_INTERFACES", null);
            //System.out.println("NEXT "+tables.next());
            if (!tables.next()) {


                System.out.println("NAO POSSUI TABELA IMPLEMENTED_INTERFACES");
                Statement stm = connection.createStatement();

                stm.executeUpdate("create table IMPLEMENTED_INTERFACES (id bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                        + "class_id bigint,"
                        + "interface_id bigint,"
                        + "PRIMARY KEY (id),"
                        + "FOREIGN KEY (class_id) REFERENCES JAVA_CLASSES(id),"
                        + "FOREIGN KEY (interface_id) REFERENCES JAVA_INTERFACES(id));");
            } else {
                System.out.println("TABELA JAH EXISTE IMPLEMENTED_INTERFACES");
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
    public void setImplementedInterfacesDao(JavaClass javaClass, JavaProject javaProject) {
        try {
            Statement stm = connection.createStatement();
            
            //long t1 = System.currentTimeMillis();
            ResultSet rs = stm.executeQuery("select * from IMPLEMENTED_INTERFACES where class_id=" + javaClass.getId() + ";");
            //long t2 = System.currentTimeMillis();
            //System.out.println("Pegar todas as implemented interfaces de uma classe de uma revisão (somente o select) : "+(t2-t1)+"  milisegundos");
            
            int i = 0;

            while (rs.next()) {
                
                String interfaceId = rs.getString("interface_id");
                JavaInterface javaInterface = javaProject.getInterfaceById(Long.valueOf(interfaceId));
                javaClass.addImplementedInterface(javaInterface);
                
                
                i++;
            }
            //stm.execute("SHUTDOWN");
            //System.out.println("QUANTIDADE: " + i);
        } catch (Exception e) {
            System.out.println("ERRO implemented interface: " + e.getMessage());
        }
    }

    @Override
    public void saveImplementedInterface(JavaClass javaClass, JavaInterface javaInterface) {
        try {
            Statement stm = connection.createStatement();
            System.out.println("insert into IMPLEMENTED_INTERFACES (class_id, interface_id) "
                    + " VALUES (" + javaClass.getId() + ","
                    + "" + javaInterface.getId() + ");");
            stm.executeUpdate("insert into IMPLEMENTED_INTERFACES (class_id, interface_id) "
                    + " VALUES (" + javaClass.getId() + ","
                    + "" + javaInterface.getId() + ");");

        } catch (SQLException e) {
            System.out.println("ERRO implemented interfaces: " + e.getMessage());
        }
    }
    
}
