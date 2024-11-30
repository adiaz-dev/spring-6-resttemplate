package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
class BeerClientImplTest {

  @Autowired
  private BeerClient beerClient;

  @Test
  void getBeerById() {
    Page<BeerDTO> restResponse = beerClient.listBeers();
    BeerDTO beer = restResponse.getContent().get(0);
    BeerDTO beerFound = beerClient.getBeerById(beer.getId());

    assertNotNull(beerFound);
  }

  @Test
  void listBeersWithName() {
   Page<BeerDTO> restResponse = beerClient.listBeers("ALE", null, null, null, null);
    assertEquals(636,restResponse.getTotalElements());
  }

  @Test
  void listAllBeers() {
    Page<BeerDTO> restResponse = beerClient.listBeers();
    assertEquals(2420,restResponse.getTotalElements());
  }

  @Test
  void testCreateBeer() {
    BeerDTO newBeerDTO = BeerDTO.builder()
        .price(new BigDecimal("10.99"))
        .beerName("Mango Bobs")
        .beerStyle(BeerStyle.IPA)
        .quantityOnHand(500)
        .upc("12345")
        .build();

    BeerDTO savedDto = beerClient.createBeer(newBeerDTO);
    assertNotNull(savedDto);
    //delete it
    beerClient.deleteBeer(savedDto);
  }

  @Test
  void testUpdateBeer() {
    BeerDTO newBeerDTO = BeerDTO.builder()
        .price(new BigDecimal("25.00"))
        .beerName("Miverva Stout")
        .beerStyle(BeerStyle.STOUT)
        .quantityOnHand(100)
        .upc("45353")
        .build();

    BeerDTO newBeer = beerClient.createBeer(newBeerDTO);
    final String beerName = "Minerva Stout Imperial";
    newBeer.setBeerName(beerName);

    BeerDTO updateBeer = beerClient.updateBeer(newBeer);
    assertEquals(beerName, updateBeer.getBeerName());

    //delete it
    beerClient.deleteBeer(updateBeer);
  }


  @Test
  void testDeleteBeer() {
    BeerDTO newBeerDTO = BeerDTO.builder()
        .price(new BigDecimal("25.00"))
        .beerName("Miverva Stout")
        .beerStyle(BeerStyle.STOUT)
        .quantityOnHand(100)
        .upc("45353")
        .build();

    //create the beer to be later removed
    BeerDTO newBeer = beerClient.createBeer(newBeerDTO);

    //delete the created beear
    beerClient.deleteBeer(newBeer);

    //validate beer was deleted
    assertThrows(HttpClientErrorException.class, () -> {
      beerClient.getBeerById(newBeer.getId());
    });
  }
}