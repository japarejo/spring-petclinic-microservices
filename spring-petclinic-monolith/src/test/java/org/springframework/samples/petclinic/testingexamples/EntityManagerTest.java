package org.springframework.samples.petclinic.testingexamples;


import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.samples.petclinic.model.Owner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class EntityManagerTest {
    @Autowired
    EntityManager em;


    @Test
    public void emTest(){
        Owner mipana=em.find(Owner.class,1);
        assertNotNull(mipana);
        assertEquals(mipana.getFirstName(),"George");
    }

    @Test
    public void emTest2(){
        Owner yo=new Owner();
        yo.setFirstName("José Antonio");
        yo.setLastName("Apellidos");
        yo.setCity("Tattoine");
        yo.setAddress("Casa del Tio Ben, choza 3");
        yo.setTelephone("66666666");
        em.persist(yo);
        Owner mipana=em.find(Owner.class,1);
        assertNotNull(mipana);
        assertEquals(mipana.getFirstName(),"George");
        assertNotNull(yo.getId());
        yo.setCity("Death Star");
        em.persist(yo);
        em.flush();
    }

}
