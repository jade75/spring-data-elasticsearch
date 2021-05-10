package ie.d00216118.services;

import ie.d00216118.models.Goods;
import ie.d00216118.utils.AbstractESRestTemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author D00216118@DkIT
 * @date 2021/5/1
 */
@Service
public class GoodsESTemplate extends AbstractESRestTemplateUtil {

    @Autowired
    protected GoodsESTemplate(final ElasticsearchOperations elasticsearchOperations) {
        super(Goods.class, elasticsearchOperations);
    }

    @Override
    protected String getIndexName() {
        return "goods";
    }


    public List<Goods> findByCategory(String value) {
        //"category", "car"
        return findByValue("category", value);
    }

    public Page<Goods> findByCategoryPage(String value, Pageable pageable) {
        return findByValueWithPage("category", value, pageable);
    }

}
