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

import com.foundationdb.sql.parser.*;

/**
 * Class that gets access to
 */
public final class OperandHelper implements IOperands {

    private String leftOperand;
    private String tableOfLeftOperand;

    public OperandHelper() {
        super();
        leftOperand = null;
        tableOfLeftOperand = null;
    }

    /**
     * Reads the left operand from valueNode if possible.
     * If valueNode is not a leave node of the tree, then left operand and its table name will be null.
     *
     * @param valueNode
     * @return True if the left operand and its table name could be fetched and are different from null. Otherwise false.
     * False is also returned if the node type of valueNode is not supported
     */
    @Override
    public boolean loadFrom(ValueNode valueNode) {
        switch (valueNode.getNodeType()) {
            case NodeTypes.IN_LIST_OPERATOR_NODE:
                InListOperatorNode inList = (InListOperatorNode) valueNode;
                ColumnReference ref = (ColumnReference) inList.getLeftOperand().getNodeList().get(0);
                setLeftOperand(ref.getColumnName());
                setLeftOperandTable(ref.getTableName());
                break;
            case NodeTypes.AND_NODE:
            case NodeTypes.OR_NODE:
            case NodeTypes.IS_NODE:
            case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
            case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
            case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
            case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
            case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
            case NodeTypes.BINARY_MINUS_OPERATOR_NODE:
            case NodeTypes.BINARY_NOT_EQUALS_OPERATOR_NODE:
            case NodeTypes.BINARY_PLUS_OPERATOR_NODE:
            case NodeTypes.BINARY_TIMES_OPERATOR_NODE:
            case NodeTypes.BINARY_BIT_OPERATOR_NODE:
            case NodeTypes.BINARY_DIV_OPERATOR_NODE:
                BinaryOperatorNode binaryOp = (BinaryOperatorNode) valueNode;
                setLeftOperand(binaryOp.getLeftOperand().getColumnName());
                setLeftOperandTable(binaryOp.getLeftOperand().getTableName());
                break;
            case NodeTypes.LIKE_OPERATOR_NODE:
                LikeEscapeOperatorNode likeNode = (LikeEscapeOperatorNode) valueNode;
                setLeftOperand(likeNode.getReceiver().getColumnName());
                setLeftOperandTable(likeNode.getReceiver().getTableName());
                break;
            default:
                return false;
        }
        return (null != getLeftOperand() && null != getTableOfLeftOperand());
    }

    @Override
    public String getLeftOperand() {
        return leftOperand;
    }

    @Override
    public String getTableOfLeftOperand() {
        return tableOfLeftOperand;
    }

    private void setLeftOperand(final String pLeftOperand) {
        leftOperand = pLeftOperand;
    }

    private void setLeftOperandTable(final String pLeftOperandTable) {
        tableOfLeftOperand = pLeftOperandTable;
    }
}
