import DTO.UserNotFoundResponseDTO;
import DTO.UserRequestDTO;
import DTO.UserResponseDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {
    private static RequestSpecification spec;

    @BeforeAll
    public static void setupSpec(){
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer S4nungQWB_-O1hiKAfHEkeIVfX1LjC41HYA0")
                .setBaseUri("https://gorest.co.in/public-api")
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    public void createUserAndCheckExistenceAndDeleteUser(){
        UserRequestDTO newUser = createDummyUser();
        String location = createResource("/users", newUser);
        UserResponseDTO retrievedUser = getResource(location, UserResponseDTO.class);
        assertEqualUser(newUser, retrievedUser);
        deleteResource(location);
        assertUserIsDeleted(location, getResource(location, UserNotFoundResponseDTO.class));
    }

    @Test
    public void updateUser(){
        UserRequestDTO user = createDummyUser();
        UserRequestDTO userUpdated = createAnUpdatedDummyUser();
        String location = createResource("/users", userUpdated);
        UserResponseDTO retrievedUserUpdated = updateResource(location, UserResponseDTO.class);
        assertEqualUser(userUpdated, retrievedUserUpdated);
        deleteResource(location);
    }

    @Test
    public void getMultipleUsers(){
        int MAX_USERS = 10;
        JsonPath genericGet = given()
                .spec(spec)
                .get("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath();
        assertThat(genericGet.getList("result")).hasSizeGreaterThan(MAX_USERS);
    }

    @Test
    public void getUserByName(){
        UserRequestDTO newUser = createDummyUser();
        String location = createResource("/users", newUser);
        assertThat(getUserByName("Teste").getList("result")).hasSizeGreaterThanOrEqualTo(1);
        deleteResource(location);
    }

    //TODO: negative tests

    private UserRequestDTO createDummyUser(){
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setFirst_name("Teste");
        userRequestDTO.setLast_name("Teste");
        userRequestDTO.setEmail("l@t.co");
        userRequestDTO.setGender("female");
        userRequestDTO.setPhone("999999999");
        userRequestDTO.setStatus("active");
        return userRequestDTO;
    }

    private UserRequestDTO createAnUpdatedDummyUser(){
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setFirst_name("Teste2");
        userRequestDTO.setLast_name("Teste2");
        userRequestDTO.setEmail("m@t.co");
        userRequestDTO.setGender("male");
        userRequestDTO.setPhone("111111111");
        userRequestDTO.setStatus("inactive");
        return userRequestDTO;
    }

    private String createResource(String path, Object bodyPayload){
        return given()
                .spec(spec)
                .body(bodyPayload)
                .when()
                .post(path)
                .then()
                .statusCode(302)
                .extract().header("Location");
    }

    private <T> T getResource(String location, Class<T> responseClass){
        return given()
                .spec(spec)
                .when()
                .get(location)
                .then()
                .statusCode(200)
                .extract().as(responseClass);
    }

    private JsonPath getUserByName(String name){
        return given()
                .spec(spec)
                .queryParam("first_name", name)
                .when()
                .get("/users")
                .then()
                .extract().jsonPath();
    }
    private <T> T updateResource(String location, Class<T> responseClass){
        return given()
                .spec(spec)
                .when()
                .put(location)
                .then()
                .statusCode(200)
                .extract().as(responseClass);
    }

    private void assertEqualUser(UserRequestDTO newUser, UserResponseDTO retrievedUser){
        assertThat(retrievedUser.result).isEqualToIgnoringGivenFields(newUser, "id");
    }

    private void deleteResource(String location){
         given()
            .spec(spec)
            .when()
            .delete(location)
            .then()
            .statusCode(200);
    }

    private void assertUserIsDeleted(String location, UserNotFoundResponseDTO retrievedUser){
        assertThat(retrievedUser.result.getStatus()).isEqualTo(404);
        assertThat(retrievedUser.result.getName()).isEqualTo("Not Found");
    }
}
