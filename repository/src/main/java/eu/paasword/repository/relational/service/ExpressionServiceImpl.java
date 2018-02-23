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
package eu.paasword.repository.relational.service;

import eu.paasword.api.repository.IExpressionService;
import eu.paasword.api.repository.exception.expression.ExpressionAlreadyExistsException;
import eu.paasword.api.repository.exception.expression.ExpressionDoesNotExist;
import eu.paasword.api.repository.exception.expression.ExpressionNameDoesNotExist;
import eu.paasword.api.repository.exception.expression.ExpressionValidityException;
import eu.paasword.repository.relational.dao.ExpressionRepository;
import eu.paasword.repository.relational.dao.InstanceRepository;
import eu.paasword.repository.relational.dao.NamespaceRepository;
import eu.paasword.repository.relational.dao.PropertyInstanceRepository;
import eu.paasword.repository.relational.domain.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author smantzouratos
 */
@Component
public class ExpressionServiceImpl implements IExpressionService<Expression> {

    @Autowired
    ExpressionRepository expressionRepository;

    @Autowired
    NamespaceRepository namespaceRepository;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    PropertyInstanceRepository propertyInstanceRepository;

    @Autowired
    TriplestoreService triplestoreService;

    private static final Logger logger = Logger.getLogger(ExpressionServiceImpl.class.getName());

    @Override
    public void create(Expression expression) throws ExpressionAlreadyExistsException, ExpressionValidityException {

        //Check if expression name already exists
        if (null != expressionRepository.findByExpressionName(expression.getExpressionName())) {
            throw new ExpressionAlreadyExistsException(expression.getExpressionName());
        }

        String triplestoreResponse = triplestoreService.validatePolicyModel(null, null, null, expression);

        if (null == triplestoreResponse) {

            //Store expression to database
            expressionRepository.save(expression);

        } else {
            throw new ExpressionValidityException(triplestoreResponse);
        }

    }

    @Override
    public void delete(long id) throws ExpressionDoesNotExist, ExpressionValidityException {
        try {

            expressionRepository.delete(id);

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ExpressionServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ExpressionDoesNotExist(id);
        }
    }

    @Override
    public Optional<Expression> findByExpressionName(String expressionName) throws ExpressionNameDoesNotExist {
        Optional<Expression> expression = Optional.ofNullable(expressionRepository.findByExpressionName(expressionName));

        if (expression.isPresent()) {
            return expression;
        }

        throw new ExpressionNameDoesNotExist(expressionName);
    }

    @Override
    public Optional<Expression> findOne(long id) throws ExpressionDoesNotExist {
        Optional<Expression> expression = Optional.ofNullable(expressionRepository.findOne(id));

        if (expression.isPresent()) {
            return expression;
        }

        throw new ExpressionDoesNotExist(id);
    }

    @Override
    public List<Expression> findAll() {
        return expressionRepository.findAll();
    }

    @Override
    public Page<Expression> findAll(Pageable pageable) {
        return expressionRepository.findAll(pageable);
    }

    @Override
    public void edit(Expression expression) throws ExpressionDoesNotExist, ExpressionValidityException {

        Expression currentExpression = expressionRepository.findOne(expression.getId());

        //Check if current expression exists
        if (null == currentExpression) {
            throw new ExpressionDoesNotExist(expression.getId());
        }

        currentExpression.setExpressionName(expression.getExpressionName());
        currentExpression.setExpression(expression.getExpression());
        currentExpression.setNamespaceID(expression.getNamespaceID());


        String triplestoreResponse = triplestoreService.validatePolicyModel(null, null, null, currentExpression);

        if (null == triplestoreResponse) {

            //Store expression to database
//            currentExpression.setExpressionFriendlyData(RepositoryUtil.addContextExpression(currentExpression, namespaceRepository, instanceRepository, propertyInstanceRepository).toString());

            expressionRepository.save(currentExpression);

        } else {
            throw new ExpressionValidityException(triplestoreResponse);
        }

    }

    @Override
    public List<Expression> findByOrderByExpressionName() {
        return expressionRepository.findByOrderByExpressionName();
    }

}
