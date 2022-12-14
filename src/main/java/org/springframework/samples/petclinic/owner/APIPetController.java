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
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@RestController
@RequestMapping("/api/v1")
class APIPetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final OwnerRepository owners;

	private final PetRepository pets;

	public APIPetController(OwnerRepository owners, PetRepository pets) {
		this.owners = owners;
		this.pets = pets;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.owners.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable(name = "ownerId", required = false) Integer ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {
		return ownerId == null || petId == null ? new Pet() : this.owners.findById(ownerId).getPet(petId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets")
	public Page<Pet> processCreationForm(@RequestParam(defaultValue = "1") int page) {
		// find owners by last name
		Page<Pet> petResults = findPaginatedForPets(page);
		return petResults;
	}

	@GetMapping("/pettypes")
	public List<PetType> processCreationForm() {
		List<PetType> petTypes = owners.findPetTypes();
		return petTypes;
	}

	private Page<Pet> findPaginatedForPets(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return pets.findAll(pageable);
	}

	@PostMapping("/owners/{ownerId}/pets/new")
	public Pet processCreationForm(Owner owner, @Valid Pet pet, BindingResult result) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}

		owner.addPet(pet);
		if (result.hasErrors()) {
			throw new RuntimeException(result.getAllErrors().toString());
		}

		this.owners.save(owner);
		return pet;
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}")
	public Pet initUpdateForm(Owner owner, @PathVariable("petId") int petId) {
		Pet pet = owner.getPet(petId);
		return pet;
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
	public Pet processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner) {
		if (result.hasErrors()) {
			throw new RuntimeException(result.getAllErrors().toString());
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return pet;
	}

}
