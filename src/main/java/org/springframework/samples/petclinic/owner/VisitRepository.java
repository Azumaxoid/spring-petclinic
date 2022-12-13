/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Repository class for <code>Owner</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface VisitRepository extends Repository<Visit, Integer> {

	/**
	 * Retrieve an {@link Owner} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link Owner} if found
	 */
	@Query("SELECT visit FROM Visit visit WHERE visit.id =:id")
	@Transactional(readOnly = true)
	Visit findById(@Param("id") Integer id);

	void save(Visit visit);

	/**
	 * Returnes all the visit from data store
	 **/
	@Query("SELECT visit FROM Visit visit")
	@Transactional(readOnly = true)
	List<Visit> findAll();

	/**
	 * Returnes all the owners from data store
	 **/
	@Query("SELECT visit FROM Visit visit")
	@Transactional(readOnly = true)
	Page<Visit> findAll(Pageable pageable);

	/**
	 * Returnes all the visit from data store
	 **/
	@Query("SELECT visit FROM Visit visit WHERE visit.date =:date ORDER BY visit.visitedTimestamp")
	@Transactional(readOnly = true)
	List<Visit> findScheduled(@Param("date") LocalDate date);

	/**
	 * Returnes all the owners from data store
	 **/
	@Query("SELECT visit FROM Visit visit WHERE visit.date =:date ")
	@Transactional(readOnly = true)
	Page<Visit> findScheduled(@Param("date") String date, Pageable pageable);

	/**
	 * Returnes all the owners from data store
	 **/
	@Query("SELECT visit FROM Visit visit WHERE visit.date =:date")
	@Transactional(readOnly = true)
	Page<Visit> findVisitsByDate(@Param("date") LocalDate date, Pageable pageable);

}
