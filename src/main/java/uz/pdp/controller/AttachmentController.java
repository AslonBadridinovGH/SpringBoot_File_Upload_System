package uz.pdp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.pdp.entity.Attachment;
import uz.pdp.repository.AttachmentContentRepository;
import uz.pdp.repository.AttachmentRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// saving  uploaded file in system; get file infos;  download file.

@RestController
@RequestMapping(value = "/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    AttachmentContentRepository attachmentContentRepository;

    public  static final String uploadDirectory="uploadFiles";

    @PostMapping("/uploadSystem")
    public String uploadFileSystem(MultipartHttpServletRequest request) throws IOException {

        Iterator<String> fileNames =  request.getFileNames();
        MultipartFile multipartFile = request.getFile(fileNames.next());

        if (multipartFile!=null){
            Attachment attachment=new Attachment();
            attachment.setOriginalName(multipartFile.getOriginalFilename());
            attachment.setContentType(multipartFile.getContentType());
            attachment.setSize(multipartFile.getSize());
            String originalFilename = multipartFile.getOriginalFilename();
            String[] split = originalFilename.split("\\.");
            String name = UUID.randomUUID().toString()+"."+split[split.length-1];
            attachment.setName(name);
            attachmentRepository.save(attachment);
            Path path = Paths.get(uploadDirectory + "/" + name);
            Files.copy(multipartFile.getInputStream(), path);
            return "file saved. ID: "+attachment.getId();
        }
        return "file not saved";
    }

    @GetMapping("/download/{id}")
    public void downloadAttachmentById(@PathVariable Integer id, HttpServletResponse response) throws IOException {

     Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
    if (optionalAttachment.isPresent()) {
        Attachment attachment = optionalAttachment.get();
        response.setHeader("Content-Disposition","attachment; filename=\""+attachment.getOriginalName()+"\"");
        response.setContentType(attachment.getContentType());
        FileInputStream fileInputStream=new FileInputStream(uploadDirectory+"/"+attachment.getName());
        FileCopyUtils.copy(fileInputStream, response.getOutputStream());
    }
}

    @GetMapping()
    public List<Attachment> infoAttachment(){
        return attachmentRepository.findAll();
    }


}
