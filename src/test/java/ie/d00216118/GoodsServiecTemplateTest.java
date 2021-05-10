package ie.d00216118;

import ie.d00216118.models.Goods;
import ie.d00216118.services.GoodsESTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author D00216118@DkIT
 * @date 2021/5/10
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GoodsServiecTemplateTest {

    @Autowired
    private GoodsESTemplate goodsService;


    /*
     if exist of index then delete and add index
     */
//    @Before
    public void setUp() {
        if (goodsService.isExists()) {
            goodsService.deleteIndex();

        }

        goodsService.createIndex();
        goodsService.putMapping();

        //print mapping
        System.out.println(goodsService.mappingToString());
        goodsService.refresh();

    }

//    @After
    public void afterUp(){
        goodsService.deleteAllItems();

        //add
        Goods g1 = new Goods(1L, "food1", "this is food1", 2.89, "food", 1100, "food company");
        Goods g2 = new Goods(2L, "food2", "this is food2", 3.99, "food", 990, "food company2");
        Goods g3 = new Goods(3L, "food3", "this is food3", 10.89, "food", 800, "food company");
        Goods g4 = new Goods(4L, "food4", "this is food4", 13.00, "food", 798, "food company2");
        Goods g5 = new Goods(5L, "car1", "this is car1", 1300.00, "car", 80, "car company");
        Goods g6 = new Goods(6L, "car2", "this is car2", 2500.00, "car", 18, "car company");
        Goods g7 = new Goods(7L, "car3", "this is car3", 4100.00, "car", 19, "car company2");
        List<Goods> list = new ArrayList<>();
        list.add(g1);
        list.add(g2);
        list.add(g3);
        list.add(g4);
        list.add(g5);
        list.add(g6);
        list.add(g7);
        List<IndexedObjectInformation>  x= goodsService.bulkIndexItem(list);
        goodsService.refresh();
        x.stream().forEach(System.out::println);

        Goods g8 = new Goods(9L, "car5", "this is car5", 9900.00, "car", 56, "car company2");
        String y = goodsService.indexItem(g8);
        goodsService.refresh();
        System.out.println(y);
    }

    @Test
    public void test1() {
       List x= goodsService.findByCategory("food");
       x.stream().forEach(System.out::println);

       System.out.println("---------");
        Pageable pageable1 = PageRequest.of(0, 1);
        Page y= goodsService.findByCategoryPage("food",pageable1);
        y.stream().forEach(System.out::println);
    }
}
