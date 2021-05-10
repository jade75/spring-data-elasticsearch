package ie.d00216118;

import ie.d00216118.models.Goods;
import ie.d00216118.repositories.GoodsRepository;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author D00216118@DkIT
 * @date 2021/5/4
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    //    @MockBean
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchRestTemplate;

    @Before
    public void initialization() {

        //delete all document
        goodsRepository.deleteAll();


        //create index of document
//        elasticsearchRestTemplate.indexOps(Goods.class).create();
//        elasticsearchRestTemplate.indexOps(Goods.class).putMapping();


        Goods g1 = new Goods(1L, "food1", "this is food1", 2.89, "food", 1100, "food company");
        Goods g2 = new Goods(2L, "food2", "this is food2", 3.99, "food", 990, "food company2");
        Goods g3 = new Goods(3L, "food3", "this is food3", 10.89, "food", 800, "food company");
        Goods g4 = new Goods(4L, "food4", "this is food4", 13.00, "food", 798, "food company2");

        Goods g5 = new Goods(5L, "car1", "this is car1", 1300.00, "car", 80, "car company");
        Goods g6 = new Goods(6L, "car2", "this is car2", 2500.00, "car", 18, "car company");
        Goods g7 = new Goods(7L, "car3", "this is car3", 4100.00, "car", 19, "car company2");
        Goods g8 = new Goods(8L, "car4", "this is car4", 2900.00, "car", 26, "car company2");

        List<Goods> list = new ArrayList<>();
        list.add(g1);
        list.add(g2);
        list.add(g3);
        list.add(g4);
        list.add(g5);
        list.add(g6);
        list.add(g7);
        list.add(g8);

        //add with all
        goodsRepository.saveAll(list);
    }

    //    @Test
    public void x() {
        assertEquals(1, 1);
    }

    // @Test
    public void find() {

        List<Goods> gs = null;
        //find by name
        Goods gds = this.goodsRepository.findByGoodsName("food1");
//        assertEquals(1, gds.);
        System.out.println("---------find by name---------");
        System.out.println(gds);

        gs = this.goodsRepository.findByDescription("this is");
        assertEquals(8, gs.size());
        System.out.println("---------find by [description]---------");
        gs.forEach(x -> System.out.println(x));

        //and
        gs.clear();
        gs = this.goodsRepository.findByCategoryAndManufacturer("food", "food company2");
        System.out.println("---------find by [category and Manufacturer]---------");
        gs.forEach(x -> System.out.println(x));

        // or
        gs.clear();
        gs = this.goodsRepository.findByCategoryOrManufacturer("food", "company");
        System.out.println("---------find by  [category or Manufacturer]---------");
        gs.forEach(x -> System.out.println(x));


        //beteen..and
        gs.clear();
        gs = this.goodsRepository.findByPriceBetween(3.00, 11.00);
        System.out.println("---------find by [ price beteen and ]---------");
        gs.forEach(x -> System.out.println(x));


        //great than
        gs.clear();
        gs = this.goodsRepository.findByQuantityGreaterThanEqualOrderByPriceDesc(20);
        System.out.println("---------find by [ greater than >= ] and [oder desc]---------");
        gs.forEach(x -> System.out.println(x));


        //page example
        gs.clear();
        Pageable pageable1 = PageRequest.of(0, 1);
        Page<Goods> pgs = this.goodsRepository.findGoodsByCategoryOrderByIdDesc("food", pageable1);
        System.out.println("---------find by page 1 size:1 ---------");
        pgs.forEach(x -> System.out.println(x));

        Pageable pageable2 = PageRequest.of(1, 1);
        Page<Goods> pgs2 = this.goodsRepository.findGoodsByCategoryOrderByIdDesc("food", pageable2);
        System.out.println("---------find by page 2 size:1 ---------");
        pgs2.forEach(x -> System.out.println(x));

        gs.clear();
        System.out.println("---------find by query ---------");
        List x = this.goodsRepository.findByCategory("food");
        System.out.println(x.size());

        gs.clear();
        System.out.println("---------find price >= ? ---------");
        gs =this.goodsRepository.findByPriceGreaterThanEqual(8.90);
        gs.stream().forEach(System.out::println);

    }

    /*
     it sames like SQl group by: select count(*)as DocCount,category from table group by category

     return:
      key = car DocCount = 4
      key = food DocCount = 4
     */
//    @Test
    public void userAggregationQuery() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("mycategory").field("category"));
        SearchHits searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), Goods.class);

        Map<String, Aggregation> results = searchHits.getAggregations().asMap();
        Terms terms = (Terms)results.get("mycategory");

        List<?extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            System.out.println("key = " + bucket.getKeyAsString());
            System.out.println("DocCount = " + bucket.getDocCount());
        }

//        System.out.println(searchHits.getTotalHits());
//        System.out.println(searchHits.hasAggregations());
    }


    /*
    https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/_structuring_aggregations.html

    same: select count(*)as DocCount,category, avg(price) from table group by category

    return:
    key = car DocCount = 4 Average price = 2700.0
    key = food DocCount = 4 Average price = 7.692500000000001

    avg , sum, max, min ... ...
     */
    @Test
    public void userAggregationWtihSubQuery(){
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.addAggregation(
                AggregationBuilders.terms("mycategory").field("category")
                        .subAggregation(AggregationBuilders.avg("priceAvg").field("price"))
        );
        SearchHits searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), Goods.class);
        Map<String, Aggregation> results = searchHits.getAggregations().asMap();
        Terms terms = (Terms)results.get("mycategory");
        List<?extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            System.out.println("key = " + bucket.getKeyAsString());
            System.out.println("DocCount = " + bucket.getDocCount());
            Avg avg = (Avg) bucket.getAggregations().asMap().get("priceAvg");
            System.out.println("Average price = " + avg.getValue());
        }

    }


}
