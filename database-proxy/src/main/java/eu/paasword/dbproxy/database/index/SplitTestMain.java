package eu.paasword.dbproxy.database.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.RemoteDBAdminstration;
import eu.paasword.dbproxy.database.RemoteDBHelper;
import eu.paasword.dbproxy.database.SQLDatabase;
import eu.paasword.dbproxy.database.utils.Type;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.encryption.EncryptionHelperBase;
import eu.paasword.dbproxy.encryption.IndexEncryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.exceptions.PluginLoadFailure;

/**
 * 
 * This class shows how to properly use the KeywordSplitter, from instantiation to select query.
 * But remember to change the variables under the TODO-tag to meet your own system.
 * 
 * @author Tobias Andersson
 *
 */
public class SplitTestMain {

	private static final int b = 3;
	
	public static void main(String[] args) {
		String tableName = "locals";
		KeywordSplitter kSplit = new KeywordSplitter(b);
		IndexEncryption indexEnc = new IndexEncryption();
		String keyword = "monkey";
		
		ArrayList<Object> valuesToInsert = new ArrayList<Object>();
		valuesToInsert.add(indexEnc.encrypt("100", Type.String));
		valuesToInsert.add(indexEnc.encrypt("200", Type.String));
		valuesToInsert.add(indexEnc.encrypt("900", Type.String));
		valuesToInsert.add(indexEnc.encrypt("400", Type.String));
		valuesToInsert.add(indexEnc.encrypt("300", Type.String));
		valuesToInsert.add(indexEnc.encrypt("500", Type.String));
		valuesToInsert.add(indexEnc.encrypt("700", Type.String));
		valuesToInsert.add(indexEnc.encrypt("300", Type.String));

		Map<String, ArrayList<Object>> res = kSplit.makeDecision(keyword, valuesToInsert, indexEnc);

		int j = 0;
		boolean isDone = false;
		while(!isDone){
			ArrayList<Object> valuesToShow;
			if(j > 0){
				valuesToShow = res.get(keyword+Integer.toString(j));
			}else{
				valuesToShow = res.get(keyword);
			}
			if(valuesToShow != null){
				System.out.println(keyword+Integer.toString(j));
				for(int i = 0; i < valuesToShow.size(); i++){
					System.out.println(indexEnc.decrypt(valuesToShow.get(i), Type.String));
				}
				System.out.println();
				j++;
			}else{
				isDone = true;
			}
		}
		Connection con;
		try{ 

			//TODO change to fit your own system
			String url = "jdbc:postgresql://localhost/split";
			Properties props = new Properties();
			props.setProperty("user","sics");
			props.setProperty("password","1Urbanape");
			props.setProperty("ssl","true");
			con = DriverManager.getConnection(url, props);

			setUp(valuesToInsert, indexEnc, url, con, res);
			
			select(keyword, tableName, con, b, indexEnc);
						
		}catch(Exception e){ 
			System.out.println(e);
		}  
	} 
	
	private static void setUp(ArrayList<Object> valuesToInsert, IndexEncryption indexEnc, String tableName, Connection con, Map<String, ArrayList<Object>> res) throws SQLException{
		valuesToInsert.add(indexEnc.encrypt("100", Type.String));
		valuesToInsert.add(indexEnc.encrypt("200", Type.String));
		valuesToInsert.add(indexEnc.encrypt("900", Type.String));
		valuesToInsert.add(indexEnc.encrypt("400", Type.String));
		valuesToInsert.add(indexEnc.encrypt("300", Type.String));
		valuesToInsert.add(indexEnc.encrypt("500", Type.String));
		valuesToInsert.add(indexEnc.encrypt("700", Type.String));
		valuesToInsert.add(indexEnc.encrypt("300", Type.String));
		
		Statement stmt=con.createStatement();

		drop(tableName, con);

		String query = "CREATE TABLE IF NOT EXISTS "+tableName+"(keyword varchar(30)";
		for(int i = 0; i < b; i++){
			query += ", value"+i+" varchar(30)";
		}
		query += ");";
		stmt.executeUpdate(query);  

		insert(res, con);
	}

	
	private static void insert(Map<String, ArrayList<Object>> data, Connection con) throws SQLException{
		for(Entry<String, ArrayList<Object>> entry : data.entrySet()){
			String query = "INSERT INTO locals(keyword";
			String values = "values (?";
			ArrayList<Object> tmpVal = entry.getValue();
			String tmpKey = entry.getKey();
			for(int i = 0; i < tmpVal.size(); i++){
				query += ", value"+Integer.toString(i);
				values += ", ?";
			}
			query += ")";
			values += ");";
			System.out.println(query);
			System.out.println(values);
			PreparedStatement prepState = con.prepareStatement(query+values);
			prepState.setString(1, tmpKey);
			int j = 2;
			for(int i = 0; i < tmpVal.size(); i++){
				prepState.setString(j, (String)tmpVal.get(i));
				j++;
			}
			prepState.executeUpdate();
		}
	}

	private static void select(String keyword, String tableName, Connection con, int b, IndexEncryption enc) throws SQLException{
		String query = "SELECT * FROM "+tableName+" WHERE keyword= ?";
		boolean isDone = false;
		int remove = 0;
		int i = 0;
		int nbrTry = 0;
		while(!isDone){
			PreparedStatement prepState = con.prepareStatement(query);
			prepState.setString(1, keyword);
			nbrTry++;
			ResultSet res = prepState.executeQuery();
			if(!res.next()){
				if(nbrTry == 2){
					isDone = true;
				}
			}else{
				int k = 1;
				while (k <= b+1) {
					System.out.println(res.getString(k));
					k++;
				}
			}

			keyword = keyword.substring(0, keyword.length()-remove);

			keyword = keyword+Integer.toString(i);
			remove = String.valueOf(i).length();
			i++;
		}

	}

	private static void drop(String tableName, Connection con) throws SQLException{
		String query = "DROP TABLE IF EXISTS "+tableName+";";
		PreparedStatement prepState = con.prepareStatement(query);
		prepState.executeUpdate();
	}
}
