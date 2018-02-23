package eu.paasword.dbproxy.database;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.Ostermiller.util.CSVParser;
import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.Type;
import eu.paasword.dbproxy.exceptions.DatabaseException;


/**
 * Class to process CSV-file to directly insert them into the remote Database 
 * @author Mark Brenner
 *
 */
public class CSVProcessor {
	private FileReader reader;
	private RemoteDBAdminstration remoteDB;
	/**
	 * Constructs a CSVprossing object for the given file and the given RemoteDBAdministration object which handles the insertion into the
	 * Adapter tablestructure
	 * @param filename the exact path of the file 
	 * @param remoteDBAdminstration The RemoteDBObject which handles the inseertion into the remoteDB
	 * @throws FileNotFoundException
	 */
   public CSVProcessor(String filename, RemoteDBAdminstration remoteDBAdminstration) throws FileNotFoundException {
	   reader = new FileReader(filename);
	   remoteDB = remoteDBAdminstration;
   }
   
   /**
    * Precesses the given CSV-file row by row and inserts each row into the database. Only works if Data is parcable to the supported
    * data types
    * @param columns of the table where the data shall be inserted into
    * @param tableName the name of the table where the data shall be inserted into
    * @param delimiter the delimiter of the given CSV-file
    * @throws DatabaseException
    * @throws IOException
    */
   public void processCSV(List<Column> columns, String tableName, char delimiter, String sessionid) throws DatabaseException, IOException {
	   CSVParser parser = new CSVParser(reader, delimiter);
	   //Extract the ColumnNames
	   List<String> columnNames = new ArrayList<String>();
	   for(Column col : columns) {
		   columnNames.add(col.getName());
	   }
	   String[] line = parser.getLine();
	   if(line.length != columns.size()) {
		   throw new DatabaseException("File does not fit into Table "+ tableName + " because the columns are not consistent\n");
	   }
		while (line != null) {
			List<Object> values = new ArrayList<Object>();
			for(int i = 0; i < line.length; i++) {
				Type type = columns.get(i).getType();
				switch(type) {
				  case String:
					  values.add(line[i]); 
					  break;
				  case Integer:
					  values.add(Integer.valueOf(line[i].trim()));
					  break;
				  case Double:
					  values.add(Double.valueOf(line[i].trim()));
					  break;
				  case Date:
					  values.add(Integer.valueOf(line[i]));
					  break;
				default:
					break;
				}
				values.add(line[i]);
			}
			// Insert data in remote DB
			remoteDB.insert(tableName, columnNames, values, sessionid); 
			line = parser.getLine();
		}
		parser.close();
   }
}
