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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@RestController
@RequestMapping("/api/v1")
class APIVisitController {

	private final OwnerRepository owners;

	private final VisitRepository visits;

	public APIVisitController(OwnerRepository owners, VisitRepository visits) {
		this.owners = owners;
		this.visits = visits;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable(name = "ownerId", required = false) Integer ownerId,
			@PathVariable(name = "petId", required = false) Integer petId, Map<String, Object> model) {
		if (ownerId == null || petId == null) {
			return new Visit();
		}
		Owner owner = this.owners.findById(ownerId);

		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		model.put("owner", owner);

		Optional<Visit> visit = owner.getPet(petId).getVisits().stream().findFirst();
		return visit.orElseGet(() -> new Visit());
	}

	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable(name = "visitId", required = false) Integer visitId,
			Map<String, Object> model) {
		if (visitId == null) {
			return new Visit();
		}
		Visit visit = this.visits.findById(visitId);

		return visit;
	}

	@GetMapping("/visits")
	public List<Visit> processCreationForm(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "false") String showAll) {
		// find owners by last name
		List<Visit> petResults = findVisits(page, showAll == "true");
		System.out.println(String.valueOf(petResults.size()));
		return petResults;
	}

	private List<Visit> findVisits(int page, boolean showAll) {
		int pageSize = 5;
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String text = date.format(formatter);
		LocalDate parsedDate = LocalDate.parse(text, formatter);
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return visits.findScheduled(parsedDate);
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm() {
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public Visit processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId,
			@RequestBody @Valid Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			throw new RuntimeException(result.getAllErrors().toString());
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called2
	@PostMapping("/visits/{visitId}/edit")
	public Visit processEditVisit(@PathVariable int visitId, @RequestBody @Valid Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			throw new RuntimeException(result.getAllErrors().toString());
		}
		visit.setVisitedTimestamp(new Date().getTime());

		this.visits.save(visit);
		return visit;
	}

}
