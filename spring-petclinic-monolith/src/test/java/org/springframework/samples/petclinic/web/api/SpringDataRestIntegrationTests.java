package org.springframework.samples.petclinic.web.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SpringDataRestIntegrationTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void shouldExposeOnlyCourseSpringDataRestResources() throws Exception {
		mockMvc.perform(get("/datarest"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._links.diseases").exists())
			.andExpect(jsonPath("$._links.petTypes").exists())
			.andExpect(jsonPath("$._links.owners").doesNotExist());
	}

	@Test
	void shouldReturnDiseasesAsHalResources() throws Exception {
		mockMvc.perform(get("/datarest/diseases"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.diseases[0].id").value(1))
			.andExpect(jsonPath("$._embedded.diseases[0].name").value("COVID-19"))
			.andExpect(jsonPath("$._embedded.diseases[0]._links.self.href").exists());
	}

	@Test
	void shouldExposeDerivedSearchMethod() throws Exception {
		mockMvc.perform(get("/datarest/diseases/search/by-name").param("name", "diab"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.diseases[0].name").value("Diabetes"));
	}

	@Test
	void shouldCreateDiseaseWithPetTypeLink() throws Exception {
		String request = """
			{
			  "name": "Otitis externa",
			  "description": "Inflamacion persistente del conducto auditivo externo en mascotas.",
			  "petTypeswithPrevalence": [
			    "http://localhost/datarest/pet-types/2"
			  ]
			}
			""";

		mockMvc.perform(post("/datarest/diseases")
				.contentType("application/json")
				.content(request))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"));
	}

}
