package dev.devon.clinic;

import dev.devon.clinic.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import dev.devon.clinic.domain.Owner;
import dev.devon.clinic.domain.Pet;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:seed-tests;DB_CLOSE_DELAY=-1"})
@ActiveProfiles("seed")
class SeedIntegrationTest {
    @Autowired OwnerRepository owners;
    @Autowired PetRepository pets;
    @Autowired VisitRepository visits;
    @Autowired ApplicationContext context;

    @Test void seedsMeaningfulRelatedRowsWithoutDuplicatesOnRepeatedExecution() throws Exception {
        assertThat(owners.count()).isEqualTo(3);
        assertThat(pets.count()).isEqualTo(4);
        assertThat(visits.count()).isEqualTo(5);
        for (CommandLineRunner runner : context.getBeansOfType(CommandLineRunner.class).values()) {
            runner.run();
            runner.run();
        }
        assertThat(owners.count()).isEqualTo(3);
        assertThat(pets.count()).isEqualTo(4);
        assertThat(visits.count()).isEqualTo(5);
        long petId = pets.findByOwnerIdOrderByIdAsc(owners.findFirstByNameAndTelephoneOrderByIdAsc("김민지", "010-1000-1001").orElseThrow().getId()).get(0).getId();
        assertThat(visits.findByPetIdOrderByVisitDateDescIdDesc(petId)).isNotEmpty();
    }

    @Test @Transactional
    void existingDuplicateNamesDoNotBreakTheSeed() {
        Owner original = owners.findFirstByNameAndTelephoneOrderByIdAsc("김민지", "010-1000-1001").orElseThrow();
        owners.saveAndFlush(new Owner("김민지", "010-1000-1001", "Another address"));
        pets.saveAndFlush(new Pet(original, "나비", "cat", LocalDate.of(2020, 4, 1)));
        assertThatCode(() -> {
            for (CommandLineRunner runner : context.getBeansOfType(CommandLineRunner.class).values()) runner.run();
        }).doesNotThrowAnyException();
        assertThat(owners.count()).isEqualTo(4);
        assertThat(pets.count()).isEqualTo(5);
        assertThat(visits.count()).isEqualTo(5);
    }
}
