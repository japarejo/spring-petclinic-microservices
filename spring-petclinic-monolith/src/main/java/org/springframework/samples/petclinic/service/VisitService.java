package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.stereotype.Service;

@Service
public class VisitService {

	
	@Autowired
	VisitRepository visitRepository;
	
	public Optional<Visit> findById(int visitId){
		return visitRepository.findById(visitId);
	}
	
	public List<Visit> findAll(){
		return visitRepository.findAll();
	}

    public Page<Visit> search(Integer petId, String ownerLastName, String petName, String petType,
            String description, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        List<Visit> filtered = findAll().stream()
                .filter(visit -> petId == null || visit.getPet().getId().equals(petId))
                .filter(visit -> ownerLastName == null || visit.getPet().getOwner().getLastName().toLowerCase().contains(ownerLastName.toLowerCase()))
                .filter(visit -> petName == null || visit.getPet().getName().toLowerCase().contains(petName.toLowerCase()))
                .filter(visit -> petType == null || visit.getPet().getType().getName().equalsIgnoreCase(petType))
                .filter(visit -> description == null || visit.getDescription().toLowerCase().contains(description.toLowerCase()))
                .filter(visit -> fromDate == null || !visit.getDate().isBefore(fromDate))
                .filter(visit -> toDate == null || !visit.getDate().isAfter(toDate))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Visit> pageContent = start <= end ? filtered.subList(start, end) : List.of();
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }
}
