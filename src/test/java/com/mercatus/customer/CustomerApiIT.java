package com.mercatus.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort
    int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("http://localhost:" + port + "/api/v1");
    }

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@x.com";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createCustomer(String email) {
        return client.post()
                .uri("/customers")
                .body(Map.of("name", "Joao", "email", email, "phone", "11999999999"))
                .retrieve()
                .body(Map.class);
    }

    private HttpStatusCode statusOfCreate(String email) {
        return client.post().uri("/customers")
                .body(Map.of("name", "Joao", "email", email))
                .exchange((request, response) -> response.getStatusCode());
    }

    private HttpStatusCode statusOfPut(String uri, Object body) {
        return client.put().uri(uri).body(body)
                .exchange((request, response) -> response.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> addAddress(String customerId, boolean asDefault) {
        return client.post()
                .uri("/customers/" + customerId + "/addresses")
                .body(Map.of(
                        "zipCode", "01001-000",
                        "street", "Praca da Se",
                        "number", "100",
                        "district", "Se",
                        "city", "Sao Paulo",
                        "state", "SP",
                        "asDefault", asDefault))
                .retrieve()
                .body(Map.class);
    }

    @Test
    void duplicateEmailReturns409() {
        String email = uniqueEmail();
        createCustomer(email);
        assertThat(statusOfCreate(email).value()).isEqualTo(409);
    }

    @Test
    void duplicateEmailIgnoringCaseReturns409() {
        String email = uniqueEmail();
        createCustomer(email.toLowerCase());
        assertThat(statusOfCreate(email.toUpperCase()).value()).isEqualTo(409);
    }

    @Test
    @SuppressWarnings("unchecked")
    void settingNewDefaultAddressUnmarksThePrevious() {
        Map<String, Object> customer = createCustomer(uniqueEmail());
        String id = (String) customer.get("id");

        addAddress(id, true);
        Map<String, Object> afterSecond = addAddress(id, true);

        List<Map<String, Object>> addresses = (List<Map<String, Object>>) afterSecond.get("addresses");
        long defaults = addresses.stream().filter(a -> Boolean.TRUE.equals(a.get("isDefault"))).count();
        assertThat(addresses).hasSize(2);
        assertThat(defaults).isEqualTo(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void firstAddressIsDefaultAndStaysSingle() {
        Map<String, Object> customer = createCustomer(uniqueEmail());
        String id = (String) customer.get("id");

        Map<String, Object> result = addAddress(id, false);
        List<Map<String, Object>> addresses = (List<Map<String, Object>>) result.get("addresses");

        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).get("isDefault")).isEqualTo(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deactivateAndReactivate() {
        Map<String, Object> customer = createCustomer(uniqueEmail());
        String id = (String) customer.get("id");

        Map<String, Object> inactive = client.post().uri("/customers/" + id + ":deactivate")
                .retrieve().body(Map.class);
        assertThat(inactive.get("status")).isEqualTo("INACTIVE");

        Map<String, Object> active = client.post().uri("/customers/" + id + ":activate")
                .retrieve().body(Map.class);
        assertThat(active.get("status")).isEqualTo("ACTIVE");
    }

    @Test
    void versionConflictReturns409() {
        Map<String, Object> customer = createCustomer(uniqueEmail());
        String id = (String) customer.get("id");

        client.put().uri("/customers/" + id)
                .body(Map.of("name", "Renamed", "email", uniqueEmail(), "version", 0))
                .retrieve().toBodilessEntity();

        assertThat(statusOfPut("/customers/" + id,
                Map.of("name", "Stale", "email", uniqueEmail(), "version", 0)).value())
                .isEqualTo(409);
    }

    @Test
    void unknownCustomerReturns404() {
        HttpStatusCode status = client.get().uri("/customers/" + UUID.randomUUID())
                .exchange((request, response) -> response.getStatusCode());
        assertThat(status.value()).isEqualTo(404);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listSupportsFiltersAndPagination() {
        Map<String, Object> a = createCustomer(uniqueEmail());
        Map<String, Object> b = createCustomer(uniqueEmail());
        createCustomer(uniqueEmail());

        client.post().uri("/customers/" + a.get("id") + ":deactivate").retrieve().toBodilessEntity();
        client.post().uri("/customers/" + b.get("id") + ":deactivate").retrieve().toBodilessEntity();

        Map<String, Object> page = client.get()
                .uri("/customers?status=INACTIVE&page=0&size=1&sort=name,asc")
                .retrieve()
                .body(Map.class);

        assertThat(((Number) page.get("totalElements")).longValue()).isEqualTo(2L);
        assertThat((List<Object>) page.get("content")).hasSize(1);
        assertThat(((Number) page.get("size")).intValue()).isEqualTo(1);
        assertThat(((Number) page.get("page")).intValue()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchFiltersByEmailCaseInsensitively() {
        String email = uniqueEmail();
        createCustomer(email);

        Map<String, Object> page = client.get()
                .uri("/customers?search=" + email.substring(0, 8).toUpperCase())
                .retrieve()
                .body(Map.class);

        assertThat(((Number) page.get("totalElements")).longValue()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void duplicateEmailOnUpdateReturns409() {
        String existing = uniqueEmail();
        createCustomer(existing);
        Map<String, Object> other = createCustomer(uniqueEmail());
        String id = (String) other.get("id");

        assertThat(statusOfPut("/customers/" + id,
                Map.of("name", "Joao", "email", existing.toUpperCase(), "version", 0)).value())
                .isEqualTo(409);
    }
}
