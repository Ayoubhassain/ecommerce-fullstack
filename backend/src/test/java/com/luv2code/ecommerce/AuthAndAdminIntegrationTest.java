package com.luv2code.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.ecommerce.dao.ProductCategoryRepository;
import com.luv2code.ecommerce.dao.ProductRepository;
import com.luv2code.ecommerce.entity.Product;
import com.luv2code.ecommerce.entity.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end tests of the security rules, against an in-memory H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthAndAdminIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProductCategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;

    Long categoryId;
    Long productId;

    @BeforeEach
    void setUp() {
        ProductCategory category = new ProductCategory();
        category.setCategoryName("Books");
        categoryId = categoryRepository.save(category).getId();

        Product product = new Product();
        product.setSku("BOOK-1");
        product.setName("Clean Code");
        product.setUnitPrice(new BigDecimal("30.00"));
        product.setActive(true);
        product.setUnitsInStock(10);
        product.setCategory(category);
        productId = productRepository.save(product).getId();
    }

    // ---------- helpers ----------

    private String register(String email, String password) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password
                + "\",\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}";
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String productJson(String sku) {
        return "{\"sku\":\"" + sku + "\",\"name\":\"New product\",\"unitPrice\":12.5,"
                + "\"unitsInStock\":5,\"active\":true,\"categoryId\":" + categoryId + "}";
    }

    // ---------- public catalog ----------

    @Test
    void catalogIsPublic() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/api/product-category")).andExpect(status().isOk());
    }

    @Test
    void customersAndUsersAreNotExposedByDataRest() throws Exception {
        String admin = login("admin@test.com", "AdminPass123");
        mvc.perform(get("/api/customers").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    // ---------- authentication ----------

    @Test
    void registerThenLoginThenMe() throws Exception {
        register("ada@test.com", "password123");
        String token = login("ADA@test.com", "password123");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void registerRejectsDuplicateEmailAndWeakPassword() throws Exception {
        register("ada@test.com", "password123");

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@test.com\",\"password\":\"password123\",\"firstName\":\"A\",\"lastName\":\"B\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bob@test.com\",\"password\":\"short\",\"firstName\":\"A\",\"lastName\":\"B\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        register("ada@test.com", "password123");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@test.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- admin area ----------

    @Test
    void adminAreaRequiresAdminRole() throws Exception {
        mvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());

        String user = register("ada@test.com", "password123");
        mvc.perform(get("/api/admin/products").header("Authorization", "Bearer " + user))
                .andExpect(status().isForbidden());

        String admin = login("admin@test.com", "AdminPass123");
        mvc.perform(get("/api/admin/products").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void adminCanCreateUpdateAndDeleteProducts() throws Exception {
        String admin = login("admin@test.com", "AdminPass123");

        MvcResult created = mvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(productJson("NEW-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Books"))
                .andReturn();
        long id = json.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(put("/api/admin/products/" + id)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("NEW-1").replace("New product", "Renamed")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));

        mvc.perform(delete("/api/admin/products/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
        assertThat(productRepository.findById(id)).isEmpty();
    }

    @Test
    void productValidationErrorsAreReturned() throws Exception {
        String admin = login("admin@test.com", "AdminPass123");
        mvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"\",\"name\":\"X\",\"unitPrice\":-1,\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.unitPrice").exists());
    }

    // ---------- orders ----------

    private String purchaseJson(String email, String clientPrice) {
        return "{\"customer\":{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"" + email + "\"},"
                + "\"shippingAddress\":{\"street\":\"1 rue\",\"city\":\"Paris\",\"state\":\"IDF\",\"country\":\"France\",\"zipCode\":\"75001\"},"
                + "\"billingAddress\":{\"street\":\"1 rue\",\"city\":\"Paris\",\"state\":\"IDF\",\"country\":\"France\",\"zipCode\":\"75001\"},"
                + "\"order\":{\"totalPrice\":" + clientPrice + ",\"totalQuantity\":2},"
                + "\"orderItems\":[{\"productId\":" + productId + ",\"quantity\":2,\"unitPrice\":" + clientPrice + "}]}";
    }

    @Test
    void loggedInCustomerSeesTheirOrdersWithServerSidePrices() throws Exception {
        String user = register("ada@test.com", "password123");

        // the browser sends a fake price of 0.01 and another email: both are ignored
        mvc.perform(post("/api/checkout/purchase")
                        .header("Authorization", "Bearer " + user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson("someone-else@test.com", "0.01")))
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(get("/api/orders/me").header("Authorization", "Bearer " + user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andReturn();
        JsonNode order = json.readTree(result.getResponse().getContentAsString()).get("content").get(0);
        assertThat(order.get("totalPrice").decimalValue()).isEqualByComparingTo("60.00");
        assertThat(order.get("status").asText()).isEqualTo("NEW");

        mvc.perform(get("/api/orders/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanListOrdersAndChangeStatus() throws Exception {
        mvc.perform(post("/api/checkout/purchase").contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson("guest@test.com", "30")))
                .andExpect(status().isOk());

        String admin = login("admin@test.com", "AdminPass123");
        MvcResult result = mvc.perform(get("/api/admin/orders").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerEmail").value("guest@test.com"))
                .andReturn();
        long orderId = json.readTree(result.getResponse().getContentAsString())
                .get("content").get(0).get("id").asLong();

        mvc.perform(put("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"shipped\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mvc.perform(put("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"LOST\"}"))
                .andExpect(status().isBadRequest());
    }
}
