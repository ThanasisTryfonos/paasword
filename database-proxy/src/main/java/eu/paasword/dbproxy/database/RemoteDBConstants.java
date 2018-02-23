/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.dbproxy.database;

/**
 * This class contains all constants for the RemoteDBAdminstration with standard index.
 * @author Mark Brenner
 *
 */
public class RemoteDBConstants {
	//Index Table constants
	public static final String INDEX_FIELD_ID = "field_id";
	public static final  String INDEX_KEY = "key";
	public static final String INDEX_PRIMKEY = "id";
	public static final String INDEX_VALUE = "value";
	//Data Table constants
	public static final String DATA_TABLE = "data";
	public static final String DATA_PRIMKEY = "key";
	public static final String DATA_KEY = "key";
	public static final String DATA_DATA = "value";
	//Column Server Mapping constants
	public static final String COLUMN_SERVER_MAPPING = "columnServerMapping"; // contains ID and server ID name for every column in every table within database-structure
	public static final String COLUMN_SERVER_ID = "id";
	public static final String COLUMN_SERVER_SERVERID = "server_id";
	public static final String COLUMN_SERVER_SERVERNAME = "server_name";
	//Table constants
	protected static final String TABLE_NAMES = "tableMeta"; // contains ID and name for every table within database-structure
	protected static final String TABLE_NAME = "name";
	protected static final String TABLE_IDS = "id";
	//Column constants
	protected static final String COLUMN_NAMES = "fieldMeta"; // contains ID and name for every column and datatype
	protected static final String COLUMN_ID = "id";
	protected static final String COLUMN_TABLE_IDS = "table_id";
	protected static final String COLUMN_NAME = "name";
	protected static final String COLUMN_DATATYPE = "datatype";
	protected static final String COLUMN_LENGTH = "length";
	protected static final String COLUMN_VARIABLE = "var";
	protected static final String COLUMN_NOT_NULL = "not_null";
	protected static final String COLUMN_UNIQUE= "uniquevalue";
	protected static final String COLUMN_PRIMARY_KEY = "primary_key";
	//Handle Null values 
	public static final String ISNULL = "IS NULL";
	public static final String ISNOTNULL = "IS NOT NULL";
	public static final String NULL = "null"; //null Value for Data Table
}

