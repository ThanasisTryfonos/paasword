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

import com.foundationdb.sql.parser.ValueNode;

/**
 * Interface to get information about the operands of a binary operation of a clause in a where statement.
 * For example there might be the clause tablenameLeft.operandLeft = tablenameRight.operandRight.
 */
public interface IOperands {
    /**
     * Scan the valueNode and if it is of an binary (or other operation that takes two operands) and assign the values to this object
     *
     * @param valueNode
     * @return true if left operand and its table could be loaded from value node. Otherwise false
     */
    boolean loadFrom(final ValueNode valueNode);
    String getLeftOperand();
    String getTableOfLeftOperand();
}
