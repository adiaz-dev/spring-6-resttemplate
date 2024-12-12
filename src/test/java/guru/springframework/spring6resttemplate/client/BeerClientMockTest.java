package guru.springframework.spring6resttemplate.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import java.util.UUID;

import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
class BeerClientMockTest {

  static final String URL = "http://localhost:8080";
  static final String fixedId = "67b26616-943e-49e0-a70c-8e9ddcba1baa";

  private BeerClient beerClient;

  MockRestServiceServer server;

  @Autowired
  RestTemplateBuilder restTemplateBuilderConfigured;

  @Autowired
  ObjectMapper objectMapper;

  @Mock
  RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

  BeerDTO dto;
  String dtoJson;

  @BeforeEach
  void setUp() throws JsonProcessingException {
    RestTemplate restTemplate = restTemplateBuilderConfigured.build();
    server = MockRestServiceServer.bindTo(restTemplate).build();
    when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
    beerClient = new BeerClientImpl(mockRestTemplateBuilder);

    //common objects
    dto = getBeerDto();
    dtoJson = objectMapper.writeValueAsString(dto);
  }

  @Test
  void getBeerById() throws JsonProcessingException{

     server.expect(method(HttpMethod.GET))
         .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
         .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, fixedId))
        .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));


    BeerDTO beerFound = beerClient.getBeerById(UUID.fromString(fixedId));
    assertNotNull(beerFound);
    assertEquals(fixedId, beerFound.getId().toString());
  }

  @Test
  void testListBeersWithQueryParam() throws JsonProcessingException{

    String response = objectMapper.writeValueAsString(getPage());
    //just build the expected URI to be called
    URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_PATH)
        .queryParam("beerName", "ALE")
        .build().toUri();

    //mock the interaction with the server
    server.expect(method(HttpMethod.GET))
        .andExpect(requestTo(uri))
        .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
        .andExpect(queryParam("beerName", "ALE"))
        .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    Page<BeerDTO> restResponse = beerClient.listBeers("ALE", null, null, null, null);
    assertThat(restResponse.getContent()).hasSize(1);
  }

  @Test
  void listAllBeers() throws JsonProcessingException {

    String payload = objectMapper.writeValueAsString(getPage());

    server.expect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
        .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
        .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));
    Page<BeerDTO> restResponse = beerClient.listBeers();
    assertThat(restResponse.getContent().size()).isGreaterThan(0);
  }

  @Test
  void testCreateBeer() {
    URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH).build(dto.getId());

    //mock a response which will responde 201 with the location header
    server.expect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
        .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
        .andRespond(withAccepted().location(uri));

    mockGetOperation();

    //actual testing: it first does a POST and then a GET to return the DTO just created
    BeerDTO responseDto = beerClient.createBeer(dto);
    assertNotNull(responseDto);
    assertEquals(dto.getId(), responseDto.getId());



  }

  private void mockGetOperation() {
    server.expect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
        .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
        .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
  }

  @Test
  void testUpdateBeer() {

    //mock a response which will respond 202 (No content)
    server.expect(method(HttpMethod.PUT))
        .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
        .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
        .andRespond(withNoContent());

    mockGetOperation();

    BeerDTO beerFound = beerClient.updateBeer(dto);
    assertNotNull(beerFound);
    assertThat(beerFound.getId()).isEqualTo(dto.getId());
  }


  @Test
  void testDeleteBeerNotFound() {

    //as we have to emulate that not found is returned, then we mock that response
    server.expect(method(HttpMethod.DELETE))
        .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
        .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
        .andRespond(withResourceNotFound());

    assertThrows(HttpClientErrorException.class, () -> {
      beerClient.deleteBeer(dto.getId());
    });

    //just verifies the endpoint was actually called and with ResourceNotFound response conde
    server.verify();
  }

  @Test
  void testDeleteBeer() {

    //mock a response which will respond 202 (No content)
    server.expect(method(HttpMethod.DELETE))
        .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
        .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
        .andRespond(withNoContent());

    beerClient.deleteBeer(dto.getId());

    //just verifies the endpoint was actually called
    server.verify();
  }

  BeerDTO getBeerDto(){

    return BeerDTO.builder()
        .id(UUID.fromString(fixedId))
        .price(new BigDecimal("10.99"))
        .beerName("Mango Bobs")
        .beerStyle(BeerStyle.IPA)
        .quantityOnHand(500)
        .upc("123245")
        .build();
  }

  BeerDTOPageImpl getPage(){
    return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
  }
}