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
package eu.paasword.repository.relational.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import eu.paasword.util.parser.ParserUtil;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.json.JSONArray;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author smantzouratos
 */
@Entity
@Table(name = "application_instance_handler", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class ApplicationInstanceHandler implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "application_instance_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JsonBackReference
    private ApplicationInstance applicationInstanceID;

    @JoinColumn(name = "handler_id", referencedColumnName = "id")
    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private Handler handlerID;

    public ApplicationInstanceHandler() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationInstance getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(ApplicationInstance applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public Handler getHandlerID() {
        return handlerID;
    }

    public void setHandlerID(Handler handlerID) {
        this.handlerID = handlerID;
    }
}

