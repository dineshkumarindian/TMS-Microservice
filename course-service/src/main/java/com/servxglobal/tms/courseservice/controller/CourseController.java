package com.servxglobal.tms.courseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.servxglobal.tms.courseservice.dto.LevelNode;
import com.servxglobal.tms.courseservice.dto.ResponseDto;
import com.servxglobal.tms.courseservice.dto.StreamCourse;
import com.servxglobal.tms.courseservice.dto.TopicPart;
import com.servxglobal.tms.courseservice.model.*;
import com.servxglobal.tms.courseservice.repository.CourseRepo;
import com.servxglobal.tms.courseservice.repository.LevelRepo;
import com.servxglobal.tms.courseservice.repository.PartsRepo;
import com.servxglobal.tms.courseservice.repository.StreamRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/course")
public class CourseController {
    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private LevelController levelController;

    @Autowired
    private SessionController sessionController;

    @Autowired
    private TopicController topicController;

    @Autowired
    PartsController partsController;

    @Autowired
    private PartsRepo partsRepo;

    @Autowired
    private LevelRepo levelRepo;

    @Autowired
    private StreamRepo streamRepo;

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    /**
     * This function handles the "add course" API.
     * It reads data from an Excel file and adds a new course along with its topics, levels, sessions, and parts.
     * The function returns the newly created course.
     */
    @PostMapping("/add")
    public ResponseEntity<ResponseDto> addCourse(@RequestParam("title") String title, @RequestParam("image") MultipartFile image, @RequestParam("course") MultipartFile file) {
        ResponseEntity<ResponseDto> responseEntity;
        ResponseDto responseDto = new ResponseDto();
        logger.info("CourseController(addCourse) >> Entry");
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0); // Assuming you're reading the first sheet

