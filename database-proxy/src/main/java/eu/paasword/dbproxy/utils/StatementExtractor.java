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
package eu.paasword.dbproxy.utils;

import java.util.ArrayList;
import java.util.List;

import com.foundationdb.sql.parser.ColumnDefinitionNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;
import com.foundationdb.sql.parser.CreateTableNode;
import com.foundationdb.sql.parser.ResultColumn;
import com.foundationdb.sql.parser.ResultColumnList;
import com.foundationdb.sql.parser.TableElementList;
import com.foundationdb.sql.parser.TableElementNode;
import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.Type;
import eu.paasword.dbproxy.exceptions.DatabaseException;

/**
 * This class provides some methods static to the adapter like the extraction of
 * a statement.
 * 
 * @author Mark Brenner
 *
 */
public class StatementExtractor {

	/**
	 * Extracts the columns in a create Table Statement
	 * 
	 * @param node
	 * @return the list of columns to insert them into the database
	 * @throws DatabaseException
	 */
	public static List<Column> extractCreateTable(CreateTableNode node)
			throws DatabaseException {
		TableElementList cols = node.getTableElementList();
		List<Column> columnDefinition = new ArrayList<Column>();
	    //Constrains are own nodes in the list an are the predecessor to their columns
		boolean unique = false;
		boolean primarykey = false;
		ArrayList<String> primareKeyColumns = new ArrayList<String>();
		TableElementNode last = cols.get(cols.size() -1);
		if(last instanceof ConstraintDefinitionNode) {
			ConstraintDefinitionNode constraint = (ConstraintDefinitionNode) last;
			if(constraint.getConstraintType() == ConstraintType.PRIMARY_KEY) {
				ResultColumnList columns = constraint.getColumnList();
				for(ResultColumn col : columns) {
					primareKeyColumns.add(col.getName());
				}
			}
			
		}
		for (int i = 0; i < cols.size(); i++) {
			ConstraintDefinitionNode constraints = null;
			if (cols.get(i) instanceof ConstraintDefinitionNode) {
				constraints = (ConstraintDefinitionNode) cols.get(i);
				if(constraints.getConstraintType() == ConstraintType.UNIQUE) {
					unique = true;
				}
				if(constraints.getConstraintType() == ConstraintType.PRIMARY_KEY) {
					primarykey = true;
				}
			} else {
				ColumnDefinitionNode current = (ColumnDefinitionNode) cols
						.get(i);
				boolean not_null = !current.getType().isNullable();
				boolean varchar = false;
				int length = -1;
				if (current.getType().getTypeName().equalsIgnoreCase("varchar")) {
					varchar = true;
					length = current.getType().getMaximumWidth();
				} else if (current.getType().getTypeName()
						.equalsIgnoreCase("char")) {
					length = current.getType().getMaximumWidth();
				} else if (current.getType().getTypeName()
						.equalsIgnoreCase("datetime")
						|| current.getType().getTypeName()
								.equalsIgnoreCase("string")
						|| current.getType().getTypeName()
								.equalsIgnoreCase("timestamp")) {
					varchar = true;
					length = -2; // Unrestricted lenght;
				}
				if(!primarykey) {
				 primarykey = primareKeyColumns.contains(current.getColumnName());
				}
				columnDefinition.add(new Column(getColumnType(current.getType()
						.getTypeName()), current.getColumnName(), length,
						varchar, not_null, unique, primarykey));
				unique = false;
				primarykey = false;
			}
		}
		return columnDefinition;
	}

	public static ArrayList<String> extractColNames(CreateTableNode node) throws DatabaseException {
		TableElementList cols = node.getTableElementList();
		ArrayList<String> columnNames = new ArrayList<String>();
		for (int i = 0; i < cols.size(); i++) {
			columnNames.add(cols.get(i).getName());
		}
		return columnNames;
	}

	/**
	 * which type the the column of the added table has
	 * 
	 * @param sqltype
	 *            the type as string as it comes from the sql parser
	 * @return which type the the column of the added table has
	 * @throws DatabaseException
	 */
	public static Type extractAddTable(String sqltype) throws DatabaseException {
		return getColumnType(sqltype);
	}

	// Check the types and return a Type opbject
	private static Type getColumnType(String sqltype) throws DatabaseException {
		switch (sqltype.toLowerCase()) {
		case "string":
			return Type.String;
		case "char":
			return Type.String;
		case "int":
			return Type.Integer;
		case "integer":
			return Type.Integer;
		case "double precision":
			return Type.Double;
		case "double":
			return Type.Double;
		case "date":
			return Type.Date; // doch besser String?
		case "time":
			return Type.String;
		case "datetime":
			return Type.String;
		case "timestamp":
			return Type.String;
		case "varchar":
			return Type.String;
		case "boolean":				// added boolean type for columns
			return Type.Boolean;
		default:
			throw new DatabaseException("Datatype " + sqltype
					+ " not implemented! \n");
		}
	}
}
