
package com.servxglobal.tms.batchservice.otherService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.servxglobal.tms.batchservice.dto.ResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

@FeignClient(name = "course-service")
//@RequestMapping("/api/course")
public interface CourseServiceClient {

    @GetMapping("api/stream/get-by-streams/{id}")
    ResponseDto getByStreamId(@PathVariable("id") Long id);

    @GetMapping("api/course/courses-by-levels/{levelId}")
    ResponseDto getCoursesByLevels(@PathVariable("levelId") Long id);


//    @GetMapping("/resource")
//    String getResourceById(@RequestParam("id") Long id);

}
