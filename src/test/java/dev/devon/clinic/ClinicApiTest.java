package dev.devon.clinic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:clinic-tests;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=none"})
@AutoConfigureMockMvc
@Transactional
class ClinicApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private ResultActions send(String path, String body) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }
    private long owner(String name) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("name", name, "telephone", "01012345678", "address", "Seoul"));
        return json.readTree(send("/api/owners", body).andExpect(status().isCreated())
            .andExpect(header().exists("Location")).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
    private long pet(long ownerId, String name) throws Exception {
        return json.readTree(send("/api/owners/" + ownerId + "/pets", "{\"name\":\"" + name + "\",\"type\":\"cat\",\"birthDate\":\"2020-04-01\"}")
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    @Test void ownerCrudAndSearchPagination() throws Exception {
        long id = owner("민지 Kim");
        owner("지훈 Lee");
        mvc.perform(get("/api/owners/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("민지 Kim"));
        mvc.perform(put("/api/owners/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Mina Kim\",\"telephone\":\"021234567\",\"address\":\"Busan\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.address").value("Busan"));
        mvc.perform(get("/api/owners").param("name", "KIM").param("page", "0").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id))
            .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.size").value(1));
        mvc.perform(get("/api/owners").param("page", "1").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1)).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(delete("/api/owners/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/owners/" + id)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test void petCrudAndNestedOwnership() throws Exception {
        long a = owner("A"), b = owner("B");
        long p = pet(a, "Nabi");
        String path = "/api/owners/" + a + "/pets/" + p;
        mvc.perform(get("/api/owners/" + a + "/pets")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(p));
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.ownerId").value(a));
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Dori\",\"type\":\"dog\",\"birthDate\":\"2021-03-01\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Dori"));
        String wrong = "/api/owners/" + b + "/pets/" + p;
        mvc.perform(get(wrong)).andExpect(status().isNotFound());
        mvc.perform(put(wrong).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\",\"type\":\"cat\",\"birthDate\":\"2020-01-01\"}")).andExpect(status().isNotFound());
        mvc.perform(delete(wrong)).andExpect(status().isNotFound());
        mvc.perform(delete(path)).andExpect(status().isNoContent());
        mvc.perform(get(path)).andExpect(status().isNotFound());
    }

    @Test void visitCreationAndRestrictDeletePolicy() throws Exception {
        long a = owner("A"), b = owner("B"), p = pet(a, "Nabi");
        String path = "/api/owners/" + a + "/pets/" + p;
        send(path + "/visits", "{\"visitDate\":\"2024-03-01\",\"description\":\"Annual vaccination\"}")
            .andExpect(status().isCreated()).andExpect(jsonPath("$.petId").value(p));
        mvc.perform(get(path + "/visits")).andExpect(status().isOk()).andExpect(jsonPath("$[0].description").value("Annual vaccination"));
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Nabi\",\"type\":\"cat\",\"birthDate\":\"2024-03-02\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.birthDate").value("2020-04-01"));
        mvc.perform(delete(path)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RELATIONSHIP_CONFLICT"));
        mvc.perform(delete("/api/owners/" + a)).andExpect(status().isConflict());
        String wrong = "/api/owners/" + b + "/pets/" + p + "/visits";
        mvc.perform(get(wrong)).andExpect(status().isNotFound());
        send(wrong, "{\"visitDate\":\"2024-03-01\",\"description\":\"X\"}").andExpect(status().isNotFound());
        mvc.perform(get(path)).andExpect(status().isOk());
    }

    @Test void invalidPayloadsProduceConsistentErrors() throws Exception {
        send("/api/owners", "{\"name\":\"  \",\"telephone\":\"\",\"address\":\"\"}")
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fields.name").exists()).andExpect(jsonPath("$.path").value("/api/owners"))
            .andExpect(jsonPath("$.timestamp").exists());
        send("/api/owners", "{bad}").andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        long a = owner("A"), p = pet(a, "Nabi");
        String pets = "/api/owners/" + a + "/pets";
        send(pets, "{\"name\":\"Nabi\",\"type\":\"cat\",\"birthDate\":\"" + LocalDate.now().plusDays(1) + "\"}")
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.birthDate").exists());
        send(pets, "{\"name\":\"Nabi\",\"type\":\"cat\"}").andExpect(status().isBadRequest());
        send(pets + "/" + p + "/visits", "{\"visitDate\":\"" + LocalDate.now().plusDays(1) + "\",\"description\":\"X\"}")
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.visitDate").exists());
        send(pets + "/" + p + "/visits", "{\"visitDate\":\"2020-01-01\",\"description\":\" \"}").andExpect(status().isBadRequest());
        send(pets + "/" + p + "/visits", "{\"visitDate\":\"2019-01-01\",\"description\":\"Before birth\"}").andExpect(status().isBadRequest());
    }

    @Test void missingResourcesAndBadPagination() throws Exception {
        mvc.perform(get("/api/owners/99999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/owners/99999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/owners/99999/pets")).andExpect(status().isNotFound());
        send("/api/owners/99999/pets", "{\"name\":\"X\",\"type\":\"dog\",\"birthDate\":\"2020-01-01\"}").andExpect(status().isNotFound());
        for (String page : new String[]{"-1", "abc"}) {
            mvc.perform(get("/api/owners").param("page", page)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        }
        for (String size : new String[]{"0", "101"}) {
            mvc.perform(get("/api/owners").param("size", size)).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/owners").param("name", "absent")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }
}
