package org.springframework.samples.petclinic.testingexamples.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.samples.petclinic.model.Diagnose;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.DiagnoseRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;

@DataJpaTest
@DisplayName("@DataJpaTest: orphanRemoval entre Visit y Diagnose")
class VisitDiagnoseOrphanRemovalJpaSliceTests {

	private static final int VISIT_WITH_DIAGNOSE_ID = 1;

	private static final int DIAGNOSE_FOR_VISIT_ID = 1;

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private VisitRepository visitRepository;

	@Autowired
	private DiagnoseRepository diagnoseRepository;

	@Test
	@DisplayName("Al eliminar una visita se elimina tambien su diagnostico asociado")
	void shouldRemoveDiagnoseWhenVisitIsRemoved() {
		assertThat(this.visitRepository.findById(VISIT_WITH_DIAGNOSE_ID)).isPresent();
		assertThat(this.diagnoseRepository.findById(DIAGNOSE_FOR_VISIT_ID)).isPresent();

		Visit visit = this.entityManager.find(Visit.class, VISIT_WITH_DIAGNOSE_ID);
		Diagnose diagnose = visit.getDiagnose();

		assertThat(diagnose).isNotNull();
		assertThat(diagnose.getId()).isEqualTo(DIAGNOSE_FOR_VISIT_ID);

		Pet pet = visit.getPet();
		pet.removeVisit(visit);

		this.entityManager.flush();
		this.entityManager.clear();

		assertThat(this.visitRepository.findById(VISIT_WITH_DIAGNOSE_ID)).isEmpty();
		assertThat(this.diagnoseRepository.findById(DIAGNOSE_FOR_VISIT_ID)).isEmpty();
	}

}