            Iterator<Row> iterator = sheet.rowIterator();
            boolean isFirstIteration = true; // Initialize a flag
            Topic currentTopic = null;
            Course courseDetail = new Course();
            courseDetail.setCourse_name(title);
            courseDetail.setLogo_Img(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
            courseDetail.setCreated_time(new Date());
            courseDetail.setModified_time(new Date());
            List<Course> allCourses = courseRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if (allCourses.isEmpty()) {
                courseDetail.setId(1L);
            } else {
                courseDetail.setId(allCourses.get(0).getId() + 1);
            }
            Course courseData = courseRepo.save(courseDetail);
            StringBuffer topicNameBuffer = new StringBuffer(); // Initialize a StringBuffer for topicName
            StringBuffer topicCategoryBuffer = new StringBuffer(); // Initialize a StringBuffer for topicCategory
            List<Integer> levelList = new ArrayList<>();
            List<Integer> SessionList = new ArrayList<>();
            List<Session> SessionObjList = new ArrayList<>();
            Topic topic = new Topic();
            while (iterator.hasNext()) {
                Row row = iterator.next();
               // Check if the first cell (levelCellData) is empty or contains no data
                Cell levelCellData = row.getCell(2);
                if (levelCellData == null || levelCellData.getCellType() == CellType.BLANK) {
                    break;
                }
                // Check if it's the first iteration
                if (isFirstIteration) {
                    isFirstIteration = false; // Set the flag to false after skipping the first iteration
                    continue; // Skip the first iteration
                }
                Cell topicNameCell = row.getCell(0);
                Cell topicCategoryCell = row.getCell(1);
                // Check if topicNameCell has a value before appending
                if (!topicNameCell.getStringCellValue().equals("")) {
                    topicNameBuffer.setLength(0);
                    topicNameBuffer.append(topicNameCell.getStringCellValue());
                }
                // Check if topicCategoryCell has a value before appending
                if (!topicCategoryCell.getStringCellValue().equals("")) {
                    topicCategoryBuffer.setLength(0);
                    topicCategoryBuffer.append(topicCategoryCell.getStringCellValue());
                }
                Cell levelCell = row.getCell(2);
                Cell sessionCell = row.getCell(3);
                Cell partCell = row.getCell(4);

                String topicName = topicNameBuffer.toString();
                String topicCategory = topicCategoryBuffer.toString();

                int level = (int) levelCell.getNumericCellValue();
                int session = (int) sessionCell.getNumericCellValue();
                int part = (int) partCell.getNumericCellValue();

                if(part==0 || session == 0){
                    continue;
                }

                if (currentTopic == null || !topicName.equals(currentTopic.getTopic_name())) {
                    currentTopic = new Topic();
                    currentTopic.setTopic_name(topicName);
                    currentTopic.setCategory(topicCategory);
                    currentTopic.setCourse_id(courseData.getId());
                    topic = topicController.addTopic(currentTopic).getBody();
                }

//              // Add level, session, and part to the current topic's lists
                Level addedLevel = new Level();
                if (!levelList.contains(level)) {
                    Level newLevel = new Level();
                    newLevel.setLevel(level);
                    newLevel.setCourse_id(courseData.getId());
                    addedLevel = levelController.addLevel(newLevel).getBody();
                    levelList.add(level);
                }
                Session addedSession = new Session();
                if (!SessionList.contains(session)) {
                    Session newSession = new Session();
                    newSession.setSession_no(session);
                    newSession.setCourse_id(courseData.getId());
                    newSession.setMax_level(level);
                    addedSession = sessionController.addSession(newSession).getBody();
                    SessionList.add(session);
                    SessionObjList.add(addedSession);
                } else {
                    Session matchedData = SessionObjList.stream()
                            .filter(n -> n.getSession_no() == session)
                            .collect(Collectors.toList()).get(0);

                    if (matchedData.getMax_level() < level) {
                        matchedData.setMax_level(level);
                        List<Session> updateSession = SessionObjList.stream()
                                .map(n -> {
                                    if (n.getSession_no() == session) {
                                        n.setMax_level(level);
                                    }
                                    return n;
                                }).collect(Collectors.toList());
                        addedSession = sessionController.updateSession(matchedData.getId(), matchedData).getBody();
                    }
                    else {
                        addedSession = matchedData;
                    }
                }
                Parts addedPart = new Parts();
                addedPart.setPart(part);
                addedPart.setMax_level(level);
                addedPart.setCourse_id(courseData.getId());
                addedPart.setSession_id(addedSession.getId());

                partsController.addParts(part, level, addedSession.getId(), addedSession.getSession_no(), topic.getId(), courseData.getId(), null);

            }
            responseDto.setSuccess(true);
            responseDto.setMessage("course added successful !!");
            responseDto.setData(new Gson().toJson(courseData));
            logger.info("CourseController(addCourse)>> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(addCourse) >> Entry");
        }
        return null;
    }


    /**
     * Retrieves all active courses from the database.
     *
     * @return A ResponseEntity containing a list of Course objects.
     */
    @GetMapping("/active-get-all-courses")
    public ResponseEntity<List<Course>> getAllActiveCourses() {
        logger.info("CourseController(getAllActiveCourses) >> Entry");
        try {
            List<Course> courseList = courseRepo.findAllActiveCourses();
            for(int i=0;i<courseList.size();i++) {
                List<Session> session_details = sessionController.getAllSessionByCourseId(courseList.get(i).getId()).getBody();
                courseList.get(i).setSession_count(session_details.size());
            }
            logger.info("CourseController(getAllActiveCourses)>> Exit");
            return new ResponseEntity<List<Course>>(courseList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(getAllActiveCourses)>> Exit");
        }
        return null;
    }

    /**
     * Retrieves all the course details.
     *
     * @return ResponseEntity<List<Course>> - The response entity containing the list of courses.
     */
    @GetMapping("/get-all-courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        logger.info("CourseController(getAllCourses) >> Entry");
        try {
            List<Course> courseList = courseRepo.findAll();
            logger.info("CourseController(getAllCourses)>> Exit");
            return new ResponseEntity<List<Course>>(courseList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(getAllCourses)>> Exit");
        }
        return null;
    }

    /**
     * Update the course details by the given ID.
     *
     * @param id     The ID of the course to update.
     * @param title  The new title of the course.
     * @param file   The new image file for the course.
     * @return       The updated course if successful, or null if an exception occurred.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestParam("title") String title, @RequestParam("image") MultipartFile file) {
        logger.info("CourseController(updateCourse) >> Entry");
        try {
            Optional<Course> course = courseRepo.findById(id);
            if (course.isPresent()) {
                course.get().setCourse_name(title);
                course.get().setLogo_Img(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
                course.get().setId(id);
                course.get().setModified_time(new Date());
                Course updateCourse = courseRepo.save(course.get());
                logger.info("CourseController(updateCourse)>> Exit");
                return new ResponseEntity<Course>(updateCourse, HttpStatus.OK);
            } else {
                Course updateCourse = null;
                logger.info("CourseController(updateCourse)>> Exit");
                return new ResponseEntity<Course>(updateCourse, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(updateCourse)>> Exit");
        }
        return null;
    }

    /**
     * Soft deletes a course by setting its "_deleted" flag to true.
     *
     * @param id The ID of the course to delete.
     * @return The updated course if found, or a bad request response if not.
     */
    @PutMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteCourse(@PathVariable Long id) {
        logger.info("CourseController(deleteCourse) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Course> courseDetails = courseRepo.findById(id);
            Course updateCourse = new Course();
            final Long courseId = id;
//            System.out.println(courseId);
            if (courseDetails.isPresent()) {
                List<Stream> streamDetails = streamRepo.findAllActiveStreams();
                for(int i=0;i<streamDetails.size();i++) {
                    Stream streamData = streamDetails.get(i);
                    Object courseLevelData = streamData.getCourseDetailsWithLevel();
                    String jsonString = courseLevelData.toString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    StreamCourse[] courses = objectMapper.readValue(jsonString, StreamCourse[].class);
                    List<StreamCourse> jsonList = new ArrayList<>();
                    for (StreamCourse course : courses) {
                        jsonList.add(course);
                    }
                    for(int k=0;k<jsonList.size();k++) {
                        StreamCourse courseData = jsonList.get(k);
                        if(courseData.getId() == courseId) {
//                            System.out.println("StreamCourse..."+courseId);
                            jsonList.remove(courseData);
                            Optional<Stream> streamByData = streamRepo.findById(streamData.getId());
                            streamByData.get().setCourseDetailsWithLevel( new Gson().toJson(jsonList));
                            Stream updateData = streamRepo.save(streamByData.get());
                        }
                    }
                }
                courseDetails.get().set_deleted(true);
                updateCourse = courseRepo.save(courseDetails.get());
                responseDto.setSuccess(true);
                responseDto.setMessage("course added successful !!");
                logger.info("CourseController(deleteCourse)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                responseDto.setSuccess(false);
                responseDto.setMessage("failed to delete the courses !!");
                logger.info("CourseController(deleteCourse)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(deleteCourse)>> Exit");
        }
        return null;
    }

    /**
     * Deletes a course permanently by its ID.
     *
     * @param id The ID of the course to be deleted.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<String> hardDeleteCourseById(@PathVariable Long id) {
        logger.info("CourseController(hardDeleteCourseById) >> Entry");
        try {
            Optional<Course> courseDetails = courseRepo.findById(id);
            if (courseDetails.isPresent()) {
                courseRepo.deleteById(id);
                logger.info("CourseController(hardDeleteCourseById) >> Exit");
                return new ResponseEntity<String>("delete successfully", HttpStatus.OK);
            } else {
                logger.info("CourseController(hardDeleteCourseById) >> Exit");
                return new ResponseEntity<String>("data not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(hardDeleteCourseById)");
        }
        return null;
    }

    /**
     * Retrieves the course details for the given ID.
     *
     * @param  id the ID of the course
     * @return    the course details as a list of LevelNode objects
     */
    @GetMapping("/getCourseDetails/{id}")
    public ResponseEntity<ResponseDto> getCourseDetails(@PathVariable("id") Long id) {
        logger.info("CourseController(getCourseDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<Level> levelsList = levelController.getLevelDetailsByCourseId(id).getBody();
            List<LevelNode> treeData = new ArrayList<>();
            if (levelsList != null && levelsList.size() != 0) {
                for (Level level : levelsList) {
                    String levelItem = "Level " + level.getLevel();
                    List<LevelNode> levelChildren = new ArrayList<>();
                    List<Parts> parts = partsController.getPartsByCourseIdAndLevel(id, level.getLevel()).getBody();
                    if (parts != null && parts.size() > 0) {
                        String indexItem = "Index";
                        List<LevelNode> indexChildren = new ArrayList<>();
                        String sessionItem = "Session";
                        List<LevelNode> sessionsChildren = new ArrayList<>();
                        Set<String> TopicNames = parts.stream()
                                .map(partsData -> partsData.getTopicDetails().getTopic_name()) // Assuming you have a method to get the session name
                                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
                        Set<Integer> SessionNames = parts.stream().map(partsData -> partsData.getSession_no()).collect(Collectors.toCollection(() -> new LinkedHashSet<>())); // Assuming you have a method to get the session name
                        // Convert the set to a list
                        List<Integer> sessionNamesList = new ArrayList<>(SessionNames);

                        // Sort the list
                        Collections.sort(sessionNamesList);

                        // If you need the result back as a Set, you can create a new TreeSet
                        SessionNames = new TreeSet<>(sessionNamesList);


                        for (int i = 0; i < TopicNames.size(); i++) {
                            String topicName = TopicNames.toArray()[i].toString();
                            List<LevelNode> topicChildren = new ArrayList<>();

                            List<Parts> filteredPartsList = parts.stream()
                                    .filter(partsData -> partsData.getTopicDetails().getTopic_name().equals(topicName)).collect(Collectors.toCollection(ArrayList::new));
                            for (int l = 0; l < filteredPartsList.size(); l++) {
                                String partItem = "Part " + filteredPartsList.get(l).getPart();
                                TopicPart topic = new TopicPart(filteredPartsList.get(l).getId(), topicName, partItem, filteredPartsList.get(l).getSession_no(),filteredPartsList.get(l).getTopicDetails().getCategory());
                                LevelNode partNode = new LevelNode(partItem, new ArrayList<>(), topic, null);
                                topicChildren.add(partNode);
                            }

                            LevelNode introductionNode = new LevelNode(topicName, topicChildren);
                            indexChildren.add(introductionNode);
                        }
                        for (int i = 0; i < SessionNames.size(); i++) {
                            int sessionName = (int) SessionNames.toArray()[i];
                            List<LevelNode> sessionChildren = new ArrayList<>();
                            List<Parts> filteredSessionsList = parts.stream().filter(partsData -> partsData.getSession_no() == (sessionName)).collect(Collectors.toCollection(ArrayList::new));
                            for (int l = 0; l < filteredSessionsList.size(); l++) {
                                String partItem = "Part " + filteredSessionsList.get(l).getPart();
                                TopicPart topic = new TopicPart(filteredSessionsList.get(l).getId(), filteredSessionsList.get(l).getTopicDetails().getTopic_name(), partItem, filteredSessionsList.get(l).getSession_no(),filteredSessionsList.get(l).getTopicDetails().getCategory());
                                LevelNode partNode = new LevelNode(partItem, new ArrayList<>(), topic, null);
                                sessionChildren.add(partNode);
                            }

                            LevelNode sessionNode = new LevelNode( "Session "+sessionName , sessionChildren);
                            sessionsChildren.add(sessionNode);
                        }
                        LevelNode indexNode = new LevelNode(indexItem, indexChildren);
                        LevelNode sessionNode = new LevelNode(sessionItem, sessionsChildren);
                        levelChildren.add(indexNode);
                        levelChildren.add(sessionNode);
                        LevelNode levelNode = new LevelNode(levelItem, levelChildren);
                        treeData.add(levelNode);
                    }
                }
            }
            if(treeData.isEmpty()){
                response.setSuccess(false);
                response.setMessage("No data found");
                response.setData(new Gson().toJson(treeData));
            }
            else{
                response.setSuccess(true);
                response.setMessage("Get course details completed successfully");
                response.setData(new Gson().toJson(treeData));
            }
            logger.info("CourseController(getCourseDetails)>> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(getCourseDetails)>> Exit");
            response.setSuccess(false);
            response.setMessage("Error in getting course details");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        }
    }
    @GetMapping("/courses-by-levels/{levelId}")
    public ResponseEntity<ResponseDto> getCoursesWithLevelsDetails(@PathVariable Long levelId){
        logger.info("StreamController(getCoursesWithLevelsDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try{
            Optional<Level> leveldetails = levelRepo.findById(levelId);
            List<Parts> parts = partsRepo.getPartsByCourseIdAndLevel(leveldetails.get().getCourse_id(), leveldetails.get().getLevel());
            String levelItem = "Level " + leveldetails.get().getLevel();
            String indexItem = "Index";
            String sessionItem = "Session";
            List<LevelNode> treeData = new ArrayList<>();
            Set<String> TopicNames = parts.stream()
                    .map(partsData -> partsData.getTopicDetails().getTopic_name()) // Assuming you have a method to get the session name
                    .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
            Set<Integer> SessionNames = parts.stream().map(partsData -> partsData.getSession_no()).collect(Collectors.toCollection(() -> new LinkedHashSet<>())); // Assuming you have a method to get the
            // Convert the set to a list
            List<Integer> sessionNamesList = new ArrayList<>(SessionNames);

            // Sort the list
            Collections.sort(sessionNamesList);

            // If you need the result back as a Set, you can create a new TreeSet
            SessionNames = new TreeSet<>(sessionNamesList);
            List<LevelNode> indexChildren = new ArrayList<>();
            List<LevelNode> sessionsChildren = new ArrayList<>();
            List<LevelNode> levelChildren = new ArrayList<>();
            for (int i = 0; i < TopicNames.size(); i++) {
                String topicName = TopicNames.toArray()[i].toString();
                List<LevelNode> topicChildren = new ArrayList<>();
                List<Parts> filteredPartsList = parts.stream()
                        .filter(partsData -> partsData.getTopicDetails().getTopic_name().equals(topicName)).collect(Collectors.toCollection(ArrayList::new));
                for (int l = 0; l < filteredPartsList.size(); l++) {
                    String partItem = "Part " + filteredPartsList.get(l).getPart();
                    TopicPart topic = new TopicPart(filteredPartsList.get(l).getId(), topicName, partItem, filteredPartsList.get(l).getSession_no(),filteredPartsList.get(l).getTopicDetails().getCategory());
                    LevelNode partNode = new LevelNode(partItem, new ArrayList<>(), topic, null);
                    topicChildren.add(partNode);
                }
                LevelNode introductionNode = new LevelNode(topicName, topicChildren);
                indexChildren.add(introductionNode);
            }
            for (int i = 0; i < SessionNames.size(); i++) {
                int sessionName = (int) SessionNames.toArray()[i];
                List<LevelNode> sessionChildren = new ArrayList<>();
                List<Parts> filteredSessionsList = parts.stream().filter(partsData -> partsData.getSession_no() == (sessionName)).collect(Collectors.toCollection(ArrayList::new));
                for (int l = 0; l < filteredSessionsList.size(); l++) {
                    String partItem = "Part " + filteredSessionsList.get(l).getPart();
                    TopicPart topic = new TopicPart(filteredSessionsList.get(l).getId(), filteredSessionsList.get(l).getTopicDetails().getTopic_name(), partItem, filteredSessionsList.get(l).getSession_no(),filteredSessionsList.get(l).getTopicDetails().getCategory());
                    LevelNode partNode = new LevelNode(partItem, new ArrayList<>(), topic, null);
                    sessionChildren.add(partNode);
                }

                LevelNode sessionNode = new LevelNode( "Session "+sessionName , sessionChildren);
                sessionsChildren.add(sessionNode);
            }
            LevelNode indexNode = new LevelNode(indexItem, indexChildren);
            LevelNode sessionNode = new LevelNode(sessionItem, sessionsChildren);
            levelChildren.add(indexNode);
            levelChildren.add(sessionNode);
            LevelNode levelNode = new LevelNode(levelItem, levelChildren);
            treeData.add(levelNode);

            response.setSuccess(true);
            response.setMessage("data is getting successfully!");
            response.setData(new Gson().toJson(treeData));
            logger.info("StreamController(getCoursesWithLevelsDetails) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);

        } catch (Exception e){
            logger.info("Exception in StreamController(getCoursesWithLevelsDetails)>>Exit");
        }
        return null;
    }


    @GetMapping("/get-all-course-name")
    public ResponseEntity<ResponseDto> getAllCourseName(){
        logger.info("CourseController(getAllCourseName) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try{
            List<?> getTheNameStreams = courseRepo.getAllCourseName();
            responseDto.setSuccess(true);
            responseDto.setMessage("All course names fetched successfully !!");
            responseDto.setData(new Gson().toJson(getTheNameStreams));
            logger.info("CourseController(getAllCourseName)>> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in CourseController(getAllCourseName)>> Exit");
        }
        return null;
    }

}
