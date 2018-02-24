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

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.exceptions.PluginLoadFailure;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Encapsulates the method load a database object from its class name
 * 
 * @author Yvonne Muelle
 * 
 */
public class DatabaseLoader {

    private static final Logger logger = Logger.getLogger(DatabaseLoader.class.getName());

	/**
	 * Loads a database objects from its class name. The fully-qualified class name is stored in the databaseNode
	 * 
	 * @param dbConfig configuration
	 * @return Database object
	 * @throws PluginLoadFailure
	 *             if the requested class could not be loaded.
	 */
	public static Database loadDatabase(Map<String, String> dbConfig,String sessionid) throws PluginLoadFailure {

		String className = dbConfig.get("class");
                logger.info("Attempting to load "+className);
		ClassLoader loader = DatabaseLoader.class.getClassLoader();

		try {
			@SuppressWarnings("unchecked")
			Class<? extends Database> dbClass = (Class<? extends Database>) loader.loadClass(className);
			Constructor<? extends Database> c = dbClass.getConstructor(Map.class,String.class);
			return c.newInstance(dbConfig,sessionid);

		} catch (ClassNotFoundException e) {
			throw new PluginLoadFailure(e);
		} catch (SecurityException e) {
			throw new PluginLoadFailure(e);
		} catch (NoSuchMethodException e) {
			throw new PluginLoadFailure(e);
		} catch (IllegalArgumentException e) {
			throw new PluginLoadFailure(e);
		} catch (InstantiationException e) {
			throw new PluginLoadFailure(e);
		} catch (IllegalAccessException e) {
			throw new PluginLoadFailure(e);
		} catch (InvocationTargetException e) {
			throw new PluginLoadFailure(e);
		}
	}

}
