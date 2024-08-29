package com.servxglobal.tms.courseservice.controller;

import com.google.gson.Gson;
import com.servxglobal.tms.courseservice.dto.PartWithoutFiles;
import com.servxglobal.tms.courseservice.dto.ResponseDto;
import com.servxglobal.tms.courseservice.dto.TopicsWithPart;
import com.servxglobal.tms.courseservice.model.Level;
import com.servxglobal.tms.courseservice.model.Parts;
import com.servxglobal.tms.courseservice.model.Topic;
import com.servxglobal.tms.courseservice.repository.LevelRepo;
import com.servxglobal.tms.courseservice.repository.PartsRepo;
import com.servxglobal.tms.courseservice.repository.TopicRepo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/course/parts")
public class PartsController {

    @Autowired
    private PartsRepo partsRepo;

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private LevelRepo levelRepo;

    private static final Logger logger = LoggerFactory.getLogger(PartsController.class);

    // add parts POST API
    @PostMapping("/add-parts")
    public ResponseEntity<Parts> addParts(@RequestParam(value = "part") int part, @RequestParam(value = "max_level") int max_level, @RequestParam(value = "session_id") Long session_id, @RequestParam(value = "session_no") int session_no, @RequestParam(value = "topic_id", defaultValue = "") Long topic_id, @RequestParam(value = "course_id") Long course_id,
//                                           @RequestParam(value = "filename") String filename,
                                          @RequestParam(value = "file") MultipartFile uploadFile) {
        logger.info("PartsController(addParts) >> Entry");
        byte[] fileData;

        try {
            Parts parts_details = new Parts();
            parts_details.setPart(part);
            parts_details.setCreate_time(new Date());
            parts_details.setModified_time(new Date());
            parts_details.setMax_level(max_level);
            parts_details.setSession_id(session_id);
            parts_details.setSession_no(session_no);
            Optional<Topic> topic_details = topicRepo.findById(topic_id);
            parts_details.setTopicDetails(topic_details.get());
            parts_details.setCourse_id(course_id);
            if (uploadFile != null) {
                String filename = uploadFile.getOriginalFilename();
                parts_details.setFilename(filename);
                parts_details.setFileData(uploadFile.getBytes());
            }
            List<Parts> parts = partsRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if (parts.isEmpty()) {
                parts_details.setId(1L);
            } else {
                parts_details.setId(parts.get(0).getId() + 1);
            }
            Parts save_data = partsRepo.save(parts_details);
            logger.info("PartsController(addParts) >> Exit");
            return new ResponseEntity<Parts>(save_data, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception in partsController(addParts) >> Exit");
        }
        return null;
    }

    //get all active parts details
    @GetMapping("/get-all-active-parts")
    public ResponseEntity<List<Parts>> getAllActiveParts() {
        logger.info("PartsController(getAllActiveParts) >> Entry");
        try {
            List<Parts> partsList = partsRepo.findAllParts();
            logger.info("PartsController(getAllActiveParts) >> Exit");
            return new ResponseEntity<List<Parts>>(partsList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception in partsController(getAllActiveParts) >> Exit");
        }
        return null;
    }

    // get all parts details
    @GetMapping("/get-all-parts")
    public ResponseEntity<List<Parts>> getAllParts() {
        logger.info("PartsController(getAllParts) >> Entry");
        try {
            List<Parts> partsList = partsRepo.findAll();
            logger.info("PartsController(getAllParts) >> Exit");
            return new ResponseEntity<List<Parts>>(partsList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception in partsController(getAllParts) >> Exit");
        }
        return null;
    }

    //get parts details by id
    @GetMapping("/get-by-parts/{id}")
    public ResponseEntity<ResponseDto> gePartsById(@PathVariable Long id) {
        logger.info("PartsController(gePartsById) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Parts> partDetails = partsRepo.findById(id);
            if (partDetails.isPresent()){
                response.setSuccess(true);
                response.setMessage("Get parts details completed successfully");
                response.setData(new Gson().toJson(partDetails.get()));
                logger.info("PartsController(gePartsById) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
            else {
                response.setSuccess(false);
                response.setMessage("Part details not found");
                response.setData(new Gson().toJson(partDetails));
                logger.info("PartsController(gePartsById) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception in partsController(gePartsById) >> Exit");
        }
        return null;
    }

    //update parts details by id
    @PutMapping("/update-parts/{id}")
    public ResponseEntity<Parts> updateParts(@PathVariable Long id, @RequestBody Parts partsDetails, @RequestParam("file") MultipartFile file) {
        logger.info("PartsController(updateParts) >> Entry");
        try {
            Optional<Parts> parts = partsRepo.findById(id);
            Parts updateParts = new Parts();
            if (parts.isPresent()) {
                parts.get().setPart(partsDetails.getPart());
                parts.get().setCourse_id(partsDetails.getCourse_id());
                parts.get().setTopicDetails(partsDetails.getTopicDetails());
                parts.get().setSession_id(partsDetails.getSession_id());
                parts.get().setSession_no(partsDetails.getSession_no());
                parts.get().setMax_level(partsDetails.getMax_level());
                parts.get().setModified_time(new Date());
                parts.get().setFilename(partsDetails.getFilename());
                parts.get().setFileData(file.getBytes());
                updateParts = partsRepo.save(parts.get());
                logger.info("PartsController(updateParts) >> Exit");
                return new ResponseEntity<Parts>(updateParts, HttpStatus.OK);
            } else {
                updateParts = null;
                logger.info("PartsController(updateParts) >> Exit");
                return new ResponseEntity<Parts>(updateParts, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception in partsController(updateParts) >> Exit");
        }
        return null;
    }

    // soft part details delete
    @PutMapping("/delete-active-parts/{id}")
    public ResponseEntity<Parts> softPartDeleteById(@PathVariable Long id) {
        logger.info("PartsController(softPartDeleteById) >> Entry");
        try {
            Optional<Parts> partsDetails = partsRepo.findById(id);
            Parts updateParts = new Parts();
            if (partsDetails.isPresent()) {
                partsDetails.get().set_deleted(true);
                updateParts = partsRepo.save(partsDetails.get());
                logger.info("PartsController(softPartDeleteById) >> Exit");
                return new ResponseEntity<Parts>(updateParts, HttpStatus.OK);
            } else {
                updateParts = null;
                logger.info("PartsController(softPartDeleteById) >> Exit");
                return new ResponseEntity<Parts>(updateParts, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception in partsController(softPartDeleteById) >> Exit");
        }
        return null;
    }

    // permanent part details delete
    @DeleteMapping("/delete-parts/{id}")
    public ResponseEntity<String> hardPartDeleteById(@PathVariable Long id) {
        logger.info("PartsController(hardPartDeleteById) >> Entry");
        try {
            Optional<Parts> partDetails = partsRepo.findById(id);
            if (partDetails.isPresent()) {
                partsRepo.deleteById(id);
                logger.info("PartsController(hardPartDeleteById) >> Exit");
                return new ResponseEntity<String>("Deleted successfully", HttpStatus.OK);
            } else {
                logger.info("PartsController(hardPartDeleteById) >> Exit");
                return new ResponseEntity<String>("Id is not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception in partsController(DeleteParts) >> Exit");
        }
        return null;
    }

    // get parts details by session id
    @GetMapping("/get-parts-by-session-id/{id}")
    public ResponseEntity<List<Parts>> getPartsBySessionId(@PathVariable Long id) {
        logger.info("PartsController(getPartsBySessionId) >> Entry");
        try {
            List<Parts> partDetails = partsRepo.findPartsBySession_id(id);
            logger.info("PartsController(getPartsBySessionId) >> Exit");
            return new ResponseEntity<List<Parts>>(partDetails, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("PartsController(getPartsBySessionId) >> Exit");
        }
        return null;
    }

    //     get parts details by topic id
    @GetMapping("/get-parts-by-topic-id/{id}")
    public ResponseEntity<ResponseDto> getPartsByTopicId(@PathVariable Long id) {
        logger.info("PartsController(getPartsByTopicId) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<Parts> partDetails = partsRepo.findPartsByTopic_Id(id);
            logger.info("PartsController(getPartsByTopicId) >> Exit");
            response.setSuccess(true);
            response.setMessage("Get parts details completed successfully");
            response.setData(new Gson().toJson(partDetails));
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("PartsController(getPartsByTopicId) >> Exit");
        }
        logger.info("PartsController(getPartsByTopicId) >> Exit");
        return null;
    }


    // To upload the files
    @PutMapping("/upload-file/{id}")
    public ResponseEntity<ResponseDto> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        logger.info("PartsController(uploadFile) >> Entry");
        ResponseEntity<ResponseDto> responseEntity;
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Parts> partDetails = partsRepo.findById(id);
            Parts updateParts = new Parts();
            if (partDetails.isPresent()) {
                partDetails.get().setFileData(file.getBytes());
                partDetails.get().setFilename(file.getOriginalFilename());
                updateParts = partsRepo.save(partDetails.get());
            }

            logger.info("PartsController(uploadFile) >> Exit");
            responseDto.setSuccess(true);
            responseDto.setMessage("parts file upload successful !!");
            responseDto.setData(String.valueOf(updateParts));
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.info("Exception in CourseController(addCourse) >> Entry");
//            return  new ResponseEntity<ResponseDto>("responseDto", HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    // To download or retrieve the files
    @GetMapping("/get-part-file/{id}")
    public ResponseEntity<ResponseDto> getPartFile(@PathVariable Long id) {
        logger.info("PartsController(getPartFile) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        String response = "";
        final JSONObject jsonObject = new JSONObject();
        try {
            Optional<Parts> data = partsRepo.findById(id);
            if (data.isPresent() && data.get().getFileData() != null) {
                ByteArrayResource resource = new ByteArrayResource(data.get().getFileData());
                byte[] byteArray = new byte[(int) resource.contentLength()];

                resource.getInputStream().read(byteArray);

                // Convert the byte array to a Base64 string
                String base64String = Base64.getEncoder().encodeToString(byteArray);

                // Decode the Base64 string to a byte array
                byte[] base64Bytes = Base64.getDecoder().decode(base64String);

                // Read PPT from bytes
                try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(base64Bytes))) {
                    // Create a PDF document
                    try (PDDocument pdf = new PDDocument()) {
                        for (XSLFSlide slide : ppt.getSlides()) {
                            // Create a new page in the PDF document for each slide
                            PDPage pdfPage = new PDPage();
                            pdf.addPage(pdfPage);

                            // Create a content stream for the page
                            try (PDPageContentStream contentStream = new PDPageContentStream(pdf, pdfPage)) {
                                BufferedImage slideImage = slideToImage(slide,1600,1200); // Implement slideToImage method

                                PDImageXObject pdImage = PDImageXObject.createFromByteArray(pdf, toByteArray(slideImage), null);

//                                contentStream.drawImage(pdImage, 0, 0);

                                // Draw the image on the PDF page
                                contentStream.drawImage(pdImage,0, 0, pdfPage.getMediaBox().getWidth(), pdfPage.getMediaBox().getHeight());
                            }

                        }

                            // Convert the PDF document to a byte array
                        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                        pdf.save(pdfOutputStream);
                        byte[] pdfBytes = pdfOutputStream.toByteArray();

                        // Convert the byte array to a Base64 string
                        String pdfBase64String = Base64.getEncoder().encodeToString(pdfBytes);
                        jsonObject.put("pdf", pdfBase64String);
                        jsonObject.put("ppt", base64String);
                        jsonObject.put("fileName", data.get().getFilename());
                        response = new Gson().toJson(jsonObject);
                        responseDto.setData(response);
                        responseDto.setSuccess(true);
                        responseDto.setMessage("Data retrieved successfully");
                        logger.info("PartsController(getPartFile) >> Exit");
                        return new ResponseEntity<>(responseDto, HttpStatus.OK);
                    }
                }
            } else {
                responseDto.setSuccess(false);
                responseDto.setMessage("File not found");
                logger.info("PartsController(getPartFile) >> Exit");
                return new ResponseEntity<>(responseDto, HttpStatus.OK);
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
            responseDto.setSuccess(false);
            responseDto.setMessage("File not found");
            logger.info("PartsController(getPartFile) >> Exception occurred "+e);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
    }
/**
 * Generates a BufferedImage of a slide from a PowerPoint presentation.
 *
 * @param  slide         the XSLFSlide object representing the slide to convert
 * @param  imageWidth    the width of the generated image
 * @param  imageHeight   the height of the generated image
 */
    private static BufferedImage slideToImage(XSLFSlide slide, int imageWidth, int imageHeight) {
        try {
            // Create a BufferedImage with increased resolution
            BufferedImage slideImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

            // Create a Graphics2D object to render the slide
            Graphics2D graphics = slideImage.createGraphics();

            // Set rendering hints for better quality
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Clear the background (optional)
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, imageWidth, imageHeight);

            // Calculate scaling factors
            double scaleX = (double) imageWidth / slide.getSlideShow().getPageSize().getWidth();
            double scaleY = (double) imageHeight / slide.getSlideShow().getPageSize().getHeight();

            // Use AffineTransform to scale and render the slide
            AffineTransform transform = AffineTransform.getScaleInstance(scaleX, scaleY);
            graphics.setTransform(transform);

            // Render the slide onto the BufferedImage
            slide.draw(graphics);

            // Dispose of the Graphics2D object
            graphics.dispose();

            return slideImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to convert BufferedImage to byte array.
    private static byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }


    @GetMapping("/get-parts-by-course-id-and-level/{id}/{level}")
    public ResponseEntity<List<Parts>> getPartsByCourseIdAndLevel(@PathVariable Long id, @PathVariable int level) {
        logger.info("PartsController(getPartsByCourseIdAndLevel) >> Entry");
        try {
            List<Level> levelDetails = levelRepo.findByLevel(id, level);
            List<Parts> partsList = new ArrayList<>();
            if (levelDetails.size() != 0) {
                partsList = partsRepo.getPartsByCourseIdAndLevel(id, level);
            }
            logger.info("PartsController(getPartsByCourseIdAndLevel)>> Exit");
            return new ResponseEntity<List<Parts>>(partsList, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in PartsController(getPartsByCourseIdAndLevel)>> Exit");
        }
        return null;
    }

    @GetMapping("/get-parts-by-course-id/{id}")
    public ResponseEntity<ResponseDto> getPartsByCourseId(@PathVariable Long id) {
        logger.info("PartsController(getPartsByCourseId) >> Entry");
        ResponseEntity<ResponseDto> responseEntity;
        ResponseDto responseDto = new ResponseDto();
        try {
            List<Parts> partDetails = partsRepo.findPartsByCourseId(id);
            List<TopicsWithPart> topicParts = new ArrayList<>();

            if (partDetails != null && partDetails.size() > 0) {
                Set<String> TopicNames = partDetails.stream().map(partsData -> partsData.getTopicDetails().getTopic_name()) // Assuming you have a method to get the session name
                        .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
                for (int i = 0; i < TopicNames.size(); i++) {
                    String topicName = TopicNames.toArray()[i].toString();
                    List<PartWithoutFiles> partWithoutFiles = new ArrayList<>();
                    List<Parts> filteredPartsList = partDetails.stream().filter(partsData -> partsData.getTopicDetails().getTopic_name().equals(topicName)).collect(Collectors.toCollection(ArrayList::new));
                    for (int k = 0; k < filteredPartsList.size(); k++) {
                        PartWithoutFiles partData = new PartWithoutFiles();
                        partData.setPart(filteredPartsList.get(k).getPart());
                        partData.setId(filteredPartsList.get(k).getId());
                        partData.setFilename(filteredPartsList.get(k).getFilename());
                        partData.setCourse_id(filteredPartsList.get(k).getCourse_id());
                        partData.setCreate_time(filteredPartsList.get(k).getCreate_time());
                        partData.setModified_time(filteredPartsList.get(k).getModified_time());
                        partData.setTopicDetails(filteredPartsList.get(k).getTopicDetails());
                        partData.setSession_id(filteredPartsList.get(k).getSession_id());
                        partData.setSession_no(filteredPartsList.get(k).getSession_no());
                        partData.setMax_level(filteredPartsList.get(k).getMax_level());
                        partWithoutFiles.add(partData);
                    }
                    TopicsWithPart topicsWithPart = new TopicsWithPart(topicName, partWithoutFiles);
                    topicParts.add(topicsWithPart);

                }
            }
            logger.info("PartsController(getPartsByCourseId) >> Exit");
            responseDto.setSuccess(true);
            responseDto.setMessage("get part details!!");
            responseDto.setData(new Gson().toJson(topicParts));
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in PartsController(getPartsByCourseId)>> Exit");
        }
        return null;
    }


}
