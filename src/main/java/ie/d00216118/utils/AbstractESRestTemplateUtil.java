package ie.d00216118.utils;

import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author D00216118@DkIT
 * @date 2021/5/1
 * <p>
 * docs:
 * https://docs.spring.io/spring-data/elasticsearch/docs/current/api/org/springframework/data/elasticsearch/core/IndexOperations.html
 */
public abstract class AbstractESRestTemplateUtil<T extends Entity> {

    private final Class<T> itemClazz;
    protected final ElasticsearchOperations elasticsearchOperations;
    //Immutable Value object encapsulating index name(s) and index type(s).
    //Type names are supported but deprecated as Elasticsearch does not support types anymore.
    private IndexCoordinates indexCoordinates;

    protected AbstractESRestTemplateUtil(Class<T> itemClazz, ElasticsearchOperations elasticsearchOperations) {
        this.itemClazz = itemClazz;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    //build the simple query which match all documents
    //{ "query": { "match_all": {} } }
    private static final Query MATCH_All = new NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery()).build();

    protected abstract String getIndexName();

    public IndexCoordinates getIndexCoordinates() {
        if (indexCoordinates == null) {
            indexCoordinates = IndexCoordinates.of(getIndexName());
        }
        return indexCoordinates;
    }

    //The operations for the Elasticsearch Index APIs.
    // IndexOperations are bound to an entity class
    private IndexOperations getIndexOperations() {
        return elasticsearchOperations.indexOps(itemClazz);
    }

    public boolean createIndex() {
        return getIndexOperations().create();
    }

    public boolean deleteIndex() {
        return getIndexOperations().delete();
    }

    //Checks if the index this IndexOperations is bound to exists
    public boolean isExists() {
        return getIndexOperations().exists();
    }

    //Refresh the index(es) this IndexOperations is bound to
    public void refresh() {
        getIndexOperations().refresh();
    }

    //Get mapping for an index defined by a class.
    private Map getMapping() {
        return getIndexOperations().getMapping();
    }

    //Writes the mapping to the index for the class this IndexOperations is bound to.
    public boolean putMapping() {
        return getIndexOperations().putMapping(createMapping());
    }

    //Creates the index mapping for the entity this IndexOperations is bound to.
    private Document createMapping() {
        return getIndexOperations().createMapping();
    }

    public long count() {
        Query searchQuery = new NativeSearchQueryBuilder().build();
        return elasticsearchOperations.count(searchQuery, itemClazz);
    }

    //add object to index
    public String indexItem(T item) {
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(String.valueOf(item.getId()))
                .withObject(item)
                .build();
        String documentId = elasticsearchOperations.index(indexQuery, getIndexCoordinates());
        return documentId;
    }

    //Bulk index all objects. Will do save or update.
    //Returns:
    //the information about of the indexed objects
    public List<IndexedObjectInformation> bulkIndexItem(final List<T> items) {
        Function<T, IndexQuery> itemToIndexQueryMapper = item -> new IndexQueryBuilder()
                .withId(String.valueOf(item.getId()))
                .withObject(item)
                .build();

        List<IndexQuery> queries = items.stream()
                .map(itemToIndexQueryMapper)
                .collect(Collectors.toList());
        return elasticsearchOperations.bulkIndex(queries, getIndexCoordinates());
    }

    //find by id
    public T findById(String id) {
        return elasticsearchOperations.get(id, itemClazz);
    }

    public void deleteAllItems() {
        elasticsearchOperations.delete(MATCH_All, itemClazz, getIndexCoordinates());
    }

    protected List<T> findByValue(String fieldName, String value) {
        Query searchQuery = createBasicMatchQuery(fieldName, value);
        SearchHits<T> searchHits = elasticsearchOperations.search(searchQuery, itemClazz, getIndexCoordinates());
        List<T> items = getItems(searchHits);
        return items;
    }

    //page with query
    protected Page<T> findByValueWithPage(String fieldName, String value, Pageable pageable){
        Query searchQuery = createPageMatchQuery(fieldName, value, pageable);
        SearchHits<T> searchHits =elasticsearchOperations.search(searchQuery, itemClazz, getIndexCoordinates());
        Page<T> items = getItemsPage(searchHits,pageable);
        return items;
    }

    protected NativeSearchQuery createBasicMatchQuery(String fieldName, String value) {
      /*
        Using NativeQuery
        NativeQuery provides the maximum flexibility for building a query using objects representing Elasticsearch
        constructs like aggregation, filter, and sort.
      */
        MatchQueryBuilder queryBuilder = createMatchQueryBuilder(fieldName, value);
        return new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
    }

    protected NativeSearchQuery createPageMatchQuery(String fieldName, String value, Pageable pageable){
        MatchQueryBuilder queryBuilder = createMatchQueryBuilder(fieldName, value);
       return new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(pageable)
                .build();
    }



    private MatchQueryBuilder createMatchQueryBuilder(String fieldName, String value) {
        return QueryBuilders
                .matchQuery(fieldName, value);
        // .fuzziness(Fuzziness.ONE)
        //    .operator(Operator.AND)
        // .prefixLength(3);
    }

    //return the object of name QueryResponse which encapsulating the searchHits
    private List<QueryResponse<T>> getQueryResponses(SearchHits<T> searchHits) {
        if (searchHits.isEmpty()) {
            return new ArrayList<>();
        }
        return searchHits.stream()
                .map(QueryResponse::new)
                .collect(Collectors.toList());
    }

    private List<T> getItems(SearchHits<T> searchHits) {
        return getQueryResponses(searchHits)
                .stream()
                .map(res -> res.getContent())
                .collect(Collectors.toList());
    }


    //wrap as page
    private Page<T> getItemsPage(SearchHits<T> searchHits, Pageable pageable){
        List<T> items=getItems(searchHits);
        return  new PageImpl<>(items, pageable,searchHits.getTotalHits());
    }


    public String mappingToString() {
        StringBuffer sb = new StringBuffer(" ------ Index mapping:\n");
        Map mapping = getMapping();
        mapping.forEach((key, val) -> {
            sb.append("key:");
            sb.append(key);
            sb.append(" val:");
            sb.append(val);
            sb.append("\n");
        });
        return sb.toString();
    }
}
