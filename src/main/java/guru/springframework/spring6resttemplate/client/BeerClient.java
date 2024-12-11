package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface BeerClient {

  BeerDTO getBeerById(UUID beerId);

  Page<BeerDTO> listBeers();

  Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize);

  BeerDTO createBeer(BeerDTO newBeerDTO);

  BeerDTO updateBeer(BeerDTO newBeer);

  void deleteBeer(BeerDTO beer);

  void deleteBeer(UUID beerId);
}
