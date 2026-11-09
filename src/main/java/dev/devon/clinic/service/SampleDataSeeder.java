package dev.devon.clinic.service;

import dev.devon.clinic.domain.*;
import dev.devon.clinic.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Component
@Profile("seed")
public class SampleDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SampleDataSeeder.class);
    private final OwnerRepository owners;
    private final PetRepository pets;
    private final VisitRepository visits;
    public SampleDataSeeder(OwnerRepository owners, PetRepository pets, VisitRepository visits) {
        this.owners = owners;
        this.pets = pets;
        this.visits = visits;
    }
    @Override @Transactional
    public void run(String... args) {
        Owner minji = owner("김민지", "010-1000-1001", "서울시 마포구 월드컵로 12");
        Owner jihoon = owner("이 지훈", "010-2000-2002", "부산시 수영구 광안로 21");
        Owner sora = owner("박소라", "010-3000-3003", "대전시 유성구 대학로 7");
        Pet nabi = pet(minji, "나비", "cat", "2020-04-01");
        Pet kong = pet(minji, "콩이", "dog", "2021-09-12");
        Pet bori = pet(jihoon, "보리", "dog", "2019-11-20");
        Pet toto = pet(sora, "토토", "rabbit", "2022-02-15");
        visit(nabi, "2024-03-10", "정기 예방접종 및 체중 확인");
        visit(nabi, "2024-09-10", "치아 검진과 식이 상담");
        visit(kong, "2024-06-05", "산책 중 발바닥 상처 소독");
        visit(bori, "2024-07-21", "피부 가려움증 진료 및 재진 안내");
        visit(toto, "2024-10-01", "정기 건강 검진과 발톱 관리");
        log.info("SEED complete: owners={}, pets={}, visits={}", owners.count(), pets.count(), visits.count());
    }
    private Owner owner(String name, String phone, String address) {
        return owners.findFirstByNameAndTelephoneOrderByIdAsc(name, phone).orElseGet(() -> owners.save(new Owner(name, phone, address)));
    }
    private Pet pet(Owner owner, String name, String type, String birth) {
        return pets.findFirstByOwnerIdAndNameOrderByIdAsc(owner.getId(), name).orElseGet(() -> pets.save(new Pet(owner, name, type, LocalDate.parse(birth))));
    }
    private void visit(Pet pet, String date, String description) {
        LocalDate day = LocalDate.parse(date);
        if (!visits.existsByPetIdAndVisitDateAndDescription(pet.getId(), day, description)) {
            visits.save(new Visit(pet, day, description));
        }
    }
}
