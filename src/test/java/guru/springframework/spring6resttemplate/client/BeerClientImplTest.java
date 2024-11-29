package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

@SpringBootTest
class BeerClientImplTest {

  @Autowired
  private BeerClient beerClient;

  @Test
  void listBeersWithName() {

    Page<BeerDTO> restResponse = beerClient.listBeers("ALE", null, null, null, null);
    assertEquals(636,restResponse.getTotalElements());
  }

  @Test
  void listAllBeers() {
    Page<BeerDTO> restResponse = beerClient.listBeers();
    assertEquals(2413,restResponse.getTotalElements());
  }
}