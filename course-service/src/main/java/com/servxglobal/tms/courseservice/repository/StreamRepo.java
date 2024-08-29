package com.servxglobal.tms.courseservice.repository;
import com.servxglobal.tms.courseservice.model.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamRepo extends MongoRepository<Stream, Long> {
    @Query("{ 'is_deleted' : false }")
    List<Stream> findAllActiveStreams();

    /**get by the stream_name given stream document **/
    @Query( value = "{'is_deleted' :  false}", fields = "{'stream_name': 1}")
    List<?> findSpecificStreamNameDetails();

    /**
     * Retrieves a list of streams by their IDs.
     *
     * @return  a list of streams whose IDs are provided
     */
    List<Stream> findByIdIn(List<Long> ids);
}
