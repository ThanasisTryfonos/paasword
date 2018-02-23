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
package eu.paasword.dbproxy.fragmentation;

import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 04/07/16.
 */
public class FragmentationClient implements FragmentationEngine {

    private static final Logger logger = Logger.getLogger(FragmentationClient.class.getName());

    public FragmentationClient() {
    }

    public String fragmentNoServerLimitation(ArrayList<String> attributes, ArrayList<ArrayList<String>> relations) {

        return new FragmentationUtil(attributes, relations).fragment();
    }

    public String fragmentNoServerLimitationWithAffinity(ArrayList<String> attributes, ArrayList<ArrayList<String>> relations, Table<String, String, Integer> affinity) {
        return new FragmentAffinity(attributes, relations, affinity).fragment();
    }

    public String fragmentWithServerLimitation(ArrayList<String> attributes, ArrayList<ArrayList<String>> relations, int serverLimitation) {

        return new FragmentationUtil(attributes, relations, serverLimitation).fragment();
    }
}
