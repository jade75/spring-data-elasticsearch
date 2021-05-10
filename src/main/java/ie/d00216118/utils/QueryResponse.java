package ie.d00216118.utils;

import lombok.Data;
import org.springframework.data.elasticsearch.core.SearchHit;

/**
 * @author D00216118@DkIT
 * @date 2021/5/1
 *
 * SearchHit{id='4', score=NaN, sortValues=[1000.86],
 * content=Goods(id=4, goodsName=truck, description=this is truck, price=1000.86, category=car, quantity=null, manufacturer=truck company), highlightFields={}}
 *
 */
@Data
public class QueryResponse<T> {
    private final float score;
    private final T content;

    public QueryResponse(SearchHit<T> searchHit) {
        this.score = searchHit.getScore();
        this.content = searchHit.getContent();
    }
}

