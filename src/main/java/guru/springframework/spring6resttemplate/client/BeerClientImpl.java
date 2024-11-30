package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class BeerClientImpl implements BeerClient {

  private static final String GET_BEER_PATH = "/api/v1/beer";
  private static final String GET_BEER_BY_ID_PATH = GET_BEER_PATH + "/{beerId}";
  private final RestTemplateBuilder restTemplateBuilder;

  @Override
  public BeerDTO getBeerById(UUID beerId) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    return restTemplate.getForObject(GET_BEER_BY_ID_PATH, BeerDTO.class, beerId);
  }

  @Override
  public Page<BeerDTO> listBeers(){
    return listBeers(null, null, null, null, null);
  }

  @Override
  public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber,
      Integer pageSize) {
    RestTemplate restTemplate = restTemplateBuilder.build();

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(GET_BEER_PATH);

    if (beerName != null) {
      uriComponentsBuilder.queryParam("beerName", beerName);
    }

    if (beerStyle != null) {
      uriComponentsBuilder.queryParam("beerStyle", beerStyle);
    }

    if (showInventory != null) {
      uriComponentsBuilder.queryParam("showInventory", beerStyle);
    }

    if (pageNumber != null) {
      uriComponentsBuilder.queryParam("pageNumber", beerStyle);
    }

    if (pageSize != null) {
      uriComponentsBuilder.queryParam("pageSize", beerStyle);
    }

    ResponseEntity<BeerDTOPageImpl> response = restTemplate.getForEntity(uriComponentsBuilder.toUriString() , BeerDTOPageImpl.class);

    return response.getBody();
  }

  @Override
  public BeerDTO createBeer(BeerDTO newBeerDTO) {
    RestTemplate restTemplate = restTemplateBuilder.build();

    URI uri = restTemplate.postForLocation(GET_BEER_PATH, newBeerDTO);

    return restTemplate.getForObject(uri.getPath(), BeerDTO.class);
  }

  @Override
  public BeerDTO updateBeer(BeerDTO updatedBeer) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    restTemplate.put(GET_BEER_BY_ID_PATH, updatedBeer, updatedBeer.getId());
    return getBeerById(updatedBeer.getId());
  }

  @Override
  public void deleteBeer(BeerDTO beer) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    restTemplate.delete(GET_BEER_BY_ID_PATH, beer.getId());
  }
}
