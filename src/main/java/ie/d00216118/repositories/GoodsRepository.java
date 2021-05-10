package ie.d00216118.repositories;

import ie.d00216118.models.Goods;
import org.elasticsearch.common.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {

    @Nullable
    Goods findByGoodsName(@Nullable  String goodsName);

    List<Goods> findByDescription(String description);

    List<Goods> findByCategoryAndManufacturer(String category, String manufacturer);

    List<Goods> findByCategoryOrManufacturer(String category, String manufacturer);

    List<Goods> findByPriceBetween(double priceStart, double priceEnd);

    List<Goods> findByQuantityGreaterThanEqualOrderByPriceDesc(Integer number);

    Page<Goods> findGoodsByCategoryOrderByIdDesc(String category, Pageable pageable);

    @Query("{\"match\": {\"category\": {\"query\": \"?0\"}}}")
    List findByCategory(String x);



    List<Goods>   findByPriceGreaterThanEqual(double price);


}
